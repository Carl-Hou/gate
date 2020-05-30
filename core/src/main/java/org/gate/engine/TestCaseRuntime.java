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
package org.gate.engine;

import org.gate.common.util.GateUtils;
import org.gate.gui.tree.GateTreeNode;
import org.gate.gui.tree.test.elements.TestCase;
import org.gate.gui.tree.test.elements.dataprovider.DataProviderElement;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/*
* Package include everything for a test case to execute by runner.
* */
public class TestCaseRuntime extends TestModelRuntime {

    final LinkedList<String> dependency;
    LinkedList<DataProviderElement> dataProviderElements = new LinkedList<>();

    String timeout;

    public TestCaseRuntime(GateTreeNode testCaseNode){
        super(testCaseNode);
        TestCase testCase = (TestCase) testCaseNode.getGateTreeElement();
        this.dependency = GateUtils.getParameterList(testCase.getDepends().trim());
        timeout = testCase.getTimeout().trim();

        Enumeration children = testCaseNode.children();
        while (children.hasMoreElements()){
            GateTreeNode child = (GateTreeNode) children.nextElement();
            if(child.includeElement(DataProviderElement.class)){
                DataProviderElement dataProviderElement = (DataProviderElement) child.getUserObject();
                if(dataProviderElement.isEnable()){
                    addDataProviderElement(dataProviderElement);
                }
            }
        }
    }

    public void clear(){
        super.clear();
        dependency.clear();
    }

    public void addDataProviderElement(DataProviderElement dataProviderElement){
        dataProviderElements.add(dataProviderElement);
    }

    public LinkedList<DataProviderElement> getDataProviderElements(){
        return dataProviderElements;
    }

    public LinkedList<String> getDependency(){
        return dependency;
    }

    public String getTimeout() {
        return timeout;
    }



}
