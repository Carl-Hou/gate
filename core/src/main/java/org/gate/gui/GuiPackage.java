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
package org.gate.gui;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gate.gui.details.properties.PropertiesPane;
import org.gate.gui.details.results.ResultTree;
import org.gate.gui.details.results.ResultsPane;
import org.gate.gui.tree.action.ActionTree;
import org.gate.gui.tree.test.TestTree;

public final class GuiPackage {
    private static final Logger log = LogManager.getLogger();

    private static GuiPackage guiPack;

    private MainFrame mainFrame;

    /**
     * GUI start from here. When GuiPackage is requested for the first time,Keep this same with JMeter.
     */
    public static void initInstance() {
        guiPack = new GuiPackage();
    }

    /**
     * Private constructor to permit instantiation only from within this class.
     * Use {@link #getIns()} to retrieve a singleton instance.
     */
    private GuiPackage() {
        this.mainFrame = new MainFrame();
    }

    public static GuiPackage getIns() {
        return guiPack;
    }

    public MainFrame getMainFrame() {
        return mainFrame;
    }

    public TestTree getTestTree(){
        return mainFrame.getTestTree();
    }

    public ActionTree getActionTree(){
        return mainFrame.getActionTree();
    }

    public ResultsPane getResultsPane(){
        return mainFrame.getResultsPane();
    }

    public PropertiesPane getPropertiesPanel(){
        return mainFrame.getPropertiesPanel();
    }

    public ResultTree getResultTree(){
        return null;
    }


}
