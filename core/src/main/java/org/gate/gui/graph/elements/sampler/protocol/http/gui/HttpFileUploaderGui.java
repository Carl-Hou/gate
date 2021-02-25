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
package org.gate.gui.graph.elements.sampler.protocol.http.gui;

import com.mxgraph.model.mxCell;
import com.mxgraph.view.mxGraph;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gate.gui.GuiPackage;
import org.gate.gui.common.GuiUtils;
import org.gate.gui.common.TestElement;
import org.gate.gui.common.VerticalLayout;
import org.gate.gui.details.properties.ArgumentsPane;
import org.gate.gui.details.properties.PropertiesTable;
import org.gate.gui.details.properties.PropertiesTableModel;
import org.gate.gui.details.properties.graph.GraphElementPropertiesGui;
import org.gate.gui.details.properties.graph.GraphNamePane;
import org.gate.gui.graph.elements.GraphElement;
import org.gate.gui.graph.elements.sampler.protocol.http.HTTPConstantsInterface;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;


public class HttpFileUploaderGui extends JPanel implements GraphElementPropertiesGui, HTTPConstantsInterface {

    protected Logger log = LogManager.getLogger(this.getName());

    GraphNamePane namePane = new GraphNamePane();
    JTextField pathField = new JTextField();
    PropertiesTable defaultPropertiesTable = new PropertiesTable(new PropertiesTableModel());
    JComboBox methodsComboBox = new JComboBox(new String[]{POST});
    JComboBox protocolComboBox = new JComboBox( new String[]{"http", "https"});

    ArgumentsPane argumentsPane = new ArgumentsPane();
    GraphElement graphElement;

    public HttpFileUploaderGui(){
        setLayout(new VerticalLayout());

        methodsComboBox.setEditable(true);
        protocolComboBox.setEditable(true);

        add(namePane);
//        pathField.getDocument().addDocumentListener(new DocumentListener() {
//            @Override
//            public void insertUpdate(DocumentEvent e) {updateElement();}
//
//            @Override
//            public void removeUpdate(DocumentEvent e) {updateElement();}
//
//            @Override
//            public void changedUpdate(DocumentEvent e) {updateElement();}
//            void updateElement(){
//                graphElement.setProp(TestElement.NS_NAME, PN_Path, pathField.getText());
//            }
//        });
//        JPanel pathPanel = new JPanel();
//        pathPanel.setLayout(new BoxLayout(pathPanel, BoxLayout.LINE_AXIS));
//        pathPanel.add(new JLabel("Path: "));
//        pathPanel.add(pathField);
//        add(pathPanel);
        JSplitPane propertiesParameters = new JSplitPane();

        JPanel propertiesPanel = GuiUtils.getPanel("Request Properties :", new BorderLayout());
        propertiesPanel.add(defaultPropertiesTable, BorderLayout.CENTER);
        propertiesPanel.add(defaultPropertiesTable.getTableHeader(), BorderLayout.NORTH);

        propertiesParameters.setLeftComponent(propertiesPanel);
        JPanel parameterPanel = GuiUtils.getPanel("Request Parameters :", new BorderLayout());
        parameterPanel.add(argumentsPane, BorderLayout.CENTER);
        propertiesParameters.setRightComponent(parameterPanel);
        int divider = (int) (GuiPackage.getIns().getPropertiesPanel().getWidth() *0.4);
        propertiesParameters.setDividerLocation(divider);
        add(propertiesParameters);
    }

    @Override
    public void setCell(mxGraph graph, mxCell cell) {
        namePane.setCell(graph, cell);
        this.graphElement = namePane.getGraphElement();
        defaultPropertiesTable.setTestElement(graphElement);
        argumentsPane.setTestElement(graphElement);
        updateParamterTabbedPane();
    }

    void updateParamterTabbedPane() {
        defaultPropertiesTable.setComboBox(PN_Method, methodsComboBox);
        defaultPropertiesTable.setBooleanOnCell(PN_UseKeepAlive);
        defaultPropertiesTable.setComboBox(PN_Protocol,protocolComboBox);
    }

}
