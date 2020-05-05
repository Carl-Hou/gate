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
package org.gate.gui.graph.elements.control;

import com.mxgraph.model.mxGraphModel;
import org.gate.engine.ModelExecutor;
import org.gate.gui.details.results.collector.ResultCollector;
import org.gate.gui.details.results.elements.ModelResult;
import org.gate.gui.details.results.elements.graph.ElementResult;
import org.gate.gui.graph.elements.AbstractGraphElement;
import org.gate.gui.tree.GateTreeModel;
import org.gate.gui.tree.GateTreeNode;
import org.gate.gui.tree.GateTreeSupport;
import org.gate.gui.tree.action.ActionTree;
import org.gate.gui.tree.ModelContainer;
import org.gate.runtime.GateContextService;

import java.util.Arrays;

public class ActionReference extends AbstractGraphElement implements Controller {
    public final  static String NP_PATH = "path";
    // don't change this
    public ActionReference(){
        addProp(NS_DEFAULT, NP_PATH, "");
    }

    public void init(String name, String path){
        setProp(NS_NAME, NP_NAME, name);
        setProp(NS_DEFAULT, NP_PATH, path);
    }

    @Override
    protected void exec(ElementResult actionLinkResult) {
        ResultCollector resultCollector = GateContextService.getContext().getResultCollector();
        actionLinkResult.setRunTimeProps(getRunTimePropsMap());
        String treeNodePath = getRunTimeProp(NS_DEFAULT, NP_PATH);
        mxGraphModel mxModel = getActionModel(treeNodePath);
        if(mxModel == null){
            actionLinkResult.setFailure("Tree Node not found by path: " + treeNodePath);
            log.fatal("Tree Node not found by path: " + treeNodePath);
            resultCollector.collect(actionLinkResult);
            return;
        }
        resultCollector.startModel((ModelResult) actionLinkResult);
        try {
            ModelExecutor me = new ModelExecutor(mxModel, (ModelResult) actionLinkResult);
            me.execute();
        }catch (Throwable t){
            actionLinkResult.setThrowable(t);
            log.fatal("Fail to execute action", t);
        }
        GateContextService.getContext().getResultCollector().endModel();
    }

    String[] getTreeNodePath(String treeNodePath){
        String[] path = treeNodePath.trim().split("\\.");
        log.trace(Arrays.toString(path));
        return path;
    }

    public mxGraphModel getActionModel(String treeNodePath){
        String[] path = getTreeNodePath(treeNodePath);
        GateTreeModel treeModel = GateTreeSupport.getActionTreeModel();
        GateTreeNode parent = treeModel.getTestTreeRoot();

        for(String nodeName : path){
            if(parent != null){
                parent = GateTreeSupport.findFirstChild(parent,nodeName);
            }else {
                log.error("TreeNode not exist in path" + treeNodePath);
                return null;
            }
        }

        log.trace("Find tree node by path:" + parent.toString());
        ModelContainer mc = (ModelContainer) parent.getUserObject();
        return mc.getMxModel();
    }

    @Override
    public String getGUI() {
        return GUI_ClassName_DefaultPropertiesGUI;
    }

    @Override
    public String getStaticLabel() {
        return "Action reference";
    }
}
