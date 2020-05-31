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

import org.gate.gui.GuiPackage;
import org.gate.gui.actions.ActionNames;
import org.gate.gui.tree.*;
import org.gate.gui.tree.test.elements.TestCase;
import org.gate.gui.tree.test.elements.TestSuites;
import org.gate.gui.tree.test.elements.TestSuite;
import org.gate.gui.tree.test.elements.TestTreeElement;
import org.gate.gui.tree.test.elements.config.HTTPHeaderManager;
import org.gate.gui.tree.test.elements.config.HTTPRequestDefaults;
import org.gate.gui.tree.test.elements.config.SeleniumDefaults;
import org.gate.gui.tree.test.elements.config.UserDefineVariables;
import org.gate.gui.tree.test.elements.dataprovider.CSVDataProvider;
import org.gate.gui.tree.test.elements.fixture.FixtureElement;
import org.gate.gui.tree.test.elements.fixture.TearDown;
import org.gate.gui.tree.test.elements.fixture.SetUp;


import javax.swing.*;
import java.util.HashMap;
import java.util.LinkedList;

public class TestTreePopUpMenuFactory extends GateTreePopupMenuFactory {

    private final static String FIXTURES = "Fixture";
    private final static String CONFIG_ELEMENTS = "Config";
    private final static String DataProvider_ELEMENTS = "Data Provider";

    private final HashMap<String, LinkedList<MenuInfo>> menuMap = new HashMap<>();


    private final LinkedList<MenuInfo> fixtures = new LinkedList<>();
    private final LinkedList<MenuInfo> configElements = new LinkedList<>();
    private final LinkedList<MenuInfo> dataProviderElements = new LinkedList<>();

    public TestTreePopUpMenuFactory() {

        menuMap.put(FIXTURES, fixtures);
        menuMap.put(CONFIG_ELEMENTS, configElements);
        menuMap.put(DataProvider_ELEMENTS, dataProviderElements);

        fixtures.add(new MenuInfo("SetUp", SetUp.class.getName()));
        fixtures.add(new MenuInfo("TearDown", TearDown.class.getName()));

        configElements.add(new MenuInfo("User Define Variables", UserDefineVariables.class.getName()));
        configElements.add(new MenuInfo("Selenium Defaults", SeleniumDefaults.class.getName()));
        configElements.add(new MenuInfo("Http Request Defaults", HTTPRequestDefaults.class.getName()));
        configElements.add(new MenuInfo("Http Header Manager", HTTPHeaderManager.class.getName()));

        dataProviderElements.add(new MenuInfo("CSV Data Provider", CSVDataProvider.class.getName()));

    }

    @Override
    protected GateTree getTestTree() {
        return GuiPackage.getIns().getTestTree();
    }

    @Override
    protected HashMap<String, LinkedList<MenuInfo>> getMenuMap() {
        return menuMap;
    }

    @Override
    protected void appendPopupMenuItems(JPopupMenu jPopupMenu, GateTreeNode selectedNode) {

        if (selectedNode.includeElement(TestSuites.class)) {
            // add test suites only node here.
            JMenu addMenu = new JMenu("Add");
            addMenu.add(makeMenu(CONFIG_ELEMENTS, ActionNames.ADD_TO_TREE));
            addMenu.add(makeMenu(FIXTURES, ActionNames.ADD_TO_TREE));
            addMenu.add(makeMenuItem("Test Suite", TestSuite.class.getName(), ActionNames.ADD_TO_TREE));
            jPopupMenu.add(addMenu);
            appendPasteElementMenu(jPopupMenu, selectedNode);
            appendEnableElementMenu(jPopupMenu, selectedNode);
            return;
        }

        if (selectedNode.includeElement(TestSuite.class)) {
            JMenu addMenu = new JMenu("Add");
            addMenu.add(makeMenu(CONFIG_ELEMENTS, ActionNames.ADD_TO_TREE));
            addMenu.add(makeMenu(FIXTURES, ActionNames.ADD_TO_TREE));
            addMenu.add(makeMenuItem("Test Case", TestCase.class.getName(), ActionNames.ADD_TO_TREE));
            jPopupMenu.add(addMenu);
        }

        if (selectedNode.includeElement(TestCase.class)) {
            // Add nodes to Tree
            JMenu addToTreeMenu = new JMenu("Add");
            JMenu configMenu = makeMenu(CONFIG_ELEMENTS, ActionNames.ADD_TO_TREE);
            addToTreeMenu.add(configMenu);
            JMenu dataProviderMenu = makeMenu(DataProvider_ELEMENTS, ActionNames.ADD_TO_TREE);
            addToTreeMenu.add(dataProviderMenu);
            // add to popup
            jPopupMenu.add(addToTreeMenu);

        }

        if (selectedNode.includeElement(FixtureElement.class)) {
            // Add nodes to Tree
            JMenu addToTreeMenu = new JMenu("Add");
            JMenu configMenu = makeMenu(CONFIG_ELEMENTS, ActionNames.ADD_TO_TREE);
            addToTreeMenu.add(configMenu);
            // add to popup
            jPopupMenu.add(addToTreeMenu);

        }

        appendDefaultItems(jPopupMenu, selectedNode);
        appendEnableElementMenu(jPopupMenu, selectedNode);

    }

    protected void appendEnableElementMenu(JPopupMenu jPopupMenu, GateTreeNode selectedTreeNode) {
        TestTreeElement testTreeElement = (TestTreeElement) selectedTreeNode.getGateTreeElement();
        JMenuItem disabled = makeMenuItem("Disable", "Gate.TestTree.Disable", ActionNames.DISABLE);// $NON-NLS-1$
        JMenuItem enabled = makeMenuItem("Enable", "Gate.TestTree.Enable", ActionNames.ENABLE);// $NON-NLS-1$
        if (testTreeElement.isEnable()) {
            enabled.setEnabled(false);
            disabled.setEnabled(true);
        } else {
            enabled.setEnabled(true);
            disabled.setEnabled(false);
        }
        jPopupMenu.add(enabled);
        jPopupMenu.add(disabled);

    }

    @Override
    public boolean canAddTo(GateTreeNode target, GateTreeNode[] nodes) {

        return GateTreeSupport.canAddToTestTree(target, nodes);
    }

}
