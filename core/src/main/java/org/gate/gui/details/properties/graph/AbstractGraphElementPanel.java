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
package org.gate.gui.details.properties.graph;

import com.mxgraph.model.mxCell;
import com.mxgraph.view.mxGraph;
import org.gate.gui.common.VerticalLayout;
import org.gate.gui.graph.elements.GraphElement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;

public abstract class AbstractGraphElementPanel extends JPanel implements GraphElementPropertiesGui {
    protected  Logger log = LogManager.getLogger(this.getName());
    GraphNamePane namePane = new GraphNamePane();

    public AbstractGraphElementPanel(){
        setLayout(new VerticalLayout());
        add(namePane);
    }

    @Override
    public void setCell(mxGraph graph, mxCell cell) {
        namePane.setCell(graph, cell);
        GraphElement element = (GraphElement) cell.getValue();
        setTestElement(element);
    }

    protected abstract void setTestElement(GraphElement element);

}
