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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexExtractor extends AbstractExtractor {


    public RegexExtractor(){
    }


    @Override
    protected String extract(String pattern, String content) {
        Matcher m = Pattern.compile(pattern).matcher(content);
        if(m.find()){
            if(m.groupCount() > 0){
                return m.group(1);
            }else{
                return m.group();
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
        return "Regex Extractor";
    }
}
