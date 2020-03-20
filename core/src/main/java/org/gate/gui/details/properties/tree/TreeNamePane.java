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
package org.gate.gui.details.properties.tree;

import org.gate.gui.GuiPackage;
import org.gate.gui.common.OptionPane;
import org.gate.gui.tree.GateTree;
import org.gate.gui.tree.GateTreeElement;
import org.gate.gui.tree.GateTreeNode;
import org.gate.gui.tree.GateTreeSupport;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gate.gui.tree.action.ActionTree;
import org.gate.gui.tree.action.elements.ActionTreeElement;
import org.gate.gui.tree.test.TestTree;
import org.gate.gui.tree.test.elements.TestTreeElement;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TreeNamePane extends JPanel {
    Logger log = LogManager.getLogger(this.getName());
    JTextField nameTextField = new JTextField();
    JLabel nameLabel = new JLabel("component name: ");

    GateTreeNode treeNode = null;

    // TODO looks for JMeter code find how to resolve the TreeNode tools long won't display full name
    public TreeNamePane(String name) {
        setLayout(new GridLayout(1, 2));
        add(nameLabel);
        add(nameTextField);

        nameTextField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String nodeName = nameTextField.getText().trim();
                // valid the the tree node name
                if (!treeNode.isRoot()) {
                    if (nodeName.contains(",") || nodeName.contains(".") || nodeName.contains("{") || nodeName.contains("}")
                            || nodeName.contains("[") || nodeName.contains("]")) {
                        OptionPane.showErrorMessageDialog("Error: ", "character \",.{}[]\" should not used in tree node name");
                        return;
                    }
                    if (GateTreeSupport.findFirstChild(treeNode.getParentGateTreeNode(), nodeName) != null) {
                        OptionPane.showErrorMessageDialog("Error: ", "node name duplication: " + nodeName);
                        return;
                    }
                }
                GateTreeElement element = (GateTreeElement) treeNode.getUserObject();
                element.setName(nodeName);
                reloadTreeNode(treeNode);
            }
        });
        setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), name));
    }

    public void setNode(GateTreeNode node) {
        treeNode = node;
        nameLabel.setText("Component: " + treeNode.getUserObject().getClass().getSimpleName() + " Name: ");
        nameTextField.setText(treeNode.getUserObject().toString());
        reloadTreeNode(treeNode);
    }

    void reloadTreeNode(GateTreeNode treeNode) {
        if (ActionTreeElement.class.isInstance(treeNode.getGateTreeElement())) {
            GuiPackage.getIns().getActionTree().getTestTreeModel().reload(treeNode);
        }
        if (TestTreeElement.class.isInstance(treeNode.getGateTreeElement())) {
            GuiPackage.getIns().getTestTree().getTestTreeModel().reload(treeNode);
        }

    }

}

