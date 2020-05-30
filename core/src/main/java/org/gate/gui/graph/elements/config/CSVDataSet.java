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

import javafx.beans.value.ObservableBooleanValue;
import org.gate.common.config.GateProps;
import org.gate.common.util.FileServer;
import org.gate.common.util.GateUtils;
import org.gate.gui.details.results.elements.graph.ElementResult;
import org.gate.gui.graph.elements.AbstractGraphElement;
import org.gate.runtime.GateContextService;
import org.gate.runtime.GateVariables;

import java.io.IOException;
import java.util.LinkedList;

public class CSVDataSet extends AbstractGraphElement implements Config {

    private static final String EOFVALUE = // value to return at EOF
            GateProps.getProperty("csvdataset.eofstring", "<EOF>"); //$NON-NLS-1$ //$NON-NLS-2$


    public final static String PN_FileName = "file name";
    public final static String PN_VariableNames = "variable names";
    public final static String PN_FileEncoding = "file encoding";
    public final static String PN_Delimiter = "delimiter";
    public final static String PN_Quoted = "Quoted";



    public CSVDataSet(){
        addProp(NS_DEFAULT, PN_FileName, "");
        addProp(NS_DEFAULT, PN_FileEncoding, "");
        addProp(NS_DEFAULT, PN_VariableNames, "");
        addProp(NS_DEFAULT, PN_Delimiter, ",");
        addProp(NS_DEFAULT, PN_Quoted, "false");
    }


    @Override
    protected void exec(ElementResult result) {
        result.setRunTimeProps(getRunTimePropsMap());

        String _fileName = getRunTimeProp(NS_DEFAULT, PN_FileName);
        String fileEncoding = getRunTimeProp(NS_DEFAULT, PN_FileEncoding);
        String delimiter = getRunTimeProp(NS_DEFAULT, PN_Delimiter);
        boolean isQuoted = Boolean.parseBoolean(getRunTimeProp(NS_DEFAULT, PN_Quoted));
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
        String alias = _fileName+"@"+System.identityHashCode(Thread.currentThread());
        FileServer server = FileServer.getFileServer();
        server.reserveFile(_fileName, fileEncoding, alias);

        GateVariables threadVars = GateContextService.getContext().getVariables();
        String[] lineValues = {};
        try {
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
        } catch (IOException e) { // treat the same as EOF
            result.setThrowable(e);
            log.error(e.toString());
        }

        if (lineValues.length == 0) {// i.e. EOF
            for (String var :variableNames) {
                threadVars.put(var, EOFVALUE);
            }
            result.setFailure(EOFVALUE);
        }
    }

    @Override
    public String getStaticLabel() {
        return "CSV Data Set";
    }

    @Override
    public String getGUI() {
        return GUI_ClassName_DefaultPropertiesGUI;
    }
}
