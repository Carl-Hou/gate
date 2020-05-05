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
import org.gate.common.util.GateClassUtils;
import org.gate.gui.common.OptionPane;
import org.gate.gui.graph.elements.GraphElement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GraphNamePane extends JPanel {
    Logger log = LogManager.getLogger(this.getName());
    JTextField nameTextField = new JTextField();
    JLabel label = new JLabel("component name: ");

    mxGraph graph = null;
    mxCell cell = null;

    public GraphNamePane(){
        init();
        setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Name"));
    }

    public GraphNamePane(String borderName){
        init();
        setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),borderName));
    }

    void init(){
        setLayout(new GridLayout(1,2));
        add(label);
        add(nameTextField);
        nameTextField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getGraphElement().setName(nameTextField.getText().trim());
                graph.cellLabelChanged(cell, cell.getValue(), false);
            }
        });
    }

    public GraphElement getGraphElement(){
        return (GraphElement) cell.getValue();
    }

    public void setCell(mxGraph graph, mxCell cell){
        GraphElement graphElement = ((GraphElement)cell.getValue());
        String elementCategory = GateClassUtils.getIns().getGraphElementCategory(graphElement);
        if(elementCategory == null){
            OptionPane.showErrorMessageDialog("Error", "Graph element is not in a supported category");
        }
        StringBuffer cellName = new StringBuffer(" Category: ");
        cellName.append(elementCategory).append(" Type: ");
        cellName.append(graphElement.getStaticLabel());
        cellName.trimToSize();
        label.setText(cellName.toString());
        this.nameTextField.setText(cell.getValue().toString());
        this.graph = graph;
        this.cell  = cell;

    }

}

