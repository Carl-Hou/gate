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
package org.gate.engine;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gate.common.config.GateProps;
import org.gate.gui.details.results.ResultManager;
import org.gate.gui.details.results.ResultTree;
import org.gate.gui.details.results.ResultTreeNode;
import org.gate.gui.details.results.elements.test.ModelContainerResult;
import org.gate.gui.details.results.elements.test.SetupResult;
import org.gate.gui.tree.GateTreeNode;
import org.gate.gui.tree.GateTreeSupport;
import org.gate.gui.tree.test.elements.TestCase;
import org.gate.gui.tree.test.elements.TestSuite;
import org.gate.gui.tree.test.elements.TestTreeElement;
import org.gate.gui.tree.test.elements.config.ConfigElement;
import org.gate.gui.tree.test.elements.fixture.TearDown;
import org.gate.gui.tree.test.elements.fixture.SetUp;
import org.gate.gui.tree.test.elements.fixture.FixtureElement;

import javax.swing.tree.TreeNode;
import java.sql.ResultSetMetaData;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/*
 * This is not thread safe because of it suppose to be called only by TestEngine.
 * */

public class TestPlan implements TestConstraint {

    Logger log = LogManager.getLogger();

    final static List FAILED_STATUSES = Arrays.asList(new String[]{TS_FAILURE, TS_ERROR});

    final static String BEFORE_SUITE = "before suite";
    final static String AFTER_SUITE = "after suite";
    final static String TEST_CASE = "test case";

    LinkedList<ConfigElement> configs = new LinkedList<>();
    LinkedList<FixtureElement> beforeSuites = new LinkedList<>();
    LinkedList<FixtureElement> afterSuites = new LinkedList<>();
    // key is suite name. value is suite.
    HashMap<String, HashMap<String, LinkedList<TestModelRuntime>>> testSuitesRuntime = new HashMap<>();

    HashMap<String, LinkedList<String>> runningTestCases = new HashMap<>();


    public String getTestSuitesName(){
        return GateTreeSupport.getTestSuitesNode().getName();
    }
    public String compile(HashMap<GateTreeNode, LinkedList<GateTreeNode>> selectedTestCases) {
        // add test cases to testSuitesRuntime
        for (Map.Entry<GateTreeNode, LinkedList<GateTreeNode>> entry : selectedTestCases.entrySet()) {
            HashMap<String, LinkedList<TestModelRuntime>> testSuiteRuntime = new HashMap<>();
            testSuiteRuntime.put(BEFORE_SUITE, new LinkedList<>());
            testSuiteRuntime.put(TEST_CASE, new LinkedList<>());
            testSuiteRuntime.put(AFTER_SUITE, new LinkedList<>());
            // add enabled suite & case only
            if( ((TestSuite) entry.getKey().getGateTreeElement()).isEnable()) {
                entry.getValue().forEach(testCaseNode -> {
                    if (((TestCase) testCaseNode.getGateTreeElement()).isEnable()){
                        TestCaseRuntime testCaseRuntime = new TestCaseRuntime(testCaseNode);
                        testSuiteRuntime.get(TEST_CASE).add(testCaseRuntime);
                    }
                });
                testSuitesRuntime.put(entry.getKey().getName(), testSuiteRuntime);
            }
        }
        if (isEmpty()) {
            return "No test case selected";
        }

        // return error message if selected nodes is node valid.
        String result = processTestCases();
        if (!result.isEmpty()) {
            return result;
        }
        prepareTestSuites();

        prepareTestSuitesRuntime(selectedTestCases.keySet().stream().filter(node ->
                testSuitesRuntime.containsKey(node.getName())).collect(Collectors.toList()));
        return "";
    }

    // Currently this is for update TestSuites result on the ResultTree.
    public void setStatus(String result) {
        ResultManager.getIns().updateTestSuitesStatus(result);
    }

