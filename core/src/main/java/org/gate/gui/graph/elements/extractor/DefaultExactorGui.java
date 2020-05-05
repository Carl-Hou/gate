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
package org.gate.gui.graph.elements.extractor;

import com.mxgraph.model.mxCell;
import com.mxgraph.view.mxGraph;
import org.gate.gui.GuiPackage;
import org.gate.gui.common.GuiUtils;
import org.gate.gui.common.VerticalLayout;
import org.gate.gui.details.properties.ArgumentsPane;
import org.gate.gui.details.properties.PropertiesTable;
import org.gate.gui.details.properties.PropertiesTableModel;

import org.gate.gui.details.properties.graph.GraphElementPropertiesGui;
import org.gate.gui.details.properties.graph.GraphNamePane;
import org.gate.gui.graph.elements.GraphElement;

import javax.swing.*;
import java.awt.*;

public class DefaultExactorGui extends JPanel implements GraphElementPropertiesGui {

    GraphNamePane namePane = new GraphNamePane();
    PropertiesTable defaultPropertiesTable = new PropertiesTable( new PropertiesTableModel());
    ArgumentsPane argumentsPane = new ArgumentsPane();

    public DefaultExactorGui(){
        setLayout(new VerticalLayout());
        add(namePane);

        JSplitPane propsPattern = new JSplitPane();

        JPanel tablePanel = GuiUtils.getPanel("Properties");
        tablePanel.setLayout(new BorderLayout());
        tablePanel.add(defaultPropertiesTable, BorderLayout.CENTER);
        tablePanel.add(defaultPropertiesTable.getTableHeader(), BorderLayout.NORTH);

        propsPattern.setLeftComponent(tablePanel);
        propsPattern.setRightComponent(argumentsPane);
        add(propsPattern);
        int divider = (int) (GuiPackage.getIns().getPropertiesPanel().getWidth() *0.3);
        propsPattern.setDividerLocation(divider);
    }

    @Override
    public void setCell(mxGraph graph, mxCell cell) {
        namePane.setCell(graph, cell);
        GraphElement element = (GraphElement) cell.getValue();
        defaultPropertiesTable.setTestElement(element);
        argumentsPane.setTestElement(element);
        updateTableEditors();
    }

    void updateTableEditors(){
        if(XPathExtractor.class.isInstance(namePane.getGraphElement())){
            defaultPropertiesTable.setBooleanOnCell(XPathExtractor.PN_ValidateXML);
            defaultPropertiesTable.setBooleanOnCell(XPathExtractor.PN_IgnoreWhiteSpace);
            defaultPropertiesTable.setBooleanOnCell(XPathExtractor.PN_Fragment);
        }
    }
}
