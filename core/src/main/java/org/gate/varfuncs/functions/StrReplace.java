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

public class StrReplace extends AbstractFunction {
    private static final List<String> desc = new LinkedList<String>();
    private static final String KEY = "__strReplace";

    static {
        desc.add("String to get part of");
        desc.add("Search substring");
        desc.add("Replacement");
        desc.add("Name of variable in which to store the result (optional)");
    }

    private Object[] values;

    @Override
    public synchronized String executeRecursion()
            throws InvalidVariableException {

        String totalString = getParameter(0).replace(getParameter(1), getParameter(2));

        GateVariables vars = getVariables();

        if (values.length > 3) {
            String varName = getParameter(3);
            if (vars != null && varName != null && varName.length() > 0) {// vars will be null on TestPlan
                vars.put(varName, totalString);
            }
        }

        return totalString;
    }

    @Override
    public synchronized void setParameters(Collection<CompoundVariable> parameters) throws InvalidVariableException {
        checkMinParameterCount(parameters, 3);
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
