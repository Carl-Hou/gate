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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gate.common.config.GateProps;
import org.gate.common.util.GateRuntimeExcepiton;
import org.gate.engine.TestConstraint;
import org.gate.engine.TestModelRuntime;
import org.gate.gui.GuiPackage;
import org.gate.gui.details.results.collector.*;
import org.gate.gui.details.results.elements.DefaultResult;
import org.gate.gui.details.results.elements.Result;
import org.gate.gui.details.results.elements.test.*;
import org.gate.gui.tree.GateTreeSupport;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Optional;

public class ResultManager implements TestConstraint {

    private final static Logger log = LogManager.getLogger();
    private static ResultManager ins = new ResultManager();
    private ArrayList<ResultCollector> collectors = new ArrayList<>();

    private ResultTreeNode root = new ResultTreeNode(new DefaultResult("ResultTreeRoot"));
    private DefaultTreeModel model = new DefaultTreeModel(root);

    private ResultManager() {
    }

    public static ResultManager getIns() {
        return ins;
    }

    public synchronized void reset() {
        stop();
        collectors.clear();
        clear();
    }

    public DefaultTreeModel getModel() {
        return model;
    }

    public synchronized void stop(){
        for (ResultCollector collector : collectors) {
            collector.close();
        }

    }
    // null means root. similar with tree model add.
    public ResultCollector createResultCollector(ResultTreeNode resultTreeNode) {
        if(null == resultTreeNode ){
            resultTreeNode = getSuitesNode();
        }
        ResultCollector resultCollector = new ResultCollector(resultTreeNode);
        collectors.add(resultCollector);
        return resultCollector;
    }

    public void updateTestSuitesStatus(String status) {
        ResultTreeNode testSuitesTreeNode = getSuitesNode();
        TestSuitesResult testSuitesResult = (TestSuitesResult) testSuitesTreeNode.getResult();
        testSuitesResult.setStatus(status);
        if (testSuitesResult.isFailure()) {
            reload(testSuitesTreeNode);
        }
    }

    //this is used when case and suite result exist.
    public void modelComplete(ModelContainerResult modelContainerResult) {
        ResultTreeNode testSuiteNode = getSuiteResultNode(modelContainerResult.getSuiteName());
        TestSuiteResult testSuitesResult = (TestSuiteResult) testSuiteNode.getResult();
        testSuitesResult.updateModelResultSummary(modelContainerResult);
        if (testSuitesResult.isFailure()) {
            reload(testSuiteNode);
        }
    }

    public synchronized void testEnd(){
        ResultTreeNode testSuitesNode = getSuitesNode();
        TestSuitesResult testSuitesResult = (TestSuitesResult) testSuitesNode.getResult();
        LinkedList<ResultTreeNode> testSuiteNodes = testSuitesNode.findChildren(TestSuiteResult.class);
        for(ResultTreeNode testSuiteNode : testSuiteNodes){
            testSuitesResult.addTestSuiteSummary((TestSuiteResult) testSuiteNode.getResult());
        }
//        testSuiteNodes.forEach(testSuiteNode -> {
//            testSuitesResult.addTestSuiteSummary((TestSuiteResult) testSuiteNode.getResult());
//        });
        stop();
        StringBuilder sb = new StringBuilder(GateProps.LineSeparator);
        sb.append("==========================Test Summary==========================").append(GateProps.LineSeparator);
        sb.append(((TestSuitesResult) testSuitesNode.getResult()).getResult());
        sb.append(GateProps.LineSeparator).append("================================================================");
        log.info(sb.toString());
    }

    public ResultTreeNode createResultNode(String suiteName, Result result) {
        if (null == suiteName) {
            throw new GateRuntimeExcepiton("suite name is null");
        }
        ResultTreeNode parentNode = getSuiteResultNode(suiteName);
        return addResult(parentNode, result);
    }

    public ResultTreeNode getSuitesNode(){
        return (ResultTreeNode) root.getFirstChild();
    }

    // this does not guarantee repeat & parameterized execute completed.
    public String getTestCaseStatus(String suiteName, String caseName){
        LinkedList<ResultTreeNode> resultTreeNodes
                = getTestCaseResultNodes(suiteName, caseName);
        //dependent test case is not executed yet. no results.
        if(resultTreeNodes.isEmpty()){
            return TS_PROCESSING;
        }
        Optional<ResultTreeNode> resultTreeNodeOptional =
                resultTreeNodes.stream().filter(node -> !((TestCaseResult) node.getResult()).getStatus().equals(TS_SUCCESS)).findAny();
        if(resultTreeNodeOptional.isPresent()){
            return ((TestCaseResult) resultTreeNodeOptional.get().getResult()).getStatus();
        }else{
            return TS_SUCCESS;
        }
    }

