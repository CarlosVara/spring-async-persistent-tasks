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

import java.util.Calendar;
import java.util.List;
import java.util.Random;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import net.carinae.dev.async.QueuedTaskHolder;
import net.carinae.dev.async.QueuedTaskHolder_;
import org.springframework.stereotype.Repository;

/**
 * JPA2 implementation of {@link QueuedTaskHolderDao}.
 * 
 * @author Carlos Vara
 */
@Repository
public class QueuedTaskHolderDaoJPA2 implements QueuedTaskHolderDao {

    
    // QueuedTaskDao methods ---------------------------------------------------
    
    @Override
    public void persist(QueuedTaskHolder queuedTask) {
        this.entityManager.persist(queuedTask);
    }
    
    @Override
    public QueuedTaskHolder findById(String taskId) {
        return this.entityManager.find(QueuedTaskHolder.class, taskId);
    }
    
    @Override
    public QueuedTaskHolder findNextTaskForExecution() {
        
        Calendar NOW = Calendar.getInstance();
        
        // select qt from QueuedTask where
        //      qt.startedStamp == null AND
        //      (qth.triggerStamp == null || qth.triggerStamp < NOW)
        // order by qth.version ASC, qt.creationStamp ASC
        CriteriaBuilder cb = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<QueuedTaskHolder> cq = cb.createQuery(QueuedTaskHolder.class);
        Root<QueuedTaskHolder> qth = cq.from(QueuedTaskHolder.class);
        cq.select(qth)
            .where(cb.and(cb.isNull(qth.get(QueuedTaskHolder_.startedStamp)), 
                    cb.or(
                            cb.isNull(qth.get(QueuedTaskHolder_.triggerStamp)),
                            cb.lessThan(qth.get(QueuedTaskHolder_.triggerStamp), NOW))))
            .orderBy(cb.asc(qth.get(QueuedTaskHolder_.version)), cb.asc(qth.get(QueuedTaskHolder_.creationStamp)));
        
        List<QueuedTaskHolder> results = this.entityManager.createQuery(cq).setMaxResults(1).getResultList();
        if ( results.isEmpty() ) {
            return null;
        }
        else {
            return results.get(0);
        }

    }
    
    @Override
    public QueuedTaskHolder findRandomStalledTask() {
        
        Calendar TOO_LONG_AGO = Calendar.getInstance();
        TOO_LONG_AGO.add(Calendar.SECOND, -7200);
        
        // select qth from QueuedTask where 
        //      qth.startedStamp != null AND
        //      qth.startedStamp < TOO_LONG_AGO
        CriteriaBuilder cb = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<QueuedTaskHolder> cq = cb.createQuery(QueuedTaskHolder.class);
        Root<QueuedTaskHolder> qth = cq.from(QueuedTaskHolder.class);
        cq.select(qth).where(
                cb.and(
                        cb.isNull(qth.get(QueuedTaskHolder_.completedStamp)),
                        cb.lessThan(qth.get(QueuedTaskHolder_.startedStamp), TOO_LONG_AGO)));
        
        List<QueuedTaskHolder> stalledTasks = this.entityManager.createQuery(cq).getResultList();
        
        if ( stalledTasks.isEmpty() ) {
            return null;
        }
        else {
            Random rand = new Random(System.currentTimeMillis());
            return stalledTasks.get(rand.nextInt(stalledTasks.size()));
        }
        
    }

    
    // Injected dependencies ---------------------------------------------------

    @PersistenceContext
    private EntityManager entityManager;
    
}
