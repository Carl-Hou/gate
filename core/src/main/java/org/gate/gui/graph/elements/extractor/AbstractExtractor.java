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

import org.gate.gui.common.TestElement;
import org.gate.gui.details.results.elements.graph.ElementResult;
import org.gate.gui.details.results.elements.graph.SamplerResult;
import org.gate.gui.graph.elements.AbstractGraphElement;
import org.gate.runtime.GateContext;
import org.gate.runtime.GateContextService;
import org.gate.runtime.GateVariables;
import org.gate.varfuncs.property.GateProperty;

import java.util.List;
import java.util.Random;


public abstract class AbstractExtractor extends AbstractGraphElement implements Extractor, ExtractorConstantsInterface{

    public AbstractExtractor(){
        addNameSpace(NS_ARGUMENT);
        addProp(NS_NAME, SourceType, SourceType_Response);
        addProp(NS_DEFAULT, PN_DefaultValue, "");
        addProp(NS_DEFAULT, PN_MatchNo, "");
    }


    public String getSelectSourceType(){
        return getProp(TestElement.NS_NAME, SourceType).getStringValue();
    }


    @Override
    protected void exec(ElementResult result) {
        // get variables
        String defaultValue = getRunTimeProp(NS_DEFAULT, PN_DefaultValue);
        String matchNoValue = getRunTimeProp(NS_DEFAULT, PN_MatchNo).trim();
        boolean matchAll = false;
        int matchNo = 0;
        if(matchNoValue.isEmpty()){
            matchAll = true;
        }else{
            matchNo = Integer.parseInt(matchNoValue);
        }
        if(matchNo < 0){
            throw new IllegalArgumentException(PN_MatchNo + "require a Positive Integer value");
        }
        // start process
        GateContext context = GateContextService.getContext();
        final SamplerResult previousSamplerResult =context.getPreviousResult();

        if(previousSamplerResult == null){
            result.setFailure("previous Sampler is null");
            return;
        }

        GateVariables vars = context.getVariables();

        String content = null;
        if(getSelectSourceType().equals(SourceType_Response)){
            content = previousSamplerResult.getResponseAsString();
        }else {
            content = vars.get(getRunTimeProp(NS_DEFAULT, PN_Variable_Name));
        }

        if(content == null){
            result.setFailure("Fail to get source for extract");
        }

        for(GateProperty patternProperty : getRunTimeProps(NS_ARGUMENT)){
            String pattern = patternProperty.getStringValue();
            if(pattern.trim().isEmpty()) throw new IllegalArgumentException("pattern is empty");
            try {
                List<String> values = extract(pattern, content);
                if(values.size() == 0){
                    vars.put(patternProperty.getName(), defaultValue);
                }else if(values.size() == 1){
                    vars.put(patternProperty.getName(), values.get(0));
                }else if (values.size() > 1){
                    if(matchAll){
                        vars.putObjects(patternProperty.getName(), values);
                    }else{
                        if(matchNo == 0){
                            Random random = new Random();
                            vars.put(patternProperty.getName(), values.get(random.nextInt(values.size())));
                        }else{
                            if(matchNo <= values.size()){
                                vars.put(patternProperty.getName(), values.get(matchNo -1));
                            }
                        }
                    }
                }
            }catch (Throwable t){
                log.error("Fail to extract from content", t);
                result.setThrowable(t);
                result.appendMessage("Fail to extract from content");
            }
        }
    }

    protected abstract List<String> extract(String pattern, String content) throws  Exception;


}
