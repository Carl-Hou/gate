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

package org.gate.gui.graph.elements.comment;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gate.common.util.GateRuntimeExcepiton;
import org.gate.gui.actions.Command;
import org.gate.gui.details.properties.graph.DefaultCommentsGui;
import org.gate.gui.details.results.elements.graph.ElementResult;
import org.gate.gui.graph.elements.AbstractGraphElement;
import org.gate.gui.graph.elements.sampler.Sampler;

public class Comments extends AbstractGraphElement implements Comment {
	final static Logger log = LogManager.getLogger();
//	Test parameters
	public final static String PN_Comments = "comments";

	public Comments() {
		addNameSpace(NS_TEXT);
		addProp(NS_TEXT, PN_Comments, "");
	}

	@Override
	public void exec(ElementResult samplerResult) {
		// should not be here.
		log.fatal("Should not have condition link to this comments vertex");
		throw new GateRuntimeExcepiton("Should not have condition link to this comments vertex");
	}

	@Override
	public String getGUI() {
		return DefaultCommentsGui.class.getName();
	}

	@Override
	public String getStaticLabel() {
		return "Comment";
	}
}
