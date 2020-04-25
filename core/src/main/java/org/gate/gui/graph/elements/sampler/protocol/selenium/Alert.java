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
import org.openqa.selenium.WebDriver;

public class Alert extends AbstractSeleniumSampler {

    public final static String PN_Seconds = "timeout_seconds";

    public Alert(){

    }

    @Override
    public String getStaticLabel() {
        return "Selenium Alert";
    }

    abstract class AlertMethod extends AbstractMethodSupplier{

        @Override
        public void run(ElementResult result) {
            WebDriver driver = getDriver(result);
            if(result.isFailure()) return;
            action(driver, result);
        }
        abstract void action(WebDriver driver, ElementResult result);
    }

    class Accept  extends AlertMethod {
        @Override
        void action(WebDriver driver, ElementResult result) {
            driver.switchTo().alert().accept();
        }
    }

    class Dismiss  extends AlertMethod {
        @Override
        void action(WebDriver driver, ElementResult result) {
            driver.switchTo().alert().dismiss();
        }
    }

    class GetText  extends AlertMethod {
        final static String VN_Text = "variable_name_text";
        @Override
        public void addArguments(){
            addArg(VN_Text, "text");
        }
        @Override
        void action(WebDriver driver, ElementResult result) {
            String text = driver.switchTo().alert().getText();
            setGateVariable(VN_Text, text);
        }
    }

    class SendKeys  extends AlertMethod {
        final static String VN_KeysToSend = "keys_to_send";
        @Override
        public void addArguments(){
            addArg(VN_KeysToSend, "keys to send");
        }

        @Override
        void action(WebDriver driver, ElementResult result) {
            driver.switchTo().alert().sendKeys(getRTArg(VN_KeysToSend));
        }
    }

}
