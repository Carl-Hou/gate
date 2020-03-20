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

import org.gate.gui.GuiPackage;
import org.gate.gui.actions.ActionNames;
import org.gate.gui.tree.*;
import org.gate.gui.tree.action.elements.Action;
import org.gate.gui.tree.action.elements.ActionSuite;
import org.gate.gui.tree.action.elements.ActionSuites;
import org.gate.gui.tree.test.elements.TestTreeElement;

import javax.swing.*;
import java.util.HashMap;
import java.util.LinkedList;

public class ActionTreePopUpMenuFactory extends GateTreePopupMenuFactory {

    private final HashMap<String, LinkedList<MenuInfo>> menuMap = new HashMap<>();
    private final static String ADD_TO_MODEL = "Add to Editor";
    private final static String ADD = "Add";
    LinkedList<MenuInfo> adds = new LinkedList<>();

    public ActionTreePopUpMenuFactory(){
        menuMap.put(ADD, adds);
        adds.add(new MenuInfo("Suite", ActionSuite.class.getName()));
        adds.add(new MenuInfo("Action", Action.class.getName()));
    }

    @Override
    protected void appendPopupMenuItems(JPopupMenu jPopupMenu, GateTreeNode selectedNode) {

        if(selectedNode.includeElement(ActionSuites.class)){
            appendAddMenu(jPopupMenu);
            return;
        }

        if(selectedNode.includeElement(ActionSuite.class)){
            appendAddMenu(jPopupMenu);
            appendDefaultItems(jPopupMenu, selectedNode);
            return;
        }

        if(selectedNode.includeElement(Action.class)){
            jPopupMenu.add(makeMenuItem(ADD_TO_MODEL, Action.class.getName(), ActionNames.ADD_TO_Graph));

            appendDefaultItems(jPopupMenu, selectedNode);
        }
    }

    protected void appendAddMenu(JPopupMenu jPopupMenu){
        jPopupMenu.add(makeMenu(ADD, ActionNames.ADD_TO_TREE));
    }

    @Override
    protected GateTree getTestTree() {
        return GuiPackage.getIns().getActionTree();
    }

    @Override
    protected HashMap<String, LinkedList<MenuInfo>> getMenuMap(){
        return menuMap;
    }

    @Override
    protected boolean canAddTo(GateTreeNode target, GateTreeNode[] nodes) {
        if (null == target || target.includeElement(Action.class)) {
            return false;
        }
        if(target.includeElement(TestTreeElement.class) || GateTreeSupport.foundClass(nodes, new Class[]{TestTreeElement.class})){
            return false;
        }
        // All other
        return true;
    }

}
