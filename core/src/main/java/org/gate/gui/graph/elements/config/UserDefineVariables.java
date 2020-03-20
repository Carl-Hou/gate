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
package org.gate.gui.graph.elements.config;

import org.gate.common.util.GateUtils;
import org.gate.gui.common.TestElement;
import org.gate.gui.details.properties.graph.DefaultArgumentsGui;
import org.gate.gui.details.results.elements.graph.ConfigureResult;
import org.gate.gui.details.results.elements.graph.ElementResult;
import org.gate.gui.graph.elements.AbstractGraphElement;
import org.gate.runtime.GateContextService;
import org.gate.runtime.GateVariables;

public class UserDefineVariables extends AbstractGraphElement implements Config {

    final static String GUI_ClassName = DefaultArgumentsGui.class.getName();

    public UserDefineVariables(){
        addNameSpace(TestElement.NS_ARGUMENT);
    }


    @Override
    protected void exec(ElementResult result) {
        result.setRunTimeProps(getRunTimePropsMap());
        GateVariables vars = GateContextService.getContext().getVariables();
        getRunTimeProps(TestElement.NS_ARGUMENT).forEach(property ->{
            vars.put(property.getName(),property.getStringValue());
        });

        result.setMessage(GateUtils.dumpToString(vars));
        return ;
    }

    @Override
    public String getGUI(){
        return GUI_ClassName;
    }

}
