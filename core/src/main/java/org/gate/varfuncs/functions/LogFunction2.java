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
import org.gate.varfuncs.CompoundVariable;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * <p>
 * Function to log a message.
 * </p>
 *
 * <p>
 * Parameters:
 * <ul>
 * <li>string value</li>
 * <li>log level (optional; defaults to INFO; or DEBUG if unrecognised; or can use OUT or ERR)</li>
 * <li>throwable message (optional)</li>
 * </ul>
 * Returns: - Empty String (so can be used where return value would be a nuisance)
 * @since 2.2
 */
public class LogFunction2 extends AbstractFunction {
    private static final Logger log = LogManager.getLogger();

    private static final List<String> desc = new LinkedList<>();

    private static final String KEY = "__logn"; //$NON-NLS-1$

    // Number of parameters expected - used to reject invalid calls
    private static final int MIN_PARAMETER_COUNT = 1;

    private static final int MAX_PARAMETER_COUNT = 3;
    static {
        desc.add("String to be logged (and returned)");    //$NON-NLS-1$
        desc.add("Log level (default INFO) or OUT or ERR");     //$NON-NLS-1$
        desc.add("Throwable text (optional)"); //$NON-NLS-1$
    }

    private static final String DEFAULT_PRIORITY = "INFO"; //$NON-NLS-1$

    private Object[] values;

    public LogFunction2() {
    }

    /** {@inheritDoc} */
    @Override
    public String executeRecursion()
            throws InvalidVariableException {
        String stringToLog = ((CompoundVariable) values[0]).execute();

        String priorityString;
        if (values.length > 1) { // We have a default
            priorityString = ((CompoundVariable) values[1]).execute();
            if (priorityString.length() == 0) {
                priorityString = DEFAULT_PRIORITY;
            }
        } else {
            priorityString = DEFAULT_PRIORITY;
        }

        Throwable t = null;
        if (values.length > 2) { // Throwable wanted
            t = new Throwable(((CompoundVariable) values[2]).execute());
        }

        LogFunction.logDetails(log, stringToLog, priorityString, t, "");

        return "";

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
