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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.Option;
import io.burt.jmespath.Expression;
import io.burt.jmespath.JmesPath;
import io.burt.jmespath.jackson.JacksonRuntime;


import java.io.IOException;


public class JMESPathExtractor extends AbstractExtractor {

    private static final Configuration DEFAULT_CONFIGURATION =
            Configuration.defaultConfiguration().addOptions(Option.ALWAYS_RETURN_LIST);

    static JmesPath<JsonNode> jmesPath = new JacksonRuntime();
    static ObjectMapper objectMapper = new ObjectMapper();


    public JMESPathExtractor(){ }

    @Override
    protected String extract(String pattern, String content) throws IOException {
        JsonNode input = objectMapper.readTree(content);
        Expression<JsonNode> expression  = jmesPath.compile(pattern);
        JsonNode jsonNode = expression.search(input);
        if(jsonNode.isTextual()){
            return jsonNode.textValue();
        }else{
            return objectMapper.writeValueAsString(jsonNode);
        }
    }

    @Override
    public String getGUI() {
        return DefaultExtractorGui.class.getName();
    }

    @Override
    public String getStaticLabel() {
        return "JMESPath Extractor";
    }
}
