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

import java.awt.event.*;
import java.util.List;

import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import org.gate.gui.actions.ActionNames;
import org.gate.gui.actions.ActionRouter;

import com.mxgraph.analysis.mxAnalysisGraph;
import com.mxgraph.analysis.mxGraphStructure;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.util.mxGraphActions;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxResources;
import com.mxgraph.view.mxGraph;
import org.gate.gui.actions.KeyStrokes;
import org.gate.gui.actions.LoadRecentProject;
import org.gate.gui.common.OptionPane;
import org.gate.gui.graph.editor.BasicGraphEditor;
import org.gate.gui.graph.editor.EditorActions;

public class GateMenuBar extends JMenuBar
{
	JMenuItem testStart = null;
	JMenuItem testEnd = null;

	public enum AnalyzeType
	{
		IS_CONNECTED, IS_SIMPLE, IS_CYCLIC_DIRECTED, IS_CYCLIC_UNDIRECTED, COMPLEMENTARY, REGULARITY, COMPONENTS, MAKE_CONNECTED, MAKE_SIMPLE, IS_TREE, ONE_SPANNING_TREE, IS_DIRECTED, GET_CUT_VERTEXES, GET_CUT_EDGES, GET_SOURCES, GET_SINKS, PLANARITY, IS_BICONNECTED, GET_BICONNECTED, SPANNING_TREE, FLOYD_ROY_WARSHALL
	}

