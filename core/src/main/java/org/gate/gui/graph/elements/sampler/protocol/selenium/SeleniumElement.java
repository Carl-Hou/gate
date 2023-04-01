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
import org.gate.gui.graph.elements.sampler.protocol.selenium.gui.SeleniumElementGui;
import org.gate.gui.graph.elements.sampler.protocol.selenium.util.SeleniumConstantsInterface;
import org.gate.gui.graph.elements.sampler.protocol.selenium.util.SeleniumUtils;
import org.gate.runtime.GateContextService;
import org.gate.varfuncs.property.GateProperty;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.Select;

import java.util.LinkedList;
import java.util.List;

public class SeleniumElement extends AbstractSeleniumElement implements SeleniumConstantsInterface {

    public SeleniumElement(){
        addProp(NS_NAME, Element_Category, PN_EC_Element);
    }

    @Override
    public String getStaticLabel() {
        return "Selenium Element";
    }

    @Override
    public String getGUI() {
        return SeleniumElementGui.class.getName();
    }

    public List<String> getMethodSuppliers() {
        GateProperty input = getProp(NS_NAME, ElementInputType);
        GateProperty operationCategory = getProp(NS_NAME, Element_Category);
        if(input == null || operationCategory == null){
            return super.getMethodSuppliers();
        }
        List<String> methods = new LinkedList<>();
        getOperations(input.getStringValue(), operationCategory.getStringValue())
                .forEach( e -> methods.add(e.getSimpleName()));

        return methods;
//        if(p != null && ElementInputType_Driver.equals(p.getStringValue())){
//            LinkedList<String> suppliers = new LinkedList<>();
//            suppliers.add(FindElement.class.getSimpleName());
//            suppliers.add(FindElements.class.getSimpleName());
//            return suppliers;
//        }else{
//            return super.getMethodSuppliers();
//        }
    }

    List<Class> getOperations(String input, String operationCategory){

        List<Class> methodClazz = super.getSuppliersClasses();
        // filter by Input
        if(input.equals(ElementInputType_Driver)){
            methodClazz.removeIf( next ->
                !AbstractDriverMethod.class.isAssignableFrom(next)
            );
        }
        // Filter by operation category
        if(operationCategory.equals(PN_EC_Element)){
            methodClazz.removeIf( next ->
                    AbstractSelectMethod.class.isAssignableFrom(next)
            );
        }else{
            methodClazz.removeIf( next ->
                    !AbstractSelectMethod.class.isAssignableFrom(next)
            );
        }

        return methodClazz;
    }


    abstract class AbstractDriverMethod extends AbstractSeleniumElementMethod {
    }

    abstract class AbstractElementMethod extends AbstractSeleniumElementMethod {
    }

    abstract class AbstractSelectMethod extends AbstractSeleniumElementMethod {
    }

    /*
        find element from an element.
        find from body tag will be same as driver.findElement
     */
    class FindElement extends AbstractDriverMethod {
        @Override
        public void addArguments(){
            addArg(PN_LocatorType, SeleniumUtils.LocatorTypes[0]);
            addArg(PN_LocatorCondition, "");
            addArg(PN_VariableName_ReturnValue, "");
        }
        @Override
        void action(WebDriver driver, WebElement element, ElementResult result) {
            String inputType = getProp(NS_NAME, ElementInputType).getStringValue();
            WebElement e;
            By locator = SeleniumUtils.getLocator(getRTArg(PN_LocatorType), getRTArg(PN_LocatorCondition));
            if(ElementInputType_Driver.equals(inputType)){
                e = driver.findElement(locator);
            }else{
                e = element.findElement(locator);
            }
            GateContextService.getContext().getVariables().putObject(getRTArg(PN_VariableName_ReturnValue), e);
        }
    }
    /*
        find elements from an element.
        find from body tag will be same as driver.findElements
     */
    class FindElements extends AbstractDriverMethod {
        @Override
        public void addArguments(){
            addArg(PN_LocatorType, SeleniumUtils.LocatorTypes[0]);
            addArg(PN_LocatorCondition, "");
            addArg(PN_VariableNamePrefix_ReturnValue, "");
        }
        @Override
        void action(WebDriver driver, WebElement element, ElementResult result) {
            String inputType = getProp(NS_NAME, ElementInputType).getStringValue();
            List<WebElement> elements;
            By locator = SeleniumUtils.getLocator(getRTArg(PN_LocatorType), getRTArg(PN_LocatorCondition));
            if(ElementInputType_Driver.equals(inputType)){
                elements = driver.findElements(locator);
            }else{
                elements = element.findElements(locator);
            }
            GateContextService.getContext().getVariables().putObjects(getRTArg(PN_VariableNamePrefix_ReturnValue), elements);
        }
    }

    class Clear extends AbstractElementMethod {
        @Override
        void action(WebDriver driver, WebElement element, ElementResult result) {
            element.clear();
        }
    }

