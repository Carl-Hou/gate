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
import org.gate.gui.details.results.elements.ModelResult;
import org.gate.gui.details.results.elements.Result;
import org.gate.gui.details.results.elements.graph.*;
import org.gate.gui.details.results.elements.graph.ActionReferenceResult;

/*
* This is for development use only.print the result and props of each vertex
* This is will log level info to print the log
* */

public class ConsoleResultHandler implements ResultHandler {

    final static String lineSeparator = GateProps.LineSeparator;

    Logger log = LogManager.getLogger(this.getClass());

    public String getResult(ElementResult currentResult) {
        if (SamplerResult.class.isInstance(currentResult)) {
            return getResult((SamplerResult) currentResult);
        }else if (ControllerResult.class.isInstance(currentResult)) {
            return getResult((ControllerResult) currentResult);
        }else if (ConfigureResult.class.isInstance(currentResult)) {
            return getResult((ConfigureResult) currentResult);
        }else if (AssertionResult.class.isInstance(currentResult)) {
            return getResult((AssertionResult) currentResult);
        }
        return "Element type is not supported";
    }

    @Override
    public void addResult(ElementResult result) {
        log.error("this is not implement yet");
    }

    @Override
    public void starModel(ModelResult modelResult) {
        log.info("-------------Start model: " + modelResult.getName());
    }

    @Override
    public void endModel() {
        log.info("-------------End model: ");
    }

    @Override
    public void close() {
        // do nothing for console handler
    }

    String getResultAsString(Result result){
        StringBuffer sb = new StringBuffer(">>>>>>>>>>>>>>>Result : ");
        if(ElementResult.class.isInstance(result)){
            ElementResult elementResult = (ElementResult)result;
            sb.append(lineSeparator).append("Result: ").append(lineSeparator).append(elementResult.getResult());
            sb.append(lineSeparator).append("Request:").append(lineSeparator).append(elementResult.getRequestMessage());
            sb.append(lineSeparator).append("Response:").append(lineSeparator).append(elementResult.getResponseAsString());
        }else{
            sb.append("---Element Result : ").append(lineSeparator).append(((ElementResult)result).getResult());
            if(result.isSuccess()){
                sb.append("Success");
            }else{
                sb.append("Failure");
            }
            sb.append(lineSeparator).append("---Message: ").append(result.getMessage()).append(lineSeparator);
        }
        sb.trimToSize();
        return  sb.toString();
    }

    public String getResult(SamplerResult result) {
        StringBuffer sb = new StringBuffer();
        sb.append(lineSeparator).append("--->>>...............SamplerResult: ").append(result.getName()).append(lineSeparator);
        sb.append(getResultAsString(result)).append(lineSeparator);
        sb.append("---Response Data As String: ").append(lineSeparator);
        sb.append(result.getResult()).append(lineSeparator);
        sb.append("...............SamplerResult: ").append(result.getName()).append(lineSeparator);
        return sb.toString();
    }


    public String getResult(ControllerResult result) {
        StringBuffer sb = new StringBuffer();
        sb.append(lineSeparator).append("--->>>...............ControllerResult: ").append(result.getName()).append(lineSeparator);
        sb.append(getResultAsString(result)).append(lineSeparator);
        sb.append("<<<---...............ControllerResult: ").append(result.getName()).append(lineSeparator);
        return sb.toString();
    }


    public String getResult(ConfigureResult result) {
        StringBuffer sb = new StringBuffer();
        sb.append(lineSeparator).append("--->>>.......... .....ConfigureResult ElementResult: ").append(result.getName()).append(lineSeparator);
        sb.append(getResultAsString(result)).append(lineSeparator);
        sb.append("<<<---...............ConfigureResult ElementResult: ").append(result.getName()).append(lineSeparator);
        return sb.toString();
    }

    public String getResult(AssertionResult result) {
        StringBuffer sb = new StringBuffer();
        sb.append(lineSeparator).append("--->>>...............AssertionResult: ").append(result.getName()).append(lineSeparator);
        sb.append(getResultAsString(result)).append(lineSeparator);
        sb.append("<<<---...............AssertionResult: ").append(result.getName()).append(lineSeparator);
        return sb.toString();
    }





}
