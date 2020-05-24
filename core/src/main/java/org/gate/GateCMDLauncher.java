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

package org.gate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gate.common.config.GateProps;
import org.gate.common.util.GateException;
import org.gate.common.util.GateUtils;
import org.gate.common.util.JUnitReportGenerator;
import org.gate.engine.TestEngine;
import org.gate.engine.TestStopListener;
import org.gate.gui.tree.GateTreeNode;
import org.gate.gui.tree.GateTreeSupport;
import org.gate.gui.tree.test.elements.TestCase;
import org.gate.gui.tree.test.elements.TestSuite;

import java.io.File;
import java.util.*;

public class GateCMDLauncher implements TestStopListener {

    Logger log = LogManager.getLogger(this);

    public void launch(String gateTestFile, String testSuiteNamePrefix, String testCaseNamePrefix){
        File testFile = new File(gateTestFile);

        if(!GateTreeSupport.load(testFile)){
            // exit when fail to load test file.
            System.exit(1);
        }

        HashMap<GateTreeNode, LinkedList<GateTreeNode>> selectedTestCases =
                GateTreeSupport.getFilteredTestCases(testSuiteNamePrefix, testCaseNamePrefix);

        TestEngine engine = new TestEngine();
        String result = engine.prepare(selectedTestCases);
        if(!result.isEmpty()){
            System.out.println("Error:" + result);
            System.exit(1);
        }
        engine.addStopTestListener(this);
        engine.runTest();
    }

    @Override
    public void testStop() {
        JUnitReportGenerator jUnitReportGenerator = new JUnitReportGenerator();
        String reportName = GateProps.getProperty("gate.report.file.name",
                GateTreeSupport.getTestSuitesNode().getName() + "_report.xml");
        String reportPath = GateProps.getProperty("gate.report.file.path", GateProps.getGateHome());

        File report = new File(reportPath, reportName);
        log.info(report.getAbsolutePath());
        try {
            jUnitReportGenerator.generate(report);
        } catch (GateException e) {
            log.warn("Fail to save report :" + report.getAbsolutePath(), e);
        }
    }
    // TODO remove this use function in GateTreeSupport instead
    HashMap<GateTreeNode, LinkedList<GateTreeNode>> getSelectedTestCases(String testSuiteNamePrefix, String testCaseNamePrefix) {
        HashMap<GateTreeNode, LinkedList<GateTreeNode>> selectedTestCases = new HashMap();

        LinkedList<String> suiteNamePrefixes = GateUtils.getParameterList(testSuiteNamePrefix);
        LinkedList<String> caseNamePrefixes = GateUtils.getParameterList(testCaseNamePrefix);

        LinkedList<GateTreeNode> suites =
                GateTreeSupport.findChildren(GateTreeSupport.getTestSuitesNode(), TestSuite.class);

        while(!suites.isEmpty()){
            GateTreeNode testSuiteNode = suites.remove();
            LinkedList<GateTreeNode> testCases = GateTreeSupport.findChildren(testSuiteNode, TestCase.class);
            if (suiteNamePrefixes.size() == 0) {
                selectedTestCases.put(testSuiteNode, testCases);
            } else {
                for (String suiteNamePrefix : suiteNamePrefixes) {
                    if (testSuiteNode.getName().startsWith(suiteNamePrefix)) {
                        selectedTestCases.put(testSuiteNode, testCases);
                    }
                }
            }
        }

        // process test name. keep test case only.
        for(HashMap.Entry<GateTreeNode, LinkedList<GateTreeNode>> suiteEntry : selectedTestCases.entrySet()){
                LinkedList<GateTreeNode> testCases = new LinkedList<>();
                for(GateTreeNode node : suiteEntry.getValue()){
                    if(TestCase.class.isInstance(node.getGateTreeElement())){
                        if(caseNamePrefixes.size() == 0){
                            testCases.add(node);
                        }else {
                            for(String caseNamePrefix : caseNamePrefixes){
                                if(node.getGateTreeElement().getName().startsWith(caseNamePrefix)){
                                    testCases.add(node);
                                }
                            }
                        }

                    }
                }
                suiteEntry.getValue().clear();
                suiteEntry.setValue(testCases);
        }

        return selectedTestCases;
    }

}
