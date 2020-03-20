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

public class If extends AbstractFunction {
    private static final List<String> desc = new LinkedList<String>();
    private static final String KEY = "__if";

    static {
        desc.add("Actual value");
        desc.add("Expected value");
        desc.add("Result if actual == expected");
        desc.add("Result if actual != expected");
        desc.add("Name of variable in which to store the result (optional)");
    }

    private Object[] values;

    public If() {
    }

    @Override
    public synchronized String executeRecursion()
            throws InvalidVariableException {

        String actual = getParameter(0);
        String expected = getParameter(1);

        String result = null;
        if (actual.equals(expected)) {
            result = getParameter(2).toString();
        } else {
            result = getParameter(3).toString();
        }

        GateVariables vars = getVariables();
        if (vars != null && values.length > 4) {
            String varName = getParameter(4).trim();
            vars.put(varName, result);
        }

        return result;
    }

    @Override
    public synchronized void setParameters(Collection<CompoundVariable> parameters) throws InvalidVariableException {
        checkMinParameterCount(parameters, 4);
        values = parameters.toArray();
    }

    @Override
    public String getReferenceKey() {
        return KEY;
    }

    @Override
    public List<String> getArgumentDesc() {
        return desc;
    }

    private String getParameter(int i) {
        return ((CompoundVariable) values[i]).execute();
    }
}
