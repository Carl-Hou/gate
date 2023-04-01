/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.gate.runtime;

import org.gate.varfuncs.property.GateProperty;

import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;


/**
 * Class which defines JMeter variables.
 * These are similar to properties, but they are local to a single thread.
 */
public class GateVariables implements Serializable{
    private final Map<String, Object> variables = new HashMap<>();
    
    public GateVariables() {
        
    }

    /**
     * Remove a variable.
     * 
     * @param key the variable name to remove
     * 
     * @return the variable VALUE, or {@code null} if there was no such variable
     */
    public Object remove(String key) {
        return variables.remove(key);
    }

    /**
     * Creates or updates a variable with a String VALUE.
     * 
     * @param key the variable name
     * @param value the variable VALUE
     */
    public void put(String key, String value) {
        variables.put(key, value);
    }

    /**
     * Creates or updates a variable with a VALUE that does not have to be a String.
     * 
     * @param key the variable name
     * @param value the variable VALUE
     */
    public void putObject(String key, Object value) {
        variables.put(key, value);
    }

    public void putObjects(String keyPrefix, Collection values){
        int i =1;
        for(Object object : values){
            putObject(keyPrefix + "_" + (i), object);
            i++;
        }
        putObject(keyPrefix + "_#", values.size());
    }

    public void putAll(Map<String, ?> vars) {
        variables.putAll(vars);
    }

    public void putAll(GateVariables vars) {
        putAll(vars.variables);
    }

    public void pullAll(LinkedList<GateProperty> vars){
        vars.forEach(p -> {
            put(p.getName(), p.getStringValue());
        });

    }
    /**
     * Gets the VALUE of a variable, coerced to a String.
     * 
     * @param key the name of the variable
     * @return the VALUE of the variable, or {@code null} if it does not exist
     */
    public String get(String key) {
        return (String) variables.get(key);
    }

    public boolean containsKey(String key){
        return variables.containsKey(key);
    }
    /**
     * Gets the VALUE of a variable (not converted to String).
     * 
     * @param key the name of the variable
     * @return the VALUE of the variable, or {@code null} if it does not exist
     */
    public Object getObject(String key) {
        return variables.get(key);
    }

    /**
     * Gets a read-only Iterator over the variables.
     *
     * @return the iterator
     */
    public Iterator<Entry<String, Object>> getIterator(){
        // Got nothing when use this. Don't understand how to use this yet. don't use it.
        return Collections.unmodifiableMap(variables).entrySet().iterator() ;
    }

    // Used by DebugSampler
    public Set<Entry<String, Object>> entrySet(){
        return Collections.unmodifiableMap(variables).entrySet();
    }


}
