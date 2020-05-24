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

import org.gate.common.util.GateUtils;
import org.gate.gui.GuiPackage;
import org.gate.gui.common.OptionPane;
import org.gate.gui.actions.ActionRouter;


import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;

public abstract class GateTreePopupMenuFactory {

    protected Logger log = LogManager.getLogger(this);

    private final static String COPY = "Copy";
    private final static String PASTE = "Paste";
    private final static String Remove = "Remove";

    protected abstract void appendPopupMenuItems(JPopupMenu jPopupMenu, GateTreeNode selectedNode);

    protected abstract GateTree getTestTree();
    protected abstract HashMap<String, LinkedList<MenuInfo>> getMenuMap();
    protected abstract boolean canAddTo(GateTreeNode target, GateTreeNode[] nodes);

    public JPopupMenu createPopupMenu(GateTreeNode selectedTreeNode) {
        JPopupMenu jPopupMenu = new JPopupMenu();
        appendPopupMenuItems(jPopupMenu, selectedTreeNode);
        return jPopupMenu;
    }

    protected void appendDefaultItems(JPopupMenu jPopupMenu, GateTreeNode selectedNode){
        appendCopyElementMenu(jPopupMenu, selectedNode);
        appendPasteElementMenu(jPopupMenu, selectedNode);
        appendRemoveElementMenu(jPopupMenu, selectedNode);
    }

    protected JMenu makeMenu(String category, String actionCommand) {
        JMenu addMenu = new JMenu(category);
        for (MenuInfo info : getMenuMap().get(category)) {
            addMenu.add(makeMenuItem(info, actionCommand));
        }
        return addMenu;
    }

    protected Component makeMenuItem(MenuInfo info, String actionCommand) {
        JMenuItem newMenuChoice = new JMenuItem(info.getLabel());
        newMenuChoice.setName(info.getClassName());
        newMenuChoice.addActionListener(ActionRouter.getInstance());
        if (actionCommand != null) {
            newMenuChoice.setActionCommand(actionCommand);
        }
        return newMenuChoice;
    }

    protected JMenuItem makeMenuItem(String label, String name, String actionCommand) {
        JMenuItem newMenuChoice = new JMenuItem(label);
        newMenuChoice.setName(name);
        newMenuChoice.addActionListener(ActionRouter.getInstance());
        if (actionCommand != null) {
            newMenuChoice.setActionCommand(actionCommand);
        }
        return newMenuChoice;
    }

