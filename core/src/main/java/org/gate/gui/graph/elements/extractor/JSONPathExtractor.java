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
package org.gate.gui.graph.elements.extractor;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import net.minidev.json.JSONStyle;
import net.minidev.json.JSONValue;
import org.gate.gui.details.results.elements.graph.ElementResult;
import org.gate.gui.details.results.elements.graph.SamplerResult;
import org.gate.gui.graph.elements.AbstractGraphElement;
import org.gate.runtime.GateContext;
import org.gate.runtime.GateContextService;
import org.gate.runtime.GateVariables;
import org.gate.varfuncs.property.GateProperty;

import java.util.List;

public class JSONPathExtractor extends AbstractGraphElement implements Extractor {

    private static final Configuration DEFAULT_CONFIGURATION =
            Configuration.defaultConfiguration().addOptions(Option.ALWAYS_RETURN_LIST);

    static final String PN_DefaultValue         = "default value"; // $NON-NLS-1$

    public JSONPathExtractor(){
        addNameSpace(NS_ARGUMENT);
        addProp(NS_DEFAULT, PN_DefaultValue, "");
    }

    @Override
    protected void exec(ElementResult result) {
        String defaultValue = getRunTimeProp(NS_DEFAULT, PN_DefaultValue);
        GateContext context = GateContextService.getContext();

        final SamplerResult previousSamplerResult =context.getPreviousResult();
        final String previousResponse = previousSamplerResult.getResponseAsString();
        if(previousSamplerResult == null){
            result.setFailure("previous Sampler is null");
            return;
        } else if(previousResponse == null){
            result.setFailure("previous response is null");
        }

        GateVariables vars = context.getVariables();
        for(GateProperty JsonPathProperty : getRunTimeProps(NS_ARGUMENT)){
            vars.put(JsonPathProperty.getName(), defaultValue);
        }

        for(GateProperty jsonPathProperty : getRunTimeProps(NS_ARGUMENT)){

            List<Object> extractedObjects = JsonPath.compile(jsonPathProperty.getStringValue()).read(
                    previousResponse, DEFAULT_CONFIGURATION);
            if(!extractedObjects.isEmpty()){
                Object value = extractedObjects.get(0);
                String jsonValue;
                if(value instanceof String){
                    jsonValue = value.toString();
                }else{
                    jsonValue = JSONValue.toJSONString(extractedObjects.get(0), JSONStyle.LT_COMPRESS);
                }
                vars.put(jsonPathProperty.getName(), jsonValue);
            }
        }
    }

    @Override
    public String getGUI() {
        return DefaultExactorGui.class.getName();
    }

    @Override
    public String getStaticLabel() {
        return "JSONPath Extractor";
    }
}