	public GateMenuBar(final BasicGraphEditor editor)
	{
		final mxGraphComponent graphComponent = editor.getGraphComponent();
		final mxGraph graph = graphComponent.getGraph();
		mxAnalysisGraph aGraph = new mxAnalysisGraph();

		JMenu menu = null;
		JMenu submenu = null;

		// Creates the file menu
		menu = add(new JMenu("File"));
		menu.add(createItem("New", ActionNames.NEW));
		menu.addSeparator();
		menu.add(createItem("Open", ActionNames.OPEN, KeyStrokes.OPEN));

		JMenu recentFileMenu = new JMenu("Open Recent");
		recentFileMenu.addMenuListener(new MenuListener() {
			@Override
			public void menuSelected(MenuEvent e) {
				JMenu recentFileMenu = (JMenu) e.getSource();
				recentFileMenu.removeAll();
				List<JComponent> recentFileMenuItems = LoadRecentProject.getRecentFileMenuItems();
				if(recentFileMenuItems.size() > 0) {
					recentFileMenuItems.forEach(jc -> {
						recentFileMenu.add(jc);
					});
				}else{
					recentFileMenu.add("No Recent selected file");
				}
			}
			@Override
			public void menuDeselected(MenuEvent e) {}
			@Override
			public void menuCanceled(MenuEvent e) {}
		});
		menu.add(recentFileMenu);
		menu.addSeparator();

		menu.add(createItem("save", ActionNames.SAVE, KeyStrokes.SAVE));
		menu.add(createItem("save as", ActionNames.SAVE_AS, KeyStrokes.SAVE_ALL_AS));

		// Creates the format menu
		menu = add(new JMenu("Cell"));

		populateFormatMenu(menu, editor);


		// Creates the view menu
		menu = add(new JMenu("Editor"));

		submenu = (JMenu) menu.add(new JMenu(mxResources.get("grid")));
		submenu.add(editor.bind(mxResources.get("gridSize"), new EditorActions.PromptPropertyAction(graph, "Grid Size", "GridSize")));
		submenu.add(editor.bind(mxResources.get("gridColor"), new EditorActions.GridColorAction()));
		submenu.addSeparator();
		submenu.add(editor.bind(mxResources.get("dashed"), new EditorActions.GridStyleAction(mxGraphComponent.GRID_STYLE_DASHED)));
		submenu.add(editor.bind(mxResources.get("dot"), new EditorActions.GridStyleAction(mxGraphComponent.GRID_STYLE_DOT)));
		submenu.add(editor.bind(mxResources.get("line"), new EditorActions.GridStyleAction(mxGraphComponent.GRID_STYLE_LINE)));
		submenu.add(editor.bind(mxResources.get("cross"), new EditorActions.GridStyleAction(mxGraphComponent.GRID_STYLE_CROSS)));

		menu.addSeparator();
		submenu = (JMenu) menu.add(new JMenu(mxResources.get("layout")));

		submenu.add(editor.graphLayout("verticalHierarchical", true));
		submenu.add(editor.graphLayout("horizontalHierarchical", true));

		submenu.addSeparator();

		submenu = (JMenu) menu.add(new JMenu(mxResources.get("selection")));

		submenu.add(editor.bind(mxResources.get("selectPath"), new EditorActions.SelectShortestPathAction(false)));
		submenu.add(editor.bind(mxResources.get("selectDirectedPath"), new EditorActions.SelectShortestPathAction(true)));

		menu.addSeparator();

		submenu = (JMenu) menu.add(new JMenu(mxResources.get("zoom")));

		submenu.add(editor.bind("400%", new EditorActions.ScaleAction(4)));
		submenu.add(editor.bind("200%", new EditorActions.ScaleAction(2)));
		submenu.add(editor.bind("150%", new EditorActions.ScaleAction(1.5)));
		submenu.add(editor.bind("100%", new EditorActions.ScaleAction(1)));
		submenu.add(editor.bind("75%", new EditorActions.ScaleAction(0.75)));
		submenu.add(editor.bind("50%", new EditorActions.ScaleAction(0.5)));

		submenu.addSeparator();

		submenu.add(editor.bind(mxResources.get("custom"), new EditorActions.ScaleAction(0)));

		menu.add(editor.bind(mxResources.get("zoomIn"), mxGraphActions.getZoomInAction()));
		menu.add(editor.bind(mxResources.get("zoomOut"), mxGraphActions.getZoomOutAction()));

		menu.add(editor.bind(mxResources.get("actualSize"), mxGraphActions.getZoomActualAction()));

		// Creates the run menu
		menu = add(new JMenu("Run"));
		testStart = createItem("Start", ActionNames.ACTION_START, KeyStrokes.ACTION_START);
		menu.add(testStart);
		menu.addSeparator();
		testEnd = createItem("Stop", ActionNames.ACTION_STOP, KeyStrokes.ACTION_SHUTDOWN);
		testEnd.setEnabled(false);
		menu.add(testEnd);

		// Creates the options menu
		menu = add(new JMenu(mxResources.get("options")));

		menu.add(new EditorActions.ToggleOutlineItem(editor, mxResources.get("outline")));
		menu.addSeparator();
		menu.add(new EditorActions.ToggleGridItem(editor, mxResources.get("grid")));
		menu.add(new EditorActions.ToggleRulersItem(editor, mxResources.get("rulers")));
		menu.addSeparator();
		/* Don't know what's the use of these. comment these out.
		menu.add(new EditorActions.TogglePropertyItem(graphComponent, mxResources.get("antialias"), "AntiAlias", true));
		submenu = (JMenu) menu.add(new JMenu(mxResources.get("display")));
		submenu.add(new EditorActions.TogglePropertyItem(graphComponent, mxResources.get("buffering"), "TripleBuffered", true));

		submenu.add(new EditorActions.TogglePropertyItem(graphComponent, mxResources.get("preferPageSize"), "PreferPageSize", true, new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				graphComponent.zoomAndCenter();
			}
		}));
		submenu.addSeparator();
		menu.addSeparator();
 		*/
		submenu = (JMenu) menu.add(new JMenu(mxResources.get("zoom")));

		submenu.add(new EditorActions.TogglePropertyItem(graphComponent, mxResources.get("centerZoom"), "CenterZoom", true));
		submenu.add(new EditorActions.TogglePropertyItem(graphComponent, mxResources.get("zoomToSelection"), "KeepSelectionVisibleOnZoom", true));

		submenu.addSeparator();

		submenu.add(new EditorActions.TogglePropertyItem(graphComponent, mxResources.get("centerPage"), "CenterPage", true, new ActionListener()
		{
			/**
			 * 
			 */
			public void actionPerformed(ActionEvent e)
			{
				if (graphComponent.isPageVisible() && graphComponent.isCenterPage())
				{
					graphComponent.zoomAndCenter();
				}
			}
		}));

		menu.addSeparator();
		submenu = (JMenu) menu.add(new JMenu("Analyze"));
		submenu.add(editor.bind("Is Connected", new AnalyzeGraph(AnalyzeType.IS_CONNECTED, aGraph)));
		menu.addSeparator();
		submenu = (JMenu) menu.add(new JMenu("Log Level"));
		submenu.add(createItem("ERROR", ActionNames.LOG_LEVEL_ERROR));
		submenu.add(createItem("WARN", ActionNames.LOG_LEVEL_WARN));
		submenu.add(createItem("INFO", ActionNames.LOG_LEVEL_INFO));
		submenu.add(createItem("DEBUG", ActionNames.LOG_LEVEL_DEBUG));
		submenu.add(createItem("TRACE", ActionNames.LOG_LEVEL_TRACE));

		menu.add(createItem("SSL Manager", ActionNames.SSL_MANAGER));

		// Creates the help menu
		menu = add(new JMenu(mxResources.get("help")));

		JMenuItem item = menu.add(new JMenuItem("about gate"));
		item.addActionListener(new ActionListener()
		{
			/*
			 * (non-Javadoc)
			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
			 */
			public void actionPerformed(ActionEvent e)
			{
				//JFrame frame = (JFrame) SwingUtilities.windowForComponent(MainFrame.gateMenuBar);
				JFrame frame = (JFrame) SwingUtilities.windowForComponent(GuiPackage.getIns().getMainFrame().gateMenuBar);
				if (frame != null)
				{
					GateAbout about = new GateAbout(frame);
					about.setModal(true);

					// Centers inside the application frame
					int x = frame.getX() + (frame.getWidth() - about.getWidth()) / 2;
					int y = frame.getY() + (frame.getHeight() - about.getHeight()) / 2;
					about.setLocation(x, y);

					// Shows the modal dialog and waits
					about.setVisible(true);
				}

			}
		});
	}

