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
package org.gate.gui.graph.elements.timer;

import org.gate.gui.details.results.elements.graph.ElementResult;
import org.gate.gui.graph.elements.AbstractGraphElement;
import org.gate.gui.graph.elements.timer.gui.ConstantTimerGui;

import java.util.concurrent.TimeUnit;

public abstract class AbstractTimer extends AbstractGraphElement implements Timer {

    public final static String NP_TimeUnit = "Time unit";

    public final static String Milliseconds = "milliseconds";
    public final static String Seconds = "seconds";
    public final static String Minutes = "minutes";

    public AbstractTimer(){
        addProp(NS_DEFAULT, NP_TimeUnit, Seconds);
    }

    abstract int getTimeToWait();

    @Override
    protected void exec(ElementResult controllerResult)  {
        controllerResult.setRunTimeProps(getRunTimePropsMap());

        try {
            switch (getRunTimeProp(NS_DEFAULT, NP_TimeUnit).toLowerCase()) {
                case Seconds:
                    TimeUnit.SECONDS.sleep(getTimeToWait());
                    break;
                case Milliseconds:
                    TimeUnit.MILLISECONDS.sleep(getTimeToWait());
                    break;
                case Minutes:
                    TimeUnit.MINUTES.sleep(getTimeToWait());
                    break;
                default:
                    log.warn(getName() + ": Time Unit is not set correctly. Use milliseconds");
                    TimeUnit.MILLISECONDS.sleep(getTimeToWait());
            }
        }catch (InterruptedException e) {
            log.trace("Interrupted:", e);
            controllerResult.setThrowable(e);
        }
    }

    @Override
    public String getGUI() {
        return ConstantTimerGui.class.getName();
    }
}