    class Click extends AbstractElementMethod {
        @Override
        void action(WebDriver driver, WebElement element, ElementResult result) {
            element.click();
        }
    }

    class Submit extends AbstractElementMethod {

        @Override
        void action(WebDriver driver, WebElement element, ElementResult result) {
            element.submit();
        }
    }

    class SendKeys extends AbstractElementMethod {
        final static String VN_Keys = "Keys";
        @Override
        public void addArguments(){
            addArg(VN_Keys, "");
        }
        @Override
        void action(WebDriver driver, WebElement element, ElementResult result) {
            element.sendKeys(getRTArg(VN_Keys));
        }
    }

    class GetAttribute extends AbstractElementMethod {
        final static String VN_AttributeName = "Attribute Name";
        final static String VN_AttributeValueName = "Variable Name";
        @Override
        public void addArguments(){
            addArg(VN_AttributeName, "");
            addArg(VN_AttributeValueName, "");
        }
        @Override
        void action(WebDriver driver, WebElement element, ElementResult result) {
            String attribute = element.getAttribute(getRTArg(VN_AttributeName));
            setGateVariable(VN_AttributeValueName, attribute);
        }
    }

    class GetCssValue extends AbstractElementMethod {
        final static String VN_PropertyName = "Property Name";
        final static String VN_CssValueName = "Variable Name";
        @Override
        public void addArguments(){
            addArg(VN_PropertyName, "");
            addArg(VN_CssValueName, "");
        }
        @Override
        void action(WebDriver driver, WebElement element, ElementResult result) {
            String cssValue = element.getCssValue(getRTArg(VN_PropertyName));
            setGateVariable(VN_CssValueName, cssValue);
        }
    }

    class GetLocation extends AbstractElementMethod {
        @Override
        public void addArguments(){
            addArg(PN_Position_X, "");
            addArg(PN_Position_Y, "");
        }
        @Override
        void action(WebDriver driver, WebElement element, ElementResult result) {
            Point point = element.getLocation();
            setGateVariable(PN_Position_X, point.getX());
            setGateVariable(PN_Position_Y, point.getY());
        }
    }

    class GetRect extends AbstractElementMethod {

        @Override
        public void addArguments(){
            addArg(PN_Position_X_ReturnValue, "Rectangle width");
            addArg(PN_Position_Y_ReturnValue, "Rectangle height");
            addArg(PN_Size_Height_ReturnValue, "");
            addArg(PN_Size_Width_ReturnValue, "");
        }

        @Override
        void action(WebDriver driver, WebElement element, ElementResult result) {
            Rectangle rect = element.getRect();
            setGateVariable(PN_Position_X_ReturnValue, rect.getX());
            setGateVariable(PN_Position_Y_ReturnValue, rect.getY());
            setGateVariable(PN_Size_Height_ReturnValue, rect.getWidth());
            setGateVariable(PN_Size_Width_ReturnValue, rect.getHeight());
        }
    }

    class GetSize extends AbstractElementMethod {

        @Override
        public void addArguments(){
            addArg(PN_Size_Height_ReturnValue, "");
            addArg(PN_Size_Width_ReturnValue, "");
        }

        @Override
        void action(WebDriver driver, WebElement element, ElementResult result) {
            Dimension dimension = element.getSize();
            setGateVariable(PN_Size_Height_ReturnValue, dimension.getWidth());
            setGateVariable(PN_Size_Width_ReturnValue, dimension.getHeight());
        }
    }

    class GetTagName extends AbstractElementMethod {

        @Override
        public void addArguments(){
            addArg(PN_VariableName_ReturnValue, "");
        }

        @Override
        void action(WebDriver driver, WebElement element, ElementResult result) {
            String tagName = element.getTagName();
            setGateVariable(PN_VariableName_ReturnValue, tagName);
        }
    }

    class GetText extends AbstractElementMethod {
        @Override
        public void addArguments(){
            addArg(PN_VariableName_ReturnValue, "");
        }
        @Override
        void action(WebDriver driver, WebElement element, ElementResult result) {
            String text = element.getText();
            setGateVariable(PN_VariableName_ReturnValue, text);
        }
    }

    class IsDisplayed extends AbstractElementMethod {
        @Override
        public void addArguments(){
            addArg(PN_VariableName_ReturnValue, "");
        }
        @Override
        void action(WebDriver driver, WebElement element, ElementResult result) {
            boolean isDisplayed = element.isDisplayed();
            setGateVariable(PN_VariableName_ReturnValue, isDisplayed);
            if(!isDisplayed){
                result.setFailure("Element is not displayed");
            }
        }
    }

    class IsEnabled extends AbstractElementMethod {
        @Override
        public void addArguments(){
            addArg(PN_VariableName_ReturnValue, "");
        }
        @Override
        void action(WebDriver driver, WebElement element, ElementResult result) {
            boolean isEnabled = element.isEnabled();
            setGateVariable(PN_VariableName_ReturnValue, isEnabled);
            if(!isEnabled){
                result.setFailure("Element is not enabled");
            }
        }
    }

