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


public class SeleniumExplicitWaitGui extends AbstractSeleniumElementGui implements GraphElementPropertiesGui, SeleniumConstantsInterface {
    JComboBox explicitWaitTypeComboBox;

    JPanel getLeftPanel() {
        // This method called by constructor of super.
        // useVariableComboBox need to be init here.
        explicitWaitTypeComboBox = new JComboBox(ExplicitWaitTypes);
        explicitWaitTypeComboBox.addPopupMenuListener(new AbstractJComboBoxListener() {
            @Override
            protected void onItemSelected(String itemName) {
                seleniumElement.setProp(TestElement.NS_NAME, ExplicitWaitType, itemName);
                seleniumElement.getProps(TestElement.NS_DEFAULT).clear();
                seleniumElement.putProp(TestElement.NS_DEFAULT, PN_DriverId, "");
                seleniumElement.putProp(TestElement.NS_DEFAULT, PN_ExplicitWaitTimeOut, "");
                seleniumElement.putProp(TestElement.NS_DEFAULT, PN_ExplicitWaitPollingInterval, "");

                if (itemName.equals(ExplicitWait_Condition)) {
                    // do nothing currently
                } else if (itemName.equals(ExplicitWait_Element)) {
                    seleniumElement.putProp(TestElement.NS_DEFAULT, PN_LocatorType, "");
                    seleniumElement.putProp(TestElement.NS_DEFAULT, PN_LocatorCondition, "");
                }
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
        samplerArgumentPanel.add(explicitWaitTypeComboBox);
        samplerArgumentPanel.add(defaultPropertiesTable.getTableHeader());
        samplerArgumentPanel.add(defaultPropertiesTable);
        return samplerArgumentPanel;
    }

    @Override
    protected void setTestElement(SeleniumElementInterface element) {
        String explicitWaitType =
                element.getProp(TestElement.NS_NAME, ExplicitWaitType).getStringValue();
        explicitWaitTypeComboBox.setSelectedItem(explicitWaitType);
        argumentPropertiesTable.setTestElement(element);
        methodSuppliesComboBox.removeAllItems();
        seleniumElement.getMethodSuppliers().forEach(name -> {
            methodSuppliesComboBox.addItem(name);
        });
        methodSuppliesComboBox.setSelectedItem(seleniumElement.getCurrentMethodSupplier());
        defaultPropertiesTable.setTestElement(element);
        argumentPropertiesTable.setTestElement(element, TestElement.NS_ARGUMENT);
    }

    void applyTableEditors() {
        defaultPropertiesTable.setComboBox(PN_LocatorType, locatorTypeComboBox);
        argumentPropertiesTable.setComboBox(PN_LocatorType, locatorTypeComboBox);
        argumentPropertiesTable.setBooleanOnCell(PN_Selected);
    }


}
