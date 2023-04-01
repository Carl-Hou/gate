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
import org.gate.gui.graph.common.ParameterUtils;
import org.gate.gui.graph.elements.sampler.protocol.selenium.gui.SeleniumInteractionGui;
import org.gate.runtime.GateContextService;
import org.gate.varfuncs.property.GateProperty;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class SeleniumInteractions extends AbstractSeleniumSampler {

    public SeleniumInteractions() {
        addProp(NS_NAME, Interaction_Category, PN_IC_Navigation);
    }

    @Override
    public String getStaticLabel() {
        return "Selenium Interactions";
    }

    @Override
    public String getGUI() {
        return SeleniumInteractionGui.class.getName();
    }

    public List<String> getMethodSuppliers() {
        GateProperty p = getProp(NS_NAME, Interaction_Category);

        List<Class> allMethodClazz = super.getSuppliersClasses();
        List<String> methods = new LinkedList<>();
        if(p == null){
            for (Class methodClazz : allMethodClazz) {
                if (NavigateMethod.class.isAssignableFrom(methodClazz)) methods.add(methodClazz.getSimpleName());
            }
            return methods;
        }
        String category = p.getStringValue();
        for (Class methodClazz : allMethodClazz) {
            if(
                    category.equals(PN_IC_Alerts) && AlertMethod.class.isAssignableFrom(methodClazz)
                    || category.equals(PN_IC_Browser) && BrowserMethod.class.isAssignableFrom(methodClazz)
                    || category.equals(PN_IC_Navigation) && NavigateMethod.class.isAssignableFrom(methodClazz)
                    || category.equals(PN_IC_Windows) && WindowsMethod.class.isAssignableFrom(methodClazz)
                    || category.equals(PN_IC_Frames) && FramesMethod.class.isAssignableFrom(methodClazz)
            )
                methods.add(methodClazz.getSimpleName());

        }
        return methods;
    }


    abstract class InteractionMethod extends AbstractMethodSupplier {
        @Override
        public void addArguments() {
        }

        @Override
        public void run(ElementResult result) {
            WebDriver driver = getDriver(result);
            if (result.isFailure()) return;
            exec(driver, result);
        }

        abstract void exec(WebDriver driver, ElementResult result);
    }

    abstract class AlertMethod extends InteractionMethod {
    }

    abstract class BrowserMethod extends InteractionMethod {
    }

    abstract class NavigateMethod extends InteractionMethod {
    }

    abstract class WindowsMethod extends InteractionMethod {
    }

    abstract class FramesMethod extends InteractionMethod {
    }

    class Accept extends AlertMethod {
        @Override
        void exec(WebDriver driver, ElementResult result) {
            driver.switchTo().alert().accept();
        }
    }

    class Dismiss extends AlertMethod {
        @Override
        void exec(WebDriver driver, ElementResult result) {
            driver.switchTo().alert().dismiss();
        }
    }

    class GetText extends AlertMethod {
        final static String VN_Text = "variable_name_text";

        @Override
        public void addArguments() {
            addArg(VN_Text, "text");
        }

        @Override
        void exec(WebDriver driver, ElementResult result) {
            String text = driver.switchTo().alert().getText();
            setGateVariable(VN_Text, text);
        }
    }

    class SendKeys extends AlertMethod {
        final static String VN_KeysToSend = "keys_to_send";

        @Override
        public void addArguments() {
            addArg(VN_KeysToSend, "keys to send");
        }

        @Override
        void exec(WebDriver driver, ElementResult result) {
            driver.switchTo().alert().sendKeys(getRTArg(VN_KeysToSend));
        }
    }

    class GetCurrentUrl extends BrowserMethod {
        @Override
        public void addArguments() {
            addArg(PN_VariableName_ReturnValue, "");
        }

        @Override
        void exec(WebDriver driver, ElementResult result) {
            String currentURL = driver.getCurrentUrl();
            if (null == currentURL) {
                result.setFailure("Current url is null:");
                return;
            }
            setGateVariable(PN_VariableName_ReturnValue, currentURL);
        }
    }

    class GetTitle extends BrowserMethod {

        @Override
        public void addArguments() {
            addArg(PN_VariableName_ReturnValue, "");
        }

        @Override
        void exec(WebDriver driver, ElementResult result) {
            String title = driver.getTitle();
            setGateVariable(PN_VariableName_ReturnValue, title);
        }
    }

    class Quit extends BrowserMethod {

        @Override
        void exec(WebDriver driver, ElementResult result) {
            driver.quit();
        }
    }


    class GetPageSource extends BrowserMethod {

        public void addArguments() {
            addArg(PN_VariableName_ReturnValue, "");
        }

        @Override
        void exec(WebDriver driver, ElementResult result) {
            String pageSource = driver.getPageSource();
            if (null == pageSource) {
                result.setFailure("Current Page Source is null:");
                return;
            }
            setGateVariable(PN_VariableName_ReturnValue, pageSource);
        }
    }

    class Back extends NavigateMethod {
        @Override
        void exec(WebDriver driver, ElementResult result) {
            driver.navigate().back();
        }
    }

    class Forward extends NavigateMethod {
        @Override
        void exec(WebDriver driver, ElementResult result) {
            driver.navigate().forward();
        }
    }

    class Refresh extends NavigateMethod {
        @Override
        void exec(WebDriver driver, ElementResult result) {
            driver.navigate().refresh();
        }
    }

    class To extends NavigateMethod {
        @Override
        public void addArguments() {
            addArg(PN_DriverURL, "");
        }

        @Override
        void exec(WebDriver driver, ElementResult result) {
            driver.navigate().to(getRTArg(PN_DriverURL));
        }
    }

    class Get extends NavigateMethod {
        @Override
        public void addArguments() {
            addArg(PN_VariableName, "");
        }

        @Override
        void exec(WebDriver driver, ElementResult result) {
            driver.get(getRTArg(PN_VariableName));
        }
    }

    class GetWindowHandle extends WindowsMethod {

        @Override
        public void addArguments() {
            addArg(PN_VariableName_ReturnValue, "");
        }

        @Override
        void exec(WebDriver driver, ElementResult result) {
            String windowHandle = driver.getWindowHandle();
            setGateVariable(PN_VariableName_ReturnValue, windowHandle);
        }
    }

    class GetWindowHandles extends WindowsMethod {

        @Override
        public void addArguments() {
            addArg(PN_VariableNamePrefix_ReturnValue, "");
        }

        @Override
        void exec(WebDriver driver, ElementResult result) {
            Set<String> windowHandles = driver.getWindowHandles();
            GateContextService.getContext().getVariables().putObjects(getRTArg(PN_VariableNamePrefix_ReturnValue), windowHandles);
        }
    }

    class SwitchToWindow extends WindowsMethod {

        @Override
        public void addArguments() {
            addArg(PN_NameOrID, "");
        }

        @Override
        void exec(WebDriver driver, ElementResult result) {
            driver.switchTo().window(getRTArg(PN_NameOrID));
        }
    }

    class Close extends WindowsMethod {

        @Override
        void exec(WebDriver driver, ElementResult result) {
            driver.close();
        }
    }


    class FullScreen extends WindowsMethod {
        @Override
        void exec(WebDriver driver, ElementResult result) {
            driver.manage().window().fullscreen();
        }
    }

    class GetPosition extends WindowsMethod {
        final static String VN_X = "Variable Name X (Return Value)";
        final static String VN_Y = "Variable Name Y (Return Value)";

        @Override
        public void addArguments() {
            addArg(PN_Position_X_ReturnValue, "");
            addArg(PN_Position_Y_ReturnValue, "");
        }

        @Override
        void exec(WebDriver driver, ElementResult result) {

            Point point = driver.manage().window().getPosition();
            setGateVariable(VN_X, point.getX());
            setGateVariable(VN_Y, point.getY());
        }
    }

    class GetSize extends WindowsMethod {
        @Override
        public void addArguments() {
            addArg(PN_Size_Width_ReturnValue, "");
            addArg(PN_Size_Height_ReturnValue, "");
        }

        @Override
        void exec(WebDriver driver, ElementResult result) {
            Dimension dimension = driver.manage().window().getSize();
            setGateVariable(PN_Size_Width_ReturnValue, dimension.getWidth());
            setGateVariable(PN_Size_Width_ReturnValue, dimension.getHeight());
        }
    }

    class Maximize extends WindowsMethod {
        @Override
        void exec(WebDriver driver, ElementResult result) {
            driver.manage().window().maximize();
        }
    }

    class SetPosition extends WindowsMethod {
        @Override
        public void addArguments() {
            addArg(PN_Position_X, "");
            addArg(PN_Position_Y, "");
        }

        @Override
        void exec(WebDriver driver, ElementResult result) {
            int x = ParameterUtils.getInt(getRTArg(PN_Position_X), result);
            int y = ParameterUtils.getInt(getRTArg(PN_Position_Y), result);
            driver.manage().window().setPosition(new Point(x, y));
        }
    }

    class SetSize extends WindowsMethod {
        @Override
        public void addArguments() {
            addArg(PN_Size_Width, "");
            addArg(PN_Size_Height, "");
        }

        @Override
        void exec(WebDriver driver, ElementResult result) {
            int h = ParameterUtils.getInt(getRTArg(PN_Size_Width), result);
            int w = ParameterUtils.getInt(getRTArg(PN_Size_Height), result);
            driver.manage().window().setSize(new Dimension(w, h));
        }
    }

    class SwitchToFrameByIndex extends FramesMethod {
        @Override
        public void addArguments() {
            addArg(PN_Index, "");
        }

        @Override
        void exec(WebDriver driver, ElementResult result) {
            int i = ParameterUtils.getInt(getRTArg(PN_Index), result);

            driver.switchTo().frame(i);
        }
    }

    class SwitchToFrameByNameOrID extends FramesMethod {
        @Override
        public void addArguments() {
            addArg(PN_NameOrID, "");

        }

        @Override
        void exec(WebDriver driver, ElementResult result) {
            String nameOrID = getRTArg(PN_NameOrID);

            driver.switchTo().frame(nameOrID);
        }
    }

    class SwitchToFrameByElement extends FramesMethod {
        @Override
        public void addArguments() {
            addArg(PN_VariableName, "");

        }

        @Override
        void exec(WebDriver driver, ElementResult result) {
            WebElement element = (WebElement) GateContextService.getContext()
                    .getVariables().getObject(getRTArg(PN_VariableName));
            driver.switchTo().frame(element);
        }
    }

    class SwitchToDefaultContent extends FramesMethod {
        @Override
        void exec(WebDriver driver, ElementResult result) {
            driver.switchTo().defaultContent();
        }
    }

    class SwitchToParentFrame extends FramesMethod {
        @Override
        void exec(WebDriver driver, ElementResult result) {
            driver.switchTo().parentFrame();
        }
    }

}
