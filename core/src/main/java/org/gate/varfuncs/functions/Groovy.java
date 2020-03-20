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

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gate.common.config.GateProps;
import org.gate.gui.graph.elements.sampler.JSR223Sampler;
import org.gate.runtime.GateContext;
import org.gate.runtime.GateContextService;
import org.gate.runtime.GateVariables;
import org.gate.varfuncs.CompoundVariable;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * __groovy function 
 * Provides a Groovy interpreter
 * @since 3.1
 */
public class Groovy extends AbstractJSR223Function {
    private static final Logger log = LogManager.getLogger();

    private static final List<String> DESCRIPTION = new LinkedList<>();

    private static final String KEY = "__groovy"; //$NON-NLS-1$

    public static final String INIT_FILE = "groovy.utilities"; //$NON-NLS-1$

    static {
        DESCRIPTION.add("Expression to evaluate");// $NON-NLS1$
        DESCRIPTION.add("Name of variable in which to store the result (optional)");// $NON-NLS1$
    }

    private Object[] values;
    private ScriptEngine scriptEngine;


    /** {@inheritDoc} */
    @Override
    public synchronized String executeRecursion() throws InvalidVariableException {

        String script = ((CompoundVariable) values[0]).execute().trim();
        String varName = ""; //$NON-NLS-1$
        if (values.length > 1) {
            varName = ((CompoundVariable) values[1]).execute().trim();
        }

        String resultStr = ""; //$NON-NLS-1$

        try {
            Bindings bindings = scriptEngine.createBindings();
            // To Use actual class name for log
            bindings.put("log", log);
            populateBindings(bindings);

            // Execute the script
            Object out = scriptEngine.eval(script, bindings);
            if (out != null) {
                resultStr = out.toString();
            }

            if (varName.length() > 0) {// vars will be null on TestPlan
                Object vars = bindings.get("vars");
                if(vars != null) {
                    ((GateVariables) vars).put(varName, resultStr);
                }
            }

        } catch (Exception ex) {
            log.warn("Error running groovy script", ex);
        }

        return resultStr;

    }

    /*
     * Helper method for use by scripts
     *
     */
    public void log_info(String s) {
        log.info(s);
    }

    /** {@inheritDoc} */
    @Override
    public void setParameters(Collection<CompoundVariable> parameters) throws InvalidVariableException {
        checkParameterCount(parameters, 1, 2);
        values = parameters.toArray();
        scriptEngine = getScriptEngine("groovy"); //$NON-NLS-N$

        String fileName = GateProps.getProperty(INIT_FILE);
        if(!StringUtils.isEmpty(fileName)) {
            File file = new File(fileName);
            if(!(file.exists() && file.canRead())) {
                // File maybe relative to JMeter home
                File oldFile = file;
                file = new File(GateProps.getGateHome(), fileName);
                if(!(file.exists() && file.canRead())) {
                    throw new InvalidVariableException("Cannot read file, neither from:"+oldFile.getAbsolutePath()+
                            ", nor from:"+file.getAbsolutePath()+", check property '"+INIT_FILE+"'");
                }
            }
            try (FileReader fr = new FileReader(file); BufferedReader reader = new BufferedReader(fr)) {
                Bindings bindings = scriptEngine.createBindings();
                bindings.put("log", log);
                scriptEngine.eval(reader, bindings);
            } catch(Exception ex) {
                throw new InvalidVariableException("Failed loading script:"+file.getAbsolutePath(), ex);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getReferenceKey() {
        return KEY;
    }

    /** {@inheritDoc} */
    @Override
    public List<String> getArgumentDesc() {
        return DESCRIPTION;
    }
}
