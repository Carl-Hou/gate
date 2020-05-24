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

package org.gate.gui.graph.elements;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;

import org.gate.common.util.GateRuntimeExcepiton;
import org.gate.common.util.GateUtils;
import org.gate.gui.common.AbstractTestElement;
import org.gate.gui.common.DefaultParameters;
import org.gate.gui.details.properties.graph.DefaultPropertiesGui;
import org.gate.gui.details.results.elements.graph.*;
import org.gate.gui.graph.elements.asseration.Assert;
import org.gate.gui.graph.elements.comment.Comment;
import org.gate.gui.graph.elements.config.Config;
import org.gate.gui.graph.elements.control.ActionReference;
import org.gate.gui.graph.elements.control.Controller;
import org.gate.gui.graph.elements.sampler.Sampler;
import org.gate.gui.graph.elements.timer.Timer;
import org.gate.gui.graph.elements.extractor.Extractor;
import org.gate.runtime.GateContextService;
import org.gate.varfuncs.ValueReplacer;
import org.gate.varfuncs.functions.InvalidVariableException;
import org.gate.varfuncs.property.GateProperty;
import org.gate.varfuncs.property.StringProperty;

abstract public class AbstractGraphElement extends AbstractTestElement implements GraphElement, Serializable {

    protected final static String GUI_ClassName_DefaultPropertiesGUI = DefaultPropertiesGui.class.getName();
    protected HashMap<String, LinkedList<GateProperty>> runTimePropsMap = new HashMap();

    public AbstractGraphElement() {
        super();
    }

    /*
     *   reset the GraphElement's status for engine execute. This is method each time after the cell execute completely
     * */
    public void reset() {
        runTimePropsMap.values().forEach(v -> v.clear());
        runTimePropsMap.clear();
    }

    protected String getRunTimeProp(String nameSpace, String name) {
        return getProp(runTimePropsMap, nameSpace, name).getStringValue();
    }

    protected LinkedList<GateProperty> getRunTimeProps(String nameSpace) {
        return runTimePropsMap.get(nameSpace);
    }

    public HashMap<String, LinkedList<GateProperty>> getRunTimePropsMap() {
        return runTimePropsMap;
    }

    private  ElementResult createResult(){
        Class thisClazz = getClass();

        if(Sampler.class.isAssignableFrom(thisClazz)){
            return new SamplerResult(getName());
        }

        if(Timer.class.isAssignableFrom(thisClazz)){
            return new SamplerResult(getName());
        }

        if(Config.class.isAssignableFrom(thisClazz)){
            return new ConfigureResult(getName());
        }

        if(Controller.class.isAssignableFrom(thisClazz)){
            if(ActionReference.class.isAssignableFrom(thisClazz)){
                return new ActionReferenceResult(getName());
            }
            return new ControllerResult(getName());
        }
        if(Assert.class.isAssignableFrom(thisClazz)){
            return new AssertionResult(getName());
        }
        if(Extractor.class.isAssignableFrom(thisClazz)){
            return new ExtractorResult(getName());
        }
        if(Comment.class.isAssignableFrom(thisClazz)){
            throw new GateRuntimeExcepiton("Only 'note' allow to link to Comments vertex：" + getName());
        }
        throw new GateRuntimeExcepiton("This element type is not support:" + thisClazz.getName());
    }

    abstract protected void exec(ElementResult result);

    @Override
    public ElementResult execute() {
        ElementResult result = createResult();
        updateRuntimeProperties(getPropertiesWithDefaults(), result);
        if (result.getThrowable() != null) {
            return result;
        }
        // Keep this for later use such like add some pre or post method.
        try {
            exec(result);
        } catch (Throwable t) {
            log.error("Element execute fail:", t);
            result.setThrowable(t);
        }
        return result;
    }

    /*
        for load before process variables. Not all graph elements need this so this is not abstract

     */

    protected HashMap<String, LinkedList<GateProperty>> getPropertiesWithDefaults() {
        String DefaultConfigName = getContextConfigKey();
        // default config not exist
        if(getContextConfigKey().isEmpty()){
            return getPropsMap();
        }

        DefaultParameters defaultSeleniumParameters =
                (DefaultParameters) GateContextService.getContext().getConfigs().get(DefaultConfigName);
        if (defaultSeleniumParameters == null) {
            return getPropsMap();
        }

        HashMap<String, LinkedList<GateProperty>> propsMapCopy = GateUtils.deepCopy(getPropsMap()).get();
        for(String nameSpace : getNameSpacesToApplyDefault()){
            defaultSeleniumParameters.applyDefaultsInNameSpace(propsMapCopy.get(nameSpace));
        }
        return propsMapCopy;


    }

    protected String getContextConfigKey(){
        return "";
    }

    protected LinkedList<String> getNameSpacesToApplyDefault(){
        LinkedList<String> nameSpaces = new LinkedList<String>();
        nameSpaces.add(NS_DEFAULT);
        return nameSpaces;
    }

    protected void updateRuntimeProperties(HashMap<String, LinkedList<GateProperty>> propsMap, ElementResult result) {
        runTimePropsMap.clear();
        try {
            runTimePropsMap.putAll(getRuntimeCopy(propsMap));
        } catch (InvalidVariableException e) {
            log.fatal("Error occur when prepare runtime variable:", e);
            result.setThrowable(e);
        }
    }

    protected HashMap<String, LinkedList<GateProperty>> getRuntimeCopy(
            HashMap<String, LinkedList<GateProperty>> properties) throws InvalidVariableException {

        HashMap<String, LinkedList<GateProperty>> runtimePropertiesCopy = new HashMap<>();
        ValueReplacer vc = new ValueReplacer();
        for (HashMap.Entry<String, LinkedList<GateProperty>> entry : properties.entrySet()) {
            LinkedList<GateProperty> runTimeProperties = new LinkedList<>();
            runtimePropertiesCopy.put(entry.getKey(), runTimeProperties);
            for (GateProperty property : entry.getValue()) {
                String rtName = vc.replaceValue(new StringProperty("", property.getName())).getStringValue();
                String rtValue = vc.replaceValue(new StringProperty("", property.getStringValue())).getStringValue();
                runTimeProperties.add(new StringProperty(rtName, rtValue));
            }

        }
        return runtimePropertiesCopy;
    }
}
