/*
 * Copyright 2010 Carlos Vara
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package net.carinae.dev.async;

import java.util.Calendar;
import java.util.TimeZone;
import net.carinae.dev.async.dao.QueuedTaskHolderDao;
import net.carinae.dev.async.task.AbstractBaseTask;
import net.carinae.dev.async.util.Serializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * A task executor with persistent task queueing.
 * 
 * @author Carlos Vara
 */
@Component("PersistentExecutor")
public class PersistentTaskExecutor implements TaskExecutor {
    
    final static Logger logger = LoggerFactory.getLogger(PersistentTaskExecutor.class);
    
    
    @Autowired
    protected QueuedTaskHolderDao queuedTaskDao;
    
    @Autowired
    protected Serializer serializer;
    
    
    /**
     * Additional requirement: must be run inside a transaction.
     * Currently using MANDATORY as Bounty won't create tasks outside a
     * transaction.
     * 
     * @see org.springframework.core.task.TaskExecutor#execute(java.lang.Runnable)
     */
    @Override
    @Transactional(propagation=Propagation.MANDATORY)
    public void execute(Runnable task) {
        
        logger.debug("Trying to enqueue: {}", task);
        
        AbstractBaseTask abt; 
        try {
            abt = AbstractBaseTask.class.cast(task);
        } catch (ClassCastException e) {
            logger.error("Only runnables that extends AbstractBaseTask are accepted.");
            throw new IllegalArgumentException("Invalid task: " + task);
        }
        
        // Serialize the task
        QueuedTaskHolder newTask = new QueuedTaskHolder();
        byte[] serializedTask = this.serializer.serializeObject(abt);
        newTask.setTriggerStamp(abt.getTriggerStamp());
        
        logger.debug("New serialized task takes {} bytes", serializedTask.length);
        
        newTask.setSerializedTask(serializedTask);
        
        // Store it in the db
        this.queuedTaskDao.persist(newTask);
        
        // POST: Task has been enqueued
    }
    
    
    /**
     * Runs enqueued tasks.
     */
    @Scheduled(fixedRate=Constants.TASK_RUNNER_RATE)
    public void runner() {
        
        logger.debug("Started runner {}", Thread.currentThread().getName());

        QueuedTaskHolder lockedTask = null;
        
        // While there is work to do...
        while ( (lockedTask = tryLockTask()) != null ) {
            
            logger.debug("Obtained lock on {}", lockedTask);
            
            // Deserialize the task
            AbstractBaseTask runnableTask = this.serializer.deserializeAndCast(lockedTask.getSerializedTask());
            runnableTask.setQueuedTaskId(lockedTask.getId());
            
            // Run it
            runnableTask.run();
        }
        
        logger.debug("Finishing runner {}, nothing else to do.", Thread.currentThread().getName());
    }
    
    
    /**
     * The hypervisor re-queues for execution possible stalled tasks.
     */
    @Scheduled(fixedRate=Constants.TASK_HYPERVISOR_RATE)
    public void hypervisor() {
        
        logger.debug("Started hypervisor {}", Thread.currentThread().getName());
        
        // Reset stalled threads, one at a time to avoid too wide transactions
        while ( tryResetStalledTask() );
        
        logger.debug("Finishing hypervisor {}, nothing else to do.", Thread.currentThread().getName());
    }


    /**
     * Tries to ensure a lock on a task in order to execute it.
     * 
     * @return A locked task, or <code>null</code> if there is no task available
     *         or no lock could be obtained.
     */
    private QueuedTaskHolder tryLockTask() {
        
        int tries = 3;
        
        QueuedTaskHolder ret = null;
        while ( tries > 0 ) {
            try {
                ret = obtainLockedTask();
                return ret;
            } catch (OptimisticLockingFailureException e) {
                tries--;
            }
        }
        
        return null;
    }

    /**
     * Tries to reset a stalled task.
     * 
     * @return <code>true</code> if one task was successfully re-queued,
     *         <code>false</code> if no task was re-queued, either because there
     *         are no stalled tasks or because there was a conflict re-queueing
     *         it.
     */
    private boolean tryResetStalledTask() {
        int tries = 3;
        
        QueuedTaskHolder qt = null;
        while ( tries > 0 ) {
            try {
                qt = resetStalledTask();
                return qt != null;
            } catch (OptimisticLockingFailureException e) {
                tries--;
            }
        }
        
        return false;
    }


    /**
     * @return A locked task ready for execution, <code>null</code> if no ready
     *         task is available.
     * @throws OptimisticLockingFailureException
     *             If getting the lock fails.
     */
    @Transactional
    public QueuedTaskHolder obtainLockedTask() {
        QueuedTaskHolder qt = this.queuedTaskDao.findNextTaskForExecution();
        logger.debug("Next possible task for execution {}", qt);
        if ( qt != null ) {
            qt.setStartedStamp(Calendar.getInstance(TimeZone.getTimeZone("etc/UTC")));
        }
        return qt;
    }


    /**
     * Tries to reset a stalled task, returns null if no stalled task was reset.
     * 
     * @return The re-queued task, <code>null</code> if no stalled task is
     *         available.
     * @throws OptimisticLockingFailureException
     *             If the stalled task is modified by another thread during
     *             re-queueing.
     */
    @Transactional
    public QueuedTaskHolder resetStalledTask() {
        QueuedTaskHolder stalledTask = this.queuedTaskDao.findRandomStalledTask();
        logger.debug("Obtained this stalledTask {}", stalledTask);
        if ( stalledTask != null ) {
            stalledTask.setStartedStamp(null);
        }
        return stalledTask;
    }
    

}
