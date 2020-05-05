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
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.gate.gui.GuiPackage;
import org.gate.gui.common.GuiUtils;
import org.gate.gui.common.TestElement;
import org.gate.gui.common.VerticalLayout;
import org.gate.gui.details.properties.PropertiesTable;
import org.gate.gui.details.properties.PropertiesTableModel;
import org.gate.gui.details.properties.graph.GraphElementPropertiesGui;
import org.gate.gui.details.properties.graph.GraphNamePane;
import org.gate.gui.graph.elements.GraphElement;
import org.gate.gui.graph.elements.sampler.JSR223Sampler;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;

public class CodeSnippetGui extends JPanel implements GraphElementPropertiesGui {

    Logger log = LogManager.getLogger(this.getName());

    GraphNamePane namePane = new GraphNamePane();

    PropertiesTable defaultPropertiesTable = new PropertiesTable( new PropertiesTableModel());
    RSyntaxTextArea rSyntaxTextArea = GuiUtils.getRSyntaxTextArea(15, 80);

    GraphElement element = null;

    public CodeSnippetGui(){

        setLayout(new VerticalLayout());
        add(namePane);

        JSplitPane propsScript = new JSplitPane();

        JPanel leftPanel = new JPanel(new VerticalLayout());
        JPanel tablePanel = GuiUtils.getPanel("Script Properties");
        tablePanel.setLayout(new BorderLayout());
        tablePanel.add(defaultPropertiesTable, BorderLayout.CENTER);
        tablePanel.add(defaultPropertiesTable.getTableHeader(), BorderLayout.NORTH);


        leftPanel.add(tablePanel);
        propsScript.setLeftComponent(leftPanel);
        rSyntaxTextArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { updateScript();}
            @Override
            public void removeUpdate(DocumentEvent e) {
                updateScript();
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                updateScript();
            }
            void updateScript(){
                namePane.getGraphElement().setProp(TestElement.NS_TEXT, JSR223Sampler.PN_Script, rSyntaxTextArea.getText());
            }
        });
        propsScript.setRightComponent(GuiUtils.getRTextScrollPane(rSyntaxTextArea));
        add(propsScript);

        int divider = (int) (GuiPackage.getIns().getPropertiesPanel().getWidth() *0.3);
        propsScript.setDividerLocation(divider);
    }

    @Override
    public void setCell(mxGraph graph, mxCell cell) {
        namePane.setCell(graph, cell);
        element = (GraphElement) cell.getValue();
        rSyntaxTextArea.setText(element.getProp(TestElement.NS_TEXT, JSR223Sampler.PN_Script).getStringValue());
        defaultPropertiesTable.setTestElement(element);

    }

}
