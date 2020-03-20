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

import java.awt.event.MouseEvent;
import java.util.HashMap;

import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;
import org.gate.gui.GuiPackage;
import org.gate.gui.common.OptionPane;
import org.gate.gui.details.properties.graph.GraphElementPropertiesGui;
import org.gate.gui.graph.elements.GraphElement;

import javax.swing.*;

public class GateGraphEditor extends BasicGraphEditor {

    private static final long serialVersionUID = 1L;
    static HashMap<String, GraphElementPropertiesGui> elementGuiCache = new HashMap<>();

    public GateGraphEditor(String appTitle, mxGraphComponent component) {
        super(appTitle, component);
    }

//	implement method called by the GraphComponent MouseLisener
    @Override
    void onMousePressedOnMxCell(MouseEvent e) {
//        Point pt = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), graphComponent);
        mxGraph graph = getGraphComponent().getGraph();
        if (graph.isSelectionEmpty()) {
            return;
          }

        mxCell cell = (mxCell) getGraphComponent().getGraph().getSelectionCell();

        if (cell.getValue() instanceof String) {
            return;
        }
        GraphElement element = (GraphElement) cell.getValue();
        try {
            GraphElementPropertiesGui gui = null;
            String guiClassName = element.getGUI();
            if(elementGuiCache.containsKey(guiClassName)){
                gui = elementGuiCache.get(guiClassName);
            }else{
                gui = (GraphElementPropertiesGui) Class.forName(guiClassName).newInstance();
                elementGuiCache.put(guiClassName, gui);
            }
            gui.setCell(graph, cell);
            GuiPackage.getIns().getPropertiesPanel().setComponent((JComponent) gui);
        } catch (Exception ex) {
            ex.printStackTrace();
            OptionPane.showErrorMessageDialog("Fatal Error:", ex);
        }
        GuiPackage.getIns().getMainFrame().activeDetailsTabProperties();
    }

}
