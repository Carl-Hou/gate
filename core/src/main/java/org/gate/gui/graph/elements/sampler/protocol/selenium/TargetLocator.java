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

public class TargetLocator extends AbstractSeleniumSampler {

    public TargetLocator(){
        // keep this for compile object
    }

    abstract class AbstractTargetLocatorMethod implements MethodSupplier{
        @Override
        public void run(ElementResult result) {
            WebDriver driver = getDriver(result);
            if(result.isFailure()){
                return;
            }
            SeleniumContext seleniumContext = getSeleniumContext();
            exec(driver, seleniumContext, result);
        }
        abstract void exec(WebDriver driver, SeleniumContext seleniumContext, ElementResult result);
    }

    class DefaultContent extends AbstractTargetLocatorMethod {
        @Override
        public void addArgumentsToProps() {
            addProp(NS_ARGUMENT, "default_content_driver_id", "value of default_content_driver_id");
        }
        @Override
        void exec(WebDriver driver, SeleniumContext seleniumContext, ElementResult result) {
            WebDriver dr = driver.switchTo().defaultContent();
            seleniumContext.putDriver(getRunTimeProp(NS_ARGUMENT, "default_content_driver_id"), dr);
        }
    }

    class Frame extends AbstractTargetLocatorMethod {
        @Override
        public void addArgumentsToProps() {
            addProp(NS_ARGUMENT, "name_or_id", "name or id of frame");
            addProp(NS_ARGUMENT, "frame_driver_id", "value of frame_driver_id");
        }
        @Override
        void exec(WebDriver driver, SeleniumContext seleniumContext, ElementResult result) {
            if(result.isFailure()) return;
            WebDriver dr = driver.switchTo().frame(getRunTimeProp(NS_ARGUMENT, "name_or_id"));
            seleniumContext.putDriver(getRunTimeProp(NS_ARGUMENT, "frame_driver_id"), dr);
        }
    }

    class ParentFrame extends AbstractTargetLocatorMethod {
        @Override
        public void addArgumentsToProps() {
            addProp(NS_ARGUMENT, "parent_frame_driver_id", "value of parent_frame_driver_id");
        }
        @Override
        void exec(WebDriver driver, SeleniumContext seleniumContext, ElementResult result) {
            WebDriver dr = driver.switchTo().parentFrame();
            seleniumContext.putDriver(getRunTimeProp(NS_ARGUMENT, "parent_frame_driver_id"), dr);
        }
    }

    class Window extends AbstractTargetLocatorMethod {
        @Override
        public void addArgumentsToProps() {
            addProp(NS_ARGUMENT, "name_or_handle", "name or handle of the window");
            addProp(NS_ARGUMENT, "target_driver_id", "value of target_driver_id");
        }
        @Override
        void exec(WebDriver driver, SeleniumContext seleniumContext, ElementResult result) {
            String nameOrHandle = getRunTimeProp(NS_ARGUMENT, "name_or_handle");
            WebDriver dr = driver.switchTo().window(nameOrHandle);
            seleniumContext.putDriver(getRunTimeProp(NS_ARGUMENT, "target_driver_id"), dr);
        }
    }

}
