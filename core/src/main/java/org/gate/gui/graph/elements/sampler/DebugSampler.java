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

package org.gate.gui.graph.elements.sampler;

import org.gate.common.config.GateProps;
import org.gate.common.util.GateException;
import org.gate.common.util.GateRuntimeExcepiton;
import org.gate.common.util.GateUtils;
import org.gate.gui.details.results.elements.graph.ElementResult;
import org.gate.gui.graph.elements.AbstractGraphElement;
import org.gate.runtime.GateContextService;

public class DebugSampler extends AbstractGraphElement implements Sampler{

//	Test parameters
	public final static String PN_Result = "Result";
	public final static String PN_Response = "Response";
	public final static String PN_ThrowException = "Throw Exception";
	public final static String PN_GateProperties = "Gate properties";
	public final static String PN_GateVariables = "Gate variables";
	public final static String PN_SystemProperties = "System properties";
	
	
	public DebugSampler() {
		init();
	}

	public void init(){
		addProp(NS_DEFAULT, PN_Result, "true");
		addProp(NS_DEFAULT, PN_Response, "None");
		addProp(NS_DEFAULT, PN_ThrowException, "false");
		addProp(NS_DEFAULT, PN_GateProperties, "false");
		addProp(NS_DEFAULT, PN_GateVariables, "true");
		addProp(NS_DEFAULT, PN_SystemProperties, "false");
	}

//	@Override
//	protected ElementResult createResult() {
//		return new SamplerResult(getName());
//	}

	//	To make all the elements behave as the same, runTimeProps updated by the engine
	@Override
	public void exec(ElementResult samplerResult) {
		String result = getRunTimeProp(NS_DEFAULT, PN_Result);
		String responseData = getRunTimeProp(NS_DEFAULT, PN_Response);
		String throwException = getRunTimeProp(NS_DEFAULT, PN_ThrowException);

		if(Boolean.valueOf(throwException)){
			samplerResult.setThrowable(new GateException("Test"));
			throw new GateRuntimeExcepiton("Runtime Exception from Debug");
		}
		
		samplerResult.setResponseObject(getRunTimeProp(NS_DEFAULT, PN_Response));

		if(Boolean.valueOf(result)){
			samplerResult.setSuccess();
		}else{
			samplerResult.setFailure();
		}

		samplerResult.setRunTimeProps(getRunTimePropsMap());

		if(getRunTimeProp(NS_DEFAULT, PN_GateProperties).toLowerCase().equals("true")){
			StringBuffer sb = new StringBuffer("Gate Properties: ");
			sb.append(GateProps.LineSeparator);
			GateProps.getProperties().forEach((k, v) ->{
				sb.append(k).append(" : ").append(v).append(GateProps.LineSeparator);
			});
			samplerResult.appendMessage(sb);
		}

		if(getRunTimeProp(NS_DEFAULT, PN_GateVariables).toLowerCase().equals("true")){
			StringBuffer sb = new StringBuffer("Variable:");
			samplerResult.appendMessage(GateUtils.dumpToString(GateContextService.getContext().getVariables()));
		}

		if(getRunTimeProp(NS_DEFAULT, PN_SystemProperties).toLowerCase().equals("true")){
			StringBuffer sb = new StringBuffer("System Properties: ");
			sb.append(GateProps.LineSeparator);
			System.getProperties().forEach((k,v) ->{
				sb.append(k).append(" : ").append(v).append(GateProps.LineSeparator);
			});
			samplerResult.appendMessage(sb);
		}
		return;
	}

	@Override
	public String getGUI() {
		return GUI_ClassName_DefaultPropertiesGUI;
	}
}
