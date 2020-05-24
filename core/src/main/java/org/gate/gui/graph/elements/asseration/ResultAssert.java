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

package org.gate.gui.graph.elements.asseration;

import org.gate.gui.details.results.elements.graph.ElementResult;
import org.gate.gui.details.results.elements.graph.SamplerResult;
import org.gate.gui.graph.elements.asseration.gui.TextAssertGui;

import org.gate.runtime.GateContextService;

public class ResultAssert extends TextAssert {

    public final static String NP_IgnoreException = "Ignore Exceptions";
    public final static String NP_IgnoreResult = "Ignore Result";

    public ResultAssert(){
        addProp(NS_DEFAULT, NP_IgnoreException, "false");
        addProp(NS_DEFAULT, NP_IgnoreResult, "false");
    }

    @Override
    String preExec(ElementResult assertionResult) {
        SamplerResult samplerResult = GateContextService.getContext().getPreviousResult();
        if(!Boolean.parseBoolean(getRunTimeProp(NS_DEFAULT, NP_IgnoreException)) && samplerResult.getThrowable() != null){
            assertionResult.setFailure("previous result throw Exception");
            return samplerResult.getResponseAsString();
        }
        if(!Boolean.parseBoolean(getRunTimeProp(NS_DEFAULT, NP_IgnoreResult)) && samplerResult.isFailure()){
            assertionResult.setFailure("previous result is failure");
            return samplerResult.getResponseAsString();
        }
        return samplerResult.getResponseAsString();
    }
    @Override
    public String getGUI(){
	    return TextAssertGui.class.getName();
    }

    @Override
    public String getStaticLabel() {
        return "Result Assertion";
    }
}
