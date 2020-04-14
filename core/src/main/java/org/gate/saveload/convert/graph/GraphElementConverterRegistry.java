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

package org.gate.saveload.convert.graph;

import org.gate.common.util.GateClassUtils;
import org.gate.gui.graph.elements.GraphElement;


import java.util.ArrayList;
import java.util.Optional;

public class GraphElementConverterRegistry {

    private static GraphElementConverterRegistry ourInstance = new GraphElementConverterRegistry();

    private ArrayList<GraphElementConverter> graphElementConverters = new ArrayList<>();

    public static GraphElementConverterRegistry getInstance() {
        return ourInstance;
    }

    private GraphElementConverterRegistry() {
        /*
        * Some times Class name cause trouble when use mxCodec. e.g Timer. mxCodec will try java.util.Timer firstly.
        * */

        GateClassUtils.getIns().getGraphElements().values().forEach( graphElementClasses -> {
            graphElementClasses.forEach(graphElementClass ->{
                addDefaultConverter(graphElementClass);
            });
        });

    }

    void addDefaultConverter(Class graphElementClass){
        graphElementConverters.add(new DefaultGraphElementConverter(graphElementClass));
    }

    public GraphElementConverter getConverter(GraphElement graphElement){
        return graphElementConverters.stream().filter(c -> c.is(graphElement)).findFirst().get();
    }

    public Optional<GraphElementConverter> getConverter(String tagName){
        return graphElementConverters.stream().filter(c -> c.is(tagName)).findFirst();
    }

    public GraphElementConverter getConverter(org.w3c.dom.Element element){
        return getConverter(element.getTagName()).get();
    }

}
