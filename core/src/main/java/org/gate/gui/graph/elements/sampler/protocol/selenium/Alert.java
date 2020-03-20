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

import net.minidev.json.JSONObject;
import org.gate.gui.details.results.elements.graph.ElementResult;
import org.openqa.selenium.WebDriver;

public class Alert extends AbstractSeleniumSampler {

    public final static String PN_Seconds = "timeout_seconds";

    public Alert(){

    }

    abstract class TargetLocatorMethod implements MethodSupplier{
        @Override
        public void addArgumentsToProps(){
        }

        @Override
        public void run(ElementResult result) {
            WebDriver driver = getDriver(result);
            if(result.isFailure()) return;
            action(driver, result);
        }
        abstract void action(WebDriver driver, ElementResult result);
    }

    class Accept  extends TargetLocatorMethod{
        @Override
        void action(WebDriver driver, ElementResult result) {
            driver.switchTo().alert().accept();
        }
    }

    class Dismiss  extends TargetLocatorMethod{
        @Override
        void action(WebDriver driver, ElementResult result) {
            driver.switchTo().alert().dismiss();
        }
    }

    class GetText  extends TargetLocatorMethod{
        @Override
        void action(WebDriver driver, ElementResult result) {
            String text = driver.switchTo().alert().getText();
            JSONObject returnValue = new JSONObject();
            returnValue.put("text", text);
            result.setResponseObject(getJSONString(returnValue));
        }
    }

    class SendKeys  extends TargetLocatorMethod{
        @Override
        public void addArgumentsToProps(){
            addProp(NS_ARGUMENT, "keys_to_send", "keys to send");
        }

        @Override
        void action(WebDriver driver, ElementResult result) {
            driver.switchTo().alert().sendKeys(getRunTimeProp(NS_ARGUMENT, "keys_to_send"));
        }
    }


}
