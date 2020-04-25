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

import org.gate.gui.details.properties.graph.DefaultScriptGui;
import org.gate.gui.details.results.elements.graph.ElementResult;
import org.gate.gui.graph.common.AbstractJSR223GraphElement;

import javax.script.*;

public class JSR223Sampler extends AbstractJSR223GraphElement implements Sampler{

//	Test parameters
	public final static String PN_Language = "Language";
	public final static String PN_Parameters = "Parameters";
	public final static String PN_Script = "script";
	public final static String SCRIPT_Groovy = "groovy";
	public final static String SCRIPT_JavaScript = "javascript";
	public final static String DEFAULT_SCRIPT_LANGUAGE = "groovy";
	public final static String[] SUPPORT_SCRIPT_LANG = {SCRIPT_Groovy, SCRIPT_JavaScript};

	public JSR223Sampler() {
		addProp(NS_DEFAULT, PN_Parameters, "");
		addNameSpace(NS_TEXT);
		addProp(NS_TEXT, PN_Language , DEFAULT_SCRIPT_LANGUAGE);
		addProp(NS_TEXT, PN_Script,"");
	}

	@Override
	public String getStaticLabel() {
		return "JSR223 Sampler";
	}

	//	To make all the elements behave as the same, runTimeProps updated by the engine
	@Override
	public void exec(ElementResult result) {

		result.setRunTimeProps(getRunTimePropsMap());
		try {
			ScriptEngine scriptEngine = getScriptEngine(getRunTimeProp(NS_TEXT, PN_Language));
			Bindings bindings = scriptEngine.createBindings();

			bindings.put("SampleResult",result);
			Object ret = processFileOrScript(scriptEngine,  bindings, getRunTimeProp(NS_DEFAULT, PN_Parameters), getRunTimeProp(NS_TEXT, PN_Script));

			if (ret != null){
				result.setResponseObject(ret);
			}
		} catch (ScriptException e) {
			log.error("Problem in JSR223 script "+getName()+", message:"+e, e);
			result.setThrowable(e);
			return;
		}
	}

	@Override
	public String getGUI(){
		return DefaultScriptGui.class.getName();
	}

}
