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
package org.gate.gui.graph.common;

import org.gate.common.config.GateProps;
import org.gate.gui.actions.ActionNames;
import org.gate.gui.actions.ActionRouter;
import org.gate.gui.graph.elements.Comments;
import org.gate.gui.graph.elements.asseration.ResponseAssert;
import org.gate.gui.graph.elements.control.ConstantTimer;
import org.gate.gui.graph.elements.control.Decide;
import org.gate.gui.graph.elements.control.Start;
import org.gate.gui.graph.elements.sampler.DebugSampler;
import org.gate.gui.graph.elements.sampler.JSR223Sampler;
import org.gate.gui.graph.elements.sampler.protocol.http.HttpRequestSampler;
import org.gate.gui.graph.elements.sampler.protocol.selenium.*;
import org.gate.gui.graph.elements.sampler.protocol.selenium.Window;
import org.gate.gui.graph.extractor.JSONExtractor;
import org.gate.gui.graph.extractor.RegexExtractor;
import org.gate.gui.graph.extractor.XPathExtractor;
import org.gate.gui.tree.MenuInfo;
import org.gate.varfuncs.functions.Variable;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;

public final class GraphMenuFactory {
    protected final static LinkedList<MenuInfo> graphLinkCells = new LinkedList<>();
    protected final static LinkedList<MenuInfo> graphAssertionCells = new LinkedList<>();
    protected final static LinkedList<MenuInfo> graphControlCells = new LinkedList<>();
    protected final static LinkedList<MenuInfo> graphConfigCells = new LinkedList<>();
    protected final static LinkedList<MenuInfo> graphSamplerCells = new LinkedList<>();
    protected final static LinkedList<MenuInfo> graphTimerCells = new LinkedList<>();
    protected final static LinkedList<MenuInfo> graphExtractorCells = new LinkedList<>();
    protected final static LinkedList<MenuInfo> graphCommentCells = new LinkedList<>();

    static {
        graphLinkCells.add(new MenuInfo("Success", GateProps.Success));
        graphLinkCells.add(new MenuInfo("Fail", GateProps.Fail));
        graphLinkCells.add(new MenuInfo("Next", GateProps.Next));
        graphLinkCells.add(new MenuInfo("Note", GateProps.Note));

        graphAssertionCells.add(new MenuInfo("Response Assertion", ResponseAssert.class.getName()));
        graphAssertionCells.add(new MenuInfo("Variable Assertion", Variable.class.getName()));

        graphConfigCells.add(new MenuInfo("User Defined Variables", org.gate.gui.graph.elements.config.UserDefineVariables.class.getName()));

        graphControlCells.add(new MenuInfo("Start", Start.class.getName()));
        graphControlCells.add(new MenuInfo("Decide", Decide.class.getName()));

        graphTimerCells.add(new MenuInfo("ConstantTimer", ConstantTimer.class.getName()));
        graphTimerCells.add(new MenuInfo("Selenium Timeouts", Timeouts.class.getName()));

        graphSamplerCells.add(new MenuInfo("Debug Sampler", DebugSampler.class.getName()));
        graphSamplerCells.add(new MenuInfo("JSR223 Sampler", JSR223Sampler.class.getName()));
        graphSamplerCells.add(new MenuInfo("Http Request", HttpRequestSampler.class.getName()));
        graphSamplerCells.add(new MenuInfo("Selenium Driver", Driver.class.getName()));
        graphSamplerCells.add(new MenuInfo("Selenium Element", Element.class.getName()));
        graphSamplerCells.add(new MenuInfo("Selenium ConditionChecker", ConditionChecker.class.getName()));
        graphSamplerCells.add(new MenuInfo("Selenium Alert", Alert.class.getName()));
        graphSamplerCells.add(new MenuInfo("Selenium Window", Window.class.getName()));
        graphSamplerCells.add(new MenuInfo("Selenium Navigation", Navigation.class.getName()));
        graphSamplerCells.add(new MenuInfo("Selenium TargetLocator", TargetLocator.class.getName()));

        graphExtractorCells.add(new MenuInfo("XPath Extractor", XPathExtractor.class.getName()));
        graphExtractorCells.add(new MenuInfo("JSON Extractor", JSONExtractor.class.getName()));
        graphExtractorCells.add(new MenuInfo("Regex Extractor", RegexExtractor.class.getName()));

        graphCommentCells.add(new MenuInfo("Comment", Comments.class.getName()));
    }

    /**
     * Private constructor to prevent instantiation.
     */
    private GraphMenuFactory(){};

    public static JMenu getAddToModelMenu(){
        // Add cells to Graph
        JMenu addToGraphMenu = new JMenu("Add");
        addToGraphMenu.add(makeMenu("Link", graphLinkCells, ActionNames.ADD_TO_Graph));
        addToGraphMenu.add(makeMenu("Config", graphConfigCells, ActionNames.ADD_TO_Graph));
        addToGraphMenu.add(makeMenu("Control", graphControlCells, ActionNames.ADD_TO_Graph));
        addToGraphMenu.add(makeMenu("Sampler", graphSamplerCells, ActionNames.ADD_TO_Graph));
        addToGraphMenu.add(makeMenu("Extractor", graphExtractorCells, ActionNames.ADD_TO_Graph));
        addToGraphMenu.add(makeMenu("Timer", graphTimerCells, ActionNames.ADD_TO_Graph));
        addToGraphMenu.add(makeMenu("Assertion", graphAssertionCells, ActionNames.ADD_TO_Graph));
        addToGraphMenu.add(makeMenu("Comments", graphCommentCells, ActionNames.ADD_TO_Graph));

        return addToGraphMenu;
    }

    protected static JMenu makeMenu(String category, LinkedList<MenuInfo> menuInfoList, String actionCommand) {
        JMenu addMenu = new JMenu(category);
        for (MenuInfo info : menuInfoList) {
            addMenu.add(makeMenuItem(info, actionCommand));
        }
        return addMenu;
    }

    protected static Component makeMenuItem(MenuInfo info, String actionCommand) {
        JMenuItem newMenuChoice = new JMenuItem(info.getLabel());
        newMenuChoice.setName(info.getClassName());
        newMenuChoice.addActionListener(ActionRouter.getInstance());
        if (actionCommand != null) {
            newMenuChoice.setActionCommand(actionCommand);
        }
        return newMenuChoice;
    }
}
