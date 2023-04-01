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

import org.gate.common.config.GateProps;
import org.gate.gui.details.properties.graph.DefaultPropertiesGui;
import org.gate.gui.details.results.elements.graph.ElementResult;
import org.gate.gui.graph.elements.AbstractGraphElement;
import org.gate.runtime.GateContextService;
import org.gate.runtime.GateVariables;

public class Range extends AbstractGraphElement implements Config {
    protected final static String GUI_ClassName_DefaultPropertiesGUI = DefaultPropertiesGui.class.getName();
    private static final String EOFVALUE = // value to return at EOF
            GateProps.getProperty("gate.default.eof", "<EOF>"); //$NON-NLS-1$ //$NON-NLS-2$
    public final static String PN_VariableName = "Variable name";
    public final static String PN_Start = "Start (exclusive)";
    public final static String PN_Stop = "Stop (inclusive)";
    public final static String PN_Step = "Step";

    boolean isComplete = true;
    int current;
    int start;
    int stop;
    int step;
    String variableName;

    public Range(){
        addProp(NS_DEFAULT, PN_VariableName , "rang_var");
        addProp(NS_DEFAULT, PN_Start , "");
        addProp(NS_DEFAULT, PN_Start , "");
        addProp(NS_DEFAULT, PN_Stop , "");
        addProp(NS_DEFAULT, PN_Step , "");
    }

    @Override
    protected void exec(ElementResult result) {

        if(isComplete){
            initValues(result);
            if(result.isSuccess()) {
                current =start;
                isComplete = false;
            }else{
                return;
            }
        }

        if((start > stop && current > stop) || (start<stop && current < stop)){
            GateVariables threadVars = GateContextService.getContext().getVariables();
            current += step;
            threadVars.put(variableName, String.valueOf(current));
        }else{
            result.setFailure(EOFVALUE);
            isComplete = true;
        }
    }

    void initValues(ElementResult result){
        try{
            variableName = getRunTimeProp(NS_DEFAULT,PN_VariableName);
            start = Integer.parseInt(getRunTimeProp(NS_DEFAULT, PN_Start));
            stop = Integer.parseInt(getRunTimeProp(NS_DEFAULT, PN_Stop));
            step = Integer.parseInt(getRunTimeProp(NS_DEFAULT, PN_Step));
        }catch (NumberFormatException e){
            result.setFailure("parameters require integer value");
            result.setThrowable(e);
            log.info(e);
            return;
        }
        if(step == 0){
            result.setFailure("step parameter should not be 0");
            log.info("step parameter should not be 0");
        } else if((start -stop) < 0 && step <0){
            result.setFailure("The parameters cause an endless loop");
            log.info("The parameters cause an endless loop");
        } else if((start -stop) > 0 && step >0){
            result.setFailure("The parameters cause an endless loop");
            log.info("The parameters cause an endless loop");
        }

    }

    @Override
    public String getStaticLabel() {
        return "Range";
    }

    @Override
    public String getGUI() {
        return GUI_ClassName_DefaultPropertiesGUI;
    }
}
