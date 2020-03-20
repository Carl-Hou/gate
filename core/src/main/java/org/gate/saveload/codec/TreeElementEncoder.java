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
import org.gate.saveload.convert.tree.TreeElementConverter;
import org.gate.saveload.convert.tree.TreeElementConverterRegistry;
import org.gate.saveload.utils.exceptions.ConvertException;
import org.gate.saveload.utils.DocumentHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Element;

public class TreeElementEncoder {

    protected Logger log = LogManager.getLogger(this);

    protected DocumentHelper documentHelper = null;


    public TreeElementEncoder(DocumentHelper documentHelper){
        this.documentHelper = documentHelper;
    }

    public DocumentHelper getDocumentHelper() {
        return documentHelper;
    }

    /*
    return parent if children's converter is not available.
    All node's children will lose once node's converter is no available.
    */
    public Element appendElement(Element parent, GateTreeElement testElement) throws ConvertException {
        Element children = null;
        children = getElement(testElement);
        parent.appendChild(children);
        return children;
    }

    public Element getElement(GateTreeElement testElement) throws ConvertException {

        TreeElementConverter converter = TreeElementConverterRegistry.getInstance().getConverter(testElement);
        converter.setDocumentHelper(documentHelper);
        return converter.marshal(testElement);

    }

}
