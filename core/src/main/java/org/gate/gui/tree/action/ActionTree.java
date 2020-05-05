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
package org.gate.gui.tree.action;

import org.gate.gui.common.GuiUtils;
import org.gate.gui.tree.GateTree;
import org.gate.gui.tree.GateTreeNode;
import org.gate.gui.tree.action.elements.Action;
import org.gate.gui.tree.action.elements.ActionSuite;
import org.gate.gui.tree.action.elements.ActionSuites;
import org.gate.gui.tree.action.elements.ActionTreeElement;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;

public class ActionTree extends GateTree {

    private static final ImageIcon imageTestSuites
            = GuiUtils.getImage("/org/gate/images/applications-science-3.png");
    private static final ImageIcon imageTestSuite
            =  GuiUtils.getImage("/org/gate/images/view-list-tree-4.png");
    private static final ImageIcon imageTestCase
            =  GuiUtils.getImage("/org/gate/images/system-run-5.png");

    private ActionTreePopUpMenuFactory popUpMenuFactory = new ActionTreePopUpMenuFactory();

    public ActionTree(){
        addGateElement(getTestTreeModel().getTestTreeRoot(), new ActionSuites());
        getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        setCellRenderer(new ActionTreeNodeRender());
    }

    public GateTreeNode getActionSuites(){
        return (GateTreeNode) getTestTreeModel().getTestTreeRoot().getFirstChild();
    }

    public void reset(){
        getTestTreeModel().getTestTreeRoot().removeAllChildren();
        addGateElement(getTestTreeModel().getTestTreeRoot(), new ActionSuites());
        getTestTreeModel().reload();
    }

    @Override
    public JPopupMenu getPopupMenu(GateTreeNode node) {
        return popUpMenuFactory.createPopupMenu(node);
    }

    private static class ActionTreeNodeRender extends DefaultTreeCellRenderer {
        private static final long serialVersionUID = 4159626601097711565L;
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                      boolean sel, boolean expanded, boolean leaf, int row, boolean focus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, focus);
            GateTreeNode gateTreeNode = (GateTreeNode) value;
            if(!ActionTreeElement.class.isInstance(gateTreeNode.getGateTreeElement())){
                return this;
            }

            ActionTreeElement actionTreeElement = (ActionTreeElement) gateTreeNode.getGateTreeElement();

            if(ActionSuites.class.isInstance(actionTreeElement)){
                setIcon(imageTestSuites);
            } else if (ActionSuite.class.isInstance(actionTreeElement)){
                setIcon(imageTestSuite);
            } else if (Action.class.isInstance(actionTreeElement)){
                setIcon(imageTestCase);
            }
            return this;
        }

    }

}
