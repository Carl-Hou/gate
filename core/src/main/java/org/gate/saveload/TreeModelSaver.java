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

package org.gate.saveload;

import java.util.Enumeration;

import org.gate.gui.tree.GateTreeModel;

import org.gate.gui.tree.GateTreeNode;

import org.gate.saveload.codec.TreeElementEncoder;
import org.gate.saveload.utils.exceptions.ConvertException;
import org.gate.saveload.utils.DocumentHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Element;

public class TreeModelSaver {
	protected Logger log = LogManager.getLogger(this);
    String RootElementTagName = null;

	GateTreeModel model = null;
	TreeElementEncoder encoder = null;

	public TreeModelSaver(String rootElementTagName, GateTreeModel model){
	    this.RootElementTagName = rootElementTagName;
		this.model=model;
		this.encoder = new TreeElementEncoder(new DocumentHelper());
	}

	public DocumentHelper save() throws ConvertException {
		saveTreeNode(encoder.getDocumentHelper().createRootElement(RootElementTagName), model.getTestTreeRoot());
		return encoder.getDocumentHelper();
	}

    void saveTreeNode(Element parentElement, GateTreeNode treeNode) throws ConvertException {
        Enumeration<GateTreeNode> enumNodes = treeNode.children();
        Element currentDocElement = encoder.appendElement(parentElement, treeNode.getGateTreeElement());
        while(enumNodes.hasMoreElements()){
            GateTreeNode childTreeNode = enumNodes.nextElement();
            saveTreeNode(currentDocElement, childTreeNode);
        }
    }
}
