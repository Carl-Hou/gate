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

import org.gate.common.config.GateProps;
import org.gate.common.util.GateUtils;
import org.gate.gui.details.results.elements.graph.ElementResult;
import org.gate.gui.common.TestElement;
import org.gate.gui.graph.elements.sampler.protocol.selenium.util.SeleniumConstantsInterface;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;

import java.time.Duration;
import java.util.regex.Pattern;

public class ConditionChecker extends AbstractSeleniumSampler implements SeleniumConstantsInterface {

    public ConditionChecker() {
        // Add shared properties
        addProp(TestElement.NS_DEFAULT, PN_WaitTimeOut, "");
        addProp(TestElement.NS_DEFAULT, PN_WaitPollingInterval, "");
    }

    @Override
    public String getStaticLabel() {
        return "Selenium ExpectedConditions";
    }

    abstract class AbstractWaitConditions extends AbstractMethodSupplier{
        public abstract boolean isReachCondition(FluentWait<WebDriver> wait);

        @Override
        public void run(ElementResult result){
            WebDriver driver = getDriver(result);
            if (result.isFailure()) return;

            String waitTimeOut = getRunTimeProp(TestElement.NS_DEFAULT, PN_WaitTimeOut);
            String waitFrequency = getRunTimeProp(TestElement.NS_DEFAULT, PN_WaitPollingInterval);

            FluentWait<WebDriver> fluentWait = new FluentWait(driver).ignoring(NoSuchElementException.class);
            fluentWait.withTimeout(Duration.ofSeconds(Integer.parseUnsignedInt(waitTimeOut)));
            fluentWait.pollingEvery(Duration.ofMillis(Long.parseUnsignedLong(waitFrequency)));
            // TODO probably no new object need to be created.
            AbstractWaitConditions waitCondition = (AbstractWaitConditions) getMethodSupplierInstance(getCurrentMethodSupplier());
            try {
                boolean waitResult = waitCondition.isReachCondition(fluentWait);
                if (waitResult == false) {
                    result.setFailure("Not get expected condition before timeout");
                }
            } catch (TimeoutException ex) {
                result.setFailure(GateUtils.getStackTrace(ex));
                log.error("Element not found before timeout", ex);
            } catch (Throwable t) {
                result.setFailure(GateUtils.getStackTrace(t));
                log.error("Run into fatal error", t);
            }
            result.setResponseObject(result.isSuccess());
            return;
        }

        By getLocatorFromRuntime(){
            return SeleniumUtils.getLocator(getRTArg(PN_LocatorType), getRTArg(PN_LocatorCondition));
        }

        void addLocatorArguments() {
            addArg(PN_LocatorType, LocatorTypes[0]);
            addArg(PN_LocatorCondition, "Input location conditions");
        }
    }

    abstract class AbstractElementWaiter extends AbstractWaitConditions{
        @Override
        public void addArguments() {
            addLocatorArguments();
        }

        @Override
        public boolean isReachCondition(FluentWait<WebDriver> wait) {
            By locator = getLocatorFromRuntime();
            if (getElement(wait, locator) == null) {
                return false;
            } else {
                return true;
            }
        }

        abstract  WebElement getElement(FluentWait<WebDriver> wait, By locator);
    }

    class ElementToBeClickable extends AbstractElementWaiter {
        @Override
        WebElement getElement(FluentWait<WebDriver> wait, By locator) {
            return wait.until(ExpectedConditions.elementToBeClickable(locator));
        }
    }

    class PresenceOfElementLocated extends AbstractElementWaiter {
        @Override
        WebElement getElement(FluentWait<WebDriver> wait, By locator) {
            return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        }
    }

    class VisibilityOfElementLocated extends AbstractElementWaiter {
        @Override
        WebElement getElement(FluentWait<WebDriver> wait, By locator) {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        }
    }

    public class AttributeContains extends AbstractWaitConditions {
        @Override
        public void addArguments() {
            addLocatorArguments();
            addArg(PN_AttributeName, "Input Name of the argument");
            addArg(PN_AttributeValue, "Input Value of the argument");
        }

        @Override
        public boolean isReachCondition(FluentWait<WebDriver> wait) {
            return wait.until(ExpectedConditions.attributeContains(getLocatorFromRuntime(),
                    getRTArg(PN_AttributeName), getRTArg(PN_AttributeValue)));
        }
    }

    public class AttributeToBe extends AbstractWaitConditions {
        @Override
        public void addArguments() {
            addLocatorArguments();
            addArg(PN_AttributeName, "Input Name of the argument");
            addArg(PN_AttributeValue, "Input Value of the argument");
        }

        @Override
        public boolean isReachCondition(FluentWait<WebDriver> wait) {
            return wait.until(ExpectedConditions.attributeToBe(getLocatorFromRuntime(),
                    getRTArg(PN_AttributeName), getRTArg(PN_AttributeValue)));
        }

    }

    public class ElementSelectionStateToBe extends AbstractWaitConditions {
        @Override
        public void addArguments() {
            addLocatorArguments();
            addArg(PN_Selected, GateProps.TRUE);
        }

        @Override
        public boolean isReachCondition(FluentWait<WebDriver> wait) {
            return wait.until(ExpectedConditions.elementSelectionStateToBe(getLocatorFromRuntime(),
                    Boolean.valueOf(getRTArg(PN_Selected))));
        }
    }

