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

/**
 * Contract for serializer implementations.
 * <p>
 * @see "http://code.google.com/p/thrift-protobuf-compare/wiki/Benchmarking"
 * 
 * @author Carlos Vara
 */
public interface Serializer {

    /**
     * Serializes an object.
     * 
     * @param obj
     *            Object to serialize.
     * @return The serialized representation of the object.
     */
    byte[] serializeObject(Object obj);


    /**
     * Deserializes an object.
     * 
     * @param serializedObj
     *            Serialized representation of the object.
     * @return The deserialized object.
     */
    Object deserializeObject(byte[] serializedObj);
    
    
    /**
     * Deserializes an object and casts it to the requested type.
     * 
     * @param <T>
     *            The type of the requested object.
     * @param serializedObj
     *            Serialized representation of the object.
     * @return The deserialized object, casted to the requested type.
     */
    <T> T deserializeAndCast(byte[] serializedObj);
    
}
