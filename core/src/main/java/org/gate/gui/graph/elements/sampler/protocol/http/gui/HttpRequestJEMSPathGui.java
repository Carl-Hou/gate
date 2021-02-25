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
package org.gate.gui.graph.elements.sampler.protocol.http.gui;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gate.gui.GuiPackage;
import org.gate.gui.details.properties.ArgumentsPane;
import org.gate.gui.graph.elements.GraphElement;
import org.gate.gui.graph.elements.sampler.protocol.http.HttpRequestJMESPath;

import javax.swing.*;

public class HttpRequestJEMSPathGui extends HttpAbstractGui {

    private static Logger log = LogManager.getLogger();

    ArgumentsPane argumentsPane;

    public HttpRequestJEMSPathGui(){
        argumentsPane = new ArgumentsPane();
        argumentsPane.setNameSpace(HttpRequestJMESPath.NS_JMESPATH_ARGUMENT);
        JSplitPane splitPane = new JSplitPane();

        splitPane.setLeftComponent(buildRequestPropertiesAndParametersPanel(
                (int) (GuiPackage.getIns().getPropertiesPanel().getWidth() * 0.2)
        ));

        splitPane.setRightComponent(argumentsPane);

        splitPane.setDividerLocation(
                (int) (GuiPackage.getIns().getPropertiesPanel().getWidth() * 0.8)
        );
        add(splitPane);
    }


    @Override
    void setGraphElement(GraphElement graphElement) {
        argumentsPane.setTestElement(graphElement);
    }


}
