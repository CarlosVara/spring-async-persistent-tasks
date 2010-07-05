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

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;

/**
 * Sample entity used in task scheduling tests.
 * 
 * @author Carlos Vara
 */
@Entity
public class DummyEntity {

    // Getters -----------------------------------------------------------------
    
    @Id
    public String getId() {
        if ( this.id == null ) {
            this.setId(UUID.randomUUID().toString());
        }
        return this.id;
    }
    
    @NotNull
    public String getData() {
        return this.data;
    }
    
    @Version
    @Column(name="OPTLOCK")
    protected int getVersion() {
        return this.version;
    }
    
    
    // Setters -----------------------------------------------------------------
    
    protected void setId(String id) {
        this.id = id;
    }
    
    public void setData(String data) {
        this.data = data;
    }
    
    protected void setVersion(int version) {
        this.version = version;
    }
    
    
    // Fields ------------------------------------------------------------------
    
    private String id;
    private String data;
    private int version;
    
}
