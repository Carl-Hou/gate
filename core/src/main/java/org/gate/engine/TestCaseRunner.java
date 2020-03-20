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

import org.gate.gui.details.results.ResultManager;
import org.gate.gui.details.results.elements.test.ModelContainerResult;
import org.gate.gui.details.results.elements.test.TestCaseResult;
import org.gate.runtime.GateContext;
import org.gate.runtime.GateContextService;
import org.gate.varfuncs.ValueReplacer;
import org.gate.varfuncs.property.StringProperty;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

public class TestCaseRunner extends GateModelRunner {

    private int timeout = -1;
    private int invocationCount = 1;
    private Instant startTime;
    private List<HashMap<String, String>> provideDataList;

    TestCaseRuntime testCaseRuntime;
    TestCaseResult testCaseResult;

    public TestCaseRunner(TestModelRuntime testModelRuntime, GateContext parentContext) {
        super(parentContext);
        testCaseRuntime = (TestCaseRuntime) testModelRuntime;
        // stopRunner always call after runner start. anyway stopRunner called before result init is an invalid status
    }

    @Override
    TestModelRuntime getTestModelRuntime() {
        return testCaseRuntime;
    }

    @Override
    ModelContainerResult getModelContainerResult() {
        return testCaseResult;
    }

    // use parent context
    @Override
    void execute(TestModelRuntime modelRuntime) {
        startTime = Instant.now();
        resetContext();
        context = GateContextService.getContext();
        ValueReplacer vc = new ValueReplacer();
        // init run case config
        try {
            if (!testCaseRuntime.getTimeout().isEmpty()) {
                timeout = vc.replaceValue(new StringProperty("timeout", testCaseRuntime.getTimeout())).getIntValue();
            }

            if (!testCaseRuntime.getInvocationCount().isEmpty()) {
                invocationCount = vc.replaceValue(new StringProperty("invocationCount", testCaseRuntime.getInvocationCount())).getIntValue();
                if(invocationCount < 1){
                    log.warn("invocationCount is not valid:" + invocationCount);
                }
            }

            provideDataList = testCaseRuntime.getDataProviderVariables();

        } catch (Exception e) {
            log.error("Fail to start test case", e);
            testCaseResult = (TestCaseResult) testCaseRuntime.createModelContainerResult();
            testCaseResult.setThrowable(e);
            ResultManager.getIns().createResultNode(testCaseResult.getSuiteName(), testCaseResult);
            return;
        }

        for(int loopIndex=0; loopIndex< invocationCount && !context.isModelShutdown(); loopIndex++){
            ListIterator<HashMap<String, String>> provideDataIterator = provideDataList.listIterator();
            do {
                testCaseResult = (TestCaseResult) testCaseRuntime.createModelContainerResult();
                if (invocationCount > 1) {
                    testCaseResult.setLoopIndex(loopIndex);
                }
                resetContext();
                if(provideDataIterator.hasNext()){
                    testCaseResult.setVariableSetIndex(provideDataIterator.nextIndex());
                    provideDataIterator.next().forEach((name,value) ->{
                        context.getVariables().put(name, value);
                    });
                }
                runCase(testCaseResult);
            }while(provideDataIterator.hasNext() && testCaseResult.isSuccess());
        }

    }

    void runCase(TestCaseResult testCaseResult){
        GateContext context = GateContextService.getContext();
        context.setResultCollector(ResultManager.getIns().createResultCollector(
                ResultManager.getIns().createResultNode(testCaseResult.getSuiteName(), testCaseResult)));
        ModelExecutor modelExecutor = new ModelExecutor(getTestModelRuntime().getTestModel(),testCaseResult);
        modelExecutor.execute();
        testCaseResult.modelShutdown();
        ResultManager.getIns().modelComplete(testCaseResult);
        if(testCaseResult.isFailure()){
            context.modelShutdown();
        }
    }



    @Override
    public boolean isShutdown() {
        if (timeout > 0 && (Duration.between(startTime, Instant.now()).getSeconds() > timeout)) {
            log.warn("time out setting (seconds) is reached");
            testCaseResult.appendMessage("time out setting (seconds) is reached");
            stopRunner();
        }
        // not start yet
        if(context == null){
            return false;
        }
        return context.isModelShutdown();
    }

}