    // this is used to skip test model which does not started. this is not thread safe.
    public void skipTestModel(TestModelRuntime testModelRuntime) {
        ModelContainerResult modelContainerResult;
        ResultTreeNode resultTreeNode = getResultNode(testModelRuntime.getSuiteName(), testModelRuntime.getModelName());
        if(resultTreeNode != null){
            modelContainerResult = (ModelContainerResult) resultTreeNode.getResult();
        }else{
            modelContainerResult = testModelRuntime.createModelContainerResult();
            createResultNode(testModelRuntime.getSuiteName(), modelContainerResult);
        }
        modelContainerResult.setStatus(TS_ERROR);
        modelContainerResult.appendMessage("test skipped");
        ResultTreeNode suiteResultNode = getSuiteResultNode(testModelRuntime.getSuiteName());
        TestSuiteResult testSuiteResult = (TestSuiteResult) suiteResultNode.getResult();
        testSuiteResult.updateModelResultSummary(modelContainerResult);
    }

    public boolean includeProcessingTestModel() {
        LinkedList<ResultTreeNode> testSuiteResultNodes = findChildren(getSuitesNode(), TestSuiteResult.class);
        for (ResultTreeNode testSuiteResultNode : testSuiteResultNodes) {
            LinkedList<ResultTreeNode> modelContainerResultNodes = findChildren(testSuiteResultNode, ModelContainerResult.class);
            for (ResultTreeNode modelContainerResultNode : modelContainerResultNodes) {
                ModelContainerResult modelContainerResult = (ModelContainerResult) modelContainerResultNode.getUserObject();
                if (modelContainerResult.getStatus().equals(TS_PROCESSING)) {
                    return true;
                }
            }
        }
        return false;
    }

    public <T> T findLastTestResult(String suiteName, Class<T> clazz) {
        ResultTreeNode suiteResultNode = getSuiteResultNode(suiteName);
        if (suiteResultNode != null) {
            LinkedList<ResultTreeNode> nodes = findChildren(suiteResultNode, clazz);
            if (nodes.size() > 0) {
                return (T) nodes.getLast().getResult();
            }
        }
        return null;
    }

    public LinkedList<ResultTreeNode> findChildren(ResultTreeNode parent, Class userObjectClass) {
        Enumeration<TreeNode> enumNodes = parent.children();
        LinkedList<ResultTreeNode> nodes = new LinkedList<>();
        while (enumNodes.hasMoreElements()) {
            ResultTreeNode childTreeNode = (ResultTreeNode) enumNodes.nextElement();
            if (userObjectClass.isAssignableFrom(childTreeNode.getUserObject().getClass())) {
                nodes.add(childTreeNode);
            }
        }
        return nodes;
    }

    public void reload(ResultTreeNode resultTreeNode){
        if(GateProps.isGuiMode()){
            synchronized (model){
                model.reload(resultTreeNode);
            }
        }
    }

    public ResultTreeNode addResult(ResultTreeNode parent, Result result){
        ResultTreeNode child = new ResultTreeNode(result);
        synchronized (model){
            model.insertNodeInto(child, parent, parent.getChildCount());
        }
        return child;
    }

    /* from result tree */
    public synchronized void clear() {
        root.removeAllChildren();
        model.reload();
        ResultTreeNode suitesNode = addResult(root, new TestSuitesResult(GateTreeSupport.getTestSuitesNode().getName()));
        if(GateProps.isGuiMode()){
            GuiPackage.getIns().getResultsPane().getResultTree().scrollPathToVisible(new TreePath(suitesNode.getPath()));
        }
    }

    public ResultTreeNode getSuiteResultNode(String testSuiteName){
        ResultTreeNode testSuiteNode = getSuitesNode().findChild(testSuiteName);
        if(testSuiteNode == null){
            testSuiteNode = addResult(getSuitesNode(), new TestSuiteResult(testSuiteName));
        }
        return testSuiteNode;
    }
    public ResultTreeNode getResultNode(String testSuiteName, String nodeName){
        return getSuiteResultNode(testSuiteName).findChild(nodeName);
    }

    public LinkedList<ResultTreeNode> getTestCaseResultNodes(String testSuiteName, String testCaseName){
        return getSuiteResultNode(testSuiteName).findChildren(TestCaseResult.class, testCaseName);
    }
}
