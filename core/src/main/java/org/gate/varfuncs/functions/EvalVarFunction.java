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


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gate.runtime.GateVariables;
import org.gate.varfuncs.CompoundVariable;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Function to evaluate a string which may contain variable or function references.
 *
 * Parameter: string to be evaluated
 *
 * Returns: the evaluated value
 * @since 2.3.1
 */
public class EvalVarFunction extends AbstractFunction {

    private static final Logger log = LogManager.getLogger();

    private static final List<String> desc = new LinkedList<>();

    private static final String KEY = "__evalVar"; //$NON-NLS-1$

    // Number of parameters expected - used to reject invalid calls
    private static final int MIN_PARAMETER_COUNT = 1;
    private static final int MAX_PARAMETER_COUNT = 1;

    static {
        desc.add("Name of variable"); //$NON-NLS-1$
    }

    private Object[] values;

    public EvalVarFunction() {
    }

    /** {@inheritDoc} */
    @Override
    public String executeRecursion()
            throws InvalidVariableException {
        String variableName = ((CompoundVariable) values[0]).execute();
        final GateVariables vars = getVariables();
        if (vars == null){
            log.error("Variables have not yet been defined");
            return "**ERROR - see log file**";
        }
        String variableValue = vars.get(variableName);
        CompoundVariable cv = new CompoundVariable(variableValue);
        return cv.execute();
    }

    /** {@inheritDoc} */
    @Override
    public void setParameters(Collection<CompoundVariable> parameters) throws InvalidVariableException {
        checkParameterCount(parameters, MIN_PARAMETER_COUNT, MAX_PARAMETER_COUNT);
        values = parameters.toArray();
    }

    /** {@inheritDoc} */
    @Override
    public String getReferenceKey() {
        return KEY;
    }

    /** {@inheritDoc} */
    @Override
    public List<String> getArgumentDesc() {
        return desc;
    }

}
