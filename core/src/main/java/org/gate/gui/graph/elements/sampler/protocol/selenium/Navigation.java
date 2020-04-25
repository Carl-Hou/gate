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
import org.gate.gui.common.TestElement;
import org.openqa.selenium.WebDriver;

public class Navigation extends AbstractSeleniumSampler {

    public Navigation(){

    }

    @Override
    public String getStaticLabel() {
        return "Selenium Navigation";
    }

    abstract class NavigationMethod extends AbstractMethodSupplier{

        @Override
        public void run(ElementResult result) {
            WebDriver driver = getDriver(result);
            if(result.isFailure()) return;
            action(driver, result);
        }
        abstract void action(WebDriver driver, ElementResult result);
    }

    class Back extends NavigationMethod {
        @Override
        void action(WebDriver driver, ElementResult result) {
            driver.navigate().back();
        }
    }

    class Forward extends NavigationMethod {
        @Override
        void action(WebDriver driver, ElementResult result) {
            driver.navigate().forward();
        }
    }

    class Refresh extends NavigationMethod {
        @Override
        void action(WebDriver driver, ElementResult result) {
            driver.navigate().refresh();
        }
    }

    class To extends NavigationMethod {
        final static String VN_Url = "url";
        @Override
        public void addArguments(){
            addArg("url", "url to navigate");
        }
        @Override
        void action(WebDriver driver, ElementResult result) {
            driver.navigate().to(getRTArg(VN_Url));
        }
    }
}
