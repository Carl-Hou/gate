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

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import org.gate.runtime.GateVariables;
import org.gate.varfuncs.CompoundVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FileToString Function to read a complete file into a String.
 * <p>
 * Parameters:
 * <ul>
 *  <li>file name</li>
 *  <li>file encoding (optional)</li>
 *  <li>variable name (optional)</li>
 * </ul>
 *
 * Returns:
 * <ul>
 *  <li>the whole text from a file</li>
 *  <li>or {@code **ERR**} if an error occurs</li>
 *  <li>value is also optionally saved in the variable for later re-use.</li>
 * </ul>
 * @since 2.4
 */
public class FileToString extends AbstractFunction {
    private static final Logger log = LoggerFactory.getLogger(FileToString.class);

    private static final List<String> desc = new LinkedList<>();

    private static final String KEY = "__FileToString";//$NON-NLS-1$

    static final String ERR_IND = "**ERR**";//$NON-NLS-1$

    static {
        desc.add("Enter path (absolute or relative) to file");//$NON-NLS-1$
        desc.add("File encoding if not the platform default (opt)");//$NON-NLS-1$
        desc.add("Name of variable in which to store the result (optional)");//$NON-NLS-1$
    }

    private static final int MIN_PARAM_COUNT = 1;

    private static final int MAX_PARAM_COUNT = 3;

    private static final int ENCODING = 2;

    private static final int PARAM_NAME = 3;

    private Object[] values;

    public FileToString() {
    }

    /** {@inheritDoc} */
    @Override
    public String executeRecursion()
            throws InvalidVariableException {

        String fileName = ((CompoundVariable) values[0]).execute();

        String encoding = null;//means platform default
        if (values.length >= ENCODING) {
            encoding = ((CompoundVariable) values[ENCODING - 1]).execute().trim();
            if (encoding.length() <= 0) { // empty encoding, return to platform default
                encoding = null;
            }
        }

        String myName = "";//$NON-NLS-1$
        if (values.length >= PARAM_NAME) {
            myName = ((CompoundVariable) values[PARAM_NAME - 1]).execute().trim();
        }

        String myValue = ERR_IND;

        try {
            File file = new File(fileName);
            if(file.exists() && file.canRead()) {
                myValue = FileUtils.readFileToString(new File(fileName), encoding);
            } else {
                log.warn("Could not read open: {} ", fileName);
            }
        } catch (IOException e) {
            log.warn("Could not read file: {} {}", fileName, e.getMessage(), e);
        }

        if (myName.length() > 0) {
            GateVariables vars = getVariables();
            if (vars != null) {// Can be null if called from Config item testEnded() method
                vars.put(myName, myValue);
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("{} name: {} value: {}", Thread.currentThread().getName(), myName, myValue); //$NON-NLS-1$
        }

        return myValue;
    }

    /** {@inheritDoc} */
    @Override
    public void setParameters(Collection<CompoundVariable> parameters) throws InvalidVariableException {
        checkParameterCount(parameters, MIN_PARAM_COUNT, MAX_PARAM_COUNT);
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
