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

package org.gate.gui.tree;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gate.gui.GuiPackage;
import org.gate.gui.common.OptionPane;

import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class GateTreeListener implements TreeSelectionListener, MouseListener {
    Logger log = LogManager.getLogger(this.getClass());

    @Override
    public void valueChanged(TreeSelectionEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        GateTree tree = (GateTree) e.getSource();
        int rowLocation = tree.getRowForLocation(e.getX(), e.getY());
        if (rowLocation == -1) {
            return;
        }
        TreePath treepath = tree.getPathForRow(rowLocation);
        GateTreeNode selectedTreeNode = (GateTreeNode) treepath.getLastPathComponent();


//      TODO: write code on click the tree node here.
        try {
            GateTreeSupport.updatePropertiesPanel(selectedTreeNode);
            GuiPackage.getIns().getMainFrame().activeDetailsTabProperties();
        } catch (Exception ex) {
            ex.printStackTrace();
            OptionPane.showErrorMessageDialog("Fatal Error:", ex);
        }
//      TODO: open model in graph model editor. How to support edit more than one model?
        if (isDoubleClick(e)) {
            if (ModelContainer.class.isInstance(selectedTreeNode.getGateTreeElement())) {
                ModelContainer modelContainer = (ModelContainer) selectedTreeNode.getGateTreeElement();
                GuiPackage.getIns().getMainFrame().openModelEditor(e.getSource().getClass().getSimpleName(),
                        GateTreeSupport.getGateTreeNodePath(selectedTreeNode), modelContainer.getMxModel());
                GuiPackage.getIns().getMainFrame().setCurrentModePath(treepath);

            }
            return;
        }

        if (isRightClick(e)) {
            if (tree.getSelectionCount() < 2) {
                tree.setSelectionPath(tree.getPathForLocation(e.getX(), e.getY()));
            }
            log.debug("About to display pop-up");
            tree.getPopupMenu(selectedTreeNode).show(e.getComponent(), e.getX(), e.getY());
        }
    }

    private boolean isRightClick(MouseEvent e) {
        return e.isPopupTrigger() || (InputEvent.BUTTON2_MASK & e.getModifiers()) > 0 || (InputEvent.BUTTON3_MASK == e.getModifiers());
    }

    private boolean isDoubleClick(MouseEvent e) {
        if (e.getClickCount() >= 2) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

}
