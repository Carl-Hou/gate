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
package org.gate.common.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gate.common.config.GateProps;
import org.gate.engine.TestPlan;
import org.gate.gui.details.results.ResultManager;
import org.gate.gui.details.results.ResultTreeNode;
import org.gate.gui.details.results.elements.test.TestCaseResult;
import org.gate.gui.details.results.elements.test.TestSuiteResult;
import org.gate.gui.details.results.elements.test.TestSuitesResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.swing.tree.TreeNode;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.time.Duration;
import java.util.Enumeration;

// Load result from ResultTree and generate a Junit format report.
public class JUnitReportGenerator {

    private static Logger log = LogManager.getLogger();

    private final static String CLASSNAME_PREFIX = GateProps.getProperty("gate.report.junit.classname.prefix", "org.gate");

    private final static String TAG_TEST_SUITES = "testsuites";
    private final static String TAG_TEST_SUITE = "testsuite";
    private final static String TAG_TEST_CASE = "testcase";
    private final static String TAG_ERROR = "error";
    private final static String TAG_FAILURE = "failure";

    private final static String AN_NAME = "name";
    private final static String AN_CLASSNAME = "classname";
    private final static String AN_MESSAGE = "message";
    private final static String AN_FAILURES = "failures";
    private final static String AN_ERRORS = "errors";
    private final static String AN_TESTS = "tests";
    private final static String AN_TIME = "time";
    private final static String AN_TYPE = "type";

    Document doc;

    public JUnitReportGenerator() {
        try {
            doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException e) {
            log.fatal("Error on create new dom object", e);
            throw new GateRuntimeExcepiton(e);
        }

    }

    public Document generate(File reportFile) throws GateException {
        Element testSuitesElement = appendTestSuitesElement();

        Enumeration<TreeNode> testSuitesChildren = ResultManager.getIns().getSuitesNode().children();

        while (testSuitesChildren.hasMoreElements()) {
            ResultTreeNode testSuitesChild = (ResultTreeNode) testSuitesChildren.nextElement();
            if (TestSuiteResult.class.isInstance(testSuitesChild.getResult())) {
                Element testSuiteElement = appendTestSuite(testSuitesElement, (TestSuiteResult) testSuitesChild.getResult());
                Enumeration<TreeNode> testSuiteChildren = testSuitesChild.children();
                while (testSuiteChildren.hasMoreElements()){
                    ResultTreeNode testSuiteChild = (ResultTreeNode) testSuiteChildren.nextElement();
                    if(TestCaseResult.class.isInstance(testSuiteChild.getResult())){
                        appendTestCase(testSuiteElement, (TestCaseResult) testSuiteChild.getResult());
                    }
                }
            }
        }
        GateXMLUtils.toFile(doc, reportFile);
        return doc;
    }


    public Element appendTestSuitesElement() {
        TestSuitesResult testSuitesResult = (TestSuitesResult) ResultManager.getIns().getSuitesNode().getResult();
        Element testSuitesElement = doc.createElement(TAG_TEST_SUITES);
        int failed = testSuitesResult.getFailedTestCaseNumber();
        int passed = testSuitesResult.getPassedTestCaseNumber();
        int error = testSuitesResult.getErrorTestCaseNumber();
        testSuitesElement.setAttribute(AN_NAME, testSuitesResult.getName());
        testSuitesElement.setAttribute(AN_FAILURES, String.valueOf(failed));
        testSuitesElement.setAttribute(AN_ERRORS, String.valueOf(error));
        testSuitesElement.setAttribute(AN_TESTS, String.valueOf(failed + error + passed));
        float time = Duration.between(testSuitesResult.getStartTime(), testSuitesResult.getEndTime()).toMillis() / 1000f;
        testSuitesElement.setAttribute(AN_TIME, String.valueOf(time));
        doc.appendChild(testSuitesElement);
        return testSuitesElement;
    }

    public Element appendTestSuite(Element testSuitesElement, TestSuiteResult testSuiteResult) {
        Element testSuiteElement = doc.createElement(TAG_TEST_SUITE);
        int failed = testSuiteResult.getFailedTestCaseNumber();
        int passed = testSuiteResult.getPassedTestCaseNumber();
        int error = testSuiteResult.getErrorTestCaseNumber();
        testSuiteElement.setAttribute(AN_NAME, testSuiteResult.getName());
        testSuiteElement.setAttribute(AN_FAILURES, testSuiteResult.getName());
        testSuiteElement.setAttribute(AN_FAILURES, String.valueOf(failed));
        testSuiteElement.setAttribute(AN_ERRORS, String.valueOf(error));
        testSuiteElement.setAttribute(AN_TESTS, String.valueOf(failed + error + passed));
        float time = Duration.between(testSuiteResult.getTestSuiteCreateTime(), testSuiteResult.getLastModelCompleteTime()).toMillis() / 1000f;
        testSuiteElement.setAttribute(AN_TIME, String.valueOf(time));
        testSuitesElement.appendChild(testSuiteElement);
        return testSuiteElement;
    }

    public Element appendTestCase(Element testSuiteElement, TestCaseResult testCaseResult) {
        Element testCaseElement = doc.createElement(TAG_TEST_CASE);
        testCaseElement.setAttribute(AN_NAME, testCaseResult.getName());
        String[] className = new String[]{CLASSNAME_PREFIX, doc.getDocumentElement().getAttribute(AN_NAME),
                testSuiteElement.getAttribute(AN_NAME) , testCaseResult.getName()};
        testCaseElement.setAttribute(AN_CLASSNAME, String.join(".", className));

        float time = Duration.between(testCaseResult.getStartTime(), testCaseResult.getShutdownTime()).toMillis() / 1000f;
        testCaseElement.setAttribute(AN_TIME, String.valueOf(time));
        // do nothing if success
        Element noneSussceeElement = null;
        if (testCaseResult.getStatus().equals(TestPlan.TS_FAILURE)) {
            noneSussceeElement = doc.createElement(TAG_FAILURE);
        } else if (testCaseResult.getStatus().equals(TestPlan.TS_ERROR)) {
            noneSussceeElement = doc.createElement(TAG_ERROR);
        }

        if (noneSussceeElement != null) {
            if (testCaseResult.getThrowable() != null) {
                noneSussceeElement.setAttribute(AN_TYPE, testCaseResult.getThrowable().getClass().getName());
            } else {
                noneSussceeElement.setAttribute(AN_TYPE, "Gate.Assert");
            }

            noneSussceeElement.setAttribute(AN_MESSAGE, testCaseResult.getMessage());

            noneSussceeElement.setTextContent(testCaseResult.getResult());
            testCaseElement.appendChild(noneSussceeElement);
        }
        testSuiteElement.appendChild(testCaseElement);
        return testCaseElement;
    }


}
