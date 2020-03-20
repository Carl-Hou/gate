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
package org.gate.gui.graph.editor;

import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;

public class GateGraphComponent extends mxGraphComponent {
    /**
     * @param graph
     */
    public GateGraphComponent(mxGraph graph) {
        super(graph);
    }

    /**
     * Creates the connection-, panning and graphhandler (in this order).
     * Override this for disable create connection on click a vertex
     */
    @Override
    protected void createHandlers()
    {
        setTransferHandler(createTransferHandler());
        panningHandler = createPanningHandler();
        selectionCellsHandler = createSelectionCellsHandler();
//		Markdonw by Carl to remove the click and draw connect function
//		connectionHandler = createConnectionHandler();
        graphHandler = createGraphHandler();
    }

    /**
     * override this for disable the double click edit the cell name.
     */
    @Override
    protected void installDoubleClickHandler(){
        //
    }
}
