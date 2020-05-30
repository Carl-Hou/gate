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

import org.gate.common.util.FileServer;
import org.gate.common.util.GateUtils;
import org.gate.runtime.GateContextService;
import org.gate.runtime.GateVariables;

import java.util.LinkedList;

public class CSVDataProvider extends DataProviderElement {

    public final static String PN_FileName = "file name";
    public final static String PN_FileEncoding = "file encoding";
    public final static String PN_VariableNames = "variable names";
    public final static String PN_Delimiter = "delimiter";
    public final static String PN_Quoted = "Quoted";

    public CSVDataProvider(){
        addProp(NS_DEFAULT, PN_FileName, "");
        addProp(NS_DEFAULT, PN_VariableNames, "");
        addProp(NS_DEFAULT, PN_FileEncoding, "");
        addProp(NS_DEFAULT, PN_Delimiter, ",");
        addProp(NS_DEFAULT, PN_Quoted, "false");
    }

    @Override
    public boolean loadVars() throws Exception {
        String _fileName = getProp(NS_DEFAULT, PN_FileName).getStringValue().trim();
        String fileEncoding = getProp(NS_DEFAULT, PN_FileEncoding).getStringValue().trim();
        String delimiter = getProp(NS_DEFAULT, PN_Delimiter).getStringValue().trim();
        boolean isQuoted = getProp(NS_DEFAULT, PN_Quoted).getBooleanValue();
        LinkedList<String> variableNames = GateUtils.getParameterList(getProp(NS_DEFAULT, PN_VariableNames).getStringValue());

        if (delimiter.equals("\\t")) { // $NON-NLS-1$
            delimiter = "\t";// Make it easier to enter a Tab // $NON-NLS-1$
        } else if (delimiter.isEmpty()){
            log.warn("Empty delimiter converted to ','");
            delimiter=",";
        }
        if(fileEncoding.isEmpty()){
            fileEncoding = System.getProperty("file.encoding");
        }
        String alias = _fileName+"#" + getName() +"@"+System.identityHashCode(Thread.currentThread());
        FileServer server = FileServer.getFileServer();
        server.reserveFile(_fileName, fileEncoding, alias);

        GateVariables threadVars = GateContextService.getContext().getVariables();
        String[] lineValues = {};

        if (isQuoted) {
            lineValues = server.getParsedLine(alias, false, false, delimiter.charAt(0));
        } else {
            String line = server.readLine(alias, false, false);
            if(line != null){
                lineValues = line.split(delimiter);
            }else{
                lineValues = new String[0];
            }
        }
        for (int a = 0; a < variableNames.size() && a < lineValues.length; a++) {
            threadVars.put(variableNames.get(a), lineValues[a]);
        }

        if (lineValues.length == 0) {// i.e. EOF
            return false;
        }else{
            return true;
        }
    }
}
