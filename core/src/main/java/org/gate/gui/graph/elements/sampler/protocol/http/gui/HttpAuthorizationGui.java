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

import com.mxgraph.model.mxCell;
import com.mxgraph.view.mxGraph;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gate.common.config.GateProps;
import org.gate.gui.GuiPackage;
import org.gate.gui.common.GuiUtils;
import org.gate.gui.common.OptionPane;
import org.gate.gui.common.TestElement;
import org.gate.gui.common.VerticalLayout;
import org.gate.gui.details.properties.ArgumentsPane;
import org.gate.gui.details.properties.PropertiesTable;
import org.gate.gui.details.properties.PropertiesTableModel;
import org.gate.gui.details.properties.graph.GraphElementPropertiesGui;
import org.gate.gui.details.properties.graph.GraphNamePane;
import org.gate.gui.graph.elements.GraphElement;
import org.gate.gui.graph.elements.sampler.protocol.http.HTTPConstantsInterface;
import org.gate.varfuncs.property.GateProperty;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.LinkedList;

public class HttpAuthorizationGui extends HttpAbstractGui {

    protected Logger log = LogManager.getLogger(this.getName());


    public HttpAuthorizationGui(){
        super();
    }

    @Override
    public void setCell(mxGraph graph, mxCell cell) {
        super.setCell(graph, cell);
    }


    @Override
    void updateParamterTabbedPane() {
        super.updateParamterTabbedPane();
    }

}
