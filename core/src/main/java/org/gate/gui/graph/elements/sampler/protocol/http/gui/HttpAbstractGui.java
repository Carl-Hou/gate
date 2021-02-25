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
import org.gate.common.config.GateProps;
import org.gate.gui.GuiPackage;
import org.gate.gui.common.GuiUtils;
import org.gate.gui.common.OptionPane;
import org.gate.gui.common.TestElement;
import org.gate.gui.common.VerticalLayout;
import org.gate.gui.details.properties.ArgumentsPane;
import org.gate.gui.details.properties.PropertiesTable;
import org.gate.gui.details.properties.PropertiesTableModel;
import org.gate.gui.details.properties.graph.GraphElementPropertiesGui;
import org.gate.gui.details.properties.graph.GraphNamePane;
import org.gate.gui.graph.elements.GraphElement;
import org.gate.gui.graph.elements.sampler.protocol.http.HTTPConstantsInterface;
import org.gate.varfuncs.property.GateProperty;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.LinkedList;

public abstract class HttpAbstractGui extends JPanel implements GraphElementPropertiesGui, HTTPConstantsInterface {

    protected Logger log = LogManager.getLogger(this.getName());

    protected final static String Parameters = "Parameters";
    protected final static String BodyData = "Body Data";
    protected final static int ParametersIndex = 0;
    protected final static int BodyDataIndex = 1;

    private JComboBox methodsComboBox = new JComboBox(new String[]{
            POST ,GET ,PUT ,HEAD ,TRACE ,OPTIONS ,DELETE ,PATCH});

    private JComboBox protocolComboBox = new JComboBox( new String[]{"http", "https"});

    private GraphNamePane namePane = new GraphNamePane();

    private PropertiesTable defaultPropertiesTable = new PropertiesTable(new PropertiesTableModel());
    private JTabbedPane parametersTabbedPane = new JTabbedPane();
    private ArgumentsPane argumentsPane = new ArgumentsPane();
    private JTextArea bodyDataTextArea = new JTextArea();

    private GraphElement graphElement;

    public HttpAbstractGui(){
        setLayout(new VerticalLayout());
        add(namePane);
        methodsComboBox.setEditable(true);
        protocolComboBox.setEditable(true);
    }

    JSplitPane buildRequestPropertiesAndParametersPanel(){
        int divider = (int) (GuiPackage.getIns().getPropertiesPanel().getWidth() *0.4);
        return buildRequestPropertiesAndParametersPanel(divider);
    }

    JSplitPane buildRequestPropertiesAndParametersPanel(int divider){
        JSplitPane propertiesParameters = new JSplitPane();
        JPanel propertiesPanel = GuiUtils.getPanel("Request Properties :", new BorderLayout());
        propertiesPanel.add(defaultPropertiesTable, BorderLayout.CENTER);
        propertiesPanel.add(defaultPropertiesTable.getTableHeader(), BorderLayout.NORTH);

        propertiesParameters.setLeftComponent(propertiesPanel);

        bodyDataTextArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateText();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                updateText();
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                updateText();
            }
            void updateText(){
                String text = bodyDataTextArea.getText();
                if(text.isEmpty()){
                    graphElement.getProps(TestElement.NS_ARGUMENT).clear();
                }else{
                    graphElement.putProp(TestElement.NS_ARGUMENT, "", bodyDataTextArea.getText());
                }
            }
        });
        parametersTabbedPane.add(Parameters, argumentsPane);
        parametersTabbedPane.add(BodyData, bodyDataTextArea);
        parametersTabbedPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int selectedIndex = parametersTabbedPane.getSelectedIndex();
                if(selectedIndex == BodyDataIndex){
                    if(graphElement.getProps(TestElement.NS_ARGUMENT).size() ==0){
                        return;
                    }
                    if(!isBodyData()){
                        OptionPane.showErrorMessageDialog("Warrning",
                                "Can not convert parameters to Body Data" + GateProps.LineSeparator
                                        + "because one of the parameters have a name");
                        parametersTabbedPane.setSelectedIndex(ParametersIndex);
                        argumentsPane.setTestElement(graphElement);
                        return;
                    }else if(graphElement.getProps(TestElement.NS_ARGUMENT).size() ==0){
                        bodyDataTextArea.setText("");
                    }
                    bodyDataTextArea.setText(graphElement.getProps(TestElement.NS_ARGUMENT).getFirst().getStringValue());
                }else{
                    argumentsPane.setTestElement(graphElement);
                }

            }
        });
        propertiesParameters.setRightComponent(parametersTabbedPane);
        propertiesParameters.setDividerLocation(divider);
        return propertiesParameters;
    }

    boolean isBodyData(){
        LinkedList<GateProperty> arguments = graphElement.getProps(TestElement.NS_ARGUMENT);
        if(arguments.size() == 1 && arguments.getFirst().getName().isEmpty()){
            return true;
        }
        return false;
    }

    @Override
    public void setCell(mxGraph graph, mxCell cell) {
        namePane.setCell(graph, cell);
        this.graphElement = namePane.getGraphElement();
        defaultPropertiesTable.setTestElement(graphElement);
        argumentsPane.setTestElement(graphElement);
        updateParameterTabbedPane();

        setGraphElement(graphElement);
    }

    abstract void setGraphElement(GraphElement graphElement);

    void updateParameterTabbedPane() {
        if (isBodyData()) {
            bodyDataTextArea.setText(graphElement.getProps(TestElement.NS_ARGUMENT).getFirst().getStringValue());
            parametersTabbedPane.setSelectedIndex(BodyDataIndex);
        }else{
            bodyDataTextArea.setText("");
            parametersTabbedPane.setSelectedIndex(ParametersIndex);
        }

        defaultPropertiesTable.setComboBox(PN_Protocol, protocolComboBox);
        defaultPropertiesTable.setComboBox(PN_Method, methodsComboBox);
        defaultPropertiesTable.setBooleanOnCell(PN_UseKeepAlive);
    }

}
