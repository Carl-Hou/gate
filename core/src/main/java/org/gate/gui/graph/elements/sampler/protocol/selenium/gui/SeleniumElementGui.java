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
package org.gate.gui.graph.elements.sampler.protocol.selenium.gui;

import org.gate.gui.common.GuiUtils;
import org.gate.gui.common.TestElement;
import org.gate.gui.common.VerticalLayout;
import org.gate.gui.details.properties.graph.GraphElementPropertiesGui;
import org.gate.gui.graph.common.AbstractJComboBoxListener;
import org.gate.gui.graph.elements.sampler.protocol.selenium.util.SeleniumElementInterface;
import org.gate.gui.graph.elements.sampler.protocol.selenium.util.SeleniumConstantsInterface;

import javax.swing.*;
import java.awt.*;


public class SeleniumElementGui extends AbstractSeleniumElementGui implements GraphElementPropertiesGui, SeleniumConstantsInterface {

    JComboBox  categoryComboBox = new JComboBox(Element_Categories);
    JPanel operationCategoryPane = GuiUtils.getPanel("Operation Category", new GridLayout(1, 1));

    JComboBox elementInputTypeComboBox;

    public SeleniumElementGui(){
        // Operation CateGory
        operationCategoryPane.add(categoryComboBox);
        topPanel.add(operationCategoryPane);
        categoryComboBox.addPopupMenuListener(new AbstractJComboBoxListener() {
            @Override
            protected void onItemSelected(String itemName) {
                seleniumElement.setProp(TestElement.NS_NAME, Element_Category, itemName);
                methodSuppliesComboBox.removeAllItems();
                seleniumElement.getMethodSuppliers().forEach(m -> {
                    methodSuppliesComboBox.addItem(m);
                });
            }
        });
    }

    // this is called by super
    JPanel getLeftPanel() {
        // This method called by constructor of super.
        // useVariableComboBox need to be init here.
        elementInputTypeComboBox = createElementInputTypeComboBox();
        elementInputTypeComboBox.addPopupMenuListener(new AbstractJComboBoxListener() {
            @Override
            protected void onItemSelected(String itemName) {
                seleniumElement.setProp(TestElement.NS_NAME, ElementInputType, itemName);
                seleniumElement.getProps(TestElement.NS_DEFAULT).clear();
                seleniumElement.putProp(TestElement.NS_DEFAULT, PN_DriverId, "");
                onInputSelected(itemName);
                methodSuppliesComboBox.removeAllItems();
                seleniumElement.getMethodSuppliers().forEach(m -> {
                    methodSuppliesComboBox.addItem(m);
                });
                String currentMethodSupplier = (String) methodSuppliesComboBox.getSelectedItem();
                seleniumElement.updateByMethodSupplier(currentMethodSupplier);
                argumentPropertiesTable.setTestElement(seleniumElement, TestElement.NS_ARGUMENT);
                defaultPropertiesTable.setTestElement(seleniumElement);
                updateTableEditors();
            }
        });
        JPanel samplerArgumentPanel = GuiUtils.getPanel("Input:", new VerticalLayout());
        samplerArgumentPanel.setLayout(new VerticalLayout());
        samplerArgumentPanel.add(elementInputTypeComboBox);
        samplerArgumentPanel.add(defaultPropertiesTable.getTableHeader());
        samplerArgumentPanel.add(defaultPropertiesTable);
        return samplerArgumentPanel;
    }

    JComboBox createElementInputTypeComboBox() {
        return new JComboBox(ElementInputTypes);
    }

    void onInputSelected(String itemName) {
        if (itemName.equals(ElementInputType_Variable)) {
            seleniumElement.putProp(TestElement.NS_DEFAULT, PN_VariableName, "");
        } else if (itemName.equals(ElementInputType_Locator)) {
            seleniumElement.putProp(TestElement.NS_DEFAULT, PN_LocatorType, "");
            seleniumElement.putProp(TestElement.NS_DEFAULT, PN_LocatorCondition, "");
        } else if (itemName.equals(ElementInputType_Driver)) {
            // current, nothing to do here
        }
    }

    @Override
    protected void beforeSetTestElement(SeleniumElementInterface seleniumElement){
        String operationCategory =
                seleniumElement.getProp(TestElement.NS_NAME, Element_Category).getStringValue();
        categoryComboBox.setSelectedItem(operationCategory);

        String elementInputType =
                seleniumElement.getProp(TestElement.NS_NAME, ElementInputType).getStringValue();
        elementInputTypeComboBox.setSelectedItem(elementInputType);
        argumentPropertiesTable.setTestElement(seleniumElement);
    }

//    @Override
//    protected void setTestElement(SeleniumElementInterface element) {
//
//        methodSuppliesComboBox.removeAllItems();
//        seleniumElement.getMethodSuppliers().forEach(name -> {
//            methodSuppliesComboBox.addItem(name);
//        });
//        methodSuppliesComboBox.setSelectedItem(seleniumElement.getCurrentMethodSupplier());
//        defaultPropertiesTable.setTestElement(element);
//        argumentPropertiesTable.setTestElement(element, TestElement.NS_ARGUMENT);
//        updateTableEditors();
//    }

}
