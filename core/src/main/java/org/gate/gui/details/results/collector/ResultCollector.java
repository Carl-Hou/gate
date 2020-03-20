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
import org.gate.common.config.GateProps;
import org.gate.gui.details.results.ResultManager;
import org.gate.gui.details.results.ResultTree;
import org.gate.gui.details.results.ResultTreeNode;
import org.gate.gui.details.results.elements.Result;
import org.gate.gui.details.results.elements.graph.ElementResult;
import org.gate.gui.details.results.elements.ModelResult;

// this will be used in one thread. It is not thread safe.
public class ResultCollector {

    Logger log = LogManager.getLogger();
    final static String logFix = "==========================";
    final static String resultDiv = "--------------------------";
    boolean isClosed = false;
    ResultTreeNode resultNode;

    public ResultCollector(ResultTreeNode resultNode) {
        this.resultNode = resultNode;
    }

    public ResultTreeNode getResultNode(){
        return resultNode;
    }

    public void startModel(ModelResult modelResult) {
        if (isClosed) {
            return;
        }
        resultNode = ResultManager.getIns().addResult(resultNode, modelResult);
    }

    public void endModel() {
        if (isClosed) {
            return;
        }
        // reload test case result
        ResultManager.getIns().reload(resultNode);
        Result modelResult = resultNode.getResult();
        resultNode = (ResultTreeNode) resultNode.getParent();
        if (modelResult.isFailure()) {
            resultNode.getResult().setFailure();
        }
        // reload test suite result
        ResultManager.getIns().reload(resultNode);
    }

    public void close() {
        isClosed = true;
    }

    public void collect(ElementResult elementResult) {
        if (isClosed) {
            return;
        }else if(GateProps.isGuiMode()){
            ResultManager.getIns().addResult(resultNode, elementResult);
            if(GateProps.getProperty("gate.gui.logging.result", false)){
                log.info(getResultText(elementResult));
            }
        }else  {
            log.info(getResultText(elementResult));
        }

    }

    String getResultText(ElementResult elementResult){
        //TODO implement this.
        StringBuilder sb = new StringBuilder(GateProps.LineSeparator);
        sb.append(logFix).append("Element Result ").append(resultNode.getParent().toString()).append(".")
                .append(resultNode.toString()).append(".").append(elementResult.getName())
                .append(logFix).append(GateProps.LineSeparator);
        sb.append("Result:").append(resultDiv).append(GateProps.LineSeparator);
        sb.append(elementResult.getResult());
        sb.append("Request message:").append(resultDiv).append(GateProps.LineSeparator);
        sb.append(elementResult.getRequestMessage());
        sb.append("Response data:").append(resultDiv).append(GateProps.LineSeparator);
        sb.append(elementResult.getResponseAsString());
        sb.append(GateProps.LineSeparator).append(logFix).append(logFix);
        return sb.toString();
    }

}
