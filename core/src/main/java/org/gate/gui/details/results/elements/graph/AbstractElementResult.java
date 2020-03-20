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
package org.gate.gui.details.results.elements.graph;

import org.gate.common.config.GateProps;
import org.gate.common.util.GateUtils;
import org.gate.gui.details.results.elements.DefaultResult;
import org.gate.varfuncs.property.GateProperty;

import java.util.HashMap;
import java.util.LinkedList;

public abstract class AbstractElementResult extends DefaultResult implements ElementResult {

    final static String LineSeparator = GateProps.LineSeparator;

    StringBuilder requestMessageBuilder = new StringBuilder();

    String runTimeProps = "";
    Object responseObject = null;


    public AbstractElementResult(String name) {
        super(name);
    }

    @Override
    public void setResponseObject(Object responseObject){
        this.responseObject = responseObject;
//        if(responseObject == null){
//            this.responseObject = "null";
//            appendMessage("Response is null");
//        }else {
//            this.responseObject = responseObject;
//        }
    }

    @Override
    public Object getResponseObject(){
        return responseObject;
    }
    @Override
    public String getResponseAsString(){
        if(responseObject != null){
            return String.valueOf(responseObject);
        }else{
            return null;
        }

    }

    @Override
    public void setRunTimeProps(HashMap<String, LinkedList<GateProperty>> runTimeProps) {
        this.runTimeProps = propsToString(runTimeProps);
    }

    @Override
    public String getResult(){
        StringBuffer sb = new StringBuffer("Execute Result: ");
        if(isSuccess()){
            sb.append("Success");
        }else {
            sb.append("Fail");
        }
        sb.append(LineSeparator);
        if(getMessage().length()>0){
            sb.append("Message:").append(LineSeparator);
            sb.append(getMessage()).append(LineSeparator);
        }
        if(getThrowable() != null){
            sb.append("Exception: ").append(LineSeparator);
            sb.append(GateUtils.getStackTrace(getThrowable()));
        }
        return sb.toString();
    }

    @Override
    public void appendRequestMessage(CharSequence message){
        requestMessageBuilder.append(message);
    }

    @Override
    public String getRequestMessage(){
        StringBuilder requestBuilder = new StringBuilder(requestMessageBuilder);
        requestBuilder.append("Runtime Properties:").append(LineSeparator).append(runTimeProps).append(LineSeparator);
        requestBuilder.trimToSize();
        return requestBuilder.toString();
    }

    String propsToString(HashMap<String, LinkedList<GateProperty>> props){
        StringBuffer sb = new StringBuffer();
        props.forEach((ns,gateProperties) ->{
            sb.append("---NameSpace: ").append(ns).append(GateProps.LineSeparator);
            gateProperties.forEach(property -> {
                sb.append(property.getName()).append(":").append(property.getStringValue()).append(GateProps.LineSeparator);
            });
        });
        sb.trimToSize();
        return sb.toString();
    }

}
