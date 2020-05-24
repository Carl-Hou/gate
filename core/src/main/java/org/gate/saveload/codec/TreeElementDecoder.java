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
package org.gate.saveload.codec;


import org.gate.gui.tree.GateTreeElement;
import org.gate.gui.tree.GateTreeNode;
import org.gate.saveload.convert.tree.TreeElementConverter;
import org.gate.saveload.convert.tree.TreeElementConverterRegistry;
import org.gate.saveload.utils.exceptions.ConvertException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gate.saveload.utils.DocumentHelper;
import org.w3c.dom.Element;

import java.util.Optional;

public class TreeElementDecoder {

    protected Logger log = LogManager.getLogger(this);

    DocumentHelper documentHelper = null;

    public TreeElementDecoder(DocumentHelper documentHelper){
        this.documentHelper = documentHelper;
    }
    /*
        return parent if children's converter is not available or any errors occur during convert.
        All node's children will lose once node's converter is no available.
     */
    public GateTreeNode appendElement(GateTreeNode parent, Element docElement)   {

        Optional<GateTreeElement> treeElementOptional = null;
        try {
            treeElementOptional = getElement(docElement);
        } catch (ConvertException e) {
            log.fatal(e);
            //		do not do anything if error occur currently.
            return parent;
        }

        if(treeElementOptional.isPresent() ){
            GateTreeNode children = new GateTreeNode(treeElementOptional.get());
            parent.add(children);
            return children;
        }
        return parent;
    }

    public Optional<GateTreeElement> getElement(Element docElement) throws ConvertException
    {
        Optional<TreeElementConverter> treeElementConverterOptional =  TreeElementConverterRegistry.getInstance().getConverter(docElement);
        if(treeElementConverterOptional.isPresent()){
            TreeElementConverter converter = treeElementConverterOptional.get();
            converter.setDocumentHelper(documentHelper);
            return Optional.of( converter.unmarshal(docElement));
        }
        return Optional.empty();
    }

}
