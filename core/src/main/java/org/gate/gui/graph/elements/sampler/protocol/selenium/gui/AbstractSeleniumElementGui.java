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

import org.gate.gui.common.OptionPane;
import org.gate.gui.common.TestElement;
import org.gate.gui.common.GuiUtils;
import org.gate.gui.details.properties.PropertiesTable;
import org.gate.gui.details.properties.PropertiesTableModel;
import org.gate.gui.details.properties.graph.AbstractGraphElementPanel;
import org.gate.gui.graph.elements.GraphElement;
import org.gate.gui.graph.elements.sampler.protocol.selenium.AbstractSeleniumSampler;
import org.gate.gui.graph.elements.sampler.protocol.selenium.SeleniumElement;
import org.gate.gui.graph.elements.sampler.protocol.selenium.SeleniumUtils;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
// this will be removed.
public abstract class AbstractSeleniumElementGui extends AbstractGraphElementPanel {

    PropertiesTable defaultPropertiesTable = new PropertiesTable(new PropertiesTableModel());
    PropertiesTable argumentPropertiesTable = new PropertiesTable(new PropertiesTableModel());

    JComboBox methodSuppliesComboBox = new JComboBox();
    SeleniumElement seleniumElement = null;

    public AbstractSeleniumElementGui(){

        methodSuppliesComboBox.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                String selectedMethodName = (String) methodSuppliesComboBox.getSelectedItem();
                seleniumElement.updateByMethodSupplier(selectedMethodName);
                onMethodSupplierSelected(selectedMethodName, seleniumElement);
                argumentPropertiesTable.setTestElement(seleniumElement, TestElement.NS_ARGUMENT);
                updateTableEditors();
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
            }
        });

        add(SeleniumUtils.getMethodSupplierPanel("Method: ", methodSuppliesComboBox));

        add(GuiUtils.getPanel(getDefaultProperitesName(),defaultPropertiesTable));
        add(GuiUtils.getPanel(getArgumentProperitesName(), argumentPropertiesTable));
    }

    protected String getDefaultProperitesName(){
        return "Element Parameters:";
    }

    protected String getArgumentProperitesName(){
        return "Method Arguments:";
    }

    @Override
    protected void setTestElement(GraphElement element) {
        seleniumElement = (SeleniumElement) element;
        methodSuppliesComboBox.removeAllItems();
        log.info(seleniumElement.getClass().getName());
        seleniumElement.getMethodSuppliers().forEach(name -> {
            methodSuppliesComboBox.addItem(name);
        });
        methodSuppliesComboBox.setSelectedItem(seleniumElement.getCurrentMethodSupplier());
        defaultPropertiesTable.setTestElement(element);
        argumentPropertiesTable.setTestElement(element, TestElement.NS_ARGUMENT);
        updateTableEditors();
    }

    void updateTableEditors(){
        try {
            defaultPropertiesTable.constraintNotEmpty(AbstractSeleniumSampler.PN_DriverId);
            applyTableEditors();
        }catch (Throwable t){
            t.printStackTrace();
            OptionPane.showErrorMessageDialog(t);
        }
    }

    void onMethodSupplierSelected(String methodSupplierName, SeleniumElement element){

    }

    PropertiesTable getDefaultPropertiesTable(){
        return defaultPropertiesTable;
    }

    PropertiesTable getArgumentPropertiesTable(){
        return argumentPropertiesTable;
    }

    abstract void applyTableEditors();
}
