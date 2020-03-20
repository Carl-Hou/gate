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
package org.gate.gui.details.results.collector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gate.common.util.GateRuntimeExcepiton;
import org.gate.engine.TestModelRuntime;
import org.gate.gui.details.results.ResultManager;
import org.gate.gui.details.results.ResultTree;
import org.gate.gui.details.results.ResultTreeNode;
import org.gate.gui.details.results.elements.ModelResult;
import org.gate.gui.details.results.elements.graph.ElementResult;

import java.util.LinkedList;

public class GuiResultHandler implements ResultHandler {

    Logger log = LogManager.getLogger(this.getClass());
    ResultTreeNode resultNode;
    ResultTreeNode modelResultNode = null;
    LinkedList<ResultTreeNode> modelResultNodes = new LinkedList<>();
    boolean isClosed = false;

    @Override
    public void addResult(ElementResult elementResult){
        if(isClosed){
            return;
        }

        if(ModelResult.class.isInstance(elementResult)){
            // Model result already set when modelStart called so no new ActionReferenceResult add.
            // did some validation when element return a model result
            if(modelResultNode == null){
                throw new GateRuntimeExcepiton("Invalid status: mode result node is not set");
            }else if(!elementResult.equals(modelResultNode.getUserObject())){
                // ActionReferenceResult add by this method should be same as model start.
                throw new GateRuntimeExcepiton("Invalid status: mode result is already set");
            }
            reloadModelNode();
            return;
        }

        ResultManager.getIns().addResult(resultNode, elementResult);
    }

    @Override
    public void starModel(ModelResult modelResult){
        if(isClosed){
            return;
        }
        ResultTreeNode modelResultNode = ResultManager.getIns().addResult(resultNode, modelResult);
        resultNode = modelResultNode;
        modelResultNodes.add(modelResultNode);
    }

    @Override
    public void endModel(){
        if(isClosed){
            return;
        }
        reloadModelNode();
        resultNode = (ResultTreeNode) resultNode.getParent();
        modelResultNode = modelResultNodes.removeLast();
    }

    @Override
    public void close(){
        isClosed = true;
    }

    // Render the model result node by latest result.
    private void reloadModelNode(){
        if(modelResultNodes.isEmpty()){
            throw new GateRuntimeExcepiton("Invalid status: mode result node is not set");
        }else{
            ResultManager.getIns().reload(modelResultNodes.getLast());
        }
    }
}
