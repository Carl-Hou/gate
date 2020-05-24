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

package org.gate.saveload.convert.tree;

import com.mxgraph.io.mxCodec;
import com.mxgraph.model.mxGraphModel;
import org.gate.gui.tree.GateTreeElement;
import org.gate.gui.tree.ModelContainer;
import org.gate.saveload.codec.GraphElementCodec;
import org.gate.saveload.utils.exceptions.ConvertException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.*;

public class ModelContainerConverter extends AbstractTreeElementConverter {


    public ModelContainerConverter(Class modelContainer) {
        super(modelContainer);
    }

    public Element marshal(GateTreeElement testElement) throws ConvertException {
        Element modelContainerDocElement = marshalByDefault(testElement);
        ModelContainer modelContainer = (ModelContainer) testElement;
        mxGraphModel mxModel = modelContainer.getMxModel();
        if(mxModel == null){
            return modelContainerDocElement;
        }
        mxGraphModel mxModelClone = null;
        try {
            mxModelClone = deepCopy(mxModel);
        } catch (IOException | ClassNotFoundException e) {
            log.fatal(e);
            throw new ConvertException("Fail to deep deepCopy mxGraphModel", e);
        }
        GraphElementCodec graphElementCodec = new GraphElementCodec();
        graphElementCodec.encode(mxModelClone);
        mxCodec codec = new mxCodec();
        Node mxModelCloneNode = codec.encode(mxModelClone);
        documentHelper.importNode(modelContainerDocElement, mxModelCloneNode);
        return modelContainerDocElement;
    }

    public GateTreeElement unmarshal(Element element) throws ConvertException {
        GateTreeElement gateTreeElement = unmarshalTreeElementByDefault(element);
        ModelContainer modelContainer = (ModelContainer) gateTreeElement;
        Node mxGraphModelNode =  element.getElementsByTagName("mxGraphModel").item(0);
        mxCodec codec = new mxCodec(mxGraphModelNode.getOwnerDocument());
        mxGraphModel mxModel = new mxGraphModel();
        codec.decode(mxGraphModelNode, mxModel);

        GraphElementCodec graphElementCodec = new GraphElementCodec();
        graphElementCodec.decode(mxModel);
        modelContainer.setMxModel(mxModel);
        return gateTreeElement;
    }

    mxGraphModel deepCopy(mxGraphModel mxModel) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject(mxModel);
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream in = new ObjectInputStream(bis);
        mxGraphModel mxModelClone = (mxGraphModel) in.readObject();
        return mxModelClone;
    }

}