    public class ElementToBeSelected extends AbstractWaitConditions {
        @Override
        public void addArguments() {
            addLocatorArguments();
        }

        @Override
        public boolean isReachCondition(FluentWait<WebDriver> wait) {
            return wait.until(ExpectedConditions.elementToBeSelected(getLocatorFromRuntime()));
        }
    }

    public class InvisibilityOfElementLocated extends AbstractWaitConditions {
        @Override
        public void addArguments() {
            addLocatorArguments();
        }

        @Override
        public boolean isReachCondition(FluentWait<WebDriver> wait) {
            return wait.until(ExpectedConditions.invisibilityOfElementLocated(getLocatorFromRuntime()));
        }
    }

    public class InvisibilityOfElementWithText extends AbstractWaitConditions {
        @Override
        public void addArguments() {
            addLocatorArguments();
            addArg(PN_Text, "");
        }

        @Override
        public boolean isReachCondition(FluentWait<WebDriver> wait) {
            return wait.until(ExpectedConditions.invisibilityOfElementWithText(getLocatorFromRuntime(), getRTArg(PN_Text)));
        }
    }

    public class JavaScriptThrowsNoExceptions extends AbstractWaitConditions {
        @Override
        public void addArguments() {
            addArg(PN_JavaScript, "");
        }

        @Override
        public boolean isReachCondition(FluentWait<WebDriver> wait) {
            return wait.until(ExpectedConditions.javaScriptThrowsNoExceptions(getRTArg(PN_JavaScript)));
        }
    }

    public class NumberOfWindowsToBe extends AbstractWaitConditions {
        @Override
        public void addArguments() {
            addArg(PN_Number, "1");
        }

        @Override
        public boolean isReachCondition(FluentWait<WebDriver> wait) {
            return wait.until(ExpectedConditions.numberOfWindowsToBe(Integer.parseUnsignedInt(getRTArg(PN_Number))));
        }
    }

    public class TextMatches extends AbstractWaitConditions {
        @Override
        public void addArguments() {
            addLocatorArguments();
            addArg(PN_Regex, "Input Regex Expression");
        }

        @Override
        public boolean isReachCondition(FluentWait<WebDriver> wait) {
            return wait.until(ExpectedConditions.textMatches(getLocatorFromRuntime(),
                    Pattern.compile(getRTArg(PN_Regex))));
        }
    }

    public class TextToBe extends AbstractWaitConditions {
        @Override
        public void addArguments() {
            addLocatorArguments();
            addArg(PN_Text, "Input text");
        }

        @Override
        public boolean isReachCondition(FluentWait<WebDriver> wait) {
            return wait.until(ExpectedConditions.textToBe(getLocatorFromRuntime(), getRTArg(PN_Text)));
        }
    }

    public class TextToBePresentInElementLocated extends AbstractWaitConditions {
        @Override
        public void addArguments() {
            addLocatorArguments();
            addArg(PN_Text, "Input text");
        }

        @Override
        public boolean isReachCondition(FluentWait<WebDriver> wait) {
            return wait.until(ExpectedConditions.textToBePresentInElementLocated(getLocatorFromRuntime(),
                    getRTArg(PN_Text)));
        }
    }

    public class TextToBePresentInElementValue extends AbstractWaitConditions {
        @Override
        public void addArguments() {
            addLocatorArguments();
            addArg(PN_Text, "Input text");
        }

        @Override
        public boolean isReachCondition(FluentWait<WebDriver> wait) {
            return wait.until(ExpectedConditions.textToBePresentInElementValue(getLocatorFromRuntime(), getRTArg(PN_Text)));
        }
    }

    public class TitleContains extends AbstractWaitConditions {
        @Override
        public void addArguments() {
            addArg(PN_Title, "Input title");
        }

        @Override
        public boolean isReachCondition(FluentWait<WebDriver> wait) {
            return wait.until(ExpectedConditions.titleContains(getRTArg(PN_Title)));
        }
    }

    public class TitleIs extends AbstractWaitConditions {
        @Override
        public void addArguments() {
            addArg(PN_Title, "Input title");
        }

        @Override
        public boolean isReachCondition(FluentWait<WebDriver> wait) {
            return wait.until(ExpectedConditions.titleIs(getRTArg(PN_Title)));
        }
    }

    public class UrlContains extends AbstractWaitConditions {
        @Override
        public void addArguments() {
            addArg(PN_Text, "Input text");
        }

        @Override
        public boolean isReachCondition(FluentWait<WebDriver> wait) {
            return wait.until(ExpectedConditions.urlContains(getRTArg(PN_Text)));
        }
    }

    public class UrlMatches extends AbstractWaitConditions {
        @Override
        public void addArguments() {
            addArg(PN_Regex, "Input regex expression");
        }

        @Override
        public boolean isReachCondition(FluentWait<WebDriver> wait) {
            return wait.until(ExpectedConditions.urlMatches(getRTArg(PN_Regex)));
        }
    }

    public class UrlToBe extends AbstractWaitConditions {
        @Override
        public void addArguments() {
            addArg(PN_Text, "Input expected Url");
        }

        @Override
        public boolean isReachCondition(FluentWait<WebDriver> wait) {
            return wait.until(ExpectedConditions.urlToBe(getRTArg(PN_Text)));
        }
    }

}
