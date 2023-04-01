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
import org.gate.gui.common.TestElement;
import org.gate.gui.details.results.elements.graph.ElementResult;
import org.gate.gui.graph.elements.sampler.protocol.selenium.gui.SeleniumExplicitWaitGui;
import org.gate.gui.graph.elements.sampler.protocol.selenium.util.SeleniumConstantsInterface;
import org.gate.gui.graph.elements.sampler.protocol.selenium.util.SeleniumUtils;
import org.gate.runtime.GateContextService;
import org.gate.varfuncs.property.GateProperty;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;

import java.time.Duration;
import java.util.List;
import java.util.regex.Pattern;

public class SeleniumExplicitWait extends AbstractSeleniumSampler implements SeleniumConstantsInterface {

    public SeleniumExplicitWait() {
        // parameters for element create on first time
        addProp(NS_NAME, ExplicitWaitType, ExplicitWait_Element);
        addProp(NS_DEFAULT, PN_ExplicitWaitTimeOut, "");
        addProp(NS_DEFAULT, PN_ExplicitWaitPollingInterval, "");
        addProp(NS_DEFAULT, PN_LocatorType, SeleniumUtils.LocatorTypes[0]);
        addProp(NS_DEFAULT, PN_LocatorCondition, "");
    }


    @Override
    public String getStaticLabel() {
        return "Selenium Explicit Wait";
    }

    @Override
    public String getGUI(){
        return SeleniumExplicitWaitGui.class.getName();
    }

    protected List<Class> getSuppliersClasses(){
        GateProperty explicitWaitType = getProp(NS_NAME, ExplicitWaitType);
        List<Class> suppliersClasses = super.getSuppliersClasses();
        if(null != explicitWaitType && ExplicitWait_Condition.equals(explicitWaitType.getStringValue())){
            suppliersClasses.removeIf( s -> AbstractWaiterForElement.class.isAssignableFrom(s));
        }else{
            suppliersClasses.removeIf( s -> AbstractConditionWaiter.class.isAssignableFrom(s));
        }

        return suppliersClasses;
    }



    /*
        Code for wait for Element/Elements
     */

    abstract class AbstractWaiterForElement extends AbstractMethodSupplier{

        abstract void waitUntilExpectedCondition(FluentWait<WebDriver> wait, By locator);

        @Override
        public void run(ElementResult result){
            WebDriver driver = getDriver(result);
            if (result.isFailure()) return;

            String waitTimeOut = getRunTimeProp(TestElement.NS_DEFAULT, PN_ExplicitWaitTimeOut);
            String waitFrequency = getRunTimeProp(TestElement.NS_DEFAULT, PN_ExplicitWaitPollingInterval);

            FluentWait<WebDriver> fluentWait = new FluentWait(driver).ignoring(NoSuchElementException.class);
            fluentWait.withTimeout(Duration.ofSeconds(Integer.parseUnsignedInt(waitTimeOut)));
            fluentWait.pollingEvery(Duration.ofMillis(Long.parseUnsignedLong(waitFrequency)));

            try {
                    waitUntilExpectedCondition(fluentWait, SeleniumUtils.getLocator(getRunTimeProp(NS_DEFAULT, PN_LocatorType),
                        getRunTimeProp(NS_DEFAULT, PN_LocatorCondition)));
            } catch (TimeoutException ex) {
                result.setFailure(GateUtils.getStackTrace(ex));
                log.error("Element not found before timeout", ex);
            } catch (Throwable t) {
                result.setFailure(GateUtils.getStackTrace(t));
                log.error("Run into fatal error", t);
            }
            result.setResponseObject(result.isSuccess());
        }
    }

    abstract class AbstractElementWaiter extends AbstractWaiterForElement {
        @Override
        public void addArguments() {
            addArg(PN_VariableName_ReturnValue, "");
        }

        @Override
        void waitUntilExpectedCondition(FluentWait<WebDriver> fluentWait, By locator) {
            WebElement element = waitForElement(fluentWait, SeleniumUtils.getLocator(getRunTimeProp(NS_DEFAULT, PN_LocatorType),
                    getRunTimeProp(NS_DEFAULT, PN_LocatorCondition)));
            GateContextService.getContext().getVariables()
                    .putObject(getRTArg(PN_VariableName_ReturnValue), element);
        }

