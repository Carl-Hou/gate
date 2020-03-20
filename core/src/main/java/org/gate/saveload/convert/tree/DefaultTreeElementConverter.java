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

/**
 * @Author: Carl Marquis
 * @Description:
 * @Date: Create in 0:30 2017-10-10
 */
package org.gate.saveload.convert.tree;



import org.gate.gui.tree.GateTreeElement;
import org.gate.saveload.utils.exceptions.ConvertException;
import org.w3c.dom.Element;

/*
*   This is the default conver for TestElement which is inherit from AbstractTestElement
* */
public class DefaultTreeElementConverter extends AbstractTreeElementConverter implements TreeElementConverter{

    public DefaultTreeElementConverter(Class testElementClass){
        super(testElementClass);
    }

    @Override
    public Element marshal(GateTreeElement treeElement) {
        return marshalByDefault(treeElement);
    }

    public GateTreeElement unmarshal(Element element) throws ConvertException {
        return unmarshalTreeElementByDefault(element);
    }
}
