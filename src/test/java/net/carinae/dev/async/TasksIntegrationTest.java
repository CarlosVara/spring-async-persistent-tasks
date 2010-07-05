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

import junit.framework.Assert;
import net.carinae.dev.async.dao.DummyEntityDao;
import net.carinae.dev.async.task.AbstractBaseTask;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.transaction.annotation.Transactional;

/**
 * Checks the correct behavior of the task scheduling system.
 * <p>
 * FIXME: Make the tests modify a row in the db, so the transactionality is tested.
 * 
 * @author Carlos Vara
 */
@ContextConfiguration( locations={"classpath:META-INF/spring/applicationContextTest.xml"} )
public class TasksIntegrationTest extends AbstractJUnit4SpringContextTests {

    @Qualifier("PersistentExecutor")
    @Autowired
    private TaskExecutor taskExecutor;
    
    @Autowired
    private DummyEntityDao dummyEntityDao;
    
    protected static volatile boolean simpleTaskCompleted = false;
    
    
    /**
     * Stores a {@link DummyEntity} with the given data.
     */
    @Configurable
    public static class SimpleTask extends AbstractBaseTask {

        @Autowired
        private transient DummyEntityDao dummyEntityDao;
        
        public SimpleTask(String data) {
            super();
            this.data = data;
        }
        
        private final String data;
        
        @Override
        public void doTaskInTransaction() {
            DummyEntity de = new DummyEntity();
            de.setData(data);
            dummyEntityDao.persist(de);
        }
        
    }
    
    
    /**
     * Enqueues a simple task and waits for 3 minutes for it to be executed.
     */
    @Test
    public void testSimpleTask() throws InterruptedException {
        
        String data = "" + System.nanoTime();
        
        enqueueSimpleTask(data);

        // Now, try to read from the database
        int tries = 0;
        while (tries < 180 && pollDummyEntity(data)) {
            Thread.sleep(1000); // 1 second
            tries++;
        }
        
        Assert.assertTrue("Task didn't execute in 3 minutes time", tries < 180);
    }

    /**
     * Schedules a simple task for 10 seconds in the future and waits for 5
     * minutes for it to be executed.
     */    
    @Test
    public void testScheduledSimpleTask() throws InterruptedException {
        
        String data = "" + System.nanoTime();
        
        scheduleSimpleTask(data);
        
        // Now, try to read from the database
        int tries = 0;
        while (tries < 300 && pollDummyEntity(data)) {
            Thread.sleep(1000); // 1 second
            tries++;
        }
        
        Assert.assertTrue("Scheduled task didn't execute in 5 minutes time", tries < 300);
    }
    
    
    @Transactional
    public void enqueueSimpleTask(String data) {
        taskExecutor.execute( new SimpleTask(data));
    }
    
    @Transactional
    public void scheduleSimpleTask(String data) {
        SimpleTask st = new SimpleTask(data);
        Calendar trigger = Calendar.getInstance();
        trigger.add(Calendar.SECOND, 10);
        st.setTriggerStamp(trigger);
        taskExecutor.execute(st);
    }
    
    @Transactional
    public boolean pollDummyEntity(String data) {
        return !this.dummyEntityDao.findByData(data).isEmpty();
    }
}
