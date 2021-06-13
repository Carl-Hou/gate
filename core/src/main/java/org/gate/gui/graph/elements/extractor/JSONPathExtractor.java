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


import java.util.List;

public class JSONPathExtractor extends AbstractExtractor {

    private static final Configuration DEFAULT_CONFIGURATION =
            Configuration.defaultConfiguration().addOptions(Option.ALWAYS_RETURN_LIST);

    public JSONPathExtractor(){

    }

    @Override
    protected String extract(String pattern, String content) {
        List<Object> extractedObjects = JsonPath.compile(pattern).read(content, DEFAULT_CONFIGURATION);
        if(!extractedObjects.isEmpty()) {
            Object value = extractedObjects.get(0);
            if (value instanceof String) {
                return value.toString();
            } else {
                return JSONValue.toJSONString(extractedObjects.get(0), JSONStyle.LT_COMPRESS);
            }
        }
        return null;
    }

    @Override
    public String getGUI() {
        return DefaultExtractorGui.class.getName();
    }

    @Override
    public String getStaticLabel() {
        return "JSONPath Extractor";
    }
}
