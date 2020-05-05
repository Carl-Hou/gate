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
package org.gate.gui.tree.test;

import org.gate.gui.common.GuiUtils;
import org.gate.gui.tree.*;
import org.gate.gui.tree.test.elements.TestCase;
import org.gate.gui.tree.test.elements.TestSuite;
import org.gate.gui.tree.test.elements.TestSuites;
import org.gate.gui.tree.test.elements.TestTreeElement;
import org.gate.gui.tree.test.elements.config.ConfigElement;
import org.gate.gui.tree.test.elements.dataprovider.DataProviderElement;
import org.gate.gui.tree.test.elements.fixture.SetUp;
import org.gate.gui.tree.test.elements.fixture.TearDown;

import javax.swing.*;

import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;

public class TestTree extends GateTree {

    private static final ImageIcon imageTestSuites
            = GuiUtils.getImage("/org/gate/images/applications-science-3.png");
    private static final ImageIcon imageTestSuite
            =  GuiUtils.getImage("/org/gate/images/view-list-tree-4.png");
    private static final ImageIcon imageTestCase
            =  GuiUtils.getImage("/org/gate/images/system-run-5.png");
    private static final ImageIcon imageConfig
            =  GuiUtils.getImage("/org/gate/images/preferences-system-4.png");
    private static final ImageIcon imageDataProvider
            =  GuiUtils.getImage("/org/gate/images/preferences-system-4.png");
    private static final ImageIcon imageSetup
            =  GuiUtils.getImage("/org/gate/images/document-import-2_custom.png");
    private static final ImageIcon imageTeardown
            =  GuiUtils.getImage("/org/gate/images/document-export-4_custom.png");


    private TestTreePopUpMenuFactory popUpMenuFactory = new TestTreePopUpMenuFactory();

    public TestTree(){
        addGateElement(getTestTreeModel().getTestTreeRoot(), new TestSuites());
        getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        // DnD support
        setDragEnabled(true);
        setDropMode(DropMode.ON_OR_INSERT);
        setTransferHandler(new TestTreeTransferHandler());
        setCellRenderer(new TestTreeNodeRender());
    }

    public GateTreeNode getTestSuitesNode(){
        return (GateTreeNode) getTestTreeModel().getTestTreeRoot().getFirstChild();
    }

    public void reset(){
        getTestTreeModel().getTestTreeRoot().removeAllChildren();
        addGateElement(getTestTreeModel().getTestTreeRoot(), new TestSuites());
        getTestTreeModel().reload();
    }

    @Override
    public JPopupMenu getPopupMenu(GateTreeNode node) {
        return popUpMenuFactory.createPopupMenu(node);
    }

    private static class TestTreeNodeRender extends DefaultTreeCellRenderer {
        private static final long serialVersionUID = 4159626601097711565L;
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                      boolean sel, boolean expanded, boolean leaf, int row, boolean focus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, focus);
            GateTreeNode testTreeNode = (GateTreeNode) value;
            if(!TestTreeElement.class.isInstance(testTreeNode.getGateTreeElement())){
                return this;
            }

            TestTreeElement testTreeElement = (TestTreeElement) testTreeNode.getGateTreeElement();
            boolean enabled = testTreeElement.isEnable();

            if(TestSuites.class.isInstance(testTreeElement)){
                setIcon(imageTestSuites, enabled);
            } else if (TestSuite.class.isInstance(testTreeElement)){
                setIcon(imageTestSuite, enabled);
            } else if (TestCase.class.isInstance(testTreeElement)){
                setIcon(imageTestCase, enabled);
            } else if (SetUp.class.isInstance(testTreeElement)){
                setIcon(imageSetup, enabled);
            } else if (TearDown.class.isInstance(testTreeElement)){
                setIcon(imageTeardown, enabled);
            } else if (ConfigElement.class.isAssignableFrom(testTreeElement.getClass())){
                setIcon(imageConfig, enabled);
            } else if (DataProviderElement.class.isAssignableFrom(testTreeElement.getClass())){
                setIcon(imageDataProvider, enabled);
            }

            this.setEnabled(enabled);
            return this;
        }

        void setIcon(ImageIcon ic, boolean enabled){
                if (enabled) {
                    this.setIcon(ic);
                } else {
                    this.setDisabledIcon(ic);
                }

        }
    }


}
