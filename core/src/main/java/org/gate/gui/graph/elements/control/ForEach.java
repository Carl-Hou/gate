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

import org.gate.common.config.GateProps;
import org.gate.gui.details.properties.graph.DefaultPropertiesGui;
import org.gate.gui.details.results.elements.graph.ElementResult;
import org.gate.gui.graph.elements.AbstractGraphElement;
import org.gate.runtime.GateContextService;
import org.gate.runtime.GateVariables;

import javax.script.*;

public class ForEach extends AbstractGraphElement implements Controller {
    /*
        ForEach store value as object to support Selenium findElemens
     */
    protected final static String GUI_ClassName_DefaultPropertiesGUI = DefaultPropertiesGui.class.getName();
    private static final String EOFVALUE = // value to return at EOF
            GateProps.getProperty("gate.default.eof", "<EOF>"); //$NON-NLS-1$ //$NON-NLS-2$
    public final static String PN_InputVariablePrefix = "Input variable prefix";
    public final static String PN_StartIndex = "Start index of loop (exclusive)";
    public final static String PN_EndIndex = "End index of loop (inclusive)";
    public final static String PN_OutputPutVariableName = "Output variable name";

    boolean isComplete =true;

    int start;
    int end;
    int current;
    String inputVariablePrefix;
    String outputPutVariableName;

    public ForEach() {
        addProp(NS_DEFAULT, PN_InputVariablePrefix , "");
        addProp(NS_DEFAULT, PN_StartIndex , "");
        addProp(NS_DEFAULT, PN_EndIndex , "");
        addProp(NS_DEFAULT, PN_OutputPutVariableName , "");
    }

    void initValues(ElementResult result){
        try{
            start = Integer.parseInt(getRunTimeProp(NS_DEFAULT, PN_StartIndex));
            end = Integer.parseInt(getRunTimeProp(NS_DEFAULT, PN_EndIndex));
            current = start;

        }catch (NumberFormatException e){
            result.setFailure("index parameters require integer (bigger than 0) value");
            result.setThrowable(e);
            log.info(e);
            return;
        }
        inputVariablePrefix = getRunTimeProp(NS_DEFAULT,PN_InputVariablePrefix);
        outputPutVariableName = getRunTimeProp(NS_DEFAULT, PN_OutputPutVariableName);

        if(start <0 || end <0){
            result.setFailure("index should bigger than 0");
            log.info("index should bigger than 0");
        }
    }

    @Override
    protected void exec(ElementResult result) {
        if(isComplete){
            initValues(result);
            if(result.isSuccess()) {
                current = start ;
                isComplete = false;
            }else{
                return;
            }
        }

        if(current < end){
            current ++;
            GateVariables threadVars = GateContextService.getContext().getVariables();
            String currentInputVariableName = inputVariablePrefix + "_" + current;
            if(threadVars.containsKey(currentInputVariableName)){
                Object v = threadVars.getObject(currentInputVariableName);
                threadVars.putObject(outputPutVariableName, v);
            }else{
                result.setFailure("Can't find variable:" + currentInputVariableName);
                isComplete = true;
                log.info("Can't find variable:" + currentInputVariableName);
            }
        }else{
            result.setFailure(EOFVALUE);
            isComplete = true;
        }

    }

    @Override
    public String getGUI() {
        return GUI_ClassName_DefaultPropertiesGUI;
    }

    @Override
    public String getStaticLabel() {
        return "ForEach";
    }

}
