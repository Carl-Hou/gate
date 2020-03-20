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
import org.gate.common.config.GateProps;
import org.gate.common.util.GateUtils;
import org.gate.gui.details.results.elements.graph.SamplerResult;
import org.gate.runtime.GateContext;
import org.gate.runtime.GateContextService;
import org.gate.runtime.GateVariables;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.IOException;

abstract public class AbstractJSR223Function extends AbstractFunction {

    /**
     * Populate variables to be passed to scripts
     * @param bindings Bindings
     */
    protected void populateBindings(Bindings bindings) {

        // Add variables for access to context and variables
        GateContext ctx = GateContextService.getContext();
        bindings.put("ctx", ctx); // $NON-NLS-1$ (this name is fixed)
        GateVariables vars = ctx.getVariables();
        bindings.put("vars", vars); // $NON-NLS-1$ (this name is fixed)
        bindings.put("props", GateProps.getProperties()); // $NON-NLS-1$ (this name is fixed)
        // For use in debugging:
        bindings.put("OUT", System.out); // $NON-NLS-1$ (this name is fixed)
        bindings.put("threadName", Thread.currentThread().getName()); //$NON-NLS-1$
        // previous sampler result
        SamplerResult prev = ctx.getPreviousResult();
        bindings.put("prev", prev); // $NON-NLS-1$ (this name is fixed)

    }

    /**
     * Initialization On Demand Holder pattern
     */
    private static class LazyHolder {
        public static final ScriptEngineManager INSTANCE = new ScriptEngineManager();
    }

    /**
     * @return ScriptEngineManager singleton
     */
    private static ScriptEngineManager getInstance() {
        return LazyHolder.INSTANCE;
    }

    /**
     * @Parameters lang language of script engine.
     * @return {@link ScriptEngine} for language defaulting to groovy if language is not set
     * @throws NullPointerException when no {@link ScriptEngine} could be found by lang
     */
    protected ScriptEngine getScriptEngine(String lang) {
        ScriptEngine scriptEngine = getInstance().getEngineByName(lang);
        return scriptEngine;
    }

    /**
     * This method will runGui inline script with special behaviour for file script:
     * - If not if will be runGui
     * @param scriptEngine ScriptEngine
     * @param bindings {@link Bindings} might be null
     * @return Object returned by script
     * @throws ScriptException when compiling or evaluation of the script fails
     */
    protected Object processFileOrScript(ScriptEngine scriptEngine, Bindings bindings, String script) throws ScriptException {
        if (bindings == null) {
            bindings = scriptEngine.createBindings();
        }
        populateBindings(bindings);

        if (!script.equals("")) {
            return scriptEngine.eval(script, bindings);
        } else {
            throw new ScriptException("script text are empty");
        }
    }

}
