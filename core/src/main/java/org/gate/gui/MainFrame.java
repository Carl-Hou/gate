/**
 * Copyright (c) 2006-2012, JGraph Ltd */
package org.gate.gui;

import java.awt.BorderLayout;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;

import javax.swing.*;
import javax.swing.tree.TreePath;

import com.mxgraph.model.mxGraphModel;
import com.mxgraph.swing.mxGraphOutline;
import com.mxgraph.util.mxResources;
import org.gate.common.config.GateUIDimensions;
import org.gate.gui.actions.LoadRecentProject;
import org.gate.gui.details.properties.PropertiesPane;
import org.gate.gui.details.results.ResultsPane;
import org.gate.gui.graph.editor.BasicGraphEditor;
import org.gate.gui.graph.editor.GateGraphComponent;
import org.gate.gui.graph.editor.GateGraphEditor;
import org.gate.gui.tree.GateTree;
import org.gate.gui.tree.action.ActionTree;
import org.gate.gui.tree.test.TestTree;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;

public class MainFrame extends JFrame
{
	final static Logger log = LogManager.getLogger(MainFrame.class);
	final static String TabTitle_Properties = "properties";
	final static String TabTitle_Results = "results";

	mxGraphComponent graphComponent = null;
	BasicGraphEditor editor = null;
	mxGraphOutline graphOutline = null;

    String testFile = null;

