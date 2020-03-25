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
package org.gate.gui.actions;

import org.gate.engine.TestStopListener;
import org.gate.gui.GuiPackage;
import org.gate.gui.common.OptionPane;
import org.gate.engine.TestEngine;
import org.gate.gui.MainFrame;
import org.gate.gui.tree.GateTreeNode;
import org.gate.gui.tree.GateTreeSupport;
import org.gate.gui.tree.test.TestTree;
import org.gate.gui.tree.test.elements.TestCase;
import org.gate.gui.tree.test.elements.TestSuite;
import org.gate.gui.tree.test.elements.TestSuites;

import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.event.ActionEvent;
import java.util.*;

public class Start extends AbstractGateAction {

    private static final Set<String> commands = new HashSet<>();

    static {
        commands.add(ActionNames.ACTION_START);
        commands.add(ActionNames.ACTION_STOP);
    }

    private TestEngine engine;

    @Override
    public void doAction(ActionEvent e) throws IllegalUserActionException {

        if(e.getActionCommand().equals(ActionNames.ACTION_START)){
            runCase();
        } else if(e.getActionCommand().equals(ActionNames.ACTION_STOP)){
            if (engine != null) {
                log.info("Stopping test");
                engine.stopTest();
            }
        }
    }

    @Override
    public Set<String> getActionNames() {
        return commands;
    }

    void runCase(){

        HashMap testSuites = getPreprocessedSelectedTestNodes();
        GateTreeSupport.syncGui();
        engine = new TestEngine();
        String result = engine.prepare(testSuites);
        if(!result.isEmpty()){
            OptionPane.showErrorMessageDialog("Error", result);
            return;
        }

        engine.addStopTestListener(new TestStopListener() {
            @Override
            public void testStop() {
                GuiPackage.getIns().getMainFrame().stopTest();
            }
        });
        engine.runTest();
        GuiPackage.getIns().getMainFrame().testStarted();
    }

    HashMap<GateTreeNode, LinkedList<GateTreeNode>> getPreprocessedSelectedTestNodes(){
        TreePath[] treePaths = GuiPackage.getIns().getTestTree().getSelectionPaths();
        if(treePaths == null){
            OptionPane.showErrorMessageDialog("Error", "No test suites or cases selected");
            // TODO why don't return an empty list here.
        }

        HashMap<GateTreeNode, LinkedList<GateTreeNode>> testSuites = new HashMap<>();

        for(TreePath treePath : treePaths){
            GateTreeNode node = (GateTreeNode) treePath.getLastPathComponent();
            if(node.includeElement(TestSuites.class)){
                testSuites = GateTreeSupport.getFilteredTestCases("","");
                break;
            }
            if(node.includeElement(TestSuite.class)){
                testSuites.put(node, new LinkedList<>());
            }
            if(node.includeElement(TestCase.class)){
                if(!testSuites.containsKey(node.getParentGateTreeNode())){
                    testSuites.put(node.getParentGateTreeNode(), new LinkedList<>());
                }
                testSuites.get(node.getParentGateTreeNode()).add(node);
            }
        }

        //sort by tree order. add all cases to empty suite.
        for(HashMap.Entry<GateTreeNode, LinkedList<GateTreeNode>> testSuite: testSuites.entrySet()){
            Enumeration<TreeNode> childrenNodes = testSuite.getKey().children();
            if(testSuite.getValue().size() == 0){
                Collections.list(childrenNodes).forEach(childrenNode -> {
                    GateTreeNode gateTreeNode = (GateTreeNode) childrenNode;
                    if(gateTreeNode.includeElement(TestCase.class)){
                        testSuite.getValue().add(gateTreeNode);
                    }
                });

            }else{
                LinkedList<GateTreeNode> processedTestCases = new LinkedList<>();
                while(childrenNodes.hasMoreElements()){
                    GateTreeNode childrenNode = (GateTreeNode) childrenNodes.nextElement();
                    if(testSuite.getValue().contains(childrenNode)){
                        processedTestCases.add(childrenNode);
                    }
                }
                testSuite.getValue().clear();
                testSuite.getValue().addAll(processedTestCases);
            }
        }
        return testSuites;
    }
}
