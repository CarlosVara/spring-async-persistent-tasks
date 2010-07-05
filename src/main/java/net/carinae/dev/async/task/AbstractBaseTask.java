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
package net.carinae.dev.async.task;

import java.io.Serializable;
import java.util.Calendar;
import net.carinae.dev.async.QueuedTaskHolder;
import net.carinae.dev.async.dao.QueuedTaskHolderDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * Superclass for all async tasks.
 * <ul>
 *  <li>Ensures that its associated queued task is marked as completed in the same tx.</li>
 *  <li>Marks the task as serializable.</li>
 * </ul>
 * 
 * @author Carlos Vara
 */
public abstract class AbstractBaseTask implements Runnable, Serializable {

    final static Logger logger = LoggerFactory.getLogger(AbstractBaseTask.class);
    
    
    // Common data -------------------------------------------------------------
    
    private transient String queuedTaskId;
    private transient QueuedTaskHolder qth;
    private transient Calendar triggerStamp;
    
    
    public void setQueuedTaskId(String queuedTaskId) {
        this.queuedTaskId = queuedTaskId;
    }

    public String getQueuedTaskId() {
        return queuedTaskId;
    }
    
    public void setTriggerStamp(Calendar triggerStamp) {
        this.triggerStamp = triggerStamp;
    }

    public Calendar getTriggerStamp() {
        return triggerStamp;
    }
    
    
    // Injected components -----------------------------------------------------
    
    @Autowired(required=true)
    protected transient QueuedTaskHolderDao queuedTaskHolderDao;
    

    // Lifecycle methods -------------------------------------------------------
    
    /**
     * Entrance point of the task.
     * <ul>
     *  <li>Ensures that the associated task in the queue exists.</li>
     *  <li>Marks the queued task as finished upon tx commit.</li>
     *  <li>In case of tx rollback, frees the task.</li>
     * </ul>
     * 
     * @see java.lang.Runnable#run()
     */
    @Override
    final public void run() {
        
        try {
            transactionalOps();
        } catch (RuntimeException e) {
            // Free the task, so it doesn't stall
            logger.warn("Exception forced task tx rollback: {}", e);
            freeTask();
        }
        
    }

    @Transactional
    private void transactionalOps() {
        doInTxBeforeTask();
        doTaskInTransaction();
        doInTxAfterTask();
    }
    
    @Transactional
    private void freeTask() {
        QueuedTaskHolder task = this.queuedTaskHolderDao.findById(this.queuedTaskId);
        task.setStartedStamp(null);
    }
    
    
    /**
     * Ensures that there is an associated task and that its state is valid.
     */
    private void doInTxBeforeTask() {
        this.qth = this.queuedTaskHolderDao.findById(this.queuedTaskId);
        if ( this.qth == null ) {
            throw new IllegalArgumentException("Not executing: no associated task exists: " + this.getQueuedTaskId());
        }
        if ( this.qth.getStartedStamp() == null || this.qth.getCompletedStamp() != null ) {
            throw new IllegalArgumentException("Illegal queued task status: " + this.qth);
        }
    }
    
    
    /**
     * Method to be implemented by concrete tasks where their operations are
     * performed.
     */
    public abstract void doTaskInTransaction();
    
    
    /**
     * Marks the associated task as finished.
     */
    private void doInTxAfterTask() {
        this.qth.setCompletedStamp(Calendar.getInstance());
    }


    private static final long serialVersionUID = 1L;
}