	GateMenuBar gateMenuBar = null;
	GateToolBar gateToolBar = null;
	JLabel statusBar = null;
	JTabbedPane testAndLibPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);

	TestTree testTree = null;
	ActionTree actionTree = null;
	ResultsPane resultsPane = null;
	PropertiesPane propertiesPanel = null;

	JTabbedPane detailsTabPane = new JTabbedPane();
	JSplitPane workspace = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
	JSplitPane treesAndEditor = new JSplitPane();
	JSplitPane detailsAndOutline = new JSplitPane();

	TreePath currentModePath= null;

	public MainFrame(){
		initGUI();
	}
	
	public void initGUI(){
		setSize(GateUIDimensions.getInstance().getMainFrameSize());
		log.trace("mainFrameSize: " + GateUIDimensions.getInstance().getMainFrameSize().toString());
		updateTitle();
		propertiesPanel = new PropertiesPane();
		detailsTabPane.add(TabTitle_Properties, new JScrollPane(propertiesPanel));
		resultsPane = new ResultsPane();
		detailsTabPane.add(TabTitle_Results, resultsPane);
		graphComponent = new GateGraphComponent(new mxGraph());
		editor = new GateGraphEditor("Gate", graphComponent);
		graphOutline = new mxGraphOutline(graphComponent);
		graphOutline.addMouseWheelListener(new MouseWheelListener()
		{
			public void mouseWheelMoved(MouseWheelEvent e)
			{
				if (e.getSource() instanceof mxGraphOutline || e.isControlDown()){
					if (e.getWheelRotation() < 0){
						graphComponent.zoomIn();
					}
					else{
						graphComponent.zoomOut();
					}
					setStatus(mxResources.get("scale") + ": " + (int) (100
							* graphComponent.getGraph().getView().getScale()) + "%");
				}
			}
		});
		closeModelEditor();
		detailsAndOutline.setRightComponent(detailsTabPane);
		detailsAndOutline.setLeftComponent(graphOutline);
		detailsAndOutline.setDividerLocation(GateUIDimensions.getInstance().getWorkspaceHorizontalDivider());
        treesAndEditor.setDividerLocation(GateUIDimensions.getInstance().getWorkspaceHorizontalDivider());
		// Test and Action tree Panle
		testTree = new TestTree();
		actionTree = new ActionTree();
		testAndLibPane.add("Test", new JScrollPane(testTree));
		testAndLibPane.add("Lib", new JScrollPane(actionTree));
		treesAndEditor.setLeftComponent(testAndLibPane);
		workspace.setDividerLocation(GateUIDimensions.getInstance().getWorkspaceVerticalDivider());
		workspace.setTopComponent(treesAndEditor);
		workspace.setBottomComponent(detailsAndOutline);

		getContentPane().add(workspace);
		gateMenuBar = new GateMenuBar(editor);
		setJMenuBar(gateMenuBar);
		gateToolBar = new GateToolBar(editor, JToolBar.HORIZONTAL);
		add(gateToolBar, BorderLayout.NORTH);
		statusBar = new JLabel("ready");
		add(statusBar, BorderLayout.SOUTH);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public void activeDetailsTabProperties(){
		int index = detailsTabPane.indexOfTab(TabTitle_Properties);
		detailsTabPane.setSelectedIndex(index);
	}

	void activeDetailsTabResults(){
		int index = detailsTabPane.indexOfTab(TabTitle_Results);
		detailsTabPane.setSelectedIndex(index);
	}

	public void openModelEditor(String treeName, String nodePath, mxGraphModel model){
		editor.setModel(model);
		editor.setModified(false);
		editor.getUndoManager().clear();
		editor.getGraphComponent().zoomAndCenter();
		editor.getGraphComponent().getGraphControl().updatePreferredSize();
		updateTitle(treeName + ":" + nodePath);
		treesAndEditor.setRightComponent(editor);
		graphOutline.setVisible(true);
		detailsAndOutline.setDividerLocation(GateUIDimensions.getInstance().getWorkspaceHorizontalDivider());
		treesAndEditor.setDividerLocation(GateUIDimensions.getInstance().getWorkspaceHorizontalDivider());
	}

	public void closeModelEditor(){
		String text = "<html><p><font size=\"+1\"> <strong>Double click on tree node to edit model</strong></font></p></html>";
		JLabel guideLine = new JLabel(text, JLabel.CENTER);
		treesAndEditor.setRightComponent(guideLine);
		graphOutline.setVisible(false);
		treesAndEditor.setDividerLocation(GateUIDimensions.getInstance().getWorkspaceHorizontalDivider());
	}

	public boolean isModelEditorOpen(){
//		log.trace(treesAndEditor.getRightComponent().getClass().getName());
		if( treesAndEditor.getRightComponent().getClass().getName().equals(GateGraphEditor.class.getName())){
			return true;
		}else{
			return  false;
		}
	}

	public GateTree getCurrentTree(){
		if(testAndLibPane.getSelectedIndex()== 0){
			return testTree;
		}else{
			return actionTree;
		}
	}

	public TestTree getTestTree(){
		return testTree;
	}

	public ActionTree getActionTree(){
		return actionTree;
	}

	public ResultsPane getResultsPane(){
		return resultsPane;
	}

	public PropertiesPane getPropertiesPanel(){
		return propertiesPanel;
	}

	public TreePath getCurrentModePath(){
		return currentModePath;
	}

	public void setCurrentModePath(TreePath path){
		currentModePath = path;
	}

	public void testStarted(){
		gateToolBar.testStart();
		gateMenuBar.setRunning();
		activeDetailsTabResults();
	}

	public void stopTest(){
		gateToolBar.testEnd();
		gateMenuBar.setEnd();
	}

	public void setTestFile(String file){
		testFile = file;
		if(file != null){
			LoadRecentProject.updateRecentFiles(file);
		}
	}

	public String getTestFile(){
		return testFile;
	}

	public mxGraphOutline getGraphOutline(){
		return graphOutline;
	}
	public void updateTitle(){
		updateTitle("");
	}

	public void updateTitle(String message){
		StringBuffer sb = new StringBuffer();
		sb.append("Gate ");
		if(testFile == null){
			sb.append("[]");
		}else{
			File file = new File(testFile);
			sb.append("[").append(file.getAbsolutePath()).append("]");
		}
		sb.append("-").append(message);
		sb.trimToSize();
		setTitle(sb.toString());
	}

	public void setStatus(String message){
		statusBar.setText(message);
	}

	public BasicGraphEditor getGraphEditor(){
		return editor;
	}

}
