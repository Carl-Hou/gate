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

import org.gate.gui.common.VerticalLayout;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gate.gui.tree.GateTree;
import org.gate.gui.tree.GateTreeNode;
import org.gate.gui.tree.GateTreeElement;

import javax.swing.*;

public abstract class AbstractTreeElementPanel extends JPanel implements TreeElementPropertiesGui {
    protected  Logger log = LogManager.getLogger(this.getName());
    TreeNamePane namePane = new TreeNamePane("Element identifier");

    public AbstractTreeElementPanel(){
        setLayout(new VerticalLayout());
        add(namePane);
    }

    @Override
    public void setNode(GateTreeNode node){
        namePane.setNode(node);
        GateTreeElement element = (GateTreeElement) node.getUserObject();
        setTestElement(element);
    }

    protected abstract void setTestElement(GateTreeElement element);
}
