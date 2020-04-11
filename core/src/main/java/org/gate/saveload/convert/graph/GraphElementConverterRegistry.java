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

import org.gate.gui.graph.elements.comment.Comments;
import org.gate.gui.graph.elements.GraphElement;
import org.gate.gui.graph.elements.asseration.ResponseAssert;
import org.gate.gui.graph.elements.asseration.VariableAssert;
import org.gate.gui.graph.elements.config.UserDefineVariables;
import org.gate.gui.graph.elements.control.Decide;
import org.gate.gui.graph.elements.control.Start;
import org.gate.gui.graph.elements.control.ConstantTimer;
import org.gate.gui.graph.elements.control.ActionReference;
import org.gate.gui.graph.elements.sampler.DebugSampler;
import org.gate.gui.graph.elements.sampler.JSR223Sampler;
import org.gate.gui.graph.elements.sampler.protocol.http.HttpRequestSampler;
import org.gate.gui.graph.elements.sampler.protocol.selenium.*;
import org.gate.gui.graph.extractor.JSONExtractor;
import org.gate.gui.graph.extractor.RegexExtractor;
import org.gate.gui.graph.extractor.XPathExtractor;

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

        addDefualtConverter(Start.class);
        addDefualtConverter(DebugSampler.class);
        addDefualtConverter(Decide.class);
        addDefualtConverter(ConstantTimer.class);
        addDefualtConverter(ResponseAssert.class);
        addDefualtConverter(VariableAssert.class);
        addDefualtConverter(XPathExtractor.class);
        addDefualtConverter(JSONExtractor.class);
        addDefualtConverter(RegexExtractor.class);
        addDefualtConverter(UserDefineVariables.class);
        addDefualtConverter(JSR223Sampler.class);
        addDefualtConverter(HttpRequestSampler.class);
        addDefualtConverter(ActionReference.class);
        // comments
        addDefualtConverter(Comments.class);

        // Selenium element
        addDefualtConverter(Driver.class);
        addDefualtConverter(Element.class);
        addDefualtConverter(ConditionChecker.class);
        addDefualtConverter(Timeouts.class);
        addDefualtConverter(Alert.class);
        addDefualtConverter(Window.class);
        addDefualtConverter(Navigation.class);
        addDefualtConverter(TargetLocator.class);

    }

    void addDefualtConverter(Class graphElementClass){
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
