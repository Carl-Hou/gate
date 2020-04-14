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
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.FluentWait;

public class Element extends AbstractSeleniumSampler implements SeleniumConstantsInterface{

    public Element(){
        addProp(NS_DEFAULT, PN_WaitExpectedCondition, SeleniumUtils.WaitExpectedConditionsForElements[0]);
        addProp(NS_DEFAULT, PN_WaitTimeOut, "5");
        addProp(NS_DEFAULT, PN_WaitPollingInterval, "200");
        addProp(NS_DEFAULT, PN_LocatorType, SeleniumUtils.LocatorTypes[0]);
        addProp(NS_DEFAULT, PN_LocatorCondition, "Input Locator Conditions");
    }

    @Override
    public String getStaticLabel() {
        return "Selenium Element";
    }

    abstract class AbstractElementMethod implements MethodSupplier {

        @Override
        public void addArgumentsToProps(){

        }

        @Override
        public void run(ElementResult result) {
            WebDriver driver = getDriver(result);
            if(result.isFailure()) return;
            FluentWait<WebDriver> wait = SeleniumUtils.getWait(driver, getRunTimeProp(NS_DEFAULT,PN_WaitTimeOut), getRunTimeProp(NS_DEFAULT, PN_WaitPollingInterval));
            By locator = SeleniumUtils.getLocator(getRunTimeProp(NS_DEFAULT, PN_LocatorType),getRunTimeProp(NS_DEFAULT, PN_LocatorCondition));
            WebElement element = SeleniumUtils.getWebElement(wait, getRunTimeProp(NS_DEFAULT, PN_WaitExpectedCondition), locator);
            action(element, result);
        }

        abstract void action(WebElement element, ElementResult result);

    }

    class Clear extends AbstractElementMethod {
        @Override
        void action(WebElement element, ElementResult result) {
            element.clear();
        }
    }

    class Click extends AbstractElementMethod {
        @Override
        void action(WebElement element, ElementResult result) {
            element.click();
        }
    }

    class Submit extends AbstractElementMethod {

        @Override
        void action(WebElement element, ElementResult result) {
            element.submit();
        }
    }

    class SendKeys extends AbstractElementMethod {

        @Override
        public void addArgumentsToProps(){
            addProp(NS_ARGUMENT, "keys_to_sent", "Input keys to send");
        }
        @Override
        void action(WebElement element, ElementResult result) {
            element.sendKeys(getRunTimeProp(NS_ARGUMENT, "keys_to_sent"));
        }
    }

    class GetAttribute extends AbstractElementMethod {

        @Override
        public void addArgumentsToProps(){
            addProp(NS_ARGUMENT, "attribute_name", "Input attribute name");
        }
        @Override
        void action(WebElement element, ElementResult result) {
            String attribute = element.getAttribute(getRunTimeProp(NS_ARGUMENT, "attribute_name"));
            JSONObject returnValue = new JSONObject();
            returnValue.put("attribute", attribute);
            result.setResponseObject(getJSONString(returnValue));
        }
    }

    class GetCssValue extends AbstractElementMethod {

        @Override
        public void addArgumentsToProps(){
            addProp(NS_ARGUMENT, "property_name", "Input attribute name");
        }
        @Override
        void action(WebElement element, ElementResult result) {
            String cssValue = element.getCssValue(getRunTimeProp(NS_ARGUMENT, "property_name"));
            JSONObject returnValue = new JSONObject();
            returnValue.put("cssValue", cssValue);
            result.setResponseObject(getJSONString(returnValue));
        }
    }

    class GetLocation extends AbstractElementMethod {
        @Override
        void action(WebElement element, ElementResult result) {
            Point point = element.getLocation();
            JSONObject returnValue = new JSONObject();
            returnValue.put("X", point.getX());
            returnValue.put("Y", point.getY());
            result.setResponseObject(getJSONString(returnValue));
        }
    }

    class GetRect extends AbstractElementMethod {

        @Override
        void action(WebElement element, ElementResult result) {
            Rectangle rect = element.getRect();
            JSONObject returnValue = new JSONObject();
            returnValue.put("x", rect.getX());
            returnValue.put("y", rect.getY());
            returnValue.put("width", rect.getWidth());
            returnValue.put("height", rect.getHeight());
            result.setResponseObject(getJSONString(returnValue));
        }
    }

    class GetSize extends AbstractElementMethod {

        @Override
        void action(WebElement element, ElementResult result) {
            Dimension dimension = element.getSize();
            JSONObject returnValue = new JSONObject();
            returnValue.put("width", dimension.getWidth());
            returnValue.put("height", dimension.getHeight());
            result.setResponseObject(getJSONString(returnValue));
        }
    }

    class GetTagName extends AbstractElementMethod {

        @Override
        void action(WebElement element, ElementResult result) {
            String tagName = element.getTagName();
            JSONObject returnValue = new JSONObject();
            returnValue.put("tagName", tagName);
            result.setResponseObject(getJSONString(returnValue));
        }
    }

    class GetText extends AbstractElementMethod {
        @Override
        void action(WebElement element, ElementResult result) {
            String text = element.getText();
            JSONObject returnValue = new JSONObject();
            returnValue.put("text", text);
            result.setResponseObject(getJSONString(returnValue));
        }
    }

    class IsDisplayed extends AbstractElementMethod {
        @Override
        void action(WebElement element, ElementResult result) {
            boolean isDisplayed = element.isDisplayed();
            JSONObject returnValue = new JSONObject();
            returnValue.put("isDisplayed", isDisplayed);
            result.setResponseObject(getJSONString(returnValue));
        }
    }

    class IsEnabled extends AbstractElementMethod {
        @Override
        void action(WebElement element, ElementResult result) {
            boolean isEnabled = element.isEnabled();
            JSONObject returnValue = new JSONObject();
            returnValue.put("isEnabled", isEnabled);
            result.setResponseObject(getJSONString(returnValue));
        }
    }

    class IsSelected extends AbstractElementMethod {
        @Override
        void action(WebElement element, ElementResult result) {
            boolean isSelected = element.isSelected();
            JSONObject returnValue = new JSONObject();
            returnValue.put("isSelected", isSelected);
            result.setResponseObject(getJSONString(returnValue));
        }
    }

//	@Override
//	public void reset(){
//        super.reset();
//        driver = null;
//  }
    // valid and set default value for RTP

}
