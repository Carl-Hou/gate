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

import org.apache.commons.codec.binary.Base64;
import org.gate.runtime.GateVariables;
import org.gate.varfuncs.CompoundVariable;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class Base64Encode extends AbstractFunction {
    private static final List<String> desc = new LinkedList<String>();
    private static final String KEY = "__base64Encode";

    // Number of parameters expected - used to reject invalid calls
    private static final int MIN_PARAMETER_COUNT = 1;
    private static final int MAX_PARAMETER_COUNT = 2;

    static {
        desc.add("Base64 string to be encoded");
        desc.add("Name of variable in which to store the result (optional)");
    }

    private CompoundVariable[] values;

    @Override
    public synchronized String executeRecursion()
            throws InvalidVariableException {
        String sourceString = values[0].execute();

        String decodedValue = new String(Base64.encodeBase64(sourceString.getBytes()));
        if (values.length > 1) {
            String variableName = values[1].execute();
            if (variableName.length() > 0) {// Allow for empty name
                final GateVariables variables = getVariables();
                if (variables != null) {
                    variables.put(variableName, decodedValue);
                }
            }
        }
        return decodedValue;
    }

    @Override
    public synchronized void setParameters(Collection<CompoundVariable> parameters) throws InvalidVariableException {
        checkParameterCount(parameters, MIN_PARAMETER_COUNT, MAX_PARAMETER_COUNT);
        values = parameters.toArray(new CompoundVariable[parameters.size()]);
    }

    @Override
    public String getReferenceKey() {
        return KEY;
    }

    @Override
    public List<String> getArgumentDesc() {
        return desc;
    }
}
