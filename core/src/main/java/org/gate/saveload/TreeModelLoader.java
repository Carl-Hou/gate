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

import java.io.File;

import java.util.LinkedList;
import java.util.Optional;


import org.gate.gui.tree.GateTreeElement;
import org.gate.gui.tree.GateTreeModel;


import org.gate.gui.tree.GateTreeNode;
import org.gate.saveload.codec.TreeElementDecoder;
import org.gate.saveload.utils.DocumentHelper;
import org.gate.saveload.utils.exceptions.ConvertException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import org.w3c.dom.Element;


public class TreeModelLoader {

	protected Logger log = LogManager.getLogger(this);

    TreeElementDecoder decoder = null;
//  delete this once the object is done.
    protected DocumentHelper documentHelper = null;
    File path;

    public TreeModelLoader(DocumentHelper documentHelper){
        this.documentHelper = documentHelper;
        this.decoder = new TreeElementDecoder(documentHelper);
    }

    public GateTreeModel loadTreeModel() throws ConvertException {

        Element testTreeDocElement = documentHelper.getChildrenElements(documentHelper.getRootElement()).getFirst();
        Optional<GateTreeElement> testPlanTreeElementOptional = decoder.getElement(testTreeDocElement);
        if(testPlanTreeElementOptional.isPresent() ){
            GateTreeElement testPlanTreeElement = testPlanTreeElementOptional.get();
            GateTreeNode testPlanTreeNode = new GateTreeNode(testPlanTreeElement);
            loadTreeNode(testPlanTreeNode, testTreeDocElement);
            return new GateTreeModel(testPlanTreeNode) ;
        }else{
            throw new ConvertException("Not able to find Tree elements save file corrupted:" + testTreeDocElement.toString());
        }
    }

    void loadTreeNode(GateTreeNode treeNode, Element docElement ){
		LinkedList<Element> childrenDocElements = documentHelper.getChildrenElements(docElement);

		for(Element childrenDocElement : childrenDocElements){
            GateTreeNode currentTreeNode = decoder.appendElement(treeNode, childrenDocElement);
            loadTreeNode(currentTreeNode, childrenDocElement);
        }
    }


}