    public boolean isEmpty() {
        for (Map.Entry<String, HashMap<String, LinkedList<TestModelRuntime>>> testSuite : testSuitesRuntime.entrySet()) {
            for (LinkedList<TestModelRuntime> testModelRuntimes : testSuite.getValue().values()) {
                if (!testModelRuntimes.isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }
    // fix the bug of is complete
    public boolean isComplete() {
        if (!isEmpty()) {
            return false;
        }
        if (ResultManager.getIns().includeProcessingTestModel()) {
            return false;
        }
        return true;
    }

    public LinkedList<ConfigElement> getConfigsSuites() {
        return configs;
    }

    public boolean hasBeforeSuites(){
        return !beforeSuites.isEmpty();
    }

    public boolean hasAfterSuites(){
        return !afterSuites.isEmpty();
    }

    public FixtureElement popBeforeSuites(){
        //throw exception when try to pop an empty list.
        return beforeSuites.pop();
    }

    public FixtureElement popAfterSuites(){
        //throw exception when try to pop an empty list.
        return afterSuites.pop();
    }

    public void skipAllQueued() {
        ResultTreeNode testSuitesNode = ResultManager.getIns().getSuitesNode();
        while (hasBeforeSuites()){
            FixtureElement beforeSuites = popBeforeSuites();
            ModelContainerResult beforeSuiteResult = new SetupResult(beforeSuites.getName());
            beforeSuiteResult.setStatus(TS_ERROR);
            beforeSuiteResult.appendMessage("Skipped by test engine");
            ResultManager.getIns().addResult(testSuitesNode, beforeSuiteResult);
        }

        for (Map.Entry<String, HashMap<String, LinkedList<TestModelRuntime>>> testSuiteRuntime : testSuitesRuntime.entrySet()) {
            for(LinkedList<TestModelRuntime> testModelContainerRuntimes: testSuiteRuntime.getValue().values()){
                for(TestModelRuntime testModelContainerRuntime: testModelContainerRuntimes){
                    ResultManager.getIns().skipTestModel(testModelContainerRuntime);
                }
                testModelContainerRuntimes.clear();
            }
        }

        while (hasAfterSuites()){
            FixtureElement beforeSuites = popAfterSuites();
            ModelContainerResult afterSuiteResult = new SetupResult(beforeSuites.getName());
            afterSuiteResult.setStatus(TS_ERROR);
            ResultManager.getIns().addResult(testSuitesNode, afterSuiteResult);
        }

    }

    /*
     * pre process to valid dependency and improve the performance.
     * Select all test case which have no dependency and put them in front in orders
     * to make sure all dependent test have result and removed before test case executes
     * */

    String processTestCases() {
        for (Map.Entry<String, HashMap<String, LinkedList<TestModelRuntime>>> testSuiteRuntime : testSuitesRuntime.entrySet()) {
            LinkedList<TestModelRuntime> testSuiteCases = testSuiteRuntime.getValue().get(TEST_CASE);
            for (TestModelRuntime testCase : testSuiteCases) {
                String dependencyCheckResult = checkTestCaseDependency((TestCaseRuntime) testCase);
                if (!dependencyCheckResult.isEmpty()) {
                    return dependencyCheckResult;
                }
            }
            LinkedList<TestModelRuntime> processedNodes = new LinkedList<>();
            Iterator<TestModelRuntime> it = testSuiteCases.iterator();
            while (it.hasNext()) {
                TestCaseRuntime testCaseNode = (TestCaseRuntime) it.next();
                List<String> dependTestCaseNames = testCaseNode.getDependency();
                if (dependTestCaseNames.size() == 0) {
                    processedNodes.add(testCaseNode);
                    it.remove();
                }
            }
            testSuiteCases.addAll(0, processedNodes);
            processedNodes.clear();
        }
        return "";
    }

    String checkTestCaseDependency(TestCaseRuntime testCaseTreeRuntime) {
        List<String> dependedTestCaseNames = testCaseTreeRuntime.getDependency();
        if (dependedTestCaseNames.size() == 0) {
            return "";
        }

        // check depends on selected nodes which are in same suite and before it
        LinkedList<TestModelRuntime> allTestCasesInSameSuite =
                testSuitesRuntime.get(testCaseTreeRuntime.getSuiteName()).get(TEST_CASE);
        int index = allTestCasesInSameSuite.indexOf(testCaseTreeRuntime);
        List<TestModelRuntime> previousTestCases = allTestCasesInSameSuite.subList(0, index);

        for (String dependTestCaseName : dependedTestCaseNames) {
            Optional dependTestCaseOptional = previousTestCases.stream().
                    filter(testCase -> testCase.getModelName().equals(dependTestCaseName)).findFirst();
            if (!dependTestCaseOptional.isPresent()) {
                return "Invalid test case dependency: ".concat(GateProps.LineSeparator)
                        .concat(testCaseTreeRuntime.getModelName()).concat(" depend on ".concat(dependTestCaseName).concat(""));
            }
        }
        return "";
    }

    void prepareTestSuites() {
        Enumeration<TreeNode> childrenOfSuites = GateTreeSupport.getTestSuitesNode().children();
        while (childrenOfSuites.hasMoreElements()) {
            // add enabled node only
            GateTreeNode node = (GateTreeNode) childrenOfSuites.nextElement();
            if (!((TestTreeElement) node.getGateTreeElement()).isEnable()) {
                continue;
            }
            if (node.includeElement(ConfigElement.class)) {
                configs.add((ConfigElement) node.getGateTreeElement());
                continue;
            }
            if (node.includeElement(SetUp.class)) {
                beforeSuites.add((SetUp) node.getGateTreeElement());
                continue;
            }
            if (node.includeElement(TearDown.class)) {
                afterSuites.add((TearDown) node.getGateTreeElement());
                continue;
            }
        }
    }

    // prepare test models of test suite runtime.
    void prepareTestSuitesRuntime(Collection<GateTreeNode> testSuiteNodes) {
        for(GateTreeNode testSuiteNode : testSuiteNodes){
            Enumeration<TreeNode> childrenOfSuite = testSuiteNode.children();
            while (childrenOfSuite.hasMoreElements()) {
                GateTreeNode node = (GateTreeNode) childrenOfSuite.nextElement();
                TestTreeElement testTreeElement = (TestTreeElement) node.getGateTreeElement();
                if (!testTreeElement.isEnable()) {
                    continue;
                } else if (SetUp.class.isInstance(testTreeElement)) {
                    testSuitesRuntime.get(testSuiteNode.getName()).get(BEFORE_SUITE)
                            .add(new SetupRuntime(node));
                } else if (TearDown.class.isInstance(testTreeElement)){
                    testSuitesRuntime.get(testSuiteNode.getName()).get(AFTER_SUITE)
                            .add(new TeardownRuntime(node));
                }
            }
        }
        for(GateTreeNode testSuiteNode : testSuiteNodes){
            Enumeration<TreeNode> childrenOfSuite = testSuiteNode.children();
            while (childrenOfSuite.hasMoreElements()) {
                GateTreeNode node = (GateTreeNode) childrenOfSuite.nextElement();
                TestTreeElement testTreeElement = (TestTreeElement) node.getGateTreeElement();
                if (!testTreeElement.isEnable()) {
                    continue;
                }
                if (ConfigElement.class.isAssignableFrom(testTreeElement.getClass())) {
                    testSuitesRuntime.get(testSuiteNode.getName()).values().forEach(testModelContainerRuntimes -> {
                        testModelContainerRuntimes.forEach(testModelContainerRuntime -> {
                            testModelContainerRuntime.addConfigElement((ConfigElement) testTreeElement);
                        });
                    });
                }
            }
        }

    }



    public TestModelRuntime getExecutableTestModelRuntime() {
        skipTestModelByResults();
        // get first test case in the testSuites in order.
        // iterate all of the test cases. no delete. mark status in the result doc
        for (Map.Entry<String, HashMap<String, LinkedList<TestModelRuntime>>> testSuiteRuntime : testSuitesRuntime.entrySet()) {
            String testSuiteName = testSuiteRuntime.getKey();
            SetupResult lastSetupResult = ResultManager.getIns().findLastTestResult(testSuiteName, SetupResult.class);
            // return before suite by condition.
            if (testSuiteRuntime.getValue().get(BEFORE_SUITE).isEmpty()) {
                if (lastSetupResult != null && !lastSetupResult.getStatus().equals(TS_SUCCESS)) {
                    continue; // check next suite if pre fixture not complete
                }
            } else {
                if (lastSetupResult == null || lastSetupResult.getStatus().equals(TS_SUCCESS)) {
                    TestModelRuntime beforeFixtureRuntime = testSuiteRuntime.getValue().get(BEFORE_SUITE).remove();
                    return beforeFixtureRuntime;
                }

            }
            // return executable test case if before suite is empty
            LinkedList<TestModelRuntime> testCaseRuntimes = testSuiteRuntime.getValue().get(TEST_CASE);
            if (!testCaseRuntimes.isEmpty()) {
                TestCaseRuntime testCaseRuntime = (TestCaseRuntime) testCaseRuntimes.getFirst();
                if (isTestCasesNodeExecutable(testCaseRuntime)) {
                    return testCaseRuntimes.remove();
                }
            }
            if (!testSuiteRuntime.getValue().get(AFTER_SUITE).isEmpty()) {
                TestModelRuntime afterFixtureRuntime = testSuiteRuntime.getValue().get(AFTER_SUITE).remove();
                return afterFixtureRuntime;
            }
        }
        return null;
    }

    boolean isTestCasesNodeExecutable(TestCaseRuntime testCase) {
        List<String> dependentTestCaseNames = testCase.getDependency();
        for (String dependentTestCaseName : dependentTestCaseNames) {

            String testCaseStatus = ResultManager.getIns().getTestCaseStatus(testCase.getSuiteName(), dependentTestCaseName);
            if (testCaseStatus == null) {
                throw new GateEngineError("Invalid status:".concat(GateProps.LineSeparator)
                        .concat("Dependent test cases status not found: ".concat(testCase.getSuiteName())
                                .concat(".").concat(testCase.getModelName())));
            }
            // one of depend case not success or running
            if (isTestCaseRunning(testCase) && !testCaseStatus.equals(TS_SUCCESS)) {
                return false;
            }
        }
        // all depend test cases found and success
        return true;
    }
    // todo complete this two.
    public void setTestCaseRunning(TestModelRuntime testCase){
        if(runningTestCases.containsKey(testCase.getSuiteName())){
            runningTestCases.get(testCase.getSuiteName()).add(testCase.getModelName());
        }else{
            LinkedList<String> testCases = new LinkedList<>();
            testCases.add(testCase.getModelName());
            runningTestCases.put(testCase.getSuiteName(), testCases);
        }
    }

    public void setTestCaseComplete(TestModelRuntime testCase){
        // should have the suite and test case. let the null point throw once anything unexpected occur
        runningTestCases.get(testCase.getSuiteName()).remove(testCase.getModelName());
    }

    boolean isTestCaseRunning(TestModelRuntime testCase){
        if(runningTestCases.containsKey(testCase.getSuiteName()) &&
                runningTestCases.get(testCase.getSuiteName()).contains(testCase.getModelName())){
            return false;
        }else{
            return true;
        }
    }

    /*
     * Any test case got from the testSuites.
     * Process test in testSuites by test which already run. skip and remove test depend on a failed or skipped test.
     * */
    void skipTestModelByResults() {
        for (Map.Entry<String, HashMap<String, LinkedList<TestModelRuntime>>> testSuiteRuntime : testSuitesRuntime.entrySet()) {
            SetupResult lastSetupResult = ResultManager.getIns()
                    .findLastTestResult(testSuiteRuntime.getKey(), SetupResult.class);
            // skip all if any before suite fixture fail.

            if (lastSetupResult != null && lastSetupResult.isFailure()) {
                testSuiteRuntime.getValue().values().forEach(testModelRuntimes -> {
                    testModelRuntimes.forEach(testModelRuntime -> {
                        ResultManager.getIns().skipTestModel(testModelRuntime);
                    });
                });
            }

            //skip test case
            if (!testSuiteRuntime.getValue().get(BEFORE_SUITE).isEmpty()) {
                SetupResult lastTestResult = ResultManager.getIns()
                        .findLastTestResult(testSuiteRuntime.getKey(), SetupResult.class);
                if (lastTestResult != null && lastTestResult.isFailure()) {
                    testSuiteRuntime.getValue().get(BEFORE_SUITE).forEach(testModelRuntime -> {
                        ResultManager.getIns().skipTestModel(testModelRuntime);
                    });
                    testSuiteRuntime.getValue().get(BEFORE_SUITE).clear();
                }
            }
            if (!testSuiteRuntime.getValue().get(TEST_CASE).isEmpty()) {
                TestCaseRuntime testCaseRuntime = (TestCaseRuntime) testSuiteRuntime.getValue().get(TEST_CASE).getFirst();
                LinkedList<String> dependTestCaseResults = getDependedTestCaseStatus(testCaseRuntime);
                dependTestCaseResults.retainAll(FAILED_STATUSES);
                if (dependTestCaseResults.size() > 0) {
                    testSuiteRuntime.getValue().get(TEST_CASE).forEach(testModelRuntime -> {
                        ResultManager.getIns().skipTestModel(testModelRuntime);
                    });
                    testSuiteRuntime.getValue().get(TEST_CASE).clear();
                }
            }
        }
        // do nothing for AfterTestSuite
    }

    LinkedList<String> getDependedTestCaseStatus(TestCaseRuntime testCaseRuntime) {
        LinkedList<String> dependedTestCaseStatus = new LinkedList<>();
        testCaseRuntime.getDependency().forEach(testCaseName -> {
            // todo add register test case as locked to test suite in/before test case runner. unlock is after test case completed
            // todo check is test case locked before call this.
            String status = ResultManager.getIns().getTestCaseStatus(testCaseRuntime.getSuiteName(), testCaseName);
            if (status != null) {
                dependedTestCaseStatus.add(status);
            }
        });
        return dependedTestCaseStatus;
    }
}
