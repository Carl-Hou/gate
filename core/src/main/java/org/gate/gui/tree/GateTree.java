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
import org.gate.gui.common.TestElement;

import javax.swing.*;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;


public abstract class GateTree extends JTree {

    Logger log = LogManager.getLogger();

    public GateTree(){
        GateTreeElement gateTreeRoot = new GateTreeElement();
        gateTreeRoot.setProp(TestElement.NS_NAME, TestElement.NP_NAME, "root");
        GateTreeNode testTreeRootNode = new GateTreeNode(gateTreeRoot);
        setModel(new GateTreeModel(testTreeRootNode));
        setRootVisible(false);
        addMouseListener(new GateTreeListener());
        getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        setShowsRootHandles(true);
    }

    public GateTreeModel getTestTreeModel(){
        // get from the tree to keep model consist after setModel call by loader
        return (GateTreeModel) super.getModel();
    }

    private GateTreeNode addObject(GateTreeNode parent, GateTreeElement child, boolean shouldBeVisible) {
        //GateTreeNode childNode = new GateTreeNode(child);
        GateTreeNode childNode = new GateTreeNode(child);
        if (parent == null) {
            parent =  getTestTreeModel().getTestTreeRoot();
        }
        //It is key to invoke this on the TreeModel, and NOT GateTreeNode
        getTestTreeModel().insertNodeInto(childNode, parent, parent.getChildCount());
        //Make sure the user can see the lovely v1 node.
        if (shouldBeVisible) {
            scrollPathToVisible(new TreePath(childNode.getPath()));
        }
        return childNode;
    }

    //
    /**
     *  Add child to the currently selected node.
     *  modify child name to unique before add it.
     * */
    public GateTreeNode addGateElement(GateTreeElement childElement){
        GateTreeNode parentNode = null;
        TreePath parentPath = getSelectionPath();
        if (parentPath == null) {
            parentNode = (GateTreeNode) getModel().getRoot();
        } else {
            parentNode = (GateTreeNode) (parentPath.getLastPathComponent());
        }
        return addGateElement(parentNode, childElement);
    }

    public GateTreeNode addGateElement(GateTreeNode parent, GateTreeElement childElement){
        String childName = childElement.getName();
        MutableTreeNode node = GateTreeSupport.findFirstChild(parent, childName);
        if(node != null){
            childName = childName + " - Copy";
            node = GateTreeSupport.findFirstChild(parent, childName);
            if(node == null){
                childElement.setName(childName);
            }else{
                int i = 1;
                do{
                    i++;
                    node = GateTreeSupport.findFirstChild(parent, childName + " (" + i + ")");
                }while (node != null);
                childElement.setName(childName+ " (" + i + ")");
            }
        }
        return addObject(parent, childElement, true);
    }

    // include children nodes of the node
    public void addNode(GateTreeNode parent, GateTreeNode node) {
        // Add this node
//      GateTreeNode newNode = addObject(parent, node.getGateTreeElement(), true);
        GateTreeNode newNode = addGateElement(parent, node.getGateTreeElement());
        // Add all the child nodes of the node we are adding
        for(int i = 0; i < node.getChildCount(); i++) {
            addNode(newNode, (GateTreeNode) node.getChildAt(i));
        }
    }


    /** Remove all nodes except the root node. */
    public void clear() {
        getTestTreeModel().getTestTreeRoot().removeAllChildren();
        getTestTreeModel().reload();
    }

    /** Remove the currently selected node. */
    public void removeCurrentNode() {
        TreePath currentSelection = getSelectionPath();
        if (currentSelection != null) {
            GateTreeNode currentNode = (GateTreeNode)
                    (currentSelection.getLastPathComponent());
            MutableTreeNode parent = (MutableTreeNode)(currentNode.getParent());
            if (parent != null) {
                getTestTreeModel().removeNodeFromParent(currentNode);
                return;
            }
        }
    }


    abstract public JPopupMenu getPopupMenu(GateTreeNode node);
}
