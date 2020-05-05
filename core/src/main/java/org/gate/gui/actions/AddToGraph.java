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

package org.gate.gui.actions;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxPoint;
import org.gate.common.config.GateProps;
import org.gate.common.util.GateRuntimeExcepiton;
import org.gate.gui.GuiPackage;
import org.gate.gui.common.OptionPane;
import org.gate.gui.graph.editor.BasicGraphEditor;
import org.gate.gui.graph.elements.comment.Comment;
import org.gate.gui.graph.elements.GraphElement;
import org.gate.gui.graph.elements.asseration.Assert;
import org.gate.gui.graph.elements.config.Config;
import org.gate.gui.graph.elements.control.ActionReference;
import org.gate.gui.graph.elements.timer.Timer;
import org.gate.gui.graph.elements.control.Controller;
import org.gate.gui.graph.elements.sampler.Sampler;
import org.gate.gui.graph.elements.extractor.Extractor;
import org.gate.gui.tree.GateTreeNode;
import org.gate.gui.tree.GateTreeSupport;
import org.gate.gui.tree.action.elements.Action;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class AddToGraph extends AbstractGateAction {

    private static final Set<String> commandSet;

    static {
        Set<String> commands = new HashSet<>();
        commands.add(ActionNames.ADD_TO_Graph);
        commandSet = Collections.unmodifiableSet(commands);
    }

    public AddToGraph() {
    }

    /**
     * Gets the Set of actions this Command class responds to.
     *
     * @return the ActionNames value
     */
    @Override
    public Set<String> getActionNames() {
        return commandSet;
    }

    /**
     * Adds the specified class to the current node of the tree.
     */
    @Override
    public void doAction(ActionEvent e) {
        try {
            mxCell cell = null;
            JComponent source = (JComponent) e.getSource();
            log.info(source.getClass().getName());
            log.info(source.getName());
            log.info(Action.class.getName());
            if(Action.class.getName().equals(source.getName())){
                TreePath treePath = GuiPackage.getIns().getMainFrame().getCurrentTree().getSelectionPath();
                if(treePath != null) {
                    if (treePath.equals(GuiPackage.getIns().getMainFrame().getCurrentModePath())) {
                        OptionPane.showMessageDialog("Can't add action to itself");
                        return;
                    }else{
                        cell = createActionCell();
                    }
                }else{
                    OptionPane.showMessageDialog("No tree node is selected");
                    return;
                }

            }else{
                cell = createCell(source);
            }
            if(cell == null){
                OptionPane.showErrorMessageDialog("Error", "Fail to create graph cell");
                return;
            }
            GuiPackage.getIns().getMainFrame().getGraphEditor().addCell(cell);
        } catch (Exception err) {
            log.error("", err); // $NON-NLS-1$
            String msg = err.getMessage();
            if (msg == null) {
                msg = err.toString();
            }
            OptionPane.showErrorMessageDialog("Error", msg);
        }
    }

    mxCell createActionCell(){
        mxCell actionCell = null;
        TreePath treePath = GuiPackage.getIns().getMainFrame().getCurrentTree().getSelectionPath();

            GateTreeNode actionNode = (GateTreeNode) treePath.getLastPathComponent();
            String path = GateTreeSupport.getGateTreeNodePath(actionNode);
            ActionReference actionReference = new ActionReference();
            actionReference.init(actionNode.getName(), path);
            mxGraphComponent graphComponent =  GuiPackage.getIns().getMainFrame().getGraphEditor().getGraphComponent();
            Rectangle rect = graphComponent.getViewport().getViewRect();
            double scale = graphComponent.getGraph().getView().getScale();
            actionCell = new mxCell(actionReference,
                    new mxGeometry(Math.round((rect.width/2+ rect.x)/scale) , Math.round((rect.height/2 + rect.y)/scale), 40, 40), "rhombusImage;image=/org/gate/images/inclusive.png");
            actionCell.setVertex(true);

        return actionCell;
    }

    mxCell createCell(JComponent source){
        String sourceName = source.getName();
        BasicGraphEditor graphEditor = GuiPackage.getIns().getMainFrame().getGraphEditor();
        double scale = graphEditor.getGraphComponent().getGraph().getView().getScale();

        mxGeometry geometry = new mxGeometry((graphEditor.getCursorLocationX())/scale, (graphEditor.getCursorLocationY())/scale, 40, 40);

        if(sourceName.equals(GateProps.Success)){
            return createEdge(geometry, sourceName, "vertical;strokeColor=#009933");
        } else if(sourceName.equals(GateProps.Fail)){
            return createEdge(geometry, sourceName, "vertical;strokeColor=#FF0000");
        } else if(sourceName.equals(GateProps.Next)){
            return createEdge(geometry, sourceName, "vertical;strokeColor=#3333FF");
        }
        else if(sourceName.equals(GateProps.Note)){
            return createEdge(geometry, sourceName, "straight;startArrow=none;endArrow=none;dashed=1");
        }

        GraphElement graphElement = createGraphElement(sourceName);
        if(Comment.class.isInstance(graphElement)){
            return createVertex(geometry, graphElement, "/org/gate/images/rounded.png");
        }else if(Timer.class.isInstance(graphElement)){
            return createVertex(geometry, graphElement, "/org/gate/images/timer.png");
        }else if(Assert.class.isAssignableFrom(graphElement.getClass())){
           return createVertex(geometry, graphElement, "/org/gate/images/terminate.png");
        }else if(Config.class.isAssignableFrom(graphElement.getClass())){
            return createVertex(geometry, graphElement, "/org/gate/images/rule.png");
        }else if(Controller.class.isAssignableFrom(graphElement.getClass())){
            return createVertex(geometry, graphElement, "/org/gate/images/merge.png");
        }else if(Sampler.class.isAssignableFrom(graphElement.getClass())){
            return createVertex(geometry, graphElement, "/org/gate/images/event.png");
        }else if(Extractor.class.isAssignableFrom(graphElement.getClass())){
            return createVertex(geometry, graphElement, "/org/gate/images/link.png");
        }
        throw new GateRuntimeExcepiton("Source name is not support:" + sourceName);
    }

    mxCell createEdge(mxGeometry geometry, Object userObject, String style){
        geometry.setTerminalPoint(new mxPoint(geometry.getX(), geometry.getY()), false);
        geometry.setTerminalPoint(new mxPoint(geometry.getX() + geometry.getWidth(), geometry.getY() + geometry.getHeight()), true);
        mxCell cell = new mxCell(userObject, geometry, style);
        cell.setEdge(true);
        return cell;
    }

    mxCell createVertex(mxGeometry geometry, Object userObject, String image){

        mxCell cell = new mxCell(userObject, geometry, "roundImage;image=" + image);
        cell.setVertex(true);
        return cell;
    }

    public GraphElement createGraphElement(String objClass) {

        try {
            GraphElement element = (GraphElement) Class.forName(objClass).newInstance();
            return element;
        } catch (NoClassDefFoundError e) {
            log.error("Problem retrieving gui for " + objClass, e);
            String msg="Cannot find class: "+e.getMessage();
            OptionPane.showErrorMessageDialog("Missing jar? See log file.", e);
            throw new RuntimeException(e.toString(), e); // Probably a missing jar
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            log.error("Problem retrieving gui for " + objClass, e);
            throw new RuntimeException(e.toString(), e); // Programming error: bail out.
        }
    }


}
