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

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;
import net.carinae.dev.async.DummyEntity;
import net.carinae.dev.async.DummyEntity_;


/**
 * JPA2 implementation of {@link DummyEntityDao}.
 * 
 * @author Carlos Vara
 */
@Repository
public class DummyEntityDaoJPA2 implements DummyEntityDao {
    
    // DummyEntityDao methods --------------------------------------------------
    
    @Override
    public void persist(DummyEntity de) {
        this.entityManager.persist(de);
    }

    @Override
    public List<DummyEntity> findByData(String data) {
        
        // select de from DummyEntity
        //  where de.data = data
        CriteriaBuilder cb = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<DummyEntity> cq = cb.createQuery(DummyEntity.class);
        Root<DummyEntity> de = cq.from(DummyEntity.class);
        cq.select(de).where(cb.equal(de.get(DummyEntity_.data), data));
        
        return this.entityManager.createQuery(cq).getResultList();
    }

    
    // Injected dependencies ---------------------------------------------------

    @PersistenceContext
    private EntityManager entityManager;

}
