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

package org.gate.gui.graph.elements.control;

import org.gate.gui.details.results.elements.graph.ControllerResult;
import org.gate.gui.details.results.elements.graph.ElementResult;
import org.gate.gui.graph.elements.AbstractGraphElement;

import javax.script.*;

public class Decide extends AbstractGraphElement implements Controller {
    public final static String PN_Condition = "Condition";

    public Decide() {
        addProp(NS_DEFAULT, PN_Condition, "");
    }

    @Override
    public String getGUI() {
        return GUI_ClassName_DefaultPropertiesGUI;
    }

    @Override
    public String getStaticLabel() {
        return "Decide";
    }

    /**
     * Initialization On Demand Holder pattern
     */
    private static class LazyHolder {
        public static final ScriptEngineManager INSTANCE = new ScriptEngineManager();
    }

    private static final ThreadLocal<ScriptEngine> NASHORN_ENGINE = new ThreadLocal<ScriptEngine>() {

        @Override
        protected ScriptEngine initialValue() {
            return getInstance().getEngineByName("nashorn");//$NON-NLS-N$
        }

    };

    /**
     * @return ScriptEngineManager singleton
     */
    private static ScriptEngineManager getInstance() {
        return LazyHolder.INSTANCE;
    }

    @Override
    protected void exec(ElementResult controllerResult) {

        controllerResult.setRunTimeProps(getRunTimePropsMap());
        String condition = getRunTimeProp(NS_DEFAULT, PN_Condition);
        String result = null;
        try {
            result = evaluate(condition);
        } catch (ScriptException e) {
            log.error(e);
            controllerResult.setThrowable(e);
            return;
        }

        switch(result) {
            case "false":
                controllerResult.setFailure();
                break;
            case "true":
                controllerResult.setSuccess();
                break;
            default:
                controllerResult.setFailure();
                controllerResult.setMessage(" BAD CONDITION ElementResult:: " + result + " :: expected true or false");
        }

        return;
    }

    public String evaluate(String condition) throws ScriptException {
        ScriptContext newContext = new SimpleScriptContext();
        newContext.setBindings(NASHORN_ENGINE.get().createBindings(), ScriptContext.ENGINE_SCOPE);
        Object o = NASHORN_ENGINE.get().eval(condition, newContext);
        return o.toString();
    }

}
