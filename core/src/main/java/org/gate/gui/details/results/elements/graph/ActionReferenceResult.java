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

package org.gate.gui.details.results.elements.graph;

import org.gate.common.config.GateProps;
import org.gate.gui.common.AbstractTestElement;
import org.gate.gui.details.results.elements.ModelResult;
import org.gate.gui.details.results.elements.graph.AbstractElementResult;
import org.gate.gui.details.results.elements.test.AbstractTestResult;

import java.util.Calendar;
import java.util.Date;

public class ActionReferenceResult extends ControllerResult implements ModelResult {

    Date startTime = null;
    Date completeTime = null;

    public ActionReferenceResult(String modelName) {
        super(modelName);
        startTime = Calendar.getInstance().getTime();
        completeTime = startTime;
    }

    public String getMessage(){
        StringBuffer sb = new StringBuffer();
        sb.append("Model Start: ").append(startTime.toInstant()).append(GateProps.LineSeparator);
        sb.append("Model ended: ").append(completeTime.toInstant()).append(GateProps.LineSeparator);
        return sb.toString();
    }

    @Override
    public String getResult() {
        return null;
    }
}
