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
import org.gate.common.util.FileServer;
import org.gate.gui.details.results.ResultManager;
import org.gate.gui.details.results.collector.ResultCollector;
import org.gate.gui.details.results.elements.test.ModelContainerResult;
import org.gate.gui.details.results.elements.test.TeardownResult;
import org.gate.gui.details.results.elements.test.SetupResult;
import org.gate.gui.tree.GateTreeNode;
import org.gate.gui.tree.test.elements.fixture.FixtureElement;
import org.gate.runtime.GateContext;
import org.gate.runtime.GateContextService;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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

    Map<GateModelRunner, Thread> workingRunner = new ConcurrentHashMap<>();
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
                Iterator<Map.Entry<GateModelRunner, Thread>> it = workingRunner.entrySet().iterator();
                while (it.hasNext()) {
                    GateModelRunner gateModelRunner = it.next().getKey();
                    if (gateModelRunner.isShutdown()) {
                        testPlan.setTestCaseComplete(gateModelRunner.getTestModelRuntime());
                        it.remove();
                    }
                }

                if (workingRunner.size() < runnerNumber && !context.isModelShutdown()) {
                    LinkedList<TestModelRuntime> modelsToBeExecute = new LinkedList<>();
                    while(modelsToBeExecute.size() < (runnerNumber - workingRunner.size())){
                        TestModelRuntime testModelRuntime = testPlan.getExecutableTestModelRuntime();
                        if(testModelRuntime != null ){
                            modelsToBeExecute.add(testModelRuntime);
                            if(SetupRuntime.class.isAssignableFrom(testModelRuntime.getClass())){
                                break;
                            }
                        }else {
                            break;
                        }

                    }
                    for(TestModelRuntime testModelRuntime : modelsToBeExecute){
                        testPlan.setTestCaseRunning(testModelRuntime);
                        GateModelRunner gateModelRunner = getModelRunner(testModelRuntime);
                        Thread thread = new Thread(gateModelRunner, testModelRuntime.getID());
                        workingRunner.put(gateModelRunner, thread);
                        thread.start();
                    }

                }
                pause(100);
            } catch (Throwable t) {
                log.fatal("Engine Exception:", t);
                context.modelShutdown();
                break;
            }
        }
        // wait for running test case complete hence test case mange it's repeating
        while (isNotTimeout(startTime) && !context.isModelShutdown() && workingRunner.size() != 0){
            Iterator<Map.Entry<GateModelRunner,Thread>> it = workingRunner.entrySet().iterator();
            while (it.hasNext()) {
                try {
                    GateModelRunner gateModelRunner = it.next().getKey();
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
        try {
            FileServer.getFileServer().closeFiles();
        }catch (IOException e){
            log.error("Problem closing files at end of test", e);
        }
        ResultManager.getIns().testEnd();
        stopListeners.forEach(stopListener ->{
            stopListener.testStop();
        });
        stopListeners.clear();
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

        for(Map.Entry<GateModelRunner, Thread> runner : workingRunner.entrySet()){
            if(!runner.getKey().isShutdown()){
                runner.getKey().stopRunner();
            }
            if(runner.getValue().getState() == Thread.State.TIMED_WAITING || runner.getValue().getState() == Thread.State.WAITING){
                try {
                    runner.getValue().interrupt();
                }catch(Throwable t){
                    log.warn("Error on interrupt model runner", t);
                }
            }
        }
    }

    public synchronized boolean isAllRunnerStopped(){
        if(workingRunner.size()!= 0){
            Iterator<Map.Entry<GateModelRunner, Thread>> it=workingRunner.entrySet().iterator();
            while(it.hasNext()){
                Map.Entry<GateModelRunner, Thread> runner = it.next();
                if(runner.getKey().isShutdown()){
                    it.remove();
                }
                if(runner.getValue().getState() == Thread.State.TIMED_WAITING || runner.getValue().getState() == Thread.State.WAITING){
                    try {
                        runner.getValue().interrupt();
                    }catch(Throwable t){
                        log.warn("Error on interrupt model runner", t);
                    }
                }
            }
            return false;
        }else{
            return true;
        }
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
            // interrupt by stop in most cases
            log.debug("interruption: ", e);
        }
    }

	private GateModelRunner getModelRunner(TestModelRuntime testModelRuntime){
        GateModelRunner gateModelRunner;
	    if(TestCaseRuntime.class.isInstance(testModelRuntime)){
            gateModelRunner = new TestCaseRunner(testModelRuntime, GateContextService.getContext());
        }else{
	        gateModelRunner = new TestModelRunner(testModelRuntime, GateContextService.getContext());
        }
		return gateModelRunner;
	}
}
