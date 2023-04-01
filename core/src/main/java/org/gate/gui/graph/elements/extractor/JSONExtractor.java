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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.PathNotFoundException;
import io.burt.jmespath.Expression;
import io.burt.jmespath.JmesPath;
import io.burt.jmespath.jackson.JacksonRuntime;
import net.minidev.json.JSONStyle;
import net.minidev.json.JSONValue;
import org.gate.gui.common.TestElement;
import org.gate.gui.graph.elements.extractor.gui.JSONExtractorGui;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class JSONExtractor extends AbstractExtractor {

    private static final Configuration DEFAULT_CONFIGURATION =
            Configuration.defaultConfiguration().addOptions(Option.ALWAYS_RETURN_LIST);

    static JmesPath<JsonNode> jmesPath = new JacksonRuntime();
    static ObjectMapper objectMapper = new ObjectMapper();

    public JSONExtractor(){
        addProp(TestElement.NS_DEFAULT, Extractor_Type, JSON_ExtractorType_JSONPath);
    }


    @Override
    protected List<String> extract(String pattern, String content) {
        String extractorType = getRunTimeProp(NS_DEFAULT, Extractor_Type);
        if(extractorType.equals(JSON_ExtractorType_JSONPath)){
            return extractWithJSONPath(pattern, content);
        }else{
            return extractWithJMESPath(pattern, content);
        }
    }

    List<String> extractWithJSONPath(String pattern, String content){
        List<String> extractedValues = new LinkedList<>();
        try {
            List<Object> extractedObjects = JsonPath.compile(pattern).read(content, DEFAULT_CONFIGURATION);
            extractedObjects.forEach(e -> {
                extractedValues.add(getValueExtractByJSONPath(e));
            });
        }catch(PathNotFoundException e){
            log.info("Use default value:", e);
        }
        return extractedValues;
    }


    List<String> extractWithJMESPath(String pattern, String content) {
        List<String> extractedValues = new LinkedList<>();
        try {
            JsonNode input = objectMapper.readTree(content);
            Expression<JsonNode> expression = jmesPath.compile(pattern);
            JsonNode extractedObjects = expression.search(input);
            if(!extractedObjects.isNull()){
                if (extractedObjects.isArray()) {
                    LinkedList values = new LinkedList();
                    for(int i=0; i< extractedObjects.size(); i++){
                        extractedValues.add(getValueExtractByJMESPath(extractedObjects.get(i)));
                    }
                }else {
                    extractedValues.add(getValueExtractByJMESPath(extractedObjects));
                }
            }
        }catch ( IOException   e ){
            log.info(e);
            throw new IllegalArgumentException(e);
        }
        return extractedValues;
    }


    String getValueExtractByJSONPath(Object value){
        if (value instanceof String) {
            return value.toString();
        } else {
            return JSONValue.toJSONString(value, JSONStyle.LT_COMPRESS);
        }
    }

    String getValueExtractByJMESPath(JsonNode jsonNode) throws JsonProcessingException {
        if(jsonNode.isTextual()){
            return jsonNode.textValue();
        }else{
            return objectMapper.writeValueAsString(jsonNode);

        }
    }

    @Override
    public String getGUI() {
        return JSONExtractorGui.class.getName();
    }

    @Override
    public String getStaticLabel() {
        return "JSON Extractor";
    }
}
