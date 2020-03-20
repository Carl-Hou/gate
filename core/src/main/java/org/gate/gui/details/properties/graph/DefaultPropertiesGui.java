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
package org.gate.gui.details.properties.graph;

import org.gate.gui.common.GuiUtils;
import org.gate.gui.details.properties.PropertiesTable;
import org.gate.gui.details.properties.PropertiesTableModel;
import org.gate.gui.graph.elements.GraphElement;

import javax.swing.*;
import java.awt.*;

public class DefaultPropertiesGui extends AbstractGraphElementPanel {

    JPanel tablePanel = new JPanel(new BorderLayout());
    protected PropertiesTable defaultPropertiesTable = new PropertiesTable(new PropertiesTableModel());

    public DefaultPropertiesGui() {
        tablePanel.add(defaultPropertiesTable, BorderLayout.CENTER);
        tablePanel.add(defaultPropertiesTable.getTableHeader(), BorderLayout.NORTH);
        add(tablePanel);
    }

    public void setBorderName(String name){
        tablePanel.setBorder(GuiUtils.getBorder(name));
    }

    @Override
    protected void setTestElement(GraphElement element) {
        defaultPropertiesTable.setTestElement(element);
        updateTableEditors();
    }

    protected void updateTableEditors() {

    }
}
