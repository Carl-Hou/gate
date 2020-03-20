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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleScriptContext;


import org.gate.gui.details.results.elements.graph.SamplerResult;
import org.gate.runtime.GateVariables;
import org.gate.runtime.GateContext;
import org.gate.runtime.GateContextService;
import org.gate.varfuncs.CompoundVariable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * javaScript function implementation that executes a piece of JavaScript (not Java!) code and returns its value
 * @since 1.9
 */
public class JavaScript extends AbstractJSR223Function {

    private static final List<String> desc = new LinkedList<>();

    private static final String KEY = "__javaScript"; //$NON-NLS-1$

    private final Logger log = LogManager.getLogger();

    private Object[] values;

    /** {@inheritDoc} */
    @Override
    public String executeRecursion() throws InvalidVariableException {
        String script = ((CompoundVariable) values[0]).execute().trim();
        String varName = ""; //$NON-NLS-1$
        if (values.length > 1) {
            varName = ((CompoundVariable) values[1]).execute().trim();
        }

        String resultStr = ""; //$NON-NLS-1$

        try {
            ScriptEngine scriptEngine = getScriptEngine("javascript");
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
            log.warn("Error running javascript", ex);
        }

        return resultStr;
    }

    /** {@inheritDoc} */
    @Override
    public void setParameters(Collection<CompoundVariable> parameters) throws InvalidVariableException {
        checkParameterCount(parameters, 1, 2);
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