    class IsSelected extends AbstractElementMethod {
        @Override
        public void addArguments(){
            addArg(PN_VariableName_ReturnValue, "");
        }
        @Override
        void action(WebDriver driver, WebElement element, ElementResult result) {
            boolean isSelected = element.isSelected();
            setGateVariable(PN_VariableName_ReturnValue, isSelected);
            if(!isSelected){
                result.setFailure("Element is not selected");
            }
        }
    }

    class DeselectAll extends AbstractSelectMethod {
        @Override
        void action(WebDriver driver, WebElement element, ElementResult result) {
            Select select = new Select(element);
            select.deselectAll();
        }
    }

    class DeselectByIndex extends AbstractSelectMethod {
        @Override
        public void addArguments(){
            addArg(PN_VariableName, "");
        }
        @Override
        void action(WebDriver driver, WebElement element, ElementResult result) {
            Select select = new Select(element);
            int index = Integer.parseInt(getRTArg(PN_VariableName));
            select.deselectByIndex(index);
        }
    }

    class DeselectByValue extends AbstractSelectMethod {
        @Override
        public void addArguments(){
            addArg(PN_VariableName, "");
        }
        @Override
        void action(WebDriver driver, WebElement element, ElementResult result) {
            Select select = new Select(element);

            select.deselectByValue(getRTArg(PN_VariableName));
        }
    }

    class DeselectByVisibleText extends AbstractSelectMethod {
        @Override
        public void addArguments(){
            addArg(PN_VariableName, "");
        }
        @Override
        void action(WebDriver driver, WebElement element, ElementResult result) {
            Select select = new Select(element);

            select.deselectByVisibleText(getRTArg(PN_VariableName));
        }
    }

    class GetAllSelectedOptions extends AbstractSelectMethod {
        @Override
        public void addArguments(){
            addArg(PN_VariableNamePrefix_ReturnValue, "");
        }

        @Override
        void action(WebDriver driver, WebElement element, ElementResult result) {
            Select select = new Select(element);
            List<WebElement> elements = select.getAllSelectedOptions();
            GateContextService.getContext().getVariables().putObjects(
                    getRTArg(PN_VariableNamePrefix_ReturnValue), elements);
        }
    }

    class GetOptions extends AbstractSelectMethod {
        @Override
        public void addArguments(){
            addArg(PN_VariableNamePrefix_ReturnValue, "");
        }

        @Override
        void action(WebDriver driver, WebElement element, ElementResult result) {
            Select select = new Select(element);
            List<WebElement> elements = select.getOptions();
            GateContextService.getContext().getVariables().putObjects(
                    getRTArg(PN_VariableNamePrefix_ReturnValue), elements);
        }
    }

    class GetFirstSelectedOption extends AbstractSelectMethod {
        @Override
        public void addArguments(){
            addArg(PN_VariableNamePrefix_ReturnValue, "");
        }

        @Override
        void action(WebDriver driver, WebElement element, ElementResult result) {
            Select select = new Select(element);
            WebElement e = select.getFirstSelectedOption();
            GateContextService.getContext().getVariables().putObject(
                    getRTArg(PN_VariableNamePrefix_ReturnValue), e);
        }
    }

    class IsMultiple extends AbstractSelectMethod {
        @Override
        public void addArguments(){
            addArg(PN_VariableName_ReturnValue, "");
        }

        @Override
        void action(WebDriver driver, WebElement element, ElementResult result) {
            Select select = new Select(element);
            boolean isMultiple = select.isMultiple();
            GateContextService.getContext().getVariables().put(
                    getRTArg(PN_VariableName_ReturnValue), String.valueOf(isMultiple));
        }
    }

    class SelectByIndex extends AbstractSelectMethod {
        @Override
        public void addArguments(){
            addArg(PN_VariableName, "");
        }

        @Override
        void action(WebDriver driver, WebElement element, ElementResult result) {
            Select select = new Select(element);
            int index= Integer.parseInt(getRTArg(PN_VariableName));
            select.selectByIndex(index);
        }
    }

    class SelectByValue extends AbstractSelectMethod {
        @Override
        public void addArguments(){
            addArg(PN_VariableName, "");
        }

        @Override
        void action(WebDriver driver, WebElement element, ElementResult result) {
            Select select = new Select(element);
            select.selectByValue(getRTArg(PN_VariableName));
        }
    }

    class SelectByVisibleText extends AbstractSelectMethod {
        @Override
        public void addArguments(){
            addArg(PN_VariableName, "");
        }

        @Override
        void action(WebDriver driver, WebElement element, ElementResult result) {
            Select select = new Select(element);
            select.selectByVisibleText(getRTArg(PN_VariableName));
        }
    }

}
