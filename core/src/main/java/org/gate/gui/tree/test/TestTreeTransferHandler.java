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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.*;
import java.util.stream.Stream;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gate.gui.GuiPackage;
import org.gate.gui.tree.GateTreeModel;
import org.gate.gui.tree.GateTreeNode;
import org.gate.gui.tree.GateTreeSupport;
import org.gate.gui.tree.test.elements.TestSuites;


public class TestTreeTransferHandler extends TransferHandler {

    private static final long serialVersionUID = 8560957372186260765L;

    Logger log = LogManager.getLogger(this.getClass().getName());

    private DataFlavor nodeFlavor;
    private DataFlavor[] testPlanTreeNodeDataFlavors = new DataFlavor[1];

    // hold the nodes that should be removed on drop
    private List<GateTreeNode> nodesForRemoval = null;

    public TestTreeTransferHandler() {
        try {
            // only allow a drag&drop inside the current jvm
            String jvmLocalFlavor = DataFlavor.javaJVMLocalObjectMimeType + ";class=\"" + GateTreeNode[].class.getName() + "\"";
            nodeFlavor = new DataFlavor(jvmLocalFlavor);
            testPlanTreeNodeDataFlavors[0] = nodeFlavor;
        } catch (ClassNotFoundException e) {
            log.fatal("Class Not Found", e);
        }
    }

    @Override
    public int getSourceActions(JComponent c) {
        return COPY_OR_MOVE;
    }

    @Override
    protected Transferable createTransferable(JComponent c) {
        this.nodesForRemoval = null;
        JTree tree = (JTree) c;
        TreePath[] paths = tree.getSelectionPaths();
        if (paths != null) {
            // sort the selected jTamerTree path by row
            sortTreePathByRow(paths, tree);

            // if child and a parent are selected : only keep the parent
            boolean[] toRemove = new boolean[paths.length];
            int size = paths.length;
            for (int i = 0; i < paths.length; i++) {
                for (int j = 0; j < paths.length; j++) {
                    if (i != j && ((GateTreeNode) paths[i].getLastPathComponent()).isNodeAncestor((GateTreeNode) paths[j].getLastPathComponent())) {
                        toRemove[i] = true;
                        size--;
                        break;
                    }
                }
            }

            // remove unneeded nodes
            GateTreeNode[] nodes = new GateTreeNode[size];
            size = 0;
            for (int i = 0; i < paths.length; i++) {
                if (!toRemove[i]) {
                    GateTreeNode node = (GateTreeNode) paths[i].getLastPathComponent();
                    nodes[size++] = node;
                }
            }

            return new NodesTransferable(nodes);
        }
        return null;
    }

    private static void sortTreePathByRow(TreePath[] paths, final JTree tree) {
        Comparator<TreePath> cp = new Comparator<TreePath>() {

            @Override
            public int compare(TreePath o1, TreePath o2) {
                int row1 = tree.getRowForPath(o1);
                int row2 = tree.getRowForPath(o2);

                return (row1 < row2 ? -1 : (row1 == row2 ? 0 : 1));
            }
        };

        Arrays.sort(paths, cp);
    }

    @Override
    protected void exportDone(JComponent source, Transferable data, int action) {

        if (this.nodesForRemoval != null && ((action & MOVE) == MOVE)) {
            for (GateTreeNode jMeterTreeNode : nodesForRemoval) {
                GuiPackage.getIns().getTestTree().getTestTreeModel().removeNodeFromParent(jMeterTreeNode);
            }
            nodesForRemoval = null;
        }
    }

    @Override
    public boolean canImport(TransferHandler.TransferSupport support) {
        if (!support.isDrop()) {
            return false;
        }

        // the tree accepts a jmx file
//		DataFlavor[] flavors = support.getDataFlavors();
//		for (DataFlavor flavor : flavors) {
//			// Check for file lists specifically
//			if (flavor.isFlavorJavaFileListType()) {
//				return true;
//			}
//		}

        // or a treenode from the same tree
        if (!support.isDataFlavorSupported(nodeFlavor)) {
            return false;
        }

        // the copy is disabled
        int action = support.getDropAction();
        if (action != MOVE) {
            return false;
        }

        support.setShowDropLocation(true);

        // Do not allow a drop on the drag source selections.
        JTree.DropLocation dl = (JTree.DropLocation) support.getDropLocation();

        TreePath dest = dl.getPath();
        GateTreeNode target = (GateTreeNode) dest.getLastPathComponent();

        GateTreeNode[] nodes = getDraggedNodes(support.getTransferable());
        if (nodes == null || nodes.length == 0) {
            return false;
        }

        for (GateTreeNode node : nodes) {
            if (target == node) {
                return false;
            }
            // Do not allow a non-leaf node to be moved into one of its children
            if (node.getChildCount() > 0
                    && target.isNodeAncestor(node)) {
                return false;
            }
        }

        // re-use node association logic
        return canDropTo(target, nodes);
    }