	/**
	 * Adds menu items to the given format menu. This is factored out because
	 * the format menu appears in the menubar and also in the popupmenu.
	 */
	public static void populateFormatMenu(JMenu menu, BasicGraphEditor editor)
	{
		JMenu

		submenu = (JMenu) menu.add(new JMenu(mxResources.get("label")));

		submenu.add(editor.bind(mxResources.get("fontcolor"), new EditorActions.ColorAction("Fontcolor", mxConstants.STYLE_FONTCOLOR),
				"/org/gate/images/fontcolor.gif"));

		submenu.addSeparator();

		submenu.add(editor.bind(mxResources.get("labelFill"), new EditorActions.ColorAction("Label Fill", mxConstants.STYLE_LABEL_BACKGROUNDCOLOR)));
		submenu.add(editor.bind(mxResources.get("labelBorder"), new EditorActions.ColorAction("Label Border", mxConstants.STYLE_LABEL_BORDERCOLOR)));

		submenu.addSeparator();

		submenu.add(editor.bind(mxResources.get("rotateLabel"), new EditorActions.ToggleAction(mxConstants.STYLE_HORIZONTAL, true)));

		submenu.addSeparator();

		JMenu subsubmenu = (JMenu) submenu.add(new JMenu(mxResources.get("position")));

		subsubmenu.add(editor.bind(mxResources.get("top"), new EditorActions.SetLabelPositionAction(mxConstants.ALIGN_TOP, mxConstants.ALIGN_BOTTOM)));
		subsubmenu.add(editor.bind(mxResources.get("middle"),
				new EditorActions.SetLabelPositionAction(mxConstants.ALIGN_MIDDLE, mxConstants.ALIGN_MIDDLE)));
		subsubmenu.add(editor.bind(mxResources.get("bottom"), new EditorActions.SetLabelPositionAction(mxConstants.ALIGN_BOTTOM, mxConstants.ALIGN_TOP)));

		subsubmenu.addSeparator();

		subsubmenu.add(editor.bind(mxResources.get("left"), new EditorActions.SetLabelPositionAction(mxConstants.ALIGN_LEFT, mxConstants.ALIGN_RIGHT)));
		subsubmenu.add(editor.bind(mxResources.get("center"),
				new EditorActions.SetLabelPositionAction(mxConstants.ALIGN_CENTER, mxConstants.ALIGN_CENTER)));
		subsubmenu.add(editor.bind(mxResources.get("right"), new EditorActions.SetLabelPositionAction(mxConstants.ALIGN_RIGHT, mxConstants.ALIGN_LEFT)));

		submenu.addSeparator();

		submenu.add(editor.bind(mxResources.get("wordWrap"), new EditorActions.KeyValueAction(mxConstants.STYLE_WHITE_SPACE, "wrap")));
		submenu.add(editor.bind(mxResources.get("noWordWrap"), new EditorActions.KeyValueAction(mxConstants.STYLE_WHITE_SPACE, null)));

		submenu = (JMenu) menu.add(new JMenu(mxResources.get("alignment")));

		submenu.add(editor.bind(mxResources.get("left"), new EditorActions.KeyValueAction(mxConstants.STYLE_ALIGN, mxConstants.ALIGN_LEFT),
				"/org/gate/images/left.gif"));
		submenu.add(editor.bind(mxResources.get("center"), new EditorActions.KeyValueAction(mxConstants.STYLE_ALIGN, mxConstants.ALIGN_CENTER),
				"/org/gate/images/center.gif"));
		submenu.add(editor.bind(mxResources.get("right"), new EditorActions.KeyValueAction(mxConstants.STYLE_ALIGN, mxConstants.ALIGN_RIGHT),
				"/org/gate/images/right.gif"));

		submenu.addSeparator();

		submenu.add(editor.bind(mxResources.get("top"), new EditorActions.KeyValueAction(mxConstants.STYLE_VERTICAL_ALIGN, mxConstants.ALIGN_TOP),
				"/org/gate/images/top.gif"));
		submenu.add(editor.bind(mxResources.get("middle"), new EditorActions.KeyValueAction(mxConstants.STYLE_VERTICAL_ALIGN, mxConstants.ALIGN_MIDDLE),
				"/org/gate/images/middle.gif"));
		submenu.add(editor.bind(mxResources.get("bottom"), new EditorActions.KeyValueAction(mxConstants.STYLE_VERTICAL_ALIGN, mxConstants.ALIGN_BOTTOM),
				"/org/gate/images/bottom.gif"));

		submenu = (JMenu) menu.add(new JMenu(mxResources.get("spacing")));

		submenu.add(editor.bind(mxResources.get("top"), new EditorActions.PromptValueAction(mxConstants.STYLE_SPACING_TOP, "Top Spacing")));
		submenu.add(editor.bind(mxResources.get("right"), new EditorActions.PromptValueAction(mxConstants.STYLE_SPACING_RIGHT, "Right Spacing")));
		submenu.add(editor.bind(mxResources.get("bottom"), new EditorActions.PromptValueAction(mxConstants.STYLE_SPACING_BOTTOM, "Bottom Spacing")));
		submenu.add(editor.bind(mxResources.get("left"), new EditorActions.PromptValueAction(mxConstants.STYLE_SPACING_LEFT, "Left Spacing")));

		menu.addSeparator();

		submenu = (JMenu) menu.add(new JMenu(mxResources.get("line")));

		submenu.add(editor.bind(mxResources.get("linecolor"), new EditorActions.ColorAction("Linecolor", mxConstants.STYLE_STROKECOLOR),
				"/org/gate/images/linecolor.gif"));

		submenu.addSeparator();

		submenu.add(editor.bind(mxResources.get("linewidth"), new EditorActions.PromptValueAction(mxConstants.STYLE_STROKEWIDTH, "Linewidth")));

		submenu = (JMenu) menu.add(new JMenu(mxResources.get("connector")));

		submenu.add(editor.bind(mxResources.get("straight"), new EditorActions.SetStyleAction("straight"),
				"/org/gate/images/straight.gif"));

		submenu.add(editor.bind(mxResources.get("horizontal"), new EditorActions.SetStyleAction(""), "/org/gate/images/connect.gif"));
		submenu.add(editor.bind(mxResources.get("vertical"), new EditorActions.SetStyleAction("vertical"),
				"/org/gate/images/vertical.gif"));
	}

