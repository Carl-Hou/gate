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


import org.apache.commons.lang3.tuple.ImmutablePair;
import org.gate.engine.TestStopListener;
import org.gate.gui.GuiPackage;
import org.gate.gui.common.OptionPane;
import org.gate.engine.TestEngine;
import org.gate.gui.tree.GateTreeNode;
import org.gate.gui.tree.GateTreeSupport;
import org.gate.gui.tree.test.elements.TestCase;
import org.gate.gui.tree.test.elements.TestSuite;
import org.gate.gui.tree.test.elements.TestSuites;

import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Start extends AbstractGateAction {

    private static final Set<String> commands = new HashSet<>();

    static {
        commands.add(ActionNames.ACTION_START);
        commands.add(ActionNames.ACTION_STOP);
    }

    private ImmutablePair<TestEngine, Thread> enginePair;

    @Override
    public void doAction(ActionEvent e) throws IllegalUserActionException {

        if(e.getActionCommand().equals(ActionNames.ACTION_START)){
            runCase();
        } else if(e.getActionCommand().equals(ActionNames.ACTION_STOP)){
            if(enginePair != null){
                log.info("Stopping test");
                stopTest();
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
        TestEngine engine = new TestEngine();
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
        Thread runningThread = new Thread(engine, "TestEngine");
        enginePair = ImmutablePair.of(engine, runningThread);
        try {
            runningThread.start();
        } catch (Exception err) {
            stopTest();
            OptionPane.showErrorMessageDialog("Fail to start test",err);
        }
        GuiPackage.getIns().getMainFrame().testStarted();
    }

    void stopTest(){
        enginePair.getKey().stopTest();
        for(int i=0; i< 20; i++){
            if(enginePair.getKey().isAllRunnerStopped() && enginePair.getValue().getState() == Thread.State.TERMINATED){
                return;
            }
            if(enginePair.getValue().getState() == Thread.State.WAITING || enginePair.getValue().getState() == Thread.State.TIMED_WAITING){
                try {
                    enginePair.getValue().interrupt();
                }catch (Throwable t){
                    log.fatal(t);
                    OptionPane.showErrorMessageDialog("Fail to stop test", t);
                    return;
                }
            }
            try {
                TimeUnit.MILLISECONDS.sleep(500);
            } catch (InterruptedException e) {
                log.info("Unexpected", e);
            }
        }
        OptionPane.showErrorMessageDialog("Fail to stop test", "Fail to stop test after retry many times");
    }

    HashMap<GateTreeNode, LinkedList<GateTreeNode>> getPreprocessedSelectedTestNodes(){
        HashMap<GateTreeNode, LinkedList<GateTreeNode>> testSuites = new HashMap<>();
        TreePath[] treePaths = GuiPackage.getIns().getTestTree().getSelectionPaths();
        if(treePaths == null){
            OptionPane.showErrorMessageDialog("Error", "No test suites or cases selected");
            return testSuites;
        }

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
