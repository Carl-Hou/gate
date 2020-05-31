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

import com.mxgraph.model.mxGraphModel;
import org.gate.common.util.GateRuntimeExcepiton;
import org.gate.gui.details.results.elements.test.TeardownResult;
import org.gate.gui.details.results.elements.test.SetupResult;
import org.gate.gui.details.results.elements.test.ModelContainerResult;
import org.gate.gui.details.results.elements.test.TestCaseResult;
import org.gate.gui.tree.GateTreeNode;
import org.gate.gui.tree.ModelContainer;
import org.gate.gui.tree.test.elements.TestCase;
import org.gate.gui.tree.test.elements.config.ConfigElement;
import org.gate.gui.tree.test.elements.dataprovider.DataProviderElement;
import org.gate.runtime.GateContext;

import java.util.Enumeration;
import java.util.LinkedList;

/*
* Package include everything for a test case to execute by runner.
* */
public abstract class TestModelRuntime {

    final String suiteName;
    final String modelName;

    mxGraphModel testModel;

    GateContext parentContext = null;
    LinkedList<ConfigElement> configElements = new LinkedList<>();

    public TestModelRuntime(GateTreeNode testCaseNode){
        this.suiteName = testCaseNode.getParentGateTreeNode().getName();
        modelName = testCaseNode.getName();
        ModelContainer modelContainer = (ModelContainer) testCaseNode.getGateTreeElement();
        testModel = modelContainer.getMxModel();
        // TODO add config here.
        Enumeration children = testCaseNode.children();
        while (children.hasMoreElements()){
            GateTreeNode child = (GateTreeNode) children.nextElement();
            if(child.includeElement(ConfigElement.class)){
                ConfigElement configElement = (ConfigElement) child.getUserObject();
                if(configElement.isEnable()){
                    addConfigElement(configElement);
                }
            }
        }
    }

    public ModelContainerResult createModelContainerResult(){
        if(SetupRuntime.class.isInstance(this)){
            return new SetupResult(suiteName, modelName);
        }else if(TeardownRuntime.class.isInstance(this)){
            return new TeardownResult(suiteName, modelName);
        }else if(TestCaseRuntime.class.isInstance(this)){
            return new TestCaseResult(suiteName, modelName);
        }
        //should never be here
        throw new GateRuntimeExcepiton("This is not a support ModelRuntime!!");
    }

    public void clear(){
        parentContext = null;
        testModel = null;
        configElements.clear();
    }

    public String getSuiteName(){
        return suiteName;
    }

    public String getModelName(){
        return modelName;
    }

    public String getID(){
        // use . here because . is not allow to use in suite or model name.
        return suiteName + "." + modelName;
    }

    public mxGraphModel getTestModel() {
        return testModel;
    }

    public void addConfigElement(ConfigElement configElement){
        configElements.add(configElement);
    }

    public LinkedList<ConfigElement> getConfigElements(){
        return configElements;
    }

}
