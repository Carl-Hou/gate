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

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxPoint;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.gate.common.config.GateProps;
import org.gate.common.util.GateRuntimeExcepiton;
import org.gate.gui.GuiPackage;
import org.gate.gui.common.OptionPane;
import org.gate.gui.graph.editor.BasicGraphEditor;
import org.gate.gui.graph.elements.Comments;
import org.gate.gui.graph.elements.GraphElement;
import org.gate.gui.graph.elements.asseration.Assert;
import org.gate.gui.graph.elements.config.Config;
import org.gate.gui.graph.elements.control.ActionReference;
import org.gate.gui.graph.elements.control.ConstantTimer;
import org.gate.gui.graph.elements.control.Controller;
import org.gate.gui.graph.elements.sampler.Sampler;
import org.gate.gui.graph.elements.sampler.protocol.selenium.Timeouts;
import org.gate.gui.graph.extractor.Extractor;
import org.gate.gui.tree.GateTreeNode;
import org.gate.gui.tree.GateTreeSupport;
import org.gate.gui.tree.action.elements.Action;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class LogLevel extends AbstractGateAction {

    private static final Set<String> commandSet;

    static {
        Set<String> commands = new HashSet<>();
        commands.add(ActionNames.LOG_LEVEL_ERROR);
        commands.add(ActionNames.LOG_LEVEL_WARN);
        commands.add(ActionNames.LOG_LEVEL_INFO);
        commands.add(ActionNames.LOG_LEVEL_DEBUG);
        commands.add(ActionNames.LOG_LEVEL_TRACE);
        commandSet = Collections.unmodifiableSet(commands);
    }

    public LogLevel() {
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

        switch (e.getActionCommand()){
            case ActionNames.LOG_LEVEL_ERROR :
                Configurator.setAllLevels("org.gate", Level.ERROR);
                break;
            case ActionNames.LOG_LEVEL_WARN:
                Configurator.setAllLevels("org.gate", Level.WARN);
                break;
            case ActionNames.LOG_LEVEL_INFO:
                Configurator.setAllLevels("org.gate", Level.INFO);
                break;
            case ActionNames.LOG_LEVEL_DEBUG:
                Configurator.setAllLevels("org.gate", Level.DEBUG);
                break;
            case ActionNames.LOG_LEVEL_TRACE:
                Configurator.setAllLevels("org.gate", Level.TRACE);
                break;
            default:
                OptionPane.showErrorMessageDialog("Error", "Fatal error Log Level is not support");
        }

    }





}
