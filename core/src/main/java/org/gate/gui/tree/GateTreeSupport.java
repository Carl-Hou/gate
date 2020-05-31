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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gate.common.config.GateProps;
import org.gate.common.util.GateException;
import org.gate.common.util.GateUtils;
import org.gate.gui.GuiPackage;
import org.gate.gui.common.OptionPane;
import org.gate.gui.details.properties.tree.TreeElementPropertiesGui;
import org.gate.gui.details.properties.tree.TreeNamePane;
import org.gate.gui.tree.action.ActionTree;
import org.gate.gui.tree.test.TestTree;
import org.gate.gui.tree.test.elements.TestCase;
import org.gate.gui.tree.test.elements.TestSuite;
import org.gate.gui.tree.test.elements.TestSuites;
import org.gate.gui.tree.test.elements.config.ConfigElement;
import org.gate.gui.tree.test.elements.dataprovider.DataProviderElement;
import org.gate.saveload.utils.DocumentHelper;
import org.gate.saveload.utils.GateLoader;

import javax.swing.*;
import javax.swing.tree.*;

import java.io.File;
import java.util.*;

// static class util class for gate trees.
public class GateTreeSupport {

    protected static Logger log = LogManager.getLogger();

    final static public String Name_TestTree="TestTree";
    final static public String Name_ActionTree="ActionTree";

    static private HashMap<String, TreeElementPropertiesGui> elementGuiCache = new HashMap<>();
    static private GateTreeModel testTreeModel = null;
    static private GateTreeModel actionTreeModel = null;


    public static void cleanCache(){
        elementGuiCache.clear();
    }

    public static void updatePropertiesPanel(GateTreeNode treeNode){
        try {
            TreeElementPropertiesGui treeElementPropertiesGui = null;
            String guiClassName = treeNode.getGateTreeElement().getGUI();

            if(elementGuiCache.containsKey(guiClassName)){
                treeElementPropertiesGui = elementGuiCache.get(guiClassName);
            }else{
                treeElementPropertiesGui = (TreeElementPropertiesGui) Class.forName(guiClassName).newInstance();
                elementGuiCache.put(guiClassName, treeElementPropertiesGui);
            }

            treeElementPropertiesGui.setNode(treeNode);
            GuiPackage.getIns().getPropertiesPanel().setComponent((JComponent) treeElementPropertiesGui);
        } catch (Exception ex) {
            ex.printStackTrace();
            OptionPane.showErrorMessageDialog("Fatal Error:", ex);
        }
    }

    public static GateTreeNode findFirstChild(GateTreeNode parent, String nodeName){
        Enumeration<TreeNode> enumNodes = parent.children();
        while(enumNodes.hasMoreElements()){
            MutableTreeNode childTreeNode = (MutableTreeNode) enumNodes.nextElement();
            if(childTreeNode.toString().equals(nodeName)){
                return (GateTreeNode) childTreeNode;
            }
        }
        return null;
    }

    public static LinkedList<GateTreeNode> findChildren(GateTreeNode parent, Class userObjectClass){
        Enumeration<TreeNode> enumNodes = parent.children();
        LinkedList<GateTreeNode> nodes = new LinkedList<>();
        while(enumNodes.hasMoreElements()){
            GateTreeNode childTreeNode = (GateTreeNode) enumNodes.nextElement();
            if(userObjectClass.isAssignableFrom(childTreeNode.getGateTreeElement().getClass())){
                nodes.add(childTreeNode);
            }
        }
        return nodes;
    }

