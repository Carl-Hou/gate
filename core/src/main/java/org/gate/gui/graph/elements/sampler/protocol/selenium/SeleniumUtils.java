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

import org.gate.common.util.GateRuntimeExcepiton;
import org.gate.gui.graph.elements.sampler.protocol.selenium.util.SeleniumConstantsInterface;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;

import javax.swing.*;
import java.awt.*;
import java.time.Duration;
import java.util.Arrays;

public class SeleniumUtils implements SeleniumConstantsInterface {

    public static WebElement getWebElement(Wait<WebDriver> wait, String expectedCondition, By by) {
        switch (expectedCondition) {
            case "elementToBeClickable":
                return wait.until(ExpectedConditions.elementToBeClickable(by));
            case "presenceOfElementLocated":
                return wait.until(ExpectedConditions.presenceOfElementLocated(by));
            case "visibilityOfElementLocated":
                return wait.until(ExpectedConditions.visibilityOfElementLocated(by));
        }
        throw new GateRuntimeExcepiton("expectedCondition is not in support list");
    }

    public static FluentWait<WebDriver> getWait(WebDriver driver, String waitTimeOut, String waitFrequency) {
        FluentWait<WebDriver> fluentWait = new FluentWait(driver).ignoring(NoSuchElementException.class);
        fluentWait.withTimeout(Duration.ofSeconds(Integer.parseUnsignedInt(waitTimeOut)));
        fluentWait.pollingEvery(Duration.ofMillis(Long.parseUnsignedLong(waitFrequency)));
        return fluentWait;
    }

    public static WebElement getWebElementByWait(FluentWait<WebDriver> wait, String expectedCondition, By locator) {
        switch (expectedCondition) {
            case "ElementToBeClickable":
                return wait.until(ExpectedConditions.elementToBeClickable(locator));
            case "presenceOfElementLocated":
                return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
            case "visibilityOfElementLocated":
                return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        }
        throw new GateRuntimeExcepiton("expectedCondition is not in support list");
    }

    public static By getLocator(String locatorType, String locatorCondition) {
        switch (locatorType) {
            case Locator_ClassName:
                return By.className(locatorCondition);
            case Locator_CssSelector:
                return By.cssSelector(locatorCondition);
            case Locator_Id:
                return By.id(locatorCondition);
            case Locator_LinkText:
                return By.linkText(locatorCondition);
            case Locator_Name:
                return By.name(locatorCondition);
            case Locator_PartialLinkText:
                return By.partialLinkText(locatorCondition);
            case Locator_TagName:
                return By.tagName(locatorCondition);
            case Locator_XPath:
                return By.xpath(locatorCondition);
        }
        // Should never been here.
        throw new GateRuntimeExcepiton("locatorType no in supported list. should be one of:" + Arrays.toString(LocatorTypes));
    }

    public static JPanel getMethodSupplierPanel(String name, JComboBox comboBox) {
        JPanel executorPanel = new JPanel(new GridLayout(1, 2));
        executorPanel.add(new JLabel(name));
        executorPanel.add(comboBox);
        return executorPanel;
    }
}
