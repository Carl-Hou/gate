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

package org.gate.gui.details.results;

import org.gate.gui.details.results.elements.graph.ElementResult;

import java.awt.*;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

public class ElementResultPane extends JPanel{

	JTabbedPane tab = new JTabbedPane();
	
	JTextArea result;
	JTextArea request;
	JTextArea response;
	
	public ElementResultPane(){
		result = new JTextArea(100, 100); // add config for this
		result.setEditable(false);
		result.setLineWrap(true);
		result.setWrapStyleWord(true);

		request = new JTextArea(100, 100); // add config for this
		request.setEditable(false);
		request.setLineWrap(true);
		request.setWrapStyleWord(true);

		response = new JTextArea(100, 100); // add config for this
		response.setEditable(false);
		response.setLineWrap(true);
		response.setWrapStyleWord(true);

//		result.
		tab.add("Sample result", new JScrollPane(result));
		tab.add("Request", new JScrollPane(request));
		tab.add("Response data", new JScrollPane(response));
		this.add(tab);
		this.setLayout(new GridLayout(1, 1));
	}
	
	public void setElementResult(ElementResult elementResult){
		result.setText(elementResult.getResult());
		request.setText(elementResult.getRequestMessage());
		String responseStr = elementResult.getResponseAsString();
		if(responseStr == null){
			response.setText("response is null");
		}else{
			response.setText(responseStr);
		}

	}
}
