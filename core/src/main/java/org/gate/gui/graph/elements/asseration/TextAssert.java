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
import org.gate.gui.graph.elements.AbstractGraphElement;
import org.gate.gui.graph.elements.asseration.gui.TextAssertGui;
import org.gate.varfuncs.property.GateProperty;

import java.util.LinkedList;
import java.util.regex.PatternSyntaxException;

public abstract class TextAssert extends AbstractGraphElement implements Assert {

    public final static String NP_MatchingRule = "Matching Rule";
	public final static String NP_Not = "Not";
	public final static String NP_Trim = "Trim";

	private final static String MR_Contains = "contains";
    private final static String MR_Equals = "equals";
    private final static String MR_Matches = "matches";
	//matches are regex
	public final static String[] MatchingRules = {MR_Contains, MR_Equals, MR_Matches};


	public TextAssert() {
		addProp(NS_DEFAULT, NP_MatchingRule, "contains");
		addProp(NS_DEFAULT, NP_Not, "false");
        addProp(NS_DEFAULT, NP_Trim, "true");
		addNameSpace(NS_ARGUMENT);
	}


	@Override
	protected void exec(ElementResult assertionResult) {
		assertionResult.setRunTimeProps(getRunTimePropsMap());
		boolean not = Boolean.parseBoolean(getRunTimeProp(NS_DEFAULT, NP_Not));
		boolean trim = Boolean.parseBoolean(getRunTimeProp(NS_DEFAULT, NP_Trim));
        LinkedList<GateProperty> patternsToTest = getRunTimeProps(NS_ARGUMENT);
        // start to work
        String input = preExec(assertionResult);
        if(assertionResult.isFailure()){
            return;
        }

        if(patternsToTest.isEmpty()){
            assertionResult.appendMessage("patterns to test is empty. skip assert input by pattern");
            return;
        }
        if (input == null) {
            assertionResult.setFailure("input of assert is null");
            return;
        }
        if(trim){
            input = input.trim();
        }

        switch (getRunTimeProp(NS_DEFAULT, NP_MatchingRule)){
			case MR_Contains:
			    for(GateProperty pattern : patternsToTest){
                    boolean result = input.contains(pattern.getStringValue());
                    result = not ? !result : result;
                    if(!result){
                        assertionResult.setFailure("fail to match pattern with contains rule:"
                                .concat(input).concat(":").concat(pattern.getStringValue()));
                        return;
                    }
                }
				break;
			case MR_Equals:
                for(GateProperty pattern : patternsToTest){
                    boolean result = input.equals(pattern.getStringValue());
                    result = not ? !result : result;
                    if(!result){
                        assertionResult.setFailure("fail to match pattern with equals rule:"
                                .concat(input).concat(":").concat(pattern.getStringValue()));
                        return;
                    }
                }
                break;
            case MR_Matches:
                for(GateProperty regex : patternsToTest){
                    try {
                        boolean result = input.matches(regex.getStringValue());
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
		if(assertionResult.isSuccess()){
		    postExec(assertionResult);
        }
	}

    // for subclass to override
    abstract String preExec(ElementResult assertionResult);

	// for subclass to override
	void postExec(ElementResult assertionResult){

    }


    @Override
    public String getGUI(){
	    return TextAssertGui.class.getName();
    }

//    abstract String getInput() throws GateException;
}
