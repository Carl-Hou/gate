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
package org.gate.gui.details.results.elements.test;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gate.engine.TestConstraint;
import org.gate.gui.details.results.elements.ModelResult;
import java.time.Instant;

public abstract class ModelContainerResult extends AbstractTestResult implements ModelResult, TestConstraint {

    Instant startTime = null;
    Instant shutdownTime = null;
    String suiteName;
    // testSuiteName is null if test suites.

    public ModelContainerResult(String suiteName, String modelName) {
        super(modelName);
        status = TS_PROCESSING;
        startTime = Instant.now();
        shutdownTime = startTime;
        this.suiteName = suiteName;
    }

    public String getSuiteName(){
        return suiteName;
    }

    public Instant getStartTime(){
        return startTime;
    }

    public Instant getShutdownTime(){
        return shutdownTime;
    }
    public void modelStart(){
        startTime = Instant.now();
        postStart();
    }

    public void modelShutdown(){
        shutdownTime = Instant.now();
        if(status.equals(TS_PROCESSING)){
            if(isSuccess()){
                status = TS_SUCCESS;
            }else{
                status = TS_FAILURE;
            }
        }
        postShutdown();
    }

    abstract public String getResult();

    void postStart(){}
    void postShutdown(){}

}
