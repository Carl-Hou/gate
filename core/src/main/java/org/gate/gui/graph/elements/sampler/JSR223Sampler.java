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
import org.gate.common.util.GateUtils;
import org.gate.gui.details.properties.graph.DefaultScriptGui;
import org.gate.gui.details.results.elements.graph.ElementResult;
import org.gate.gui.details.results.elements.graph.SamplerResult;
import org.gate.gui.graph.elements.AbstractGraphElement;
import org.gate.runtime.GateContext;
import org.gate.runtime.GateContextService;
import org.gate.runtime.GateVariables;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.script.*;
import java.io.IOException;

public class JSR223Sampler extends AbstractGraphElement implements Sampler{

//	Test parameters
	public final static String PN_Language = "Language";
	public final static String PN_Parameters = "Parameters";
	public final static String PN_Script = "script";
	public final static String SCRIPT_Groovy = "groovy";
	public final static String SCRIPT_JavaScript = "javascript";
	public final static String DEFAULT_SCRIPT_LANGUAGE = "groovy";
	public final static String[] SUPPORT_SCRIPT_LANG = {SCRIPT_Groovy, SCRIPT_JavaScript};

	public JSR223Sampler() {
		init();
	}

	public void init(){
		addProp(NS_DEFAULT, PN_Parameters, "");
		addNameSpace(NS_TEXT);
		addProp(NS_TEXT, PN_Language , DEFAULT_SCRIPT_LANGUAGE);
		addProp(NS_TEXT, PN_Script,"");
	}

	private static class LazyHolder {
		public static final ScriptEngineManager INSTANCE = new ScriptEngineManager();
	}

	public static ScriptEngineManager getInstance() {
		return LazyHolder.INSTANCE;
	}

	/**
	 * @return {@link ScriptEngine} for language defaulting to groovy if language is not set
	 * @throws ScriptException when no {@link ScriptEngine} could be found
	 */
	protected ScriptEngine getScriptEngine() throws ScriptException {

		String lang = getProp(NS_TEXT, PN_Language).getStringValue();
		ScriptEngine scriptEngine = getInstance().getEngineByName(lang);
		if (scriptEngine == null) {
			throw new ScriptException("Cannot find engine named: '"+lang+"', ensure you set language field in JSR223 Test Element: "+getName());
		}
		return scriptEngine;
	}

	/**
	 * Populate variables to be passed to scripts
	 * @param bindings Bindings
	 */
	protected void populateBindings(Bindings bindings) {
		final String label = getName();
		final String scriptParameters = getRunTimeProp(NS_DEFAULT, PN_Parameters);
		// Use actual class name for log
		final Logger logger = LogManager.getLogger(getClass().getName());
		bindings.put("log", logger); // $NON-NLS-1$ (this name is fixed)
		bindings.put("Label", label); // $NON-NLS-1$ (this name is fixed)
		bindings.put("Parameters", scriptParameters); // $NON-NLS-1$ (this name is fixed)
		String [] args= GateUtils.split(scriptParameters, " ");//$NON-NLS-1$
		bindings.put("args", args); // $NON-NLS-1$ (this name is fixed)
		// Add variables for access to context and variables
		GateContext ctx = GateContextService.getContext();
		bindings.put("ctx", ctx); // $NON-NLS-1$ (this name is fixed)
		GateVariables vars = ctx.getVariables();
		bindings.put("vars", vars); // $NON-NLS-1$ (this name is fixed)
		bindings.put("props", GateProps.getProperties()); // $NON-NLS-1$ (this name is fixed)
		// For use in debugging:
		bindings.put("OUT", System.out); // $NON-NLS-1$ (this name is fixed)
		// previous sampler result
		SamplerResult prev = ctx.getPreviousResult();
		bindings.put("prev", prev); // $NON-NLS-1$ (this name is fixed)
	}

	/**
	 * This method will runGui inline script or file script with special behaviour for file script:
	 * - If ScriptEngine implements Compilable script will be compiled and cached
	 * - If not if will be runGui
	 * @param scriptEngine ScriptEngine
	 * @param bindings {@link Bindings} might be null
	 * @return Object returned by script
	 * @throws IOException when reading the script fails
	 * @throws ScriptException when compiling or evaluation of the script fails
	 */
	protected Object processFileOrScript(ScriptEngine scriptEngine, Bindings bindings) throws IOException, ScriptException {
		if (bindings == null) {
			bindings = scriptEngine.createBindings();
		}
		populateBindings(bindings);
		// TODO support script file like JMeter
		String script = getRunTimeProp(NS_TEXT, PN_Script);
		if (!script.equals("")) {
			return scriptEngine.eval(script, bindings);
		} else {
			throw new ScriptException("Both script file and script text are empty for element:"+getName());
		}
	}

	//	To make all the elements behave as the same, runTimeProps updated by the engine
	@Override
	public void exec(ElementResult result) {

		result.setRunTimeProps(getRunTimePropsMap());
		try {
			ScriptEngine scriptEngine = getScriptEngine();
			Bindings bindings = scriptEngine.createBindings();

			bindings.put("SampleResult",result);
			Object ret = processFileOrScript(scriptEngine, bindings);

			if (ret != null){
				result.setResponseObject(ret);
			}
		} catch (IOException | ScriptException e) {
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
