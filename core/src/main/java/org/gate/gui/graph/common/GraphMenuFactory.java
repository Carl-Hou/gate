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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gate.common.config.GateProps;
import org.gate.common.util.GateClassUtils;
import org.gate.gui.actions.ActionNames;
import org.gate.gui.actions.ActionRouter;
import org.gate.gui.graph.elements.GraphElement;
import org.gate.gui.graph.elements.asseration.Assert;
import org.gate.gui.graph.elements.comment.Comment;
import org.gate.gui.graph.elements.config.Config;
import org.gate.gui.graph.elements.control.ActionReference;
import org.gate.gui.graph.elements.control.Controller;
import org.gate.gui.graph.elements.extractor.Extractor;
import org.gate.gui.graph.elements.sampler.Sampler;
import org.gate.gui.graph.elements.timer.Timer;
import org.gate.gui.tree.MenuInfo;

import javax.swing.*;
import java.awt.*;
import java.util.Comparator;
import java.util.LinkedList;

public final class GraphMenuFactory {

    private static final Logger log = LogManager.getLogger();

    protected final static LinkedList<MenuInfo> graphLinkCells = new LinkedList<>();
    protected final static LinkedList<MenuInfo> graphAssertionCells = new LinkedList<>();
    protected final static LinkedList<MenuInfo> graphControlCells = new LinkedList<>();
    protected final static LinkedList<MenuInfo> graphConfigCells = new LinkedList<>();
    protected final static LinkedList<MenuInfo> graphSamplerCells = new LinkedList<>();
    protected final static LinkedList<MenuInfo> graphTimerCells = new LinkedList<>();
    protected final static LinkedList<MenuInfo> graphExtractorCells = new LinkedList<>();
    protected final static LinkedList<MenuInfo> graphCommentCells = new LinkedList<>();

    static {
        graphLinkCells.add(new MenuInfo("Next", GateProps.Next));
        graphLinkCells.add(new MenuInfo("Success", GateProps.Success));
        graphLinkCells.add(new MenuInfo("Fail", GateProps.Fail));
        graphLinkCells.add(new MenuInfo("Note", GateProps.Note));

        GateClassUtils.getIns().getGraphElements().forEach((category, graphElementClasses) ->{
            //deffer error handling to real issue comes.

            if(category.equals(Assert.class.getName())){
                graphElementClasses.forEach( graphElementClass ->{
                    GraphElement item = GateClassUtils.getIns().newGraphElementInstance (graphElementClass);
                    graphAssertionCells.add(new MenuInfo(item.getStaticLabel(), graphElementClass.getName()));
                });
            }
            else if(category.equals(Comment.class.getName())){
                graphElementClasses.forEach( graphElementClass ->{
                    GraphElement item = GateClassUtils.getIns().newGraphElementInstance (graphElementClass);
                    graphCommentCells.add(new MenuInfo(item.getStaticLabel(), graphElementClass.getName()));
                });
            }
            else if(category.equals(Config.class.getName())){
                graphElementClasses.forEach( graphElementClass ->{
                    GraphElement item = GateClassUtils.getIns().newGraphElementInstance (graphElementClass);
                    graphConfigCells.add(new MenuInfo(item.getStaticLabel(), graphElementClass.getName()));
                });
            }
            else if(category.equals(Controller.class.getName())){
                graphElementClasses.forEach( graphElementClass ->{
                    if(!graphElementClass.getName().equals(ActionReference.class.getName())){
                        GraphElement item = GateClassUtils.getIns().newGraphElementInstance (graphElementClass);
                        graphControlCells.add(new MenuInfo(item.getStaticLabel(), graphElementClass.getName()));
                    }
                });
            }
            else if(category.equals(Extractor.class.getName())){
                graphElementClasses.forEach( graphElementClass ->{
                    GraphElement item = GateClassUtils.getIns().newGraphElementInstance (graphElementClass);
                    graphExtractorCells.add(new MenuInfo(item.getStaticLabel(), graphElementClass.getName()));
                });
            }
            else if(category.equals(Sampler.class.getName())){
                graphElementClasses.forEach( graphElementClass ->{
                    GraphElement item = GateClassUtils.getIns().newGraphElementInstance (graphElementClass);
                    graphSamplerCells.add(new MenuInfo(item.getStaticLabel(), graphElementClass.getName()));
                });
                graphSamplerCells.sort(new Comparator<MenuInfo>() {
                    @Override
                    public int compare(MenuInfo o1, MenuInfo o2) {
                        return o1.getLabel().compareTo(o2.getLabel());
                    }
                });
            }
            else if(category.equals(Timer.class.getName())){
                graphElementClasses.forEach( graphElementClass ->{
                    GraphElement item = GateClassUtils.getIns().newGraphElementInstance (graphElementClass);
                    graphTimerCells.add(new MenuInfo(item.getStaticLabel(), graphElementClass.getName()));
                });
            }
        });
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