    protected void appendCopyElementMenu(JPopupMenu jPopupMenu, GateTreeNode selectedTreeNode){

        JMenuItem CopyTreeElementMenuItem = new JMenuItem(COPY);
        CopyTreeElementMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GateTreeNode[] nodes = getSelectedNodes(selectedTreeNode);
                nodes = keepOnlyAncestors(nodes);
                nodes = cloneTreeNodes(nodes);
                setCopiedNodes(nodes);
            }
        });
        jPopupMenu.add(CopyTreeElementMenuItem);
    }

    protected void appendPasteElementMenu(JPopupMenu jPopupMenu, GateTreeNode selectedTreeNode){

        JMenuItem pasteTreeElementMenuItem = new JMenuItem(PASTE);
        pasteTreeElementMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GateTreeNode[] draggedNodes = getCopiedNodes();
                if (draggedNodes == null) {
                    Toolkit.getDefaultToolkit().beep();
                    return;
                }
                if(canAddTo(selectedTreeNode, draggedNodes)){
                    for(GateTreeNode draggedNode : draggedNodes){
                        if(draggedNode != null){
                            getTestTree().addNode(selectedTreeNode, draggedNode);
                        }
                    }
                }
            }
        });
        jPopupMenu.add(pasteTreeElementMenuItem);
    }

    protected void appendRemoveElementMenu(JPopupMenu jPopupMenu, GateTreeNode selectedTreeNode){
        JMenuItem removeTreeElementsMenuItem = new JMenuItem(Remove);
        removeTreeElementsMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(!OptionPane.showConfirmMessageDialog("Confirm Remove?", "Are you sure you want remove the selected element(s)?")){
                    return;
                }
                GuiPackage.getIns().getMainFrame().closeModelEditor();
                GateTreeNode[] nodes = getSelectedNodes(selectedTreeNode);
                // no delete option on test suites node. should have not null value
                TreePath parent = getTestTree().getSelectionPath().getParentPath();
                for (int i = nodes.length - 1; i >= 0; i--) {
                    getTestTree().getTestTreeModel().removeNodeFromParent(nodes[i]);
                }
                getTestTree().setSelectionPath(parent);
            }
        });
        jPopupMenu.add(removeTreeElementsMenuItem);
    }

    protected GateTreeNode[] getSelectedNodes(GateTreeNode selectedTreeNode) {
        TreePath[] paths = getTestTree().getSelectionPaths();
        if (paths == null) {
            return new GateTreeNode[] { selectedTreeNode };
        }
        GateTreeNode[] nodes = new GateTreeNode[paths.length];
        for (int i = 0; i < paths.length; i++) {
            nodes[i] = (GateTreeNode) paths[i].getLastPathComponent();
        }
        return nodes;
    }

    protected GateTreeNode cloneTreeNode(GateTreeNode node) {
        GateTreeNode treeNode = (GateTreeNode) node.clone();
        treeNode.setUserObject(GateUtils.deepCopy(treeNode.getGateTreeElement()).get());
        cloneChildren(treeNode, node);
        return treeNode;
    }

    /**
     * If a child and one of its ancestors are selected : only keep the ancestor
     * @param currentNodes GateTreeNode[]
     * @return GateTreeNode[]
     */
    protected GateTreeNode[] keepOnlyAncestors(GateTreeNode[] currentNodes) {
        List<GateTreeNode> nodes = new ArrayList<>();
        for (int i = 0; i < currentNodes.length; i++) {
            boolean exclude = false;
            for (int j = 0; j < currentNodes.length; j++) {
                if(i!=j && currentNodes[i].isNodeAncestor(currentNodes[j])) {
                    exclude = true;
                    break;
                }
            }
            if(!exclude) {
                nodes.add(currentNodes[i]);
            }
        }
        return nodes.toArray(new GateTreeNode[nodes.size()]);
    }
    // Gui only method
    protected void setCopiedNodes(GateTreeNode[] nodes) {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        try {
            GateTreeNodeTransferable transferable = new GateTreeNodeTransferable();
            transferable.setTransferData(nodes);
            clipboard.setContents(transferable, null);
        } catch (Exception ex) {
            log.error("Clipboard node read error:" + ex.getMessage(), ex);
            OptionPane.showErrorMessageDialog("clipboard_node_read_error: \n", ex);
        }
    }
    // Gui only method
    protected GateTreeNode[] getCopiedNodes() {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        if (clipboard.isDataFlavorAvailable(GateTreeNodeTransferable.Gate_TREE_NODE_ARRAY_DATA_FLAVOR)) {
            try {
                return (GateTreeNode[]) clipboard.getData(GateTreeNodeTransferable.Gate_TREE_NODE_ARRAY_DATA_FLAVOR);
            } catch (Exception ex) {
                log.error("Clipboard node read error:" + ex.getMessage(), ex);
                OptionPane.showErrorMessageDialog("clipboard_node_read_error: \n", ex);
            }
        }
        return null;
    }

    protected GateTreeNode[] cloneTreeNodes(GateTreeNode[] nodes) {
        GateTreeNode[] treeNodes = new GateTreeNode[nodes.length];
        for (int i = 0; i < nodes.length; i++) {
            treeNodes[i] = cloneTreeNode(nodes[i]);
        }
        return treeNodes;
    }

    protected void cloneChildren(GateTreeNode to, GateTreeNode from) {
        Enumeration<?> enumFrom = from.children();
        while (enumFrom.hasMoreElements()) {
            GateTreeNode child = (GateTreeNode) enumFrom.nextElement();
            GateTreeNode childClone = (GateTreeNode) child.clone();
            childClone.setUserObject(GateUtils.deepCopy(child.getGateTreeElement()).get());
            to.add(childClone);
            cloneChildren((GateTreeNode) to.getLastChild(), child);
        }
    }

}
