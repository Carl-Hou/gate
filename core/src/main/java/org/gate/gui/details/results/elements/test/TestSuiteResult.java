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

package org.gate.gui.details.results.elements.test;

import org.gate.common.config.GateProps;
import org.gate.engine.TestConstraint;

import java.time.Instant;
import java.util.LinkedList;

public class TestSuiteResult extends AbstractTestResult implements TestConstraint {
    Instant testSuiteCreateTime;
    Instant lastModelCompleteTime;
//    Date shutdownTime; what's use for this? implement on required

    int passedTestCaseNumber = 0;
    int failedTestCaseNumber = 0;
    int errorTestCaseNumber = 0;

    int passedFixtureNumber = 0;
    int failedFixtureNumber = 0;
    int errorFixtureNumber = 0;
    // TODO remove this later
    LinkedList<String> lockedTestCaseRegistry = new LinkedList<>();

    public synchronized void updateModelResultSummary(ModelContainerResult modelContainerResult){
        lastModelCompleteTime = Instant.now();
        if (modelContainerResult.getStatus().equals(TS_ERROR)) {
            if(TestCaseResult.class.isInstance(modelContainerResult)){
                setFailure();
                errorTestCaseNumber++;
            }else if(SetupResult.class.isInstance(modelContainerResult)){
                setFailure();
                errorFixtureNumber++;
            }else if (TeardownResult.class.isInstance(modelContainerResult)){
                errorFixtureNumber++;
            }
        } else if(modelContainerResult.isSuccess()){
            if(TestCaseResult.class.isInstance(modelContainerResult)){
                passedTestCaseNumber ++;
            }else{
                passedFixtureNumber ++;
            }
        } else{
            if(TestCaseResult.class.isInstance(modelContainerResult)){
                setFailure();
                failedTestCaseNumber ++;
            }else if(SetupResult.class.isInstance(modelContainerResult)){
                setFailure();
                failedTestCaseNumber ++;
            }else if(TeardownResult.class.isInstance(modelContainerResult)){
                failedFixtureNumber ++;
            }
        }

    }

    public int getPassedTestCaseNumber() {
        return passedTestCaseNumber;
    }

    public int getFailedTestCaseNumber() {
        return failedTestCaseNumber;
    }

    public int getErrorTestCaseNumber() {
        return errorTestCaseNumber;
    }

    public int getPassedFixtureNumber() {
        return passedFixtureNumber;
    }

    public int getFailedFixtureNumber() {
        return failedFixtureNumber;
    }

    public int getErrorFixtureNumber() {
        return errorFixtureNumber;
    }

    public Instant getTestSuiteCreateTime(){
        return testSuiteCreateTime;
    }

    public Instant getLastModelCompleteTime(){
        return lastModelCompleteTime;
    }

    public TestSuiteResult(String name) {
        super(name);
        testSuiteCreateTime = Instant.now();
        lastModelCompleteTime = testSuiteCreateTime;
    }

    @Override
    public String getResult(){
        StringBuffer sb = new StringBuffer();
        sb.append("Test suite create: ").append(testSuiteCreateTime).append(GateProps.LineSeparator);
        sb.append("Last model update: ").append(testSuiteCreateTime).append(GateProps.LineSeparator);
        sb.append("Result: ").append(isSuccess()).append(GateProps.LineSeparator);
        sb.append("Test Case Passed: ").append(getPassedTestCaseNumber()).append(" Failed: ").append(getFailedTestCaseNumber())
                .append("Test Case Error: ").append(getErrorTestCaseNumber()).append(GateProps.LineSeparator);
        sb.append("Test Fixture Passed: ").append(getPassedFixtureNumber()).append(" Failed: ").append(getFailedFixtureNumber())
                .append(" Test Fixture error: ").append(getErrorFixtureNumber()).append(GateProps.LineSeparator);
        sb.append("Message: ").append(getMessage());
        return sb.toString();
    }


}