        abstract WebElement waitForElement(FluentWait<WebDriver> wait, By locator);
    }

    abstract class AbstractElementsWaiter extends AbstractWaiterForElement {
        @Override
        public void addArguments() {
            addArg(PN_VariableNamePrefix_ReturnValue, "");
        }


        @Override
        void waitUntilExpectedCondition(FluentWait<WebDriver> fluentWait, By locator) {
            List<WebElement> elements = waitForElements(fluentWait, SeleniumUtils.getLocator(getRunTimeProp(NS_DEFAULT, PN_LocatorType),
                    getRunTimeProp(NS_DEFAULT, PN_LocatorCondition)));
            GateContextService.getContext().getVariables().putObjects(getRTArg(PN_VariableNamePrefix_ReturnValue), elements);
        }

        abstract List<WebElement> waitForElements(FluentWait<WebDriver> wait, By locator);
    }

    class ElementToBeClickable extends AbstractElementWaiter {
        @Override
        WebElement waitForElement(FluentWait<WebDriver> wait, By locator) {
            return wait.until(ExpectedConditions.elementToBeClickable(locator));
        }
    }

    class PresenceOfElementLocated extends AbstractElementWaiter {
        @Override
        WebElement waitForElement(FluentWait<WebDriver> wait, By locator) {
            return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        }
    }

    class VisibilityOfElementLocated extends AbstractElementWaiter {
        @Override
        WebElement waitForElement(FluentWait<WebDriver> wait, By locator) {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        }
    }

    class NumberOfElementsToBe extends AbstractElementsWaiter {
        final static String VN_ElementsNumber = "Elements Number";
        @Override
        public void addArguments() {
            addArg(VN_ElementsNumber, "");
            super.addArguments();
        }

        @Override
        List<WebElement> waitForElements(FluentWait<WebDriver> wait, By locator) {
            int numberOfElements = Integer.parseInt(getRTArg(VN_ElementsNumber));
            return wait.until(ExpectedConditions.numberOfElementsToBe(locator, numberOfElements));
        }
    }

    class NumberOfElementsToBeLessThan extends AbstractElementsWaiter {
        final static String VN_ElementsNumber = "Elements Number";
        @Override
        public void addArguments() {
            addArg(VN_ElementsNumber, "");
            super.addArguments();
        }

        @Override
        List<WebElement> waitForElements(FluentWait<WebDriver> wait, By locator) {
            int numberOfElements = Integer.parseInt(getRTArg(VN_ElementsNumber));
            return wait.until(ExpectedConditions.numberOfElementsToBeLessThan(locator, numberOfElements));
        }
    }

    class NumberOfElementsToBeMoreThan extends AbstractElementsWaiter {
        final static String VN_ElementsNumber = "Elements Number";
        @Override
        public void addArguments() {
            addArg(VN_ElementsNumber, "");
            super.addArguments();
        }

