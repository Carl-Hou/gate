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
package org.gate.gui.graph.elements.sampler.protocol.http;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.burt.jmespath.Expression;
import io.burt.jmespath.JmesPath;
import io.burt.jmespath.jackson.JacksonRuntime;
import org.gate.gui.details.results.elements.graph.ElementResult;
import org.gate.gui.graph.elements.sampler.protocol.http.gui.HttpRequestJEMSPathGui;
import org.gate.runtime.GateContextService;
import org.gate.runtime.GateVariables;
import org.gate.varfuncs.property.GateProperty;

import java.io.IOException;

public class HttpRequestJMESPath extends HTTPHCAbstractImpl {
    static final public String NS_JMESPATH_ARGUMENT = "jmespath_argument";

    static JmesPath<JsonNode> jmesPath = new JacksonRuntime();
    static ObjectMapper objectMapper = new ObjectMapper();

    public HttpRequestJMESPath() { }

    @Override
    void addProps() {
        addNameSpace(NS_JMESPATH_ARGUMENT);
    }

    @Override
    void postRequest(ElementResult result) {
        try {
            JsonNode input = objectMapper.readTree(result.getResponseAsString());
            GateVariables vars = GateContextService.getContext().getVariables();
            for(GateProperty jmesPathProperty : getRunTimeProps(NS_JMESPATH_ARGUMENT)){
                Expression<JsonNode> expression  = jmesPath.compile(jmesPathProperty.getStringValue());
                JsonNode jsonNode = expression.search(input);
                if(jsonNode.isTextual()){
                    vars.put(jmesPathProperty.getName(), jsonNode.textValue());
                }else{
                    vars.put(jmesPathProperty.getName(), objectMapper.writeValueAsString(jsonNode));
                }
            }
        } catch (IOException e) {
            result.setFailure("Fail to extract variable from response");
            log.error("Fail to parse Json object from response", e);
        }

    }

    @Override
    public String getGUI() {
        return HttpRequestJEMSPathGui.class.getName();
    }

    @Override
    public String getStaticLabel() {
        return "Http Request JMESPath";
    }

}
