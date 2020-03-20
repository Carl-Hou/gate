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

import org.gate.gui.GuiPackage;
import org.gate.gui.common.OptionPane;
import org.gate.gui.MainFrame;
import org.gate.gui.tree.GateTreeElement;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class AddToTree extends AbstractGateAction {

    private static final Set<String> commandSet;

    static {
        Set<String> commands = new HashSet<>();
        commands.add(ActionNames.ADD_TO_TREE);
        commandSet = Collections.unmodifiableSet(commands);
    }

    public AddToTree() {
    }

    /**
     * Gets the Set of actions this Command class responds to.
     *
     * @return the ActionNames value
     */
    @Override
    public Set<String> getActionNames() {
        return commandSet;
    }

    /**
     * Adds the specified class to the current node of the tree.
     */
    @Override
    public void doAction(ActionEvent e) {
        try {
            GateTreeElement element = createGateTreeElement(((JComponent) e.getSource()).getName());
            GuiPackage.getIns().getMainFrame().getCurrentTree().addGateElement(element);
        } catch (Exception err) {
            log.error("", err); // $NON-NLS-1$
            String msg = err.getMessage();
            if (msg == null) {
                msg = err.toString();
            }
            OptionPane.showErrorMessageDialog("Error", msg);
        }
    }

    public GateTreeElement createGateTreeElement(String objClass) {
        try {

            GateTreeElement element = (GateTreeElement) Class.forName(objClass).newInstance();
            return element;
        } catch (NoClassDefFoundError e) {
            log.error("Problem retrieving gui for " + objClass, e);
            String msg="Cannot find class: "+e.getMessage();
            JOptionPane.showMessageDialog(null,
                    msg,
                    "Missing jar? See log file." ,
                    JOptionPane.ERROR_MESSAGE);
            throw new RuntimeException(e.toString(), e); // Probably a missing jar
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            log.error("Problem retrieving gui for " + objClass, e);
            throw new RuntimeException(e.toString(), e); // Programming error: bail out.
        }
    }
}
