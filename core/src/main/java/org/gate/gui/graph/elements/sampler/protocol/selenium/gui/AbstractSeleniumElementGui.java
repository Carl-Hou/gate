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

import com.mxgraph.model.mxCell;
import com.mxgraph.view.mxGraph;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gate.gui.GuiPackage;
import org.gate.gui.common.OptionPane;
import org.gate.gui.common.GuiUtils;
import org.gate.gui.common.TestElement;
import org.gate.gui.common.VerticalLayout;
import org.gate.gui.details.properties.PropertiesTable;
import org.gate.gui.details.properties.PropertiesTableModel;
import org.gate.gui.details.properties.graph.GraphElementPropertiesGui;
import org.gate.gui.details.properties.graph.GraphNamePane;
import org.gate.gui.graph.common.AbstractJComboBoxListener;
import org.gate.gui.graph.elements.sampler.protocol.selenium.SeleniumElement;
import org.gate.gui.graph.elements.sampler.protocol.selenium.util.SeleniumConstantsInterface;
import org.gate.gui.graph.elements.sampler.protocol.selenium.util.SeleniumElementInterface;
import org.gate.gui.graph.elements.sampler.protocol.selenium.util.SeleniumUtils;

import javax.swing.*;
import java.awt.*;


public abstract class AbstractSeleniumElementGui extends JPanel implements GraphElementPropertiesGui, SeleniumConstantsInterface {

    Logger log = LogManager.getLogger(this.getName());

    JPanel topPanel = new JPanel(new GridLayout(1,1));
    GraphNamePane namePane = new GraphNamePane();
    PropertiesTable defaultPropertiesTable = new PropertiesTable(new PropertiesTableModel());
    PropertiesTable argumentPropertiesTable = new PropertiesTable(new PropertiesTableModel());

    JComboBox locatorTypeComboBox = new JComboBox(SeleniumUtils.LocatorTypes);
    JComboBox methodSuppliesComboBox = new JComboBox();


    SeleniumElementInterface seleniumElement = null;

    public AbstractSeleniumElementGui(){
        setLayout(new VerticalLayout());
        topPanel.add(namePane);
        add(topPanel);

        JSplitPane elementMethod = new JSplitPane();

        JPanel methodPanel = GuiUtils.getPanel("Operation:", new VerticalLayout());

        methodSuppliesComboBox.addPopupMenuListener(new AbstractJComboBoxListener() {
            @Override
            protected void onItemSelected(String selectedMethodName) {
                seleniumElement.updateByMethodSupplier(selectedMethodName);
                onMethodSupplierSelected(selectedMethodName, seleniumElement);
                argumentPropertiesTable.setTestElement(seleniumElement, TestElement.NS_ARGUMENT);
                updateTableEditors();
            }
        });
        methodPanel.add(methodSuppliesComboBox);
        methodPanel.add(argumentPropertiesTable.getTableHeader());
        methodPanel.add(argumentPropertiesTable);

        elementMethod.setRightComponent(methodPanel);
        elementMethod.setLeftComponent(getLeftPanel());
        add(elementMethod);

        int divider = (int) (GuiPackage.getIns().getPropertiesPanel().getWidth() *0.4);
        elementMethod.setDividerLocation(divider);
    }



    JPanel getLeftPanel(){
        JPanel samplerArgumentPanel = GuiUtils.getPanel("Driver:");
        samplerArgumentPanel.setLayout(new VerticalLayout());
        samplerArgumentPanel.add(defaultPropertiesTable.getTableHeader());
        samplerArgumentPanel.add(defaultPropertiesTable);
        return samplerArgumentPanel;
    }

    // keep this for extension
    void onMethodSupplierSelected(String methodSupplierName, SeleniumElementInterface element){

    }

    GraphNamePane getNamePane(){
        return namePane;
    }

    @Override
    public void setCell(mxGraph graph, mxCell cell) {
        namePane.setCell(graph, cell);
        seleniumElement = (SeleniumElementInterface) cell.getValue();
        log.info(seleniumElement.getClass().getName());
        setTestElement(seleniumElement);
        updateTableEditors();
    }

    protected void setTestElement(SeleniumElementInterface element) {
        beforeSetTestElement(element);
        methodSuppliesComboBox.removeAllItems();
        seleniumElement.getMethodSuppliers().forEach(name -> {
            methodSuppliesComboBox.addItem(name);
        });
        methodSuppliesComboBox.setSelectedItem(seleniumElement.getCurrentMethodSupplier());
        defaultPropertiesTable.setTestElement(element);
        argumentPropertiesTable.setTestElement(element, TestElement.NS_ARGUMENT);
        afterSetTestElement(element);
    }

    protected void beforeSetTestElement(SeleniumElementInterface seleniumElement){}

    protected void afterSetTestElement(SeleniumElementInterface seleniumElement){}


    void updateTableEditors(){
        try {
            applyTableEditors();
        }catch (Throwable t){
            log.error("Fail to update Gui", t);
            OptionPane.showErrorMessageDialog(t);
        }
    }

    void applyTableEditors() {
        defaultPropertiesTable.setComboBox(PN_LocatorType, locatorTypeComboBox);
        argumentPropertiesTable.setComboBox(PN_LocatorType, locatorTypeComboBox);
        argumentPropertiesTable.setBooleanOnCell(PN_Selected);
    }

}
