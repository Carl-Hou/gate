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
package org.gate.varfuncs.functions;

import org.gate.runtime.GateVariables;
import org.gate.varfuncs.CompoundVariable;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class Env extends AbstractFunction{
    private static final List<String> desc = new LinkedList<String>();
    private static final String KEY = "__env";

    // Number of parameters expected - used to reject invalid calls
    private static final int MIN_PARAMETER_COUNT = 1;
    private static final int MAX_PARAMETER_COUNT = 3;

    static {
        desc.add("Name of environment variable");
        desc.add("Name of variable in which to store the result (optional)");
        desc.add("Default value");
    }

    private CompoundVariable[] values;

    /**
     * No-arg constructor.
     */
    public Env() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized String executeRecursion()
            throws InvalidVariableException {
        String propertyName = values[0].execute();
        String propertyDefault = propertyName;
        if (values.length > 2) { // We have a 3rd parameter
            propertyDefault = values[2].execute();
        }
        String propertyValue = getEnvDefault(propertyName, propertyDefault);
        if (values.length > 1) {
            String variableName = values[1].execute();
            if (variableName.length() > 0) {// Allow for empty name
                final GateVariables variables = getVariables();
                if (variables != null) {
                    variables.put(variableName, propertyValue);
                }
            }
        }
        return propertyValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void setParameters(Collection<CompoundVariable> parameters) throws InvalidVariableException {
        checkParameterCount(parameters, MIN_PARAMETER_COUNT, MAX_PARAMETER_COUNT);
        values = parameters.toArray(new CompoundVariable[0]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getReferenceKey() {
        return KEY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getArgumentDesc() {
        return desc;
    }

    /**
     * Get a String value (environment) with default if not present.
     *
     * @param propName   the name of the environment variable.
     * @param defaultVal the default value.
     * @return The PropDefault value
     */
    String getEnvDefault(String propName, String defaultVal) {
        String ans = defaultVal;
        String value = System.getenv(propName);
        if (value != null) {
            ans = value.trim();
        } else if (defaultVal != null) {
            ans = defaultVal.trim();
        }
        return ans;
    }
}
