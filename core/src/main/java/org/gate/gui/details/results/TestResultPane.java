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

import org.gate.gui.details.results.elements.test.TestResult;

import javax.swing.JPanel;
import javax.swing.JTextArea;
import java.awt.*;

public class TestResultPane extends JPanel{

	public JTextArea resultTextArea = new JTextArea();
	public TestResultPane(){
		setLayout(new GridLayout(1,1));
		resultTextArea.setEditable(false);
		add(resultTextArea);
	}

	public void setTestResult(TestResult result){
		resultTextArea.removeAll();
		resultTextArea.setText(result.getResult());
	}
	
}
