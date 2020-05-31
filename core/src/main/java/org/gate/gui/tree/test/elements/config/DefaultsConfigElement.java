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
package org.gate.gui.tree.test.elements.config;

import org.gate.common.util.GateRuntimeExcepiton;
import org.gate.gui.common.DefaultParameters;
import org.gate.gui.details.properties.tree.DefaultPropertiesGui;
import org.gate.runtime.GateContext;

abstract public class DefaultsConfigElement extends ConfigElement{

    @Override
    public void update(GateContext context){
        updateDefaultsInContext(context);
    }

    protected void addConfigProperty(String name, String defaultValue){
        addProp(NS_DEFAULT, name, defaultValue);
    }

    void updateDefaultsInContext(GateContext context){
        String defaultConfigName = getContextConfigKey();
        if(defaultConfigName.isEmpty()){
            throw new GateRuntimeExcepiton("Config element key should not be empty");
        }
        DefaultParameters defaultParameters;
        if(context.getConfigs().containsKey(defaultConfigName)){
            defaultParameters = (DefaultParameters) context.getConfigs().get(defaultConfigName);
        }else{
            defaultParameters = new DefaultParameters();
            context.getConfigs().put(defaultConfigName, defaultParameters);
        }
        defaultParameters.modify(NS_DEFAULT, getProps(NS_DEFAULT));
    }

    abstract protected String getContextConfigKey();

    @Override
    public String getGUI(){
        return DefaultPropertiesGui.class.getName();
    }
}
