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
package org.gate.gui.graph.elements.config;

import com.mxgraph.model.mxCell;
import com.mxgraph.view.mxGraph;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gate.gui.GuiPackage;
import org.gate.gui.common.GuiUtils;
import org.gate.gui.common.OptionPane;
import org.gate.gui.common.TestElement;
import org.gate.gui.common.VerticalLayout;
import org.gate.gui.details.properties.PropertiesTable;
import org.gate.gui.details.properties.PropertiesTableModel;
import org.gate.gui.details.properties.graph.GraphElementPropertiesGui;
import org.gate.gui.details.properties.graph.GraphNamePane;
import org.gate.gui.graph.common.AbstractJComboBoxListener;
import org.gate.gui.graph.elements.GraphElement;
import org.gate.gui.graph.elements.sampler.protocol.selenium.util.SeleniumElementInterface;
import org.gate.gui.graph.elements.sampler.protocol.selenium.util.SeleniumConstantsInterface;

import javax.swing.*;
import java.awt.*;


public class SeleniumDriverConfigGui extends JPanel implements GraphElementPropertiesGui, SeleniumConstantsInterface {

    protected Logger log = LogManager.getLogger(this.getName());

    GraphNamePane namePane = new GraphNamePane();
    PropertiesTable defaultPropertiesTable = new PropertiesTable(new PropertiesTableModel());
    PropertiesTable argumentPropertiesTable = new PropertiesTable (new PropertiesTableModel());

    JComboBox methodSuppliesComboBox = new JComboBox();
    JComboBox browserNamesComboBox = new JComboBox(BrowserNames);

    SeleniumElementInterface seleniumElement = null;

    public SeleniumDriverConfigGui(){
        setLayout(new VerticalLayout());
        // to use Selenium default config
        browserNamesComboBox.setEditable(true);
        browserNamesComboBox.setSelectedItem("");
        add(namePane);

        JSplitPane elementMethod = new JSplitPane();

        JPanel methodPanel = GuiUtils.getPanel("Create Method:", new VerticalLayout());
        JPanel methodSelectPanel = GuiUtils.getPanel("Name:", new GridLayout(1, 1));

        methodSuppliesComboBox.addPopupMenuListener(new AbstractJComboBoxListener() {
            @Override
            protected void onItemSelected(String selectedMethodName) {
                seleniumElement.updateByMethodSupplier(selectedMethodName);
                onMethodSupplierSelected(selectedMethodName, seleniumElement);
                argumentPropertiesTable.setTestElement(seleniumElement, TestElement.NS_ARGUMENT);
                updateTableEditors();
            }
        });
        methodSelectPanel.add(methodSuppliesComboBox);
        methodPanel.add(methodSelectPanel);
        JPanel methodArgumentsPanel = GuiUtils.getPanel("Arguments:", new VerticalLayout());
        methodArgumentsPanel.add(argumentPropertiesTable.getTableHeader());
        methodArgumentsPanel.add(argumentPropertiesTable);
        methodPanel.add(methodArgumentsPanel);

        JPanel settingArgumentPanel = GuiUtils.getPanel("Settings:");
        settingArgumentPanel.setLayout(new VerticalLayout());
        settingArgumentPanel.add(defaultPropertiesTable.getTableHeader());
        settingArgumentPanel.add(defaultPropertiesTable);

        elementMethod.setRightComponent(settingArgumentPanel);
        elementMethod.setLeftComponent(methodPanel);
        add(elementMethod);

        int divider = (int) (GuiPackage.getIns().getPropertiesPanel().getWidth() *0.4);
        elementMethod.setDividerLocation(divider);
    }

    // keep this for extension
    void onMethodSupplierSelected(String methodSupplierName, SeleniumElementInterface element){

    }

    @Override
    public void setCell(mxGraph graph, mxCell cell) {
        namePane.setCell(graph, cell);
        GraphElement element = (GraphElement) cell.getValue();
        setTestElement(element);
    }

    protected void setTestElement(GraphElement element) {
        seleniumElement = (SeleniumElementInterface) element;
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
            applyTableEditors();
        }catch (Throwable t){
            log.error("Fail to update Gui", t);
            OptionPane.showErrorMessageDialog(t);
        }
    }

    void applyTableEditors() {
        argumentPropertiesTable.setComboBox(PN_BrowserName, browserNamesComboBox);
        defaultPropertiesTable.setBooleanOnCell(PN_CloseBrowserAfterTest);
    }
}
