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
package org.gate.gui.graph.elements.sampler.protocol.selenium;

import org.gate.gui.details.results.elements.graph.ElementResult;
import org.gate.gui.graph.common.ParameterUtils;
import org.openqa.selenium.WebDriver;

import java.util.concurrent.TimeUnit;

public class Timeouts extends AbstractSeleniumSampler {

    public final static String PN_Seconds = "timeout_seconds";

    public Timeouts(){

    }

    abstract class TimeoutsMethod implements MethodSupplier{
        @Override
        public void addArgumentsToProps(){
            addProp(NS_ARGUMENT, PN_Seconds, "30");
        }

        @Override
        public void run(ElementResult result) {
            WebDriver driver = getDriver(result);
            long seconds = ParameterUtils.getLong(getRunTimeProp(NS_ARGUMENT, PN_Seconds), result);
            if(result.isFailure()) return;
            setTimeOuts(driver, seconds, result);

        }
        abstract void setTimeOuts(WebDriver driver, long seconds, ElementResult result);
    }

    class ImplicitlyWait extends TimeoutsMethod{

        @Override
        void setTimeOuts(WebDriver driver, long seconds, ElementResult result) {
            driver.manage().timeouts().implicitlyWait(seconds, TimeUnit.SECONDS);
        }
    }

    class PageLoadTimeout extends TimeoutsMethod{

        @Override
        void setTimeOuts(WebDriver driver, long seconds, ElementResult result) {
            driver.manage().timeouts().pageLoadTimeout(seconds, TimeUnit.SECONDS);
        }
    }

    class SetScriptTimeout extends TimeoutsMethod{

        @Override
        void setTimeOuts(WebDriver driver, long seconds, ElementResult result) {
            driver.manage().timeouts().setScriptTimeout(seconds, TimeUnit.SECONDS);
        }
    }
}
