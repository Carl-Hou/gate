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

package org.gate.gui;

import javax.swing.*;

import org.gate.gui.actions.*;

import com.mxgraph.swing.util.mxGraphActions;
import com.mxgraph.util.mxConstants;
import org.gate.gui.graph.editor.BasicGraphEditor;
import org.gate.gui.graph.editor.EditorActions;

import java.awt.*;

public class GateToolBar extends JToolBar
{

    private static final long serialVersionUID = -8015443128436394471L;

    JButton testStartButton = null;
    JButton testStopButton = null;

    public GateToolBar(final BasicGraphEditor editor, int orientation)
    {
        super(orientation);
        setBorder(BorderFactory.createCompoundBorder(BorderFactory
                .createEmptyBorder(3, 3, 3, 3), getBorder()));
        setFloatable(false);
        add(createButton("/org/gate/images/open.gif", "Open test file", ActionNames.OPEN));
        add(createButton("/org/gate/images/save.gif", "Save test file", ActionNames.SAVE));
        addSeparator();
        add(editor.bind("Cut", TransferHandler.getCutAction(),"/org/gate/images/cut.gif"))
                .setToolTipText("Cut nodes on graph editor");
        add(editor.bind("Copy", TransferHandler.getCopyAction(),"/org/gate/images/copy.gif"))
                .setToolTipText("Copy nodes on graph editor");
        add(editor.bind("Paste", TransferHandler.getPasteAction(),"/org/gate/images/paste.gif"))
                .setToolTipText("Paste nodes on graph editor");
        add(editor.bind("Delete", mxGraphActions.getDeleteAction(),"/org/gate/images/delete.gif"))
                .setToolTipText("Delete nodes on graph editor");
        add(editor.bind("Undo", new EditorActions.HistoryAction(true),"/org/gate/images/undo.gif"))
                .setToolTipText("Undo edit on graph editor");
        add(editor.bind("Redo", new EditorActions.HistoryAction(false),"/org/gate/images/redo.gif"))
                .setToolTipText("Redo edit on graph editor");
        addSeparator();
        add(editor.bind("Left", new EditorActions.KeyValueAction(mxConstants.STYLE_ALIGN, mxConstants.ALIGN_LEFT),
                "/org/gate/images/left.gif")).setToolTipText("Align left of text on graph editor");
        add(editor.bind("Center", new EditorActions.KeyValueAction(mxConstants.STYLE_ALIGN, mxConstants.ALIGN_CENTER),
                "/org/gate/images/center.gif")).setToolTipText("Align center of text on graph editor");
        add(editor.bind("Right", new EditorActions.KeyValueAction(mxConstants.STYLE_ALIGN,mxConstants.ALIGN_RIGHT),
                "/org/gate/images/right.gif")).setToolTipText("Align right of text on graph editor");
        addSeparator();
        add(editor.bind("Font", new EditorActions.ColorAction("Font", mxConstants.STYLE_FONTCOLOR),
                "/org/gate/images/fontcolor.gif")).setToolTipText("Set text color of graph editor");
        add(editor.bind("Stroke", new EditorActions.ColorAction("Stroke", mxConstants.STYLE_STROKECOLOR),
                "/org/gate/images/linecolor.gif")).setToolTipText("Set line color of graph editor");
        addSeparator();
        // add by Carl for Gate.
        testStartButton = createButton("/org/gate/images/arrow-right-3.png",
                "run selected test cases",ActionNames.ACTION_START);
        add(testStartButton);
        testStopButton = createButton("/org/gate/images/road-sign-us-stop.png",
                "stop test", ActionNames.ACTION_STOP);
        add(testStopButton);
        testStopButton.setEnabled(false);
    }

    public void testStart(){
        testStartButton.setEnabled(false);
        testStopButton.setEnabled(true);
    }

    public void testEnd(){
        testStartButton.setEnabled(true);
        testStopButton.setEnabled(false);
    }
    private JButton createButton(String iconUrl, String tipText, String actionName){
        ImageIcon imageIcon = new ImageIcon(BasicGraphEditor.class.getResource(iconUrl));
        // to keep the tool bar icons in same size.
        imageIcon.setImage(imageIcon.getImage().getScaledInstance(16,16, Image.SCALE_DEFAULT));
        JButton button = new JButton(imageIcon);
        button.setToolTipText(tipText);
        button.setActionCommand(actionName);
        button.addActionListener(ActionRouter.getInstance());
        return button;
    }

}
