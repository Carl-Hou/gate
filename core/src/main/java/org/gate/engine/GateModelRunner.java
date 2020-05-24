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
import org.gate.gui.details.results.elements.test.ModelContainerResult;
import org.gate.gui.graph.elements.ElementContext;
import org.gate.gui.tree.test.elements.config.ConfigElement;
import org.gate.runtime.GateContext;
import org.gate.runtime.GateContextService;

import java.util.LinkedList;


public abstract class GateModelRunner implements Runnable, TestConstraint {

    protected Logger log = LogManager.getLogger(this);

    GateContext parentContext;
    // keep context for public method which need to use context of this thread.
    // this will be init on first line of run method. not suppose to have null point.
    GateContext context;

    public GateModelRunner(GateContext parentContext) {
        this.parentContext = parentContext;
    }

    // stop runner externally. set test case result to fail and status to skip when call this.
    public void stopRunner() {
        getModelContainerResult().setStatus(TS_ERROR);
        getModelContainerResult().setFailure("stop externally");
        context.modelShutdown();
        getModelContainerResult().modelShutdown();
    }

    public boolean isShutdown() {
        return context.isModelShutdown();
    }

    abstract TestModelRuntime getTestModelRuntime();

    abstract ModelContainerResult getModelContainerResult();

    abstract void execute(TestModelRuntime modelRuntime);

    /*
    *   bellowing code won't have Throwable. Try catch is not need for it.
    * */
    @Override
    public void run() {
        // this should not throw exception.
        context = GateContextService.getContext();
        try {
            execute(getTestModelRuntime());
        }catch(Throwable t){
            log.fatal("Fatal error:", t);
            getModelContainerResult().setThrowable(t);
        }finally {
            context.modelShutdown();
            closeExternalResource();
        }

    }

    void resetContext(){
        context.clear();
        context.copy(parentContext);
        LinkedList<ConfigElement> configElements = getTestModelRuntime().getConfigElements();
        configElements.forEach(configElement -> {
            configElement.updateContext(context);
        });
    }

    // close mode context
    void closeExternalResource(){
        try {
            context.getGraphElementContext().values().forEach(v -> {
                if (ElementContext.class.isInstance(v)) {
                    ElementContext er = (ElementContext) v;
                    er.close();
                }
            });
        }catch (Throwable t){
            log.fatal("Fail to close external resource", t);
        }
        // TODO release context here?
    }

}