        @Override
        List<WebElement> waitForElements(FluentWait<WebDriver> wait, By locator) {
            int numberOfElements = Integer.parseInt(getRTArg(VN_ElementsNumber));
            return wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(locator, numberOfElements));
        }
    }

    class PresenceOfAllElementsLocatedBy extends AbstractElementsWaiter {

        @Override
        List<WebElement> waitForElements(FluentWait<WebDriver> wait, By locator) {
            return wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(locator));
        }
    }

    class PresenceOfNestedElementsLocatedBy extends AbstractElementsWaiter {
        final static String VN_LocatorType = "Locator Type";
        final static String VN_LocatorCondition = "Locator Condition";

        @Override
        public void addArguments() {
            addArg(VN_LocatorType, LocatorTypes[0]);
            addArg(VN_LocatorCondition, "");
            super.addArguments();
        }

        @Override
        List<WebElement> waitForElements(FluentWait<WebDriver> wait, By locator) {
            By nestedLocator = SeleniumUtils.getLocator(getRTArg(VN_LocatorType), getRTArg(VN_LocatorCondition));
            return wait.until(ExpectedConditions.presenceOfNestedElementsLocatedBy(locator, nestedLocator));
        }
    }

    class VisibilityOfAllElementsLocatedBy extends AbstractElementsWaiter {

        @Override
        List<WebElement> waitForElements(FluentWait<WebDriver> wait, By locator) {
            return wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(locator));
        }
    }

    class VisibilityOfNestedElementsLocatedBy extends AbstractElementsWaiter {
        final static String VN_LocatorType = "Locator Type";
        final static String VN_LocatorCondition = "Locator Condition";

        @Override
        public void addArguments() {
            addArg(VN_LocatorType, LocatorTypes[0]);
            addArg(VN_LocatorCondition, "");
            super.addArguments();
        }

        @Override
        List<WebElement> waitForElements(FluentWait<WebDriver> wait, By locator) {
            By nestedLocator = SeleniumUtils.getLocator(getRTArg(VN_LocatorType), getRTArg(VN_LocatorCondition));
            return wait.until(ExpectedConditions.visibilityOfNestedElementsLocatedBy(locator, nestedLocator));
        }
    }

    /*
        Code for wait for Conditions
     */
    abstract class AbstractConditionWaiter extends AbstractMethodSupplier{

        @Override
        public void addArguments() {
            addArgumentsOfWaiter();
            addArg(PN_VariableName_ReturnValue, "");
        }

        abstract void addArgumentsOfWaiter();

        @Override
        public void run(ElementResult result){
            WebDriver driver = getDriver(result);
            if (result.isFailure()) return;

            String waitTimeOut = getRunTimeProp(TestElement.NS_DEFAULT, PN_ExplicitWaitTimeOut);
            String waitFrequency = getRunTimeProp(TestElement.NS_DEFAULT, PN_ExplicitWaitPollingInterval);

            FluentWait<WebDriver> fluentWait = new FluentWait(driver).ignoring(NoSuchElementException.class);
            fluentWait.withTimeout(Duration.ofSeconds(Integer.parseUnsignedInt(waitTimeOut)));
            fluentWait.pollingEvery(Duration.ofMillis(Long.parseUnsignedLong(waitFrequency)));

            try {
                boolean waitResult = isReachCondition(fluentWait);
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
            GateContextService.getContext().getVariables().put(getRTArg(PN_VariableName_ReturnValue)
                    , String.valueOf(result.isSuccess()));
        }

        By getLocatorFromRuntime(){
            return SeleniumUtils.getLocator(getRTArg(PN_LocatorType), getRTArg(PN_LocatorCondition));
        }

        void addLocatorArguments() {
            addArg(PN_LocatorType, LocatorTypes[0]);
            addArg(PN_LocatorCondition, "");
        }
        public abstract boolean isReachCondition(FluentWait<WebDriver> wait);
    }

    public class AlertIsPresent extends AbstractConditionWaiter {

        @Override
        void addArgumentsOfWaiter() {

        }

        @Override
        public boolean isReachCondition(FluentWait<WebDriver> wait) {
            wait.until(ExpectedConditions.alertIsPresent());
            // no exception throw means the alert show ups
            return true;
        }
    }

    public class AttributeContains extends AbstractConditionWaiter {
        @Override
        public void addArguments() {
            addLocatorArguments();
            addArg(PN_AttributeName, "");
            addArg(PN_AttributeValue, "");
        }

        @Override
        void addArgumentsOfWaiter() {

        }

        @Override
        public boolean isReachCondition(FluentWait<WebDriver> wait) {
            return wait.until(ExpectedConditions.attributeContains(getLocatorFromRuntime(),
                    getRTArg(PN_AttributeName), getRTArg(PN_AttributeValue)));
        }
    }

    public class AttributeToBe extends AbstractConditionWaiter {

        @Override
        void addArgumentsOfWaiter() {
            addLocatorArguments();
            addArg(PN_AttributeName, "");
            addArg(PN_AttributeValue, "");
        }

        @Override
        public boolean isReachCondition(FluentWait<WebDriver> wait) {
            return wait.until(ExpectedConditions.attributeToBe(getLocatorFromRuntime(),
                    getRTArg(PN_AttributeName), getRTArg(PN_AttributeValue)));
        }

    }

    public class AttributeToBeNotEmpty extends AbstractConditionWaiter {

        @Override
        void addArgumentsOfWaiter() {
            addArg(PN_VariableName, "");
            addArg(PN_AttributeName, "");
        }

        @Override
        public boolean isReachCondition(FluentWait<WebDriver> wait) {
            WebElement element = (WebElement) GateContextService.getContext().getVariables().getObject(getRTArg(PN_VariableName));
            return wait.until(ExpectedConditions.attributeToBeNotEmpty(element, getRTArg(PN_AttributeName)));
        }
    }

    public class ElementSelectionStateToBe extends AbstractConditionWaiter {

        @Override
        void addArgumentsOfWaiter() {
            addLocatorArguments();
            addArg(PN_Selected, GateProps.TRUE);
        }

        @Override
        public boolean isReachCondition(FluentWait<WebDriver> wait) {
            return wait.until(ExpectedConditions.elementSelectionStateToBe(getLocatorFromRuntime(),
                    Boolean.valueOf(getRTArg(PN_Selected))));
        }
    }

    public class ElementToBeSelected extends AbstractConditionWaiter {

        @Override
        void addArgumentsOfWaiter() {
            addLocatorArguments();
        }

        @Override
        public boolean isReachCondition(FluentWait<WebDriver> wait) {
            return wait.until(ExpectedConditions.elementToBeSelected(getLocatorFromRuntime()));
        }
    }

    public class FrameToBeAvailableAndSwitchToIt extends AbstractConditionWaiter {
        final static String VN_FrameLocator = "Frame Locator";

        @Override
        void addArgumentsOfWaiter() {
            addLocatorArguments();
        }

        @Override
        public boolean isReachCondition(FluentWait<WebDriver> wait) {
            wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(getRTArg(VN_FrameLocator)));
            return true ;
        }
    }

    public class InvisibilityOfElementLocated extends AbstractConditionWaiter {

        @Override
        void addArgumentsOfWaiter() {
            addLocatorArguments();
        }

        @Override
        public boolean isReachCondition(FluentWait<WebDriver> wait) {
            return wait.until(ExpectedConditions.invisibilityOfElementLocated(getLocatorFromRuntime()));
        }
    }

    public class InvisibilityOf extends AbstractConditionWaiter {

        @Override
        void addArgumentsOfWaiter() {
            addArg(PN_VariableName, "");
        }

        @Override
        public boolean isReachCondition(FluentWait<WebDriver> wait) {
            WebElement element = (WebElement) GateContextService.getContext().getVariables().getObject(getRTArg(PN_VariableName));
            return wait.until(ExpectedConditions.invisibilityOf(element));
        }
    }

    public class InvisibilityOfElementWithText extends AbstractConditionWaiter {

        @Override
        void addArgumentsOfWaiter() {
            addLocatorArguments();
            addArg(PN_Text, "");
        }

        @Override
        public boolean isReachCondition(FluentWait<WebDriver> wait) {
            return wait.until(ExpectedConditions.invisibilityOfElementWithText(getLocatorFromRuntime(), getRTArg(PN_Text)));
        }
    }

    public class JavaScriptThrowsNoExceptions extends AbstractConditionWaiter {

        @Override
        void addArgumentsOfWaiter() {
            addArg(PN_JavaScript, "");
        }

        @Override
        public boolean isReachCondition(FluentWait<WebDriver> wait) {
            return wait.until(ExpectedConditions.javaScriptThrowsNoExceptions(getRTArg(PN_JavaScript)));
        }
    }

    public class NumberOfWindowsToBe extends AbstractConditionWaiter {

        @Override
        void addArgumentsOfWaiter() {
            addArg(PN_Number, "");
        }

        @Override
        public boolean isReachCondition(FluentWait<WebDriver> wait) {
            return wait.until(ExpectedConditions.numberOfWindowsToBe(Integer.parseUnsignedInt(getRTArg(PN_Number))));
        }
    }

    public class StalenessOf extends AbstractConditionWaiter {

        @Override
        void addArgumentsOfWaiter() {
            addArg(PN_VariableName, "");
        }

        @Override
        public boolean isReachCondition(FluentWait<WebDriver> wait) {
            WebElement element = (WebElement) GateContextService.getContext().getVariables().getObject(getRTArg(PN_VariableName));
            return wait.until(ExpectedConditions.stalenessOf(element));
        }
    }

    public class TextMatches extends AbstractConditionWaiter {

        @Override
        void addArgumentsOfWaiter() {
            addLocatorArguments();
            addArg(PN_Regex, "Input Regex Expression");
        }

        @Override
        public boolean isReachCondition(FluentWait<WebDriver> wait) {
            return wait.until(ExpectedConditions.textMatches(getLocatorFromRuntime(),
                    Pattern.compile(getRTArg(PN_Regex))));
        }
    }

    public class TextToBe extends AbstractConditionWaiter {

        @Override
        void addArgumentsOfWaiter() {
            addLocatorArguments();
            addArg(PN_Text, "Input text");
        }

        @Override
        public boolean isReachCondition(FluentWait<WebDriver> wait) {
            return wait.until(ExpectedConditions.textToBe(getLocatorFromRuntime(), getRTArg(PN_Text)));
        }
    }

    public class TextToBePresentInElementLocated extends AbstractConditionWaiter {

        @Override
        void addArgumentsOfWaiter() {
            addLocatorArguments();
            addArg(PN_Text, "Input text");
        }

        @Override
        public boolean isReachCondition(FluentWait<WebDriver> wait) {
            return wait.until(ExpectedConditions.textToBePresentInElementLocated(getLocatorFromRuntime(),
                    getRTArg(PN_Text)));
        }
    }

    public class TextToBePresentInElementValue extends AbstractConditionWaiter {

        @Override
        void addArgumentsOfWaiter() {
            addLocatorArguments();
            addArg(PN_Text, "");
        }

        @Override
        public boolean isReachCondition(FluentWait<WebDriver> wait) {
            return wait.until(ExpectedConditions.textToBePresentInElementValue(getLocatorFromRuntime(), getRTArg(PN_Text)));
        }
    }

    public class TitleContains extends AbstractConditionWaiter {

        @Override
        void addArgumentsOfWaiter() {
            addArg(PN_Title, "");
        }

        @Override
        public boolean isReachCondition(FluentWait<WebDriver> wait) {
            return wait.until(ExpectedConditions.titleContains(getRTArg(PN_Title)));
        }
    }

    public class TitleIs extends AbstractConditionWaiter {

        @Override
        void addArgumentsOfWaiter() {
            addArg(PN_Title, "");
        }

        @Override
        public boolean isReachCondition(FluentWait<WebDriver> wait) {
            return wait.until(ExpectedConditions.titleIs(getRTArg(PN_Title)));
        }
    }

    public class UrlContains extends AbstractConditionWaiter {

        @Override
        void addArgumentsOfWaiter() {
            addArg(PN_Text, "");
        }

        @Override
        public boolean isReachCondition(FluentWait<WebDriver> wait) {
            return wait.until(ExpectedConditions.urlContains(getRTArg(PN_Text)));
        }
    }

    public class UrlMatches extends AbstractConditionWaiter {

        @Override
        void addArgumentsOfWaiter() {
            addArg(PN_Regex, "");
        }

        @Override
        public boolean isReachCondition(FluentWait<WebDriver> wait) {
            return wait.until(ExpectedConditions.urlMatches(getRTArg(PN_Regex)));
        }
    }

    public class UrlToBe extends AbstractConditionWaiter {

        @Override
        void addArgumentsOfWaiter() {
            addArg(PN_Text, "");
        }

        @Override
        public boolean isReachCondition(FluentWait<WebDriver> wait) {
            return wait.until(ExpectedConditions.urlToBe(getRTArg(PN_Text)));
        }
    }
}
