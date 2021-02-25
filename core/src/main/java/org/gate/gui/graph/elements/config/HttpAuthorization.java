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

import org.gate.gui.common.DefaultParameters;
import org.gate.gui.details.properties.graph.DefaultPropertiesGui;
import org.gate.gui.details.results.elements.graph.ElementResult;
import org.gate.gui.graph.elements.AbstractGraphElement;
import org.gate.gui.graph.elements.sampler.protocol.http.HTTPConstantsInterface;
import org.gate.runtime.GateContext;
import org.gate.runtime.GateContextService;
import org.gate.varfuncs.property.GateProperty;
import org.gate.varfuncs.property.StringProperty;

import java.util.LinkedList;

public class HttpAuthorization  extends AbstractGraphElement implements HTTPConstantsInterface, Config {

    public static final String NS_Authorization = "authorization";

    public static final String PN_TOKEN_EXTRACT_EXPRESSION = "token_extract_expression";

    public static final String PN_TOKEN_NAME = "token_name";

    public static final String PN_TOKEN_VALUE = "token_value";


    public HttpAuthorization() {
        addProp(NS_DEFAULT, PN_TOKEN_NAME, "Authorization");
        addProp(NS_DEFAULT, PN_TOKEN_VALUE, "Bearer ${token}");
    }

    @Override
    protected void exec(ElementResult result) {
        log.debug(getRunTimeProp(NS_DEFAULT, PN_TOKEN_VALUE));
        GateContext context = GateContextService.getContext();
        DefaultParameters httpHeaders;
        if(context.getConfigs().containsKey(HeaderManagerName)){
            httpHeaders = (DefaultParameters) context.getConfigs().get(HeaderManagerName);
        }else{
            httpHeaders = new DefaultParameters();
            context.getConfigs().put(HeaderManagerName, httpHeaders);
        }
        LinkedList<GateProperty> headers = new LinkedList<>();
        headers.add(new StringProperty(getRunTimeProp(NS_DEFAULT, PN_TOKEN_NAME), getRunTimeProp(NS_DEFAULT, PN_TOKEN_VALUE)));
        httpHeaders.modify(NS_ARGUMENT, headers);
    }


    @Override
    public String getGUI() {
        return DefaultPropertiesGui.class.getName();
    }

    @Override
    public String getStaticLabel() {
        return "Http Authorization";
    }

}
