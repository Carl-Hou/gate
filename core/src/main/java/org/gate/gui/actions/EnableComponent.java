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
package org.gate.gui.actions;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gate.gui.GuiPackage;
import org.gate.gui.common.OptionPane;
import org.gate.gui.tree.GateTreeNode;
import org.gate.gui.tree.GateTreeSupport;
import org.gate.gui.tree.test.TestTree;
import org.gate.gui.tree.test.elements.TestTreeElement;

import javax.swing.tree.TreePath;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;

public class EnableComponent extends AbstractGateAction {
    private static final Logger log = LogManager.getLogger();

    private static final Set<String> commands = new HashSet<>();

    static {
        commands.add(ActionNames.ENABLE);
        commands.add(ActionNames.DISABLE);
        commands.add(ActionNames.TOGGLE);
    }

    @Override
    public void doAction(ActionEvent e) throws IllegalUserActionException {
        TreePath[] treePaths = GuiPackage.getIns().getTestTree().getSelectionPaths();
        if(treePaths == null){
            OptionPane.showErrorMessageDialog("Error", "No element selected");
            return;
        }

        if(e.getActionCommand().equals(ActionNames.ENABLE)){
            log.debug("enabling currently selected gui objects");
            enableComponents(treePaths, true);
        }else if(e.getActionCommand().equals(ActionNames.DISABLE)){
            log.debug("disabling currently selected gui objects");
            enableComponents(treePaths, false);
        }

    }

    private void enableComponents(TreePath[] treePaths, boolean enable) {
        for(TreePath treePath : treePaths){
            GateTreeNode node = (GateTreeNode) treePath.getLastPathComponent();
            ((TestTreeElement) node.getGateTreeElement()).enable(enable);
            GuiPackage.getIns().getTestTree().getTestTreeModel().reload(node);
            GateTreeSupport.updatePropertiesPanel(node);
        }
    }

    @Override
    public Set<String> getActionNames() {
        return commands;
    }
}
