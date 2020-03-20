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

import javax.swing.tree.DefaultMutableTreeNode;

public class GateTreeNode extends DefaultMutableTreeNode {
    public GateTreeNode(GateTreeElement element){
        super(element);
    }

    public GateTreeElement getGateTreeElement(){
        return (GateTreeElement) getUserObject();
    }

    public GateTreeNode getParentGateTreeNode(){
        return (GateTreeNode) getParent();
    }

    public GateTreeNode getPreviousSiblingGateTreeNode(){
        return (GateTreeNode) getPreviousSibling();
    }

    //TODO remove this
    public boolean includeElement(Class treeElementClass){
//        return getGateTreeElement().getClass().isAssignableFrom(treeElementClass);
        return treeElementClass.isAssignableFrom(getGateTreeElement().getClass());
    }

    public String getName(){
        return toString();
    }


}
