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
import net.carinae.dev.async.DummyEntity;


/**
 * Simple DAO contract for {@link DummyEntity}s.
 * 
 * @author Carlos Vara
 */
public interface DummyEntityDao {

    /**
     * Add the given {@link DummyEntity} to the current persistence context.
     * 
     * @param de
     *            the entity to add.
     */
    void persist(DummyEntity de);
    
    /**
     * Finds all the persisted {@link DummyEntity}s whose data is equals to the
     * given one.
     * 
     * @param data
     *            Data to compare to.
     * @return A list of the entities with the same data.
     */
    List<DummyEntity> findByData(String data);
    
}
