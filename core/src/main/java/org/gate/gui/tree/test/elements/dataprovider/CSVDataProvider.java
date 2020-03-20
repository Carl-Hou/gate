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
package org.gate.gui.tree.test.elements.dataprovider;

import org.apache.commons.io.FileUtils;
import org.gate.common.util.GateUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class CSVDataProvider extends DataProviderElement {

    public final static String PN_FileName = "file name";
    public final static String PN_FileEncoding = "file encoding";
    public final static String PN_VariableNames = "variable names";
    public final static String PN_Delimiter = "delimiter";

    public CSVDataProvider(){
        addProp(NS_DEFAULT, PN_FileName, "");
        addProp(NS_DEFAULT, PN_FileEncoding, "");
        addProp(NS_DEFAULT, PN_VariableNames, "");
        addProp(NS_DEFAULT, PN_Delimiter, ",");
    }

    @Override
    public List<HashMap<String, String>> loadData() throws IOException {
        File csvFile = new File(getProp(NS_DEFAULT, PN_FileName).getStringValue().trim());
        String fileEncoding = getProp(NS_DEFAULT, PN_FileEncoding).getStringValue().trim();
        String delimiter = getProp(NS_DEFAULT, PN_Delimiter).getStringValue().trim();
        if(fileEncoding.isEmpty()){
            fileEncoding = System.getProperty("file.encoding");
        }
        List<String> lines = FileUtils.readLines(csvFile, fileEncoding);
        LinkedList<String> variableNames = GateUtils.getParameterList(getProp(NS_DEFAULT, PN_VariableNames).getStringValue());
        List<HashMap<String, String>> variableMaps = new LinkedList<>();
        for(String line : lines){
            LinkedList<String> variables = GateUtils.getParameterList(line, delimiter);
            HashMap<String, String> variableMap = new HashMap<>();
            for(String variableName : variableNames){
                if(!variables.isEmpty()){
                    variableMap.put(variableName, variables.remove());
                }
            }
            variableMaps.add(variableMap);
        }
        return variableMaps;
    }
}
