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
package org.gate.gui.graph.elements.sampler.protocol.selenium;

import org.gate.common.util.GateException;
import org.gate.gui.details.results.elements.graph.ElementResult;
import org.gate.gui.graph.common.AbstractJSR223GraphElement;
import org.gate.gui.graph.elements.sampler.Sampler;
import org.gate.gui.graph.elements.sampler.protocol.selenium.gui.SeleniumCodeSnippetGui;
import org.gate.gui.graph.elements.sampler.protocol.selenium.util.SeleniumConstantsInterface;
import org.gate.runtime.GateContext;
import org.gate.runtime.GateContextService;
import org.openqa.selenium.WebDriver;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import static org.gate.gui.graph.elements.sampler.JSR223Sampler.PN_Parameters;

public class SeleniumCodeSnippet extends AbstractJSR223GraphElement implements SeleniumConstantsInterface, Sampler {

    final static String language = "groovy";

    public final static String PN_Script = "script";

    public SeleniumCodeSnippet(){
        addNameSpace(NS_ARGUMENT);
        addProp(NS_DEFAULT, PN_DriverId, "");
        addProp(NS_DEFAULT, PN_Parameters, "");
        addNameSpace(NS_TEXT);
        addProp(NS_TEXT, PN_Script,"");
    }

    @Override
    public String getStaticLabel() {
        return "Selenium Code Snippet";
    }
    SeleniumContext getSeleniumContext(){
        GateContext context = GateContextService.getContext();
        SeleniumContext seleniumContext = (SeleniumContext) context.getGraphElementContext().get(Selenium);
        if(seleniumContext == null){
            seleniumContext = new SeleniumContext();
            context.getGraphElementContext().put(Selenium, seleniumContext);
        }
        return  seleniumContext;
    }

    WebDriver getDriver(ElementResult result){
        String driverId = getRunTimeProp(NS_DEFAULT, PN_DriverId);
        WebDriver driver = getSeleniumContext().getDriver(driverId);
        if(driver == null) {
            result.setThrowable(new GateException("Driver not found by id: ".concat(getRunTimeProp(NS_DEFAULT, PN_DriverId))));
        }
        return driver;
    }

    @Override
    protected void exec(ElementResult result) {
        result.setRunTimeProps(getRunTimePropsMap());
        try {
            WebDriver driver = getDriver(result);
            if(result.isFailure()){
                return;
            }
            ScriptEngine scriptEngine = getScriptEngine(language);
            Bindings bindings = scriptEngine.createBindings();
            bindings.put("SampleResult",result);
            bindings.put("driver", driver);
            Object ret = processFileOrScript(scriptEngine,  bindings, getRunTimeProp(NS_DEFAULT, PN_Parameters), getRunTimeProp(NS_TEXT, PN_Script));

            if (ret != null){
                result.setResponseObject(ret);
            }
        } catch (ScriptException e) {
            log.error("Problem in JSR223 script "+getName()+", message:"+e, e);
            result.setThrowable(e);
            return;
        }
    }

    @Override
    public String getGUI() {
        return SeleniumCodeSnippetGui.class.getName();
    }
}
