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

import org.gate.engine.GateEngineError;
import org.gate.gui.common.TestElement;
import org.gate.gui.details.properties.tree.DefaultArgumentsGui;
import org.gate.gui.tree.test.elements.config.gui.DefaultTreeConfigGui;
import org.gate.runtime.GateContext;
import org.gate.varfuncs.ValueReplacer;
import org.gate.varfuncs.functions.InvalidVariableException;
import org.gate.varfuncs.property.GateProperty;
import org.gate.varfuncs.property.StringProperty;


public class UserDefineVariables extends ConfigElement{

//    final static String GUI_ClassName = DefaultArgumentsGui.class.getName();
    public UserDefineVariables(){
        addNameSpace(TestElement.NS_ARGUMENT);
    }

    @Override
    public void update(GateContext context)  {
        ValueReplacer vc = new ValueReplacer();
        try {
            for(GateProperty property : getProps(NS_ARGUMENT)){
                String rtName = vc.replaceValue(new StringProperty("", property.getName())).getStringValue();
                String rtValue = vc.replaceValue(new StringProperty("", property.getStringValue())).getStringValue();
                context.getVariables().put(rtName, rtValue);
            }
        } catch (InvalidVariableException e) {
            log.fatal("Fail on process user define variables", e);
            throw new GateEngineError(e);
        }
    }

    @Override
    public String getGUI(){
        return DefaultTreeConfigGui.class.getName();
    }
}
