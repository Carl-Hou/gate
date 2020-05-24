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
package org.gate.gui.graph.elements.asseration;

import org.gate.common.config.GateProps;
import org.gate.common.util.GateException;
import org.gate.common.util.GateUtils;
import org.gate.gui.details.results.elements.graph.AssertionResult;
import org.gate.gui.details.results.elements.graph.ElementResult;
import org.gate.gui.graph.elements.GateVariableNotFoundException;
import org.gate.runtime.GateContextService;
import org.gate.runtime.GateVariables;

public class VariableAssert extends TextAssert {

    final static String NP_VariableName = "Variable Name";

    public VariableAssert(){
        addProp(NS_DEFAULT, NP_VariableName, "");
    }

    @Override
    String preExec(ElementResult assertionResult){
        String variableName = getRunTimeProp(NS_DEFAULT, NP_VariableName);
        GateVariables vars = GateContextService.getContext().getVariables();
        String value = vars.get(variableName);
        if(value == null){
            StringBuffer sb = new StringBuffer();
            sb.append("Variable not found. name : ".concat(variableName)).append(GateProps.LineSeparator);
            sb.append("Variables: ").append(GateProps.LineSeparator).append(GateUtils.dumpToString(vars));
            sb.trimToSize();
            assertionResult.setFailure(sb.toString());
        }
        return value;
    }

    @Override
    public String getStaticLabel() {
        return "Variable Assertion";
    }
}
