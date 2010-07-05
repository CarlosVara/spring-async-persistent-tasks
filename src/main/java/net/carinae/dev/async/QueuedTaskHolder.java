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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import org.springframework.core.style.ToStringCreator;


/**
 * Persistent entity that stores an async task.
 * 
 * @author Carlos Vara
 */
@Entity
@Table(name="TASK_QUEUE")
public class QueuedTaskHolder {
    
    // Getters -----------------------------------------------------------------
    
    @Id
    public String getId() {
        if ( this.id == null ) {
            this.setId(UUID.randomUUID().toString());
        }
        return this.id;
    }
    
    @NotNull
    @Past
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="CREATION_STAMP")
    public Calendar getCreationStamp() {
        return this.creationStamp;
    }
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="TRIGGER_STAMP")
    public Calendar getTriggerStamp() {
        return triggerStamp;
    }

    @Past
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="STARTED_STAMP")
    public Calendar getStartedStamp() {
        return this.startedStamp;
    }
    
    @Past
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="COMPLETED_STAMP")
    public Calendar getCompletedStamp() {
        return this.completedStamp;
    }
    
    @Lob
    @NotNull
    @Column(name="SERIALIZED_TASK")
    public byte[] getSerializedTask() {
        return this.serializedTask;
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
    
    public void setCreationStamp(Calendar creationStamp) {
        this.creationStamp = creationStamp;
    }
    
    public void setTriggerStamp(Calendar triggerStamp) {
        this.triggerStamp = triggerStamp;
    }
    
    public void setStartedStamp(Calendar startedStamp) {
        this.startedStamp = startedStamp;
    }

    public void setCompletedStamp(Calendar completedStamp) {
        this.completedStamp = completedStamp;
    }

    public void setSerializedTask(byte[] serializedTask) {
        this.serializedTask = serializedTask;
    }
    
    protected void setVersion(int version) {
        this.version = version;
    }
    
    
    // Fields ------------------------------------------------------------------

    private String id;
    private Calendar creationStamp;
    private Calendar triggerStamp = null;
    private Calendar startedStamp = null;
    private Calendar completedStamp = null;
    private byte[] serializedTask;
    private int version;
    
    
    // Lifecycle events --------------------------------------------------------
    
    @SuppressWarnings("unused")
    @PrePersist
    private void onAbstractBaseEntityPrePersist() {
        this.ensureId();
        this.markCreation();
    }
    
    /**
     * Ensures that the entity has a unique UUID.
     */
    private void ensureId() {
        this.getId();
    }
    
    /**
     * Sets the creation stamp to now.
     */
    private void markCreation() {
        setCreationStamp(Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC")));
    }
    
    
    // Methods -----------------------------------------------------------------
    
    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss z");
        return new ToStringCreator(this).append("id", getId())
            .append("creationStamp", (getCreationStamp()!=null)?sdf.format(getCreationStamp().getTime()):null)
            .append("startedStamp", (getStartedStamp()!=null)?sdf.format(getStartedStamp().getTime()):null)
            .append("completedStamp", (getCompletedStamp()!=null)?sdf.format(getCompletedStamp().getTime()):null)
            .toString();
    }    

}