    // Is any node an instance of one of the classes?
    public static boolean foundClass(DefaultMutableTreeNode[] nodes, Class<?>[] classes) {
        for (DefaultMutableTreeNode node : nodes) {
            for (Class<?> aClass : classes) {
                if (aClass.isInstance(node.getUserObject())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean canAddToTestTree(GateTreeNode target, GateTreeNode[] nodes) {
        if (null == target) {
            return false;
        }
        // Can't add a TestPlan anywhere
        if (foundClass(nodes, new Class[]{TestSuites.class})){
            return false;
        }

        GateTreeElement parent = target.getGateTreeElement();
        // test suite only allowed under test plan
        if (parent instanceof TestSuites) {
            if (foundClass(nodes,new Class[]{TestCase.class})){
                return false;
            }else if(foundClass(nodes,new Class[]{DataProviderElement.class})){
                return false;
            }

            return true;
        }
        // case only allowed under testsuite
        if(parent instanceof TestSuite){
            if (foundClass(nodes,new Class[]{TestSuite.class})){
                return false;
            }else if(foundClass(nodes,new Class[]{DataProviderElement.class})){
                return false;
            }
            return true;
        }
        // case only allowed under model container
        if(ModelContainer.class.isAssignableFrom(parent.getClass())){
            if (foundClass(nodes,new Class[]{ConfigElement.class})){
                return true;
            }else if(foundClass(nodes,new Class[]{DataProviderElement.class}) && parent instanceof TestCase){
                return true;
            }
            return false;
        }
        // All other
        return false;
    }

    public static String getGateTreeNodePath(GateTreeNode node){
        StringBuffer path = new StringBuffer();
        TreeNode[] paths = node.getPath();
        for(int i=1; i< paths.length; i++){
            path.append(paths[i].toString());
            if(i != paths.length -1){
                path.append(".");
            }
        }
        return path.toString();
    }

    static public boolean load(File testFile)  {
        log.info("load test file:" + testFile.getAbsolutePath());
        if (!testFile.canRead()) {
            log.fatal("Test file not exit or not readable:" + testFile.getAbsolutePath());
            return false;
        }

        try {
            DocumentHelper rootDocumentHelper = new DocumentHelper();
            rootDocumentHelper.loadFromFile(testFile);
            GateLoader gateLoader = new GateLoader(rootDocumentHelper);
            testTreeModel = gateLoader.loadTreeModel(TestTree.class.getSimpleName());
            actionTreeModel =  gateLoader.loadTreeModel(ActionTree.class.getSimpleName());
        } catch (GateException e) {
            log.fatal("Error on load test file: ", e);
            if(GateProps.isGuiMode()){
                OptionPane.showErrorMessageDialog("Error on load test file: ", e);
            }
            return false;
        }


        if (testTreeModel == null || actionTreeModel == null) {
            log.fatal("critical error occur. Fail to load gate model. Model is empty exit");
            // will get null point when access the models array. no exception throws here
            return false;
        }

        if(GateProps.isGuiMode()){
            GuiPackage.getIns().getTestTree().setModel(testTreeModel);
            GuiPackage.getIns().getActionTree().setModel(actionTreeModel);
        }

        return true;
    }

    static public void syncGui(){
        actionTreeModel = (GateTreeModel) GuiPackage.getIns().getActionTree().getModel();
        testTreeModel = (GateTreeModel) GuiPackage.getIns().getTestTree().getModel();
    }

    static public GateTreeModel getTestTreeModel(){
        return testTreeModel;
    }

    static public GateTreeModel getActionTreeModel(){
        return  actionTreeModel;
    }

    static public GateTreeNode getTestSuitesNode(){
        return (GateTreeNode) testTreeModel.getTestTreeRoot().getFirstChild();

    }

    static public HashMap<GateTreeNode, LinkedList<GateTreeNode>> getFilteredTestCases(
            String testSuiteNamePrefixes, String testCaseNamePrefixes){
        HashMap<GateTreeNode, LinkedList<GateTreeNode>> selectedTestCases = new HashMap();

        LinkedList<String> suiteNamePrefixes = GateUtils.getParameterList(testSuiteNamePrefixes);
        LinkedList<String> caseNamePrefixes = GateUtils.getParameterList(testCaseNamePrefixes);

        LinkedList<GateTreeNode> suites =
                findChildren(getTestSuitesNode(), TestSuite.class);

        while(!suites.isEmpty()){
            GateTreeNode testSuiteNode = suites.remove();
            LinkedList<GateTreeNode> testCases = findChildren(testSuiteNode, TestCase.class);
            if (suiteNamePrefixes.size() == 0) {
                selectedTestCases.put(testSuiteNode, testCases);
            } else {
                for (String suiteNamePrefix : suiteNamePrefixes) {
                    if (testSuiteNode.getName().startsWith(suiteNamePrefix)) {
                        selectedTestCases.put(testSuiteNode, testCases);
                    }
                }
            }
        }

        // process test name. keep test case only.
        for(HashMap.Entry<GateTreeNode, LinkedList<GateTreeNode>> suiteEntry : selectedTestCases.entrySet()){
            LinkedList<GateTreeNode> testCases = new LinkedList<>();
            for(GateTreeNode node : suiteEntry.getValue()){
                if(TestCase.class.isInstance(node.getGateTreeElement())){
                    if(caseNamePrefixes.size() == 0){
                        testCases.add(node);
                    }else {
                        for(String caseNamePrefix : caseNamePrefixes){
                            if(node.getGateTreeElement().getName().startsWith(caseNamePrefix)){
                                testCases.add(node);
                            }
                        }
                    }

                }
            }
            suiteEntry.getValue().clear();
            suiteEntry.setValue(testCases);
        }

        return selectedTestCases;
    }



}
