package org.gate.gui.details.results.elements.test;

import org.gate.common.config.GateProps;
import org.gate.gui.details.results.ResultManager;
import org.gate.gui.details.results.ResultTree;

import java.time.Instant;

public class TestSuitesResult extends AbstractTestResult {
    Instant startTime;
    Instant endTime;

    int passedTestCaseNumber = 0;
    int failedTestCaseNumber = 0;
    int errorTestCaseNumber = 0;

    int passedFixtureNumber = 0;
    int failedFixtureNumber = 0;
    int errorFixtureNumber = 0;

    public TestSuitesResult(String suitesName) {
        super(suitesName);
        startTime = Instant.now();
        endTime = startTime;
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

    public Instant getStartTime() {
        return startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public void addTestSuiteSummary(TestSuiteResult testSuiteResult){
        endTime = Instant.now();

        passedFixtureNumber += testSuiteResult.passedFixtureNumber;
        passedTestCaseNumber += testSuiteResult.passedTestCaseNumber;
        failedFixtureNumber += testSuiteResult.failedFixtureNumber;
        failedTestCaseNumber += testSuiteResult.failedTestCaseNumber;
        errorFixtureNumber += testSuiteResult.errorFixtureNumber;
        errorTestCaseNumber += testSuiteResult.errorTestCaseNumber;
        if(testSuiteResult.isFailure()){
            setFailure();
            ResultManager.getIns().reload(ResultManager.getIns().getSuitesNode());
        }
    }

    @Override
    public String getResult(){
        StringBuffer sb = new StringBuffer();
        sb.append("Test Start: ").append(startTime).append(GateProps.LineSeparator);
        sb.append("Test Complete: ").append(endTime).append(GateProps.LineSeparator);
        sb.append("Result: ").append(isSuccess()).append(GateProps.LineSeparator);
        sb.append("Test Case Passed: ").append(getPassedTestCaseNumber()).append(" Failed: ").append(getFailedTestCaseNumber())
                .append(" Test Case Error: ").append(getErrorTestCaseNumber()).append(GateProps.LineSeparator);
        sb.append("Test Fixture Passed: ").append(getPassedFixtureNumber()).append(" Failed: ").append(getFailedFixtureNumber())
                .append("Test Fixture Error: ").append(getErrorFixtureNumber()).append(GateProps.LineSeparator);
        return sb.toString();
    }
}
