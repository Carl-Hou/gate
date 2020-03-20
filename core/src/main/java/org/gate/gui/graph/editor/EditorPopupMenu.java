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

import javax.swing.JPopupMenu;
import javax.swing.TransferHandler;
import com.mxgraph.swing.util.mxGraphActions;
import com.mxgraph.util.mxResources;
import org.gate.gui.graph.common.GraphMenuFactory;

public class EditorPopupMenu extends JPopupMenu
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3132749140550242191L;

	public EditorPopupMenu(BasicGraphEditor editor)
	{
		boolean selected = !editor.getGraphComponent().getGraph()
				.isSelectionEmpty();
		add(GraphMenuFactory.getAddToModelMenu());
		addSeparator();

		add(editor.bind(mxResources.get("undo"), new EditorActions.HistoryAction(true),
				"/org/gate/images/undo.gif"));
		addSeparator();

		add(
				editor.bind(mxResources.get("cut"), TransferHandler
						.getCutAction(),
						"/org/gate/images/cut.gif"))
				.setEnabled(selected);
		add(
				editor.bind(mxResources.get("copy"), TransferHandler
						.getCopyAction(),
						"/org/gate/images/copy.gif"))
				.setEnabled(selected);
		add(editor.bind(mxResources.get("paste"), TransferHandler
				.getPasteAction(),
				"/org/gate/images/paste.gif"));
		addSeparator();

		add(
				editor.bind(mxResources.get("delete"), mxGraphActions
						.getDeleteAction(),
						"/org/gate/images/delete.gif"))
				.setEnabled(selected);

		addSeparator();

		add(editor.bind(mxResources.get("selectVertices"), mxGraphActions
				.getSelectVerticesAction()));
		add(editor.bind(mxResources.get("selectEdges"), mxGraphActions
				.getSelectEdgesAction()));

		addSeparator();

		add(editor.bind(mxResources.get("selectAll"), mxGraphActions
				.getSelectAllAction()));
	}

}
