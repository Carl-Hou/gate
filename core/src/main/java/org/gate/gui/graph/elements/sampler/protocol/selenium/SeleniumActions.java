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
import org.gate.gui.graph.elements.sampler.protocol.selenium.util.SeleniumUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;

public class SeleniumActions extends AbstractSeleniumSampler {

    public SeleniumActions(){
    }

    @Override
    public String getStaticLabel() {
        return "Selenium Actions";
    }

    abstract class ActionMethod extends AbstractMethodSupplier{
        @Override
        public void addArguments(){
            beforeAddArguments();
            addArg(PN_PAUSE, "");
        }

        abstract void beforeAddArguments();

        @Override
        public void run(ElementResult result) {
            WebDriver driver = getDriver(result);
            if(result.isFailure()) return;
            Actions actions = new Actions(driver);
            exec(driver, actions, result);
            String pause = getRTArg(PN_PAUSE).trim();
            if(pause.isEmpty()){
                actions.perform();
            }else {
                long pauseTime = Long.parseLong(pause);
                actions.pause(pauseTime);
            }
        }
        abstract void exec(WebDriver driver, Actions action, ElementResult result);
    }

    abstract class DriverMethod extends ActionMethod{

        void beforeAddArguments(){};

        void exec(WebDriver driver, Actions actions, ElementResult result){
            execute(actions, result);
        }
        abstract void execute(Actions actions, ElementResult result);
    }

    abstract class ElementMethod extends ActionMethod{

        void beforeAddArguments(){
            addArg(PN_LocatorType, SeleniumUtils.LocatorTypes[0]);
            addArg(PN_LocatorCondition, "");
        };

        void exec(WebDriver driver, Actions actions, ElementResult result){
            By locator = SeleniumUtils.getLocator(getRTArg(PN_LocatorType), getRTArg(PN_LocatorCondition));
            WebElement element = driver.findElement(locator);
            execute(actions, element, result);
        }

        abstract void execute(Actions actions, WebElement element, ElementResult result);
    }

    class MoveToElement extends ElementMethod {
        @Override
        void execute(Actions actions, WebElement element, ElementResult result) {
            actions.moveToElement(element);
        }
    }

    class Click extends DriverMethod {
        @Override
        void execute(Actions actions, ElementResult result) {
            actions.click();
        }
    }

    class ClickOnElement extends ElementMethod {
        @Override
        void execute(Actions actions, WebElement element, ElementResult result) {
            actions.click(element);
        }
    }

    class ClickAndHold extends DriverMethod {
        @Override
        void execute(Actions actions, ElementResult result) {
            actions.clickAndHold();
        }
    }

    class Release extends DriverMethod {
        @Override
        void execute(Actions actions, ElementResult result) {
            actions.release();
        }
    }

    class ClickAndHoldOnElement extends ElementMethod {
        @Override
        void execute(Actions actions, WebElement element, ElementResult result) {
            actions.clickAndHold(element);
        }
    }

    class ReleaseOnElement extends ElementMethod {
        @Override
        void execute(Actions actions, WebElement element, ElementResult result) {
            actions.release(element);
        }
    }

    class DoubleClick extends DriverMethod {
        @Override
        void execute(Actions actions, ElementResult result) {
            actions.doubleClick();
        }
    }

    class DoubleClickOnElement extends ElementMethod {
        @Override
        void execute(Actions actions, WebElement element, ElementResult result) {
            actions.doubleClick(element);
        }
    }

    class contextClick extends DriverMethod {
        @Override
        void execute(Actions actions, ElementResult result) {
            actions.contextClick();
        }
    }

    class ContextClickClickOnElement extends ElementMethod {
        @Override
        void execute(Actions actions, WebElement element, ElementResult result) {
            actions.contextClick(element);
        }
    }

    class KeyDown extends DriverMethod {
        void beforeAddArguments(){
            addArg(Keyboard_Key, KK_Control);
        }
        @Override
        void execute(Actions actions, ElementResult result) {
            Keys key = SeleniumUtils.getKeys(getRTArg(Keyboard_Key));
            actions.keyDown(key);
        }
    }

    class KeyDownOnElement extends ElementMethod {
        void beforeAddArguments(){
            addArg(Keyboard_Key, KK_Control);
        }
        @Override
        void execute(Actions actions, WebElement element, ElementResult result) {
            Keys key = SeleniumUtils.getKeys(getRTArg(Keyboard_Key));
            actions.keyDown(element, key);
        }

    }

    class KeyUp extends DriverMethod {
        void beforeAddArguments(){
            addArg(Keyboard_Key, KK_Control);
        }
        @Override
        void execute(Actions actions, ElementResult result) {
            Keys key = SeleniumUtils.getKeys(getRTArg(Keyboard_Key));
            actions.keyUp(key);
        }
    }

    class KeyUpOnElement extends ElementMethod {
        void beforeAddArguments(){
            addArg(Keyboard_Key, KK_Control);
        }
        @Override
        void execute(Actions actions, WebElement element, ElementResult result) {
            Keys key = SeleniumUtils.getKeys(getRTArg(Keyboard_Key));
            actions.keyUp(element, key);
        }

    }

    class SendKeys extends DriverMethod {
        void beforeAddArguments(){
            addArg(Keyboard_KeysToSend, "");
        }
        @Override
        void execute(Actions actions, ElementResult result) {
            String key = getRTArg(Keyboard_KeysToSend);
            actions.sendKeys(key);
        }
    }

    class SendKeysOnElement extends ElementMethod {
        void beforeAddArguments(){
            addArg(Keyboard_KeysToSend, "");
        }
        @Override
        void execute(Actions actions, WebElement element, ElementResult result) {
            String key = getRTArg(Keyboard_KeysToSend);
            actions.sendKeys(element, key);
        }

    }



}
