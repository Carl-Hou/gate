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
import org.gate.gui.graph.elements.sampler.protocol.selenium.util.SeleniumConstantsInterface;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.FluentWait;

public class Element extends AbstractSeleniumSampler implements SeleniumConstantsInterface {

    public Element(){
        addProp(NS_DEFAULT, PN_WaitExpectedCondition, SeleniumUtils.WaitExpectedConditionsForElements[0]);
        addProp(NS_DEFAULT, PN_WaitTimeOut, "");
        addProp(NS_DEFAULT, PN_WaitPollingInterval, "");
        addProp(NS_DEFAULT, PN_LocatorType, SeleniumUtils.LocatorTypes[0]);
        addProp(NS_DEFAULT, PN_LocatorCondition, "Input Locator Conditions");
    }

    @Override
    public String getStaticLabel() {
        return "Selenium Element";
    }

    abstract class AbstractElementMethod extends AbstractMethodSupplier {

        @Override
        public void run(ElementResult result) {
            WebDriver driver = getDriver(result);
            if(result.isFailure()) return;
            FluentWait<WebDriver> wait = SeleniumUtils.getWait(driver, getRunTimeProp(NS_DEFAULT,PN_WaitTimeOut),
                    getRunTimeProp(NS_DEFAULT, PN_WaitPollingInterval));
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
        final static String VN_Keys = "keys_to_sent";
        @Override
        public void addArguments(){
            addArg(VN_Keys, "Input keys to send");
        }
        @Override
        void action(WebElement element, ElementResult result) {
            element.sendKeys(getRTArg(VN_Keys));
        }
    }

    class GetAttribute extends AbstractElementMethod {
        final static String VN_AttributeName = "attribute_name";
        final static String VN_AttributeValueName = "variable_name_attribute_value";
        @Override
        public void addArguments(){
            addArg(VN_AttributeName, "Input attribute name");
            addArg(VN_AttributeValueName, "attribute_value");
        }
        @Override
        void action(WebElement element, ElementResult result) {
            String attribute = element.getAttribute(getRTArg(VN_AttributeName));
            setGateVariable(VN_AttributeValueName, attribute);
        }
    }

    class GetCssValue extends AbstractElementMethod {
        final static String VN_PropertyName = "property_name";
        final static String VN_CssValueName = "variable_name_css_value";
        @Override
        public void addArguments(){
            addArg(VN_PropertyName, "Input property name");
            addArg(VN_CssValueName, "css_value");
        }
        @Override
        void action(WebElement element, ElementResult result) {
            String cssValue = element.getCssValue(getRTArg(VN_PropertyName));
            setGateVariable(VN_CssValueName, cssValue);
        }
    }

    class GetLocation extends AbstractElementMethod {
        final static String VN_X = "variable_name_x";
        final static String VN_Y = "variable_name_y";
        @Override
        public void addArguments(){
            addArg(VN_X, "location_x");
            addArg(VN_Y, "location_y");
        }
        @Override
        void action(WebElement element, ElementResult result) {
            Point point = element.getLocation();
            setGateVariable(VN_X, point.getX());
            setGateVariable(VN_Y, point.getY());
        }
    }

    class GetRect extends AbstractElementMethod {
        final static String VN_X = "variable_name_x";
        final static String VN_Y = "variable_name_y";
        final static String VN_Width = "variable_name_width";
        final static String VN_Height = "variable_name_height";

        @Override
        public void addArguments(){
            addArg(VN_X, "rec_x");
            addArg(VN_Y, "rec_y");
            addArg(VN_Width, "rec_width");
            addArg(VN_Height, "rec_height");
        }

        @Override
        void action(WebElement element, ElementResult result) {
            Rectangle rect = element.getRect();
            setGateVariable(VN_X, rect.getX());
            setGateVariable(VN_Y, rect.getY());
            setGateVariable(VN_Width, rect.getWidth());
            setGateVariable(VN_Height, rect.getHeight());
        }
    }

    class GetSize extends AbstractElementMethod {
        final static String VN_Width = "variable_name_width";
        final static String VN_Height = "variable_name_height";

        @Override
        public void addArguments(){
            addArg(VN_Width, "element_width");
            addArg(VN_Height, "element_height");
        }

        @Override
        void action(WebElement element, ElementResult result) {
            Dimension dimension = element.getSize();
            setGateVariable(VN_Width, dimension.getWidth());
            setGateVariable(VN_Height, dimension.getHeight());
        }
    }

    class GetTagName extends AbstractElementMethod {
        final static String VN_TagName = "variable_name_tag_name";

        @Override
        public void addArguments(){
            addArg(VN_TagName, "tag_name");
        }

        @Override
        void action(WebElement element, ElementResult result) {
            String tagName = element.getTagName();
            setGateVariable(VN_TagName, tagName);
        }
    }

    class GetText extends AbstractElementMethod {
        final static String VN_Text = "variable_name_text";
        @Override
        public void addArguments(){
            addArg(VN_Text, "text");
        }
        @Override
        void action(WebElement element, ElementResult result) {
            String text = element.getText();
            setGateVariable(VN_Text, text);
        }
    }

    class IsDisplayed extends AbstractElementMethod {
        final static String VN_IsDisplayed = "variable_name_is_displayed";
        @Override
        public void addArguments(){
            addArg(VN_IsDisplayed, "is_displayed");
        }
        @Override
        void action(WebElement element, ElementResult result) {
            boolean isDisplayed = element.isDisplayed();
            setGateVariable(VN_IsDisplayed, isDisplayed);
        }
    }

    class IsEnabled extends AbstractElementMethod {
        final static String VN_IsEnabled = "variable_name_is_enabled";
        @Override
        public void addArguments(){
            addArg(VN_IsEnabled, "is_enabled");
        }
        @Override
        void action(WebElement element, ElementResult result) {
            boolean isEnabled = element.isEnabled();
            setGateVariable(VN_IsEnabled, isEnabled);
        }
    }

    class IsSelected extends AbstractElementMethod {
        final static String VN_IsSelected = "variable_name_is_selected";
        @Override
        public void addArguments(){
            addArg(VN_IsSelected, "is_selected");
        }
        @Override
        void action(WebElement element, ElementResult result) {
            boolean isSelected = element.isSelected();
            setGateVariable(VN_IsSelected, isSelected);
        }
    }

}
