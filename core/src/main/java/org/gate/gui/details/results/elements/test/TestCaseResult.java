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

package org.gate.gui.details.results.elements.test;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gate.common.config.GateProps;

public class TestCaseResult extends ModelContainerResult{
    Logger log = LogManager.getLogger();
    final static  String logFix = GateProps.LineSeparator + "=====================";

    private int loopIndex = -1;
    private int variableSetIndex = -1;

    public TestCaseResult(String suiteName, String testCaseName) {
        super(suiteName, testCaseName);
    }

    public void setLoopIndex(int loopIndex){
        // TODO need check index >=0 here?
        this.loopIndex = loopIndex;
    }

    public void setVariableSetIndex(int variableSetIndex){
        // TODO need check index >=0 here?
        this.variableSetIndex = variableSetIndex;
    }

    //for display  loop and variable set index in result tree
    @Override
    public String toString(){
        StringBuffer testCaseResultDisplayName = new StringBuffer(super.getName());
        if(loopIndex > -1){
            testCaseResultDisplayName.append(" (").append(loopIndex).append(")");
        }
        if(variableSetIndex > -1){
            testCaseResultDisplayName.append("  [").append(variableSetIndex).append("]");
        }
        testCaseResultDisplayName.trimToSize();
        return testCaseResultDisplayName.toString();
    }

    void postShutdown(){
//        StringBuilder sb = new StringBuilder(logFix);
//        sb.append("Test Case ");
//        sb.append(getSuiteName()).append(".").append(getName()).append(" Shutdown ").append(logFix);
//        log.info(sb.toString());
    }

    void postStart(){
        //TODO  start and stop time is record in result. Is this really needed?
//        StringBuilder sb = new StringBuilder(logFix);
//        sb.append("Test Case ");
//        sb.append(getSuiteName()).append(".").append(getName()).append(" Start ").append(logFix);
//        log.info(sb.toString());
    }


    public String getResult(){
        StringBuffer sb = new StringBuffer();
        sb.append("Test Start: ").append(startTime).append(GateProps.LineSeparator);
        sb.append("Test Terminate: ").append(shutdownTime).append(GateProps.LineSeparator);
        sb.append("Status: ").append(status).append(GateProps.LineSeparator);
        sb.append("Message: ").append(getMessage()).append(GateProps.LineSeparator);
        return sb.toString();
    }


}
