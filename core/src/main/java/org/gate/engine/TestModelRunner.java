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
import org.gate.gui.details.results.elements.test.SetupResult;
import org.gate.gui.details.results.elements.test.TeardownResult;
import org.gate.runtime.GateContext;
import org.gate.runtime.GateContextService;


public class TestModelRunner extends GateModelRunner {

    TestModelRuntime testModelRuntime;
    ModelContainerResult modelContainerResult;

    public TestModelRunner(TestModelRuntime testModelRuntime, GateContext parentContext) {
        super(parentContext);
        this.testModelRuntime = testModelRuntime;
        if(SetupRuntime.class.isInstance(testModelRuntime)){
            modelContainerResult = new SetupResult(testModelRuntime.getSuiteName(), testModelRuntime.getModelName());
        }else if(TeardownRuntime.class.isInstance(testModelRuntime)){
            modelContainerResult = new TeardownResult(testModelRuntime.getSuiteName(), testModelRuntime.getModelName());
        }
    }

    @Override
    TestModelRuntime getTestModelRuntime() {
        return testModelRuntime;
    }

    @Override
    ModelContainerResult getModelContainerResult() {
        return modelContainerResult;
    }

    @Override
    void execute(TestModelRuntime modelRuntime) {
        getModelContainerResult().modelStart();
        GateContext context = GateContextService.getContext();
        resetContext();
        context.setResultCollector(ResultManager.getIns().createResultCollector(
                ResultManager.getIns().createResultNode(modelContainerResult.getSuiteName(), modelContainerResult)));
        ModelExecutor modelExecutor = new ModelExecutor(getTestModelRuntime().getTestModel(), modelContainerResult);
        modelExecutor.execute();
        getModelContainerResult().modelShutdown();
        ResultManager.getIns().modelComplete(getModelContainerResult());
    }
}
