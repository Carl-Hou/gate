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

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGraphModel;
import org.gate.gui.graph.elements.GraphElement;
import org.gate.saveload.convert.graph.GraphElementConverter;
import org.gate.saveload.convert.graph.GraphElementConverterRegistry;
import org.gate.saveload.utils.exceptions.ConvertException;
import org.gate.saveload.utils.DocumentHelper;
import org.w3c.dom.Element;

import java.util.Collection;

public class GraphElementCodec {

    DocumentHelper documentHelper = null;
    public GraphElementCodec(DocumentHelper documentHelper){
        this.documentHelper = documentHelper;
    }

    public GraphElementCodec(){
        this.documentHelper = new DocumentHelper();
    }

    public void encode(mxGraphModel mxModel) throws ConvertException {

        Collection<mxCell> cells = (Collection) mxModel.getCells().values();

        for(mxCell cell : cells){
            Object value = cell.getValue();
            if(value instanceof GraphElement){
                GraphElement graphElement = (GraphElement) value;
                GraphElementConverter converter = GraphElementConverterRegistry.getInstance().getConverter(graphElement);
                converter.setDocumentHelper(documentHelper);
                Element element = converter.marshal(graphElement);
                cell.setValue(element);
            }
//          do nothing if user object is no an GraphElement
        }
    }

    public void decode(mxGraphModel mxModel) throws ConvertException {
//        DocumentHelper documentHelper = v1 DocumentHelper();
        Collection<mxCell> cells = (Collection) mxModel.getCells().values();
        for(mxCell cell : cells){
            Object value = cell.getValue();
            if(value instanceof Element){
                Element element = (Element) value;
                GraphElementConverter converter = GraphElementConverterRegistry.getInstance().getConverter(element);
                converter.setDocumentHelper(documentHelper);
                GraphElement graphElement = converter.unmarshal(element);
                cell.setValue(graphElement);
            }
        }
    }

}
