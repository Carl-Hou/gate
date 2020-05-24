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

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGraphModel;
import com.mxgraph.model.mxICell;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gate.common.config.GateProps;
import org.gate.common.util.GateUtils;
import org.gate.gui.details.results.collector.ResultCollector;
import org.gate.gui.details.results.elements.graph.ActionReferenceResult;
import org.gate.gui.details.results.elements.graph.AssertionResult;
import org.gate.gui.details.results.elements.graph.ElementResult;
import org.gate.gui.details.results.elements.graph.SamplerResult;
import org.gate.gui.details.results.elements.ModelResult;
import org.gate.gui.graph.elements.GraphElement;
import org.gate.gui.graph.elements.control.Start;
import org.gate.runtime.GateContext;
import org.gate.runtime.GateContextService;

import java.util.Collection;
import java.util.Optional;

public class ModelExecutor {

    protected Logger log = LogManager.getLogger(this.getClass());
    private volatile boolean hasNextLoop = true;

    protected mxGraphModel mxModel = null;
    protected ModelResult modelResult = null;

    public ModelExecutor(mxGraphModel mxModel, ModelResult modelResult) {
        this.mxModel = mxModel;
        this.modelResult = modelResult;
    }

    public ModelResult getModelResult(){
        return modelResult;
    }

    public void execute() {
        GateContext context = GateContextService.getContext();

        try {
            Optional<mxCell> entryOptional = getModelEntry(GateUtils.deepCopy(mxModel).get());
            if (!entryOptional.isPresent()) {
                modelResult.setFailure("Entry point not found. Check if Start element exist in the model");
                return;
            }
            executeBranch(context, entryOptional.get());

        } catch (Throwable t) {
            log.error("Error on execute model ".concat(modelResult.getName()), t);
            modelResult.setFailure("Exception when execute model:".concat(GateUtils.getStackTrace(t)));
        }
    }

    /*
     * First entry should be start node.
     * Whether to start next element is decide after current element is executed.
     * Failed on any error, exception or assert failure
     * shutdown model from model context.
     * */
    void executeBranch(GateContext context, mxICell entry) {
        mxICell nextVertex = entry;
        ResultCollector resultCollector = context.getResultCollector();
        while (hasNextLoop && modelResult.isSuccess() && !context.isModelShutdown()) {
//            prepare for current vertex and execute
            mxICell currentVertex = nextVertex;
            GraphElement graphElement = (GraphElement) currentVertex.getValue();
            ElementResult currentResult = graphElement.execute();
            graphElement.reset();
            // ActionReference will handel result collect itself to avoid duplicate mode node.
            if(!ActionReferenceResult.class.isInstance(currentResult)){
                resultCollector.collect(currentResult);
            }

            if (SamplerResult.class.isInstance(currentResult)) {
                SamplerResult samplerResult = (SamplerResult) currentResult;
                context.setPreviousResult(samplerResult);
                if(samplerResult.getThrowable() != null){
                    modelResult.setThrowable(samplerResult.getThrowable());
                    return;
                    // Fail on any Exception on Sampler Execution. Change it to RuntimeException like bellowing if needed.
                }

            } else if (AssertionResult.class.isInstance(currentResult) ||
                    ModelResult.class.isInstance(currentResult)){
                if (currentResult.isFailure()) {
                    modelResult.setFailure("Refer latest element result for details");
                    return;
                }
            }
            nextVertex = getNextVertex(currentVertex, currentResult.isSuccess());
            if (nextVertex == null) {
                break;
            }
        }
    }

    protected mxICell getNextVertex(mxICell vertex, boolean condition) {
        mxICell nextCell = null;
        if (condition) {
            nextCell = getTargetCell(vertex, GateProps.Success);
        } else {
            nextCell = getTargetCell(vertex, GateProps.Fail);
        }

        if (null == nextCell) {
            nextCell = getTargetCell(vertex, GateProps.Next);
        }

        return nextCell;
    }

    //	must return null if no target elements
    protected mxICell getTargetCell(mxICell currentCell, String name) {
        int count = currentCell.getEdgeCount();
        mxICell nextCell = null;
        for (int i = 0; i < count; i++) {
            mxICell edge = currentCell.getEdgeAt(i);
            if (edge.getValue().equals(name)) {
                nextCell = edge.getTerminal(false);
                if (nextCell!= null && !nextCell.equals(currentCell)) {
                    return nextCell;
                }
            }
        }
        return null;
    }

    protected Optional<mxCell> getModelEntry(mxGraphModel mxModel) {
        Collection<mxCell> cells = (Collection) mxModel.getCells().values();
        return cells.stream().filter(a -> a.getValue() instanceof Start).findFirst();
    }
}
