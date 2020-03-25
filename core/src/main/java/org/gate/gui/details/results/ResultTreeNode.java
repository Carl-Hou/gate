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
package org.gate.gui.details.results;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gate.gui.details.results.elements.Result;
import org.gate.gui.details.results.elements.test.TestResult;
import org.gate.gui.tree.GateTreeNode;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Optional;

public class ResultTreeNode extends DefaultMutableTreeNode {
    final private static Logger log = LogManager.getLogger();
    public ResultTreeNode(Result result) {
        super(result);
    }

    public ResultTreeNode findChild(String nodeName){
        Enumeration<TreeNode> enumNodes = children();
        while(enumNodes.hasMoreElements()){
            ResultTreeNode childTreeNode = (ResultTreeNode) enumNodes.nextElement();
            if(childTreeNode.toString().equals(nodeName)){
                return childTreeNode;
            }
        }
        return null;
    }

    public LinkedList<ResultTreeNode> findChildren(Class userObjectClass){
        Enumeration<TreeNode> enumNodes = children();
        LinkedList<ResultTreeNode> nodes = new LinkedList<>();
        while(enumNodes.hasMoreElements()){
            ResultTreeNode childTreeNode = (ResultTreeNode) enumNodes.nextElement();
            if(userObjectClass.isInstance(childTreeNode.getResult())){
                nodes.add(childTreeNode);
            }
        }
        return nodes;
    }

    public LinkedList<ResultTreeNode> findChildren(Class userObjectClass, String nodeName){
        // TODO need to check if userObjectClass is a Result
        Enumeration<TreeNode> enumNodes = children();
        LinkedList<ResultTreeNode> nodes = new LinkedList<>();
        while(enumNodes.hasMoreElements()){
            ResultTreeNode childTreeNode = (ResultTreeNode) enumNodes.nextElement();
            if(childTreeNode.getResult().getName().equals(nodeName) && userObjectClass.isInstance(childTreeNode.getResult())){
                nodes.add(childTreeNode);
            }
        }
        return nodes;
    }

    public Result getResult(){
        return (Result) getUserObject();
    }
}
