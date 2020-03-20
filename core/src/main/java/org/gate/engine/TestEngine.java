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
import org.gate.gui.details.results.collector.ResultCollector;
import org.gate.gui.details.results.elements.test.ModelContainerResult;
import org.gate.gui.details.results.elements.test.TeardownResult;
import org.gate.gui.details.results.elements.test.SetupResult;
import org.gate.gui.tree.GateTreeNode;
import org.gate.gui.tree.test.elements.fixture.FixtureElement;
import org.gate.runtime.GateContext;
import org.gate.runtime.GateContextService;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

public class TestEngine implements Runnable{

    Logger log = LogManager.getLogger(this.getClass());

    // Engine Parameters
	int runnerNumber = GateProps.getProperty("gate.engine.test.runner.number",3); //3;
	int timeOut = GateProps.getProperty("gate.engine.test.timeout", 300); //5*60 in seconds;

	private GateContext context;
    // don't support async stop but keep this.
	private volatile static TestEngine engine;

	TestPlan testPlan = null;
    ModelExecutor fixtureExecutor = null;

	LinkedList<GateModelRunner> workingRunner = new LinkedList<>();
	LinkedList<TestStopListener> stopListeners = new LinkedList<>();

	public String prepare(HashMap<GateTreeNode, LinkedList<GateTreeNode>> testSuites){
        testPlan = new TestPlan();
        return testPlan.compile(testSuites);
    }

    @Override
	public void run() {
        ResultManager.getIns().reset();
        Instant startTime = Instant.now();
	    context = GateContextService.getContext();
        ResultCollector collector = ResultManager.getIns().createResultCollector(null);
        context.setResultCollector(collector);
        context.setTestSuitesName(testPlan.getTestSuitesName());
        Thread.currentThread().setName(testPlan.getTestSuitesName());
        // process test suites configuration
        testPlan.getConfigsSuites().forEach(configElement -> {
            try {
                configElement.updateContext(context);
            } catch (Throwable e) {
                log.error("Got Invalid Variable on config", e);
                context.modelShutdown();
                // TODO need to do something here. add TestSuiteSResult here ?
            }
        });

        // process test suites before fixtures
        while (isNotTimeout(startTime) &&  !context.isModelShutdown() && testPlan.hasBeforeSuites()) {
            FixtureElement beforeSuites = testPlan.popBeforeSuites();
            SetupResult setupResult = new SetupResult(beforeSuites.getName());
            executeFixture(beforeSuites, collector, setupResult);
        }

        // run test cases
        // current implementation will looks like single thread when test case complete very fast (5 million seconds)
        while (isNotTimeout(startTime) && !testPlan.isComplete() && !context.isModelShutdown()) {
            try {
                Iterator<GateModelRunner> it = workingRunner.iterator();
                while (it.hasNext()) {
                    GateModelRunner gateModelRunner = it.next();
                    if (gateModelRunner.isShutdown()) {
                        testPlan.setTestCaseComplete(gateModelRunner.getTestModelRuntime());
                        it.remove();
                    }
                }

                if (workingRunner.size() < runnerNumber && !context.isModelShutdown()) {
                    LinkedList<TestModelRuntime> modelsToBeExecute = new LinkedList<>();
                    while(modelsToBeExecute.size() < (runnerNumber - workingRunner.size())){
                        TestModelRuntime testModelRuntime = testPlan.getExecutableTestModelRuntime();
                        if(testModelRuntime != null){
                            modelsToBeExecute.add(testModelRuntime);
                        }else {
                            break;
                        }

                    }
                    for(TestModelRuntime testModelRuntime : modelsToBeExecute){
                        testPlan.setTestCaseRunning(testModelRuntime);
                        workingRunner.add(execute(testModelRuntime));
                    }

                }
                pause(10);
            } catch (Throwable t) {
                log.fatal("Engine Exception:", t);
                context.modelShutdown();
                break;
            }
        }
        // wait for running test case complete hence test case mange it's repeating
        while (isNotTimeout(startTime) && !context.isModelShutdown() && workingRunner.size() != 0){
            Iterator<GateModelRunner> it = workingRunner.iterator();
            while (it.hasNext()) {
                try {
                    GateModelRunner gateModelRunner = it.next();
                    if (gateModelRunner.isShutdown()) {
                        it.remove();
                    }
                    pause(100);
                }catch (Throwable t){
                    log.fatal("Engine Exception:", t);
                    context.modelShutdown();
                    break;
                }
            }
        }

        // process tear down fixtures
        while (isNotTimeout(startTime) && !context.isModelShutdown() && testPlan.hasAfterSuites()) {
            FixtureElement afterSuites = testPlan.popAfterSuites();
            TeardownResult afterSuitesResult = new TeardownResult(afterSuites.getName());
            executeFixture(afterSuites, collector, afterSuitesResult);
        }
        stopTest();
    }

    void executeFixture(FixtureElement fixture, ResultCollector collector, ModelContainerResult fixtureResult){
        fixtureExecutor = new ModelExecutor(fixture.getMxModel(), fixtureResult);
        collector.startModel(fixtureResult);
        fixtureExecutor.execute();
        collector.endModel();
        if(fixtureExecutor.getModelResult().isFailure()){
            context.modelShutdown();
            testPlan.setStatus(TestPlan.TS_FAILURE);
            fixtureResult.setStatus(TestPlan.TS_FAILURE);
        }else{
            fixtureResult.setStatus(TestPlan.TS_SUCCESS);
        }
    }

    public void runTest() throws GateEngineError {
        try {
            Thread runningThread = new Thread(this, "TestEngine");
            runningThread.start();
        } catch (Exception err) {
            stopTest();
            throw new GateEngineError(err);
        }
    }

    public synchronized void stopTest(){
        testPlan.skipAllQueued();
        context.modelShutdown();
        workingRunner.forEach(runner -> {
            if(!runner.isShutdown()){
                runner.stopRunner();
            }
        });
        while(workingRunner.size()!= 0){
            pause(100);
            workingRunner.removeIf(runner -> runner.isShutdown());
        }

        ResultManager.getIns().testEnd();
        //TODO anyway update test suite, suites by test case result here. is thi need?
        stopListeners.forEach(stopListener ->{
            stopListener.testStop();
        });
        stopListeners.clear();
    }

    public void addStopTestListener(TestStopListener testStopListener){
	    stopListeners.add(testStopListener);
    }

    boolean isNotTimeout(Instant startTime){
        if(Duration.between(startTime, Instant.now()).getSeconds() < timeOut){
            return true;
        }
	    return false;
    }


    private void pause(long ms){
        try {
            TimeUnit.MILLISECONDS.sleep(ms);
        } catch (InterruptedException e) {
            log.error("Unexpected interruption: ", e);
        }
    }

	private GateModelRunner execute(TestModelRuntime testModelRuntime){
        GateModelRunner gateModelRunner;
	    if(TestCaseRuntime.class.isInstance(testModelRuntime)){
            gateModelRunner = new TestCaseRunner(testModelRuntime, GateContextService.getContext());
        }else{
	        gateModelRunner = new TestModelRunner(testModelRuntime, GateContextService.getContext());
        }
		new Thread(gateModelRunner, testModelRuntime.getID()).start();
		return gateModelRunner;
	}
}
