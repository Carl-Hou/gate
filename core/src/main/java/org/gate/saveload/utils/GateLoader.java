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
package org.gate.saveload.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gate.gui.tree.GateTreeModel;
import org.gate.saveload.TreeModelLoader;
import org.gate.saveload.utils.exceptions.ConvertException;
import org.w3c.dom.Element;

public class GateLoader {
    DocumentHelper documentHelper = null;

    public GateLoader(DocumentHelper documentHelper){
        this.documentHelper = documentHelper;
    }

    public GateTreeModel loadTreeModel(String treeName) throws ConvertException {
        DocumentHelper gateTreeDocHelper = new DocumentHelper();
        gateTreeDocHelper.createRootElement(treeName);
        Element element = documentHelper.getChildrenElements(documentHelper.getRootElement(), treeName).getFirst();
        gateTreeDocHelper.importNode(gateTreeDocHelper.getDocument().getDocumentElement(), gateTreeDocHelper.getChildrenElements(element).getFirst());
        TreeModelLoader gateTreeModelerLoader = new TreeModelLoader(gateTreeDocHelper);
        return gateTreeModelerLoader.loadTreeModel();
    }

}
