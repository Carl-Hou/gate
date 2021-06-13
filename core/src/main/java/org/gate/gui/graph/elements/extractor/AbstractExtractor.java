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
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;


public abstract class AbstractExtractor extends AbstractGraphElement implements Extractor, ExtractorConstantsInterface{

    public AbstractExtractor(){
        addNameSpace(NS_ARGUMENT);
        addProp(NS_NAME, PN_SourceType, ST_Response);
        setResponseAsSourceType();
    }

    void setResponseAsSourceType(){
        getProps(NS_DEFAULT).clear();
        addProp(NS_DEFAULT, PN_DefaultValue, "");
        initProperties();
    }

    void setVariableAsSourceType(){
        getProps(NS_DEFAULT).clear();
        addProp(NS_DEFAULT, PN_Variable_Name,"");
        addProp(NS_DEFAULT, PN_DefaultValue, "");
        initProperties();
    }

    protected void initProperties(){ }

    public String getSelectSourceType(){
        return getProp(TestElement.NS_NAME, PN_SourceType).getStringValue();
    }

    public void onSelectSourceType(String sourceType){
        setProp(TestElement.NS_NAME, PN_SourceType, sourceType);
        switch (sourceType){
            case ST_Response:
                setResponseAsSourceType();
                break;
            case ST_Variable:
                setVariableAsSourceType();
                break;
            default:
                log.fatal("Internal Error");
        }
    }

    protected void preExtract(){ }

    @Override
    protected void exec(ElementResult result) {

        String defaultValue = getRunTimeProp(NS_DEFAULT, PN_DefaultValue);
        GateContext context = GateContextService.getContext();

        final SamplerResult previousSamplerResult =context.getPreviousResult();

        if(previousSamplerResult == null){
            result.setFailure("previous Sampler is null");
            return;
        }

        GateVariables vars = context.getVariables();
        for(GateProperty patternProperty : getRunTimeProps(NS_ARGUMENT)){
            vars.put(patternProperty.getName(), defaultValue);
        }

        String content = null;
        if(getSelectSourceType().equals(ST_Response)){
            content = previousSamplerResult.getResponseAsString();
        }else {
            content = vars.get(getRunTimeProp(NS_DEFAULT, PN_Variable_Name));
        }

        if(content == null){
            result.setFailure("Fail to get source for extract");
        }
        // extract each pattern to var
        preExtract();
        for(GateProperty patternProperty : getRunTimeProps(NS_ARGUMENT)){
            // take care "\\" when copy from java code
            String pattern = patternProperty.getStringValue();
            try {
                String value = extract(pattern, content);
                if (null != value) {
                    vars.put(patternProperty.getName(), value);
                }
            }catch (Throwable t){
                log.error("Fail to extract from content", t);
                result.setThrowable(t);
                result.appendMessage("Fail to extract from content");
            }
        }
    }

    protected abstract String extract(String pattern, String content) throws  Exception;
}
