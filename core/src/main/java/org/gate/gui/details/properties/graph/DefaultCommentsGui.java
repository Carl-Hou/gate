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

import com.mxgraph.model.mxCell;
import com.mxgraph.view.mxGraph;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gate.gui.common.TestElement;
import org.gate.gui.common.VerticalLayout;
import org.gate.gui.details.properties.PropertiesTable;
import org.gate.gui.details.properties.PropertiesTableModel;
import org.gate.gui.graph.elements.comment.Comments;
import org.gate.gui.graph.elements.GraphElement;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class DefaultCommentsGui extends JPanel implements GraphElementPropertiesGui {

    final static Logger log = LogManager.getLogger();

    GraphNamePane namePane = new GraphNamePane();

    PropertiesTable defaultPropertiesTable = new PropertiesTable( new PropertiesTableModel());
    JTextArea rSyntaxTextArea = new JTextArea(15, 80);

    GraphElement element = null;

    public DefaultCommentsGui(){
        setLayout(new VerticalLayout());
        add(namePane);

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
                namePane.getGraphElement().setProp(TestElement.NS_TEXT, Comments.PN_Comments, rSyntaxTextArea.getText());
            }
        });
        add(rSyntaxTextArea);

    }

    @Override
    public void setCell(mxGraph graph, mxCell cell) {
        namePane.setCell(graph, cell);
        element = (GraphElement) cell.getValue();
        rSyntaxTextArea.setText(element.getProp(TestElement.NS_TEXT, Comments.PN_Comments).getStringValue());
        defaultPropertiesTable.setTestElement(element);
    }

}
