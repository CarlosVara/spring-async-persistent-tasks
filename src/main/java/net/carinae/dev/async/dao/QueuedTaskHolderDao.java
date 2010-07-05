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
package net.carinae.dev.async.dao;

import net.carinae.dev.async.QueuedTaskHolder;


/**
 * DAO operations for the {@link QueuedTaskHolder} entities.
 * 
 * @author Carlos Vara
 */
public interface QueuedTaskHolderDao {

    /**
     * Adds a new task to the current persistence context. The task will be
     * persisted into the database at flush/commit.
     * 
     * @param queuedTask
     *            The task to be saved (enqueued).
     */
    void persist(QueuedTaskHolder queuedTask);
    
    
    /**
     * Finder that retrieves a task by its id.
     * 
     * @param taskId
     *            The id of the requested task.
     * @return The task with that id, or <code>null</code> if no such task
     *         exists.
     */
    QueuedTaskHolder findById(String taskId);


    /**
     * @return A task which is candidate for execution. The receiving thread
     *         will need to ensure a lock on it. <code>null</code> if no
     *         candidate task is available.
     */
    QueuedTaskHolder findNextTaskForExecution();


    /**
     * @return A task which has been in execution for too long without
     *         finishing. <code>null</code> if there aren't stalled tasks.
     */
    QueuedTaskHolder findRandomStalledTask();

}

