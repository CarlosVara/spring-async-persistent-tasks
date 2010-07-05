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
package net.carinae.dev.async.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import org.springframework.stereotype.Component;

/**
 * Implementation of {@link Serializer} using Java serialization.
 * 
 * @author Carlos Vara
 */
@Component
public class SerializerJavaImpl implements Serializer {
    
    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] serializeObject(Object obj) {
        
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutput out;
            out = new ObjectOutputStream(bos);
            out.writeObject(obj);
            out.close();
            
            byte[] buf = bos.toByteArray();
            return buf;
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Could not serialize object " + obj, e);
        }

    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Object deserializeObject(byte[] serializedObj) {
        
        try {
            ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(serializedObj));
            Object obj = in.readObject();
            return obj;
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Could not deserialize", e);
        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Could not deserialize", e);
        }

    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T deserializeAndCast(byte[] serializedObj) {
        Object obj = deserializeObject(serializedObj);
        return (T)obj;
    }

}