	public void setRunning(){
		testStart.setEnabled(false);
		testEnd.setEnabled(true);
	}

	public void setEnd(){
		testStart.setEnabled(true);
		testEnd.setEnabled(false);
	}

	public static class AnalyzeGraph extends AbstractAction
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 6926170745240507985L;

		mxAnalysisGraph aGraph;

		/**
		 * 
		 */
		protected AnalyzeType analyzeType;

		/**
		 * Examples for calling analysis methods from mxGraphStructure 
		 */
		public AnalyzeGraph(AnalyzeType analyzeType, mxAnalysisGraph aGraph)
		{
			this.analyzeType = analyzeType;
			this.aGraph = aGraph;
		}

		public void actionPerformed(ActionEvent e)
		{
			if(!GuiPackage.getIns().getMainFrame().isModelEditorOpen()){
				OptionPane.showMessageDialog("Editor is closed");
				return;
			}

			if (e.getSource() instanceof mxGraphComponent)
			{
				mxGraphComponent graphComponent = (mxGraphComponent) e.getSource();
				mxGraph graph = graphComponent.getGraph();
				aGraph.setGraph(graph);

				if (analyzeType == AnalyzeType.IS_CONNECTED)
				{

					if(aGraph.getChildVertices(aGraph.getGraph().getDefaultParent()).length <2){
						OptionPane.showMessageDialog("Add two vertices first");
					}else{
						boolean isConnected = mxGraphStructure.isConnected(aGraph);

						if (isConnected) {
							OptionPane.showMessageDialog("The graph is connected");
						} else {
							OptionPane.showMessageDialog("The graph is not connected");
						}
					}
				}
			}
		}
	};

	private JMenuItem createItem(String name, String actionCommandName, KeyStroke keyStroke){
		JMenuItem item = new JMenuItem(name);
		item.setActionCommand(actionCommandName);
		item.addActionListener(ActionRouter.getInstance());
		if(keyStroke != null){
			item.setAccelerator(keyStroke);
		}
		return item;
	}

	private JMenuItem createItem(String name, String actionCommandName){
		return createItem(name, actionCommandName, null);

	}
}