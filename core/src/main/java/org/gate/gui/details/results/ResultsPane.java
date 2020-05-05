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


import org.gate.common.config.GateUIDimensions;

import org.gate.gui.details.results.elements.Result;
import org.gate.gui.details.results.elements.graph.ElementResult;
import org.gate.gui.details.results.elements.test.TestResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import javax.swing.*;

public class ResultsPane extends JPanel {
    Logger log = LogManager.getLogger();

    JSplitPane treeAndResults = new JSplitPane();
	JPanel resultDetailsPane = new JPanel(new GridLayout(1,1));

	ElementResultPane elementResultPane = new ElementResultPane();
	TestResultPane testResultPane = new TestResultPane();

	ResultTree resultTree = new ResultTree();

	public ResultsPane(){
		setLayout(new GridLayout(1,1));
		this.add(treeAndResults);
		treeAndResults.setRightComponent(resultDetailsPane);
		treeAndResults.setLeftComponent(resultTree);
		treeAndResults.setDividerLocation(GateUIDimensions.getInstance().getWorkspaceHorizontalDivider());
	}

    public void showResult(Result result){
	    if(ElementResult.class.isInstance(result)){
            showElementResult((ElementResult) result);
        }else if(TestResult.class.isInstance(result)){
	        showTestResult((TestResult) result);
        }
    }

	void showElementResult(ElementResult result){
		elementResultPane.setElementResult(result);
		updateResultDetailsPaneContent(elementResultPane);
	}

	void showTestResult(TestResult result){
		testResultPane.setTestResult(result);
		updateResultDetailsPaneContent(testResultPane);
	}

	void updateResultDetailsPaneContent(JComponent content){
		resultDetailsPane.removeAll();
		resultDetailsPane.add(content);
		resultDetailsPane.updateUI();
	}

	public JTree getResultTree() {
		return resultTree.getTree();
	}
}
