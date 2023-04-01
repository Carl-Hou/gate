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

import org.gate.common.config.GateProps;
import org.gate.common.util.GateUtils;
import org.gate.gui.details.results.elements.graph.ElementResult;
import org.gate.gui.details.results.elements.graph.SamplerResult;
import org.gate.gui.graph.elements.AbstractGraphElement;
import org.gate.gui.graph.elements.asseration.gui.TextAssertGui;
import org.gate.runtime.GateContextService;
import org.gate.runtime.GateVariables;
import org.gate.varfuncs.property.GateProperty;

import java.util.LinkedList;
import java.util.regex.PatternSyntaxException;

public class TextAssert extends AbstractGraphElement implements Assert {


    public final static String AssertType_Response = "Response";
    public final static String AssertType_Variable = "Variable";
    public final static String[] AssertTypes = {AssertType_Response, AssertType_Variable};
    public final static String AssertType = "Assert Type";
    public final static String NP_VariableName = "Variable Name";
    public final static String NP_IgnoreException = "Ignore Exceptions";
    public final static String NP_IgnoreResult = "Ignore Result";
    public final static String NP_MatchingRule = "Matching Rule";
	public final static String NP_Not = "Not";
	public final static String NP_Trim = "Trim";

	public final static String MR_Contains = "contains";
    public final static String MR_Equals = "equals";
    public final static String MR_Matches = "matches";
	// different with JMeter. matches are regex not perl
	public final static String[] MatchingRules = {MR_Contains, MR_Equals, MR_Matches};



	public TextAssert() {
        addProp(NS_NAME, AssertType, AssertType_Variable);
        addProp(NS_DEFAULT, NP_VariableName, "");
		addProp(NS_DEFAULT, NP_MatchingRule, MR_Contains);
		addProp(NS_DEFAULT, NP_Not, GateProps.FALSE);
        addProp(NS_DEFAULT, NP_Trim, GateProps.TRUE);
		addNameSpace(NS_ARGUMENT);

	}

    @Override
    public String getGUI(){
        return TextAssertGui.class.getName();
    }

    @Override
    public String getStaticLabel() {
        return "Text Assertion";
    }

	@Override
	protected void exec(ElementResult assertionResult) {
		assertionResult.setRunTimeProps(getRunTimePropsMap());
		boolean not = Boolean.parseBoolean(getRunTimeProp(NS_DEFAULT, NP_Not));
		boolean trim = Boolean.parseBoolean(getRunTimeProp(NS_DEFAULT, NP_Trim));
        LinkedList<GateProperty> patternsToTest = getRunTimeProps(NS_ARGUMENT);
        // start to work
        String valueToAssert = getValueToAssert(assertionResult);
        if(assertionResult.isFailure()){
            return;
        }

        if(patternsToTest.isEmpty()){
            assertionResult.appendMessage("patterns to test is empty. skip assert input by pattern");
            return;
        }
        if (valueToAssert == null) {
            assertionResult.setFailure("input of assert is null");
            return;
        }
        if(trim){
            valueToAssert = valueToAssert.trim();
        }

        switch (getRunTimeProp(NS_DEFAULT, NP_MatchingRule)){
			case MR_Contains:
			    for(GateProperty pattern : patternsToTest){
                    boolean result = valueToAssert.contains(pattern.getStringValue());
                    result = not ? !result : result;
                    if(!result){
                        assertionResult.setFailure("fail to match pattern with contains rule:"
                                .concat(valueToAssert).concat(":").concat(pattern.getStringValue()));
                        return;
                    }
                }
				break;
			case MR_Equals:
                for(GateProperty pattern : patternsToTest){
                    boolean result = valueToAssert.equals(pattern.getStringValue());
                    result = not ? !result : result;
                    if(!result){
                        assertionResult.setFailure("fail to match pattern with equals rule:"
                                .concat(valueToAssert).concat(":").concat(pattern.getStringValue()));
                        return;
                    }
                }
                break;
            case MR_Matches:
                for(GateProperty regex : patternsToTest){
                    try {
                        boolean result = valueToAssert.matches(regex.getStringValue());
                        result = not ? !result : result;
                        if(!result){
                            assertionResult.setFailure("fail to match pattern with equals rule:".concat(regex.getStringValue()));
                            return;
                        }
                    }catch(PatternSyntaxException e){
                        assertionResult.setThrowable(e);
                        assertionResult.setFailure("regex is not valid");
                    }
                    if(assertionResult.isFailure()){
                        return;
                    }
                }
                break;
			default:
			    assertionResult.setFailure("Matching Rule not found");
				log.error("Matching Rule not found");
				break;
		}
	}

    String getValueToAssert(ElementResult assertionResult){
        GateProperty assertType = getProp(NS_NAME, AssertType);
        if(assertType.getStringValue().equals(AssertType_Variable)){
            String variableName = getRunTimeProp(NS_DEFAULT, NP_VariableName);

            GateVariables vars = GateContextService.getContext().getVariables();
            String value = vars.get(variableName);
            if(value == null){
                StringBuffer sb = new StringBuffer();
                sb.append("Variable not found. name : ".concat(variableName)).append(GateProps.LineSeparator);
                sb.append("Variables: ").append(GateProps.LineSeparator).append(GateUtils.dumpToString(vars));
                sb.trimToSize();
                assertionResult.setFailure(sb.toString());
            }
            return value;
        }else{
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
    }




}
