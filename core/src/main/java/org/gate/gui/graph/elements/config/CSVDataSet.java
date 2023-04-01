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
            GateProps.getProperty("gate.default.eof", "<EOF>"); //$NON-NLS-1$ //$NON-NLS-2$


    public final static String PN_FileName = "File Name";
    public final static String PN_VariableNames = "Variable Names";
    public final static String PN_SkipFirstLine = "Skip First Line";
    public final static String PN_FileEncoding = "File Encoding";
    public final static String PN_Delimiter = "Delimiter";
    public final static String PN_Quoted = "Quoted";

    boolean isComplete = true;

    String _fileName;
    String fileEncoding;
    String delimiter;
    boolean isQuoted;
    boolean skipFirstLine;
    LinkedList<String> variableNames;
    FileServer server;
    String alias;

    public CSVDataSet(){
        addProp(NS_DEFAULT, PN_FileName, "");
        addProp(NS_DEFAULT, PN_SkipFirstLine, "true");
        addProp(NS_DEFAULT, PN_FileEncoding, "");
        addProp(NS_DEFAULT, PN_VariableNames, "");
        addProp(NS_DEFAULT, PN_Delimiter, ",");
        addProp(NS_DEFAULT, PN_Quoted, "false");
    }


    void init(ElementResult result){
        _fileName = getRunTimeProp(NS_DEFAULT, PN_FileName);
        skipFirstLine = Boolean.parseBoolean(getRunTimeProp(NS_DEFAULT, PN_SkipFirstLine));
        fileEncoding = getRunTimeProp(NS_DEFAULT, PN_FileEncoding);
        delimiter = getRunTimeProp(NS_DEFAULT, PN_Delimiter);
        isQuoted = Boolean.parseBoolean(getRunTimeProp(NS_DEFAULT, PN_Quoted));
        variableNames = GateUtils.getParameterList(getProp(NS_DEFAULT, PN_VariableNames).getStringValue());
        if (delimiter.equals("\\t")) { // $NON-NLS-1$
            delimiter = "\t";// Make it easier to enter a Tab // $NON-NLS-1$
        } else if (delimiter.isEmpty()){
            log.warn("Empty delimiter converted to ','");
            delimiter=",";
        }
        if(fileEncoding.isEmpty()){
            fileEncoding = System.getProperty("file.encoding");
        }
        alias = _fileName+"@"+System.identityHashCode(Thread.currentThread());
        server = FileServer.getFileServer();
        server.reserveFile(_fileName, fileEncoding, alias);
        try {
            server.resetReader(alias);
            if (skipFirstLine){
                server.readLine(alias, false, false);
            }
        } catch (IOException e) {
            result.setFailure("Fail to reset reader file");
            result.setThrowable(e);
        }
    }


    @Override
    protected void exec(ElementResult result) {
        result.setRunTimeProps(getRunTimePropsMap());

        if(isComplete){
            init(result);
            if(result.isSuccess()){
                isComplete = false;
            }else{
                return;
            }
        }

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
            isComplete = true;
            log.error(e.toString());
        }

        if (lineValues.length == 0) {// i.e. EOF
            for (String var :variableNames) {
                threadVars.put(var, EOFVALUE);
            }
            result.setFailure(EOFVALUE);
            isComplete = true;
        }
    }

    @Override
    public String getStaticLabel() {
        return "CSV Data Set";
    }

    @Override
    public String getGUI() {
        return CSVDataSetGui.class.getName();
    }
}
