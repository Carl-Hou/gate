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
package org.gate.gui.tree.test.elements.config.gui;

import org.gate.gui.GuiPackage;
import org.gate.gui.common.GuiUtils;
import org.gate.gui.details.properties.ArgumentsPane;
import org.gate.gui.details.properties.PropertiesTable;
import org.gate.gui.details.properties.PropertiesTableModel;
import org.gate.gui.details.properties.tree.AbstractTreeElementPanel;
import org.gate.gui.tree.GateTreeElement;
import org.gate.gui.tree.test.elements.TestTreeElement;

import javax.swing.*;
import java.awt.*;

public class DefaultTreeConfigGui extends AbstractTreeElementPanel {

    PropertiesTable defaultPropertiesTable = new PropertiesTable( new PropertiesTableModel());
    ArgumentsPane argumentsPane = new ArgumentsPane("Arguments");

    public DefaultTreeConfigGui(){

        JSplitPane propertiesAndArguments = new JSplitPane();
        JPanel tablePanel = GuiUtils.getPanel("Properties");
        tablePanel.setLayout(new BorderLayout());
        tablePanel.add(defaultPropertiesTable, BorderLayout.CENTER);
        tablePanel.add(defaultPropertiesTable.getTableHeader(), BorderLayout.NORTH);

        propertiesAndArguments.setLeftComponent(tablePanel);
        propertiesAndArguments.setRightComponent(argumentsPane);

        add(propertiesAndArguments);
        int divider = (int) (GuiPackage.getIns().getPropertiesPanel().getWidth() *0.3);
        propertiesAndArguments.setDividerLocation(divider);


    }

    @Override
    protected void setTestElement(GateTreeElement element) {
        argumentsPane.setTestElement(element);
        defaultPropertiesTable.setTestElement(element);
        defaultPropertiesTable.constraintReadOnly(TestTreeElement.PN_ENABLE);
    }
}