    /*
    * dnd test tree node have more limit conditions then copy/paste.
    * Check can drop to before can add.
    * */
    boolean canDropTo(GateTreeNode target, GateTreeNode[] nodes) {
        if (null == target) {
            return false;
        }
        // not support drag nodes from different source folder
        GateTreeNode parentOfNodes = (GateTreeNode) nodes[0].getParent();
        for(GateTreeNode node : nodes){
            if(!node.getParent().equals(parentOfNodes)) return false;
        }

        /*
         * support dnd in same suite.
         * not support dnd to target which include children which include same name with source
         * */
        if(!nodes[0].getParentGateTreeNode().equals(target)){
            Enumeration<TreeNode> enumNodes = target.children();
            while(enumNodes.hasMoreElements()){
                GateTreeNode childTreeNode = (GateTreeNode) enumNodes.nextElement();
                if(Stream.of(nodes).filter(n -> n.toString().equals(childTreeNode.toString())).count() > 0){
                    return false;
                }
            }
        }

        if (GateTreeSupport.foundClass(nodes, new Class[]{TestSuites.class})){// Can't add a TestPlan anywhere
            return false;
        }
        return GateTreeSupport.canAddToTestTree(target, nodes);
    }


    @Override
    public boolean importData(TransferHandler.TransferSupport support) {
        if (!canImport(support)) {
            return false;
        }
        DataFlavor[] flavors = support.getDataFlavors();
        Transferable t = support.getTransferable();
        // deal with the jmx files
//		GuiPackage guiInstance = GuiPackage.getInstance();
//		DataFlavor[] flavors = support.getDataFlavors();
//		Transferable t = support.getTransferable();
//		for (DataFlavor flavor : flavors) {
//			// Check for file lists specifically
//			if (flavor.isFlavorJavaFileListType()) {
//				try {
//					return guiInstance.getMainFrame().openJmxFilesFromDragAndDrop(t);
//				}
//				catch (Exception e) {
//					LOG.error("Drop file failed", e);
//				}
//				return false;
//			}
//		}
        // Extract transfer data.
        GateTreeNode[] nodes = getDraggedNodes(t);

        if (nodes == null || nodes.length == 0) {
            return false;
        }

        // Get drop location and mode
        JTree.DropLocation dl = (JTree.DropLocation) support.getDropLocation();
        TreePath dest = dl.getPath();
        GateTreeNode target = (GateTreeNode) dest.getLastPathComponent();

        nodesForRemoval = new ArrayList<>();
        int index = dl.getChildIndex();
        TreePath[] pathsToSelect = new TreePath[nodes.length];
        int pathPosition = 0;
        GateTreeModel treeModel = GuiPackage.getIns().getTestTree().getTestTreeModel();
        for (GateTreeNode node : nodes) {

            if (index == -1) { // drop mode == DropMode.ON
                index = target.getChildCount();
            }

            // Insert a clone of the node, the original one will be removed by the exportDone method
            // the children are not cloned but moved to the cloned node
            // working on the original node would be harder as 
            //    you'll have to deal with the insertion index offset if you re-order a node inside a parent
            GateTreeNode copy = (GateTreeNode) node.clone();

            // first deepCopy the children as the call to deepCopy.add will modify the collection we're iterating on
            Enumeration<?> enumFrom = node.children();
            List<GateTreeNode> tmp = new ArrayList<>();
            while (enumFrom.hasMoreElements()) {
                GateTreeNode child = (GateTreeNode) enumFrom.nextElement();
                tmp.add(child);
            }

            for (GateTreeNode GateTreeNode : tmp) {
                copy.add(GateTreeNode);
            }
            treeModel.insertNodeInto(copy, target, index++);
            nodesForRemoval.add(node);
            pathsToSelect[pathPosition++] = new TreePath(treeModel.getPathToRoot(copy));
        }

        TreePath treePath = new TreePath(target.getPath());
        // expand the destination node
        JTree tree = (JTree) support.getComponent();
        tree.expandPath(treePath);
        tree.setSelectionPaths(pathsToSelect);
        return true;
    }

    private GateTreeNode[] getDraggedNodes(Transferable t) {
        GateTreeNode[] nodes = null;
        try {
            nodes = (GateTreeNode[]) t.getTransferData(nodeFlavor);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return nodes;
    }


    private class NodesTransferable implements Transferable {
        GateTreeNode[] nodes;

        public NodesTransferable(GateTreeNode[] nodes) {
            this.nodes = nodes;
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
            if (!isDataFlavorSupported(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }
            return nodes;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return testPlanTreeNodeDataFlavors;
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return nodeFlavor.equals(flavor);
        }
    }
}