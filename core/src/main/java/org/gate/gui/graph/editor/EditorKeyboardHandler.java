/**
 * Copyright (c) 2008, Gaudenz Alder
 */
package org.gate.gui.graph.editor;

import javax.swing.*;

import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.handler.mxKeyboardHandler;
import com.mxgraph.swing.util.mxGraphActions;

/**
 * @author Administrator
 * 
 */
public class EditorKeyboardHandler extends mxKeyboardHandler
{

	/**
	 * 
	 * @param graphComponent
	 */
	public EditorKeyboardHandler(mxGraphComponent graphComponent)
	{
		super(graphComponent);
	}

	// Override this for disable the some unsupported options
	@Override
	protected InputMap getInputMap(int condition) {
		InputMap map = null;
		if (condition == 1) {
			map = (InputMap) UIManager.get("ScrollPane.ancestorInputMap");
		} else if (condition == 0) {
			map = new InputMap();
			/*
			 * unsupported options
			map.put(KeyStroke.getKeyStroke("F2"), "edit");
			map.put(KeyStroke.getKeyStroke("UP"), "selectParent");
			map.put(KeyStroke.getKeyStroke("DOWN"), "selectChild");
			map.put(KeyStroke.getKeyStroke("RIGHT"), "selectNext");
			map.put(KeyStroke.getKeyStroke("LEFT"), "selectPrevious");
		    map.put(KeyStroke.getKeyStroke("PAGE_DOWN"), "enterGroup");
			map.put(KeyStroke.getKeyStroke("PAGE_UP"), "exitGroup");
			map.put(KeyStroke.getKeyStroke("HOME"), "home");
			map.put(KeyStroke.getKeyStroke("ENTER"), "expand");
			map.put(KeyStroke.getKeyStroke("BACK_SPACE"), "collapse");
			map.put(KeyStroke.getKeyStroke("control G"), "group");
			map.put(KeyStroke.getKeyStroke("control U"), "ungroup");
			map.put(KeyStroke.getKeyStroke("control shift V"),"selectVertices");
			map.put(KeyStroke.getKeyStroke("control shift E"), "selectEdges");
			 */
			map.put(KeyStroke.getKeyStroke("DELETE"), "delete");
			map.put(KeyStroke.getKeyStroke("control A"), "selectAll");
			map.put(KeyStroke.getKeyStroke("control D"), "selectNone");
			map.put(KeyStroke.getKeyStroke("control X"), "cut");
			map.put(KeyStroke.getKeyStroke("CUT"), "cut");
			map.put(KeyStroke.getKeyStroke("control C"), "copy");
			map.put(KeyStroke.getKeyStroke("COPY"), "copy");
			map.put(KeyStroke.getKeyStroke("control V"), "paste");
			map.put(KeyStroke.getKeyStroke("PASTE"), "paste");
			map.put(KeyStroke.getKeyStroke("control ADD"), "zoomIn");
			map.put(KeyStroke.getKeyStroke("control SUBTRACT"), "zoomOut");
			map.put(KeyStroke.getKeyStroke("control Z"), "undo");
			map.put(KeyStroke.getKeyStroke("control Y"), "redo");
		}

		return map;
	}

	/**
	 * Return the mapping between JTree's input map and JGraph's action.
	 */
    protected ActionMap createActionMap()
    {
        ActionMap map = (ActionMap) UIManager.get("ScrollPane.actionMap");
        /*
        map.put("home", mxGraphActions.getHomeAction());
        map.put("enterGroup", mxGraphActions.getEnterGroupAction());
        map.put("exitGroup", mxGraphActions.getExitGroupAction());
        map.put("collapse", mxGraphActions.getCollapseAction());
        map.put("expand", mxGraphActions.getExpandAction());
        map.put("toBack", mxGraphActions.getToBackAction());
        map.put("toFront", mxGraphActions.getToFrontAction());
        map.put("edit", mxGraphActions.getEditAction());
        map.put("selectNext", mxGraphActions.getSelectNextAction());
        map.put("selectPrevious", mxGraphActions.getSelectPreviousAction());
        map.put("selectParent", mxGraphActions.getSelectParentAction());
        map.put("selectChild", mxGraphActions.getSelectChildAction());
        map.put("group", mxGraphActions.getGroupAction());
        map.put("ungroup", mxGraphActions.getUngroupAction());
        map.put("selectVertices", mxGraphActions.getSelectVerticesAction());
        map.put("selectEdges", mxGraphActions.getSelectEdgesAction());
         */

        map.put("delete", mxGraphActions.getDeleteAction());
        map.put("selectNone", mxGraphActions.getSelectNoneAction());
        map.put("selectAll", mxGraphActions.getSelectAllAction());
        map.put("cut", TransferHandler.getCutAction());
        map.put("copy", TransferHandler.getCopyAction());
        map.put("paste", TransferHandler.getPasteAction());
        map.put("zoomIn", mxGraphActions.getZoomInAction());
        map.put("zoomOut", mxGraphActions.getZoomOutAction());
        map.put("undo", new EditorActions.HistoryAction(true));
		map.put("redo", new EditorActions.HistoryAction(false));

        return map;
    }


}
