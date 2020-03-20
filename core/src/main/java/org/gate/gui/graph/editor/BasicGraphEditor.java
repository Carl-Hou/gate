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

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.List;

import javax.swing.*;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.util.*;
import org.gate.gui.GuiPackage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;

import com.mxgraph.io.mxCodec;
import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.layout.mxCompactTreeLayout;
import com.mxgraph.layout.mxEdgeLabelLayout;
import com.mxgraph.layout.mxIGraphLayout;
import com.mxgraph.layout.mxOrganicLayout;
import com.mxgraph.layout.mxParallelEdgeLayout;
import com.mxgraph.layout.mxPartitionLayout;
import com.mxgraph.layout.mxStackLayout;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.model.mxGraphModel;
import com.mxgraph.swing.mxGraphComponent;

import com.mxgraph.swing.handler.mxKeyboardHandler;
import com.mxgraph.swing.handler.mxRubberband;
import com.mxgraph.swing.util.mxMorphing;
import com.mxgraph.util.mxEventSource.mxIEventListener;
import com.mxgraph.util.mxUndoableEdit.mxUndoableChange;
import com.mxgraph.view.mxGraph;


abstract public class BasicGraphEditor extends JPanel
{
	Logger log = LogManager.getLogger();
	/**
	 * 
	 */
	
	private static final long serialVersionUID = -6561623072112577140L;

	/**
	 * Adds required resources for i18n
	 */
	static
	{
		try
		{
			mxResources.add("org/gate/resources/editor");
		}
		catch (Exception e)
		{
			// ignore
		}
	}

	/**
	 * 
	 */
	protected static mxGraphComponent graphComponent;

	/**
	 * 
	 */
	protected static mxUndoManager undoManager;

	/**
	 *
	 */
	protected String appTitle;

	/**
	 * Flag indicating whether the current graph has been modified 
	 */
	protected boolean modified = false;

	/**
	 * 
	 */
	protected mxRubberband rubberband;

	/**
	 * 
	 */
	protected mxKeyboardHandler keyboardHandler;

	/**
	 *
	 */
	protected int cursorLocationX = 0;
	protected int cursorLocationY = 0;

	protected static mxIEventListener undoHandler = new mxIEventListener()
	{
		public void invoke(Object source, mxEventObject evt)
		{
			undoManager.undoableEditHappened((mxUndoableEdit) evt
					.getProperty("edit"));
		}
	};

	/**
	 * 
	 */
	protected mxIEventListener changeTracker = new mxIEventListener()
	{
		public void invoke(Object source, mxEventObject evt)
		{
			setModified(true);
		}
	};

	/**
	 * 
	 */
	
	
	public BasicGraphEditor(String appTitle, mxGraphComponent component)
	{
		// Stores and updates the frame title. No use currently. Keep this for later use, probably multi-tab editor feature?
		this.appTitle = appTitle;

		// Stores a reference to the graph and creates the command history
		graphComponent = component;
		final mxGraph graph = graphComponent.getGraph();
		undoManager = createUndoManager();

		// Do not change the scale and translation after files have been loaded
		graph.setResetViewOnRootChange(false);

		// Updates the modified flag if the graph model changes
		graph.getModel().addListener(mxEvent.CHANGE, changeTracker);

		// Adds the command history to the model and view
		graph.getModel().addListener(mxEvent.UNDO, undoHandler);
		graph.getView().addListener(mxEvent.UNDO, undoHandler);

		// Keeps the selection in sync with the command history
		mxIEventListener undoHandler = new mxIEventListener()
		{
			public void invoke(Object source, mxEventObject evt)
			{
				List<mxUndoableChange> changes = ((mxUndoableEdit) evt
						.getProperty("edit")).getChanges();
				graph.setSelectionCells(graph
						.getSelectionCellsForChanges(changes));
			}
		};

		undoManager.addListener(mxEvent.UNDO, undoHandler);
		undoManager.addListener(mxEvent.REDO, undoHandler);

		// Display some useful information about repaint events
		installRepaintListener();

		// Puts everything together
		setLayout(new BorderLayout());
//		add(graphEditor, BorderLayout.CENTER);
		add(graphComponent, BorderLayout.CENTER);

		// Installs rubberband selection and handling for some special
		// keystrokes such as F2, Control-C, -V, X, A etc.
		installHandlers();
		installListeners();

		// By Carl set the default GUI style
		graphComponent.setPageVisible(false);
		graphComponent.setGridVisible(true);
		graphComponent.setToolTips(true);
		mxCodec codec = new mxCodec();
		Document doc = mxUtils.loadDocument(BasicGraphEditor.class.getResource(
				"/org/gate/resources/default-style.xml")
				.toString());
		codec.decode(doc.getDocumentElement(), graph.getStylesheet());

		// Sets the background to white. Affect the outline window
		graphComponent.getViewport().setOpaque(true);
		graphComponent.getViewport().setBackground(Color.WHITE);
	}

	/**
	 * 
	 */
	protected mxUndoManager createUndoManager()
	{
		return new mxUndoManager();
	}

	/**
	 * 
	 */
	protected void installHandlers()
	{
		rubberband = new mxRubberband(graphComponent);
		keyboardHandler = new EditorKeyboardHandler(graphComponent);
	}



	/**
	 * 
	 */
	protected void installRepaintListener()
	{
		graphComponent.getGraph().addListener(mxEvent.REPAINT,
				new mxIEventListener()
				{
					public void invoke(Object source, mxEventObject evt)
					{
						String buffer = (graphComponent.getTripleBuffer() != null) ? ""
								: " (unbuffered)";
						mxRectangle dirty = (mxRectangle) evt
								.getProperty("region");

						if (dirty == null)
						{
							status("Repaint all" + buffer);
						}
						else
						{
							status("Repaint: x=" + (int) (dirty.getX()) + " y="
									+ (int) (dirty.getY()) + " w="
									+ (int) (dirty.getWidth()) + " h="
									+ (int) (dirty.getHeight()) + buffer);
						}
					}
				});
	}

	abstract void onMousePressedOnMxCell(MouseEvent e);

	protected void showGraphPopupMenu(MouseEvent e)
	{
		Point pt = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(),
				graphComponent);
		EditorPopupMenu menu = new EditorPopupMenu(BasicGraphEditor.this);
		menu.show(graphComponent, pt.x, pt.y);

		e.consume();
	}

	/**
	 * 
	 */
	protected void mouseLocationChanged(MouseEvent e)
	{
		cursorLocationX = e.getX();
		cursorLocationY = e.getY();
		status("cursor location: " + e.getX() + ", " + e.getY());
	}

	/**
	 * 
	 */
	protected void installListeners()
	{
		// Installs the popup menu in the graph component
		graphComponent.getGraphControl().addMouseListener(new MouseAdapter()
		{

			/**
			 * 
			 */
			public void mousePressed(MouseEvent e)
			{
//				Action defined for click mouse on mxCell
//				onMousePressed(e);
				onMousePressedOnMxCell(e);
				// Handles context menu on the Mac where the trigger is on mousepressed
				mouseReleased(e);
			}

			public void mouseReleased(MouseEvent e)
			{
				if (e.isPopupTrigger())
				{
					showGraphPopupMenu(e);
				}
			}

		});

		// Installs a mouse motion listener to display the mouse location
		graphComponent.getGraphControl().addMouseMotionListener(
				new MouseMotionListener()
				{

					/*
					 * (non-Javadoc)
					 * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
					 */
					public void mouseDragged(MouseEvent e)
					{
						mouseLocationChanged(e);
					}

					/*
					 * (non-Javadoc)
					 * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
					 */
					public void mouseMoved(MouseEvent e)
					{
						mouseDragged(e);
					}

				});
	}

	/**
	 *
	 * @param modified
	 */
	public void setModified(boolean modified)
	{
		boolean oldValue = this.modified;
		this.modified = modified;

		firePropertyChange("modified", oldValue, modified);

		if (oldValue != modified)
		{
			// need to any
		}
	}

	/**
	 * 
	 * @return whether or not the current graph has been modified
	 */
	public boolean isModified()
	{
		return modified;
	}

	/**
	 * 
	 */
	public mxGraphComponent getGraphComponent()
	{
		return graphComponent;
	}




	/**
	 * 
	 */
	public mxUndoManager getUndoManager()
	{
		return undoManager;
	}

	/**
	 * 
	 * @param name
	 * @param action
	 * @return a new Action bound to the specified string name
	 */
	public Action bind(String name, final Action action)
	{
		return bind(name, action, null);
	}

	/**
	 * 
	 * @param name
	 * @param action
	 * @return a new Action bound to the specified string name and icon
	 */
	@SuppressWarnings("serial")
	public Action bind(String name, final Action action, String iconUrl)
	{
		AbstractAction newAction = new AbstractAction(name, (iconUrl != null) ? new ImageIcon(
				BasicGraphEditor.class.getResource(iconUrl)) : null)
		{
			public void actionPerformed(ActionEvent e)
			{
				action.actionPerformed(new ActionEvent(getGraphComponent(), e.getID(), e.getActionCommand()));
			}
		};
		
		newAction.putValue(Action.SHORT_DESCRIPTION, action.getValue(Action.SHORT_DESCRIPTION));
		return newAction;
	}

	/**
	 * 
	 * @param msg
	 */
	public void status(String msg)
	{
		GuiPackage.getIns().getMainFrame().setStatus(msg);
	}


	public void exit()
	{
		JFrame frame = (JFrame) SwingUtilities.windowForComponent(this);

		if (frame != null)
		{
			frame.dispose();
		}
	}

	/**
	 * Creates an action that executes the specified layout.
	 * 
	 * @param key Key to be used for getting the label from mxResources and also
	 * to create the layout instance for the commercial graph editor example.
	 * @return an action that executes the specified layout
	 */
	@SuppressWarnings("serial")
	public Action graphLayout(final String key, boolean animate)
	{
		final mxIGraphLayout layout = createLayout(key, animate);

		if (layout != null)
		{
			return new AbstractAction(mxResources.get(key))
			{
				public void actionPerformed(ActionEvent e)
				{
					final mxGraph graph = graphComponent.getGraph();
					Object cell = graph.getSelectionCell();

					if (cell == null
							|| graph.getModel().getChildCount(cell) == 0)
					{
						cell = graph.getDefaultParent();
					}

					graph.getModel().beginUpdate();
					try
					{
						long t0 = System.currentTimeMillis();
						layout.execute(cell);
						status("Layout: " + (System.currentTimeMillis() - t0)
								+ " ms");
					}
					finally
					{
						mxMorphing morph = new mxMorphing(graphComponent, 20,
								1.2, 20);

						morph.addListener(mxEvent.DONE, new mxIEventListener()
						{

							public void invoke(Object sender, mxEventObject evt)
							{
								graph.getModel().endUpdate();
							}

						});

						morph.startAnimation();
					}

				}

			};
		}
		else
		{
			return new AbstractAction(mxResources.get(key))
			{

				public void actionPerformed(ActionEvent e)
				{
					JOptionPane.showMessageDialog(graphComponent,
							mxResources.get("noLayout"));
				}

			};
		}
	}

	/**
	 * Creates a layout instance for the given identifier.
	 */
	protected mxIGraphLayout createLayout(String ident, boolean animate)
	{
		mxIGraphLayout layout = null;

		if (ident != null)
		{
			mxGraph graph = graphComponent.getGraph();

			if (ident.equals("verticalHierarchical"))
			{
				layout = new mxHierarchicalLayout(graph);
			}
			else if (ident.equals("horizontalHierarchical"))
			{
				layout = new mxHierarchicalLayout(graph, JLabel.WEST);
			}
			else if (ident.equals("verticalTree"))
			{
				layout = new mxCompactTreeLayout(graph, false);
			}
			else if (ident.equals("horizontalTree"))
			{
				layout = new mxCompactTreeLayout(graph, true);
			}
			else if (ident.equals("parallelEdges"))
			{
				layout = new mxParallelEdgeLayout(graph);
			}
			else if (ident.equals("placeEdgeLabels"))
			{
				layout = new mxEdgeLabelLayout(graph);
			}
			else if (ident.equals("organicLayout"))
			{
				layout = new mxOrganicLayout(graph);
			}
			if (ident.equals("verticalPartition"))
			{
				layout = new mxPartitionLayout(graph, false)
				{
					/**
					 * Overrides the empty implementation to return the size of the
					 * graph control.
					 */
					public mxRectangle getContainerSize()
					{
						return graphComponent.getLayoutAreaSize();
					}
				};
			}
			else if (ident.equals("horizontalPartition"))
			{
				layout = new mxPartitionLayout(graph, true)
				{
					/**
					 * Overrides the empty implementation to return the size of the
					 * graph control.
					 */
					public mxRectangle getContainerSize()
					{
						return graphComponent.getLayoutAreaSize();
					}
				};
			}
			else if (ident.equals("verticalStack"))
			{
				layout = new mxStackLayout(graph, false)
				{
					/**
					 * Overrides the empty implementation to return the size of the
					 * graph control.
					 */
					public mxRectangle getContainerSize()
					{
						return graphComponent.getLayoutAreaSize();
					}
				};
			}
			else if (ident.equals("horizontalStack"))
			{
				layout = new mxStackLayout(graph, true)
				{
					/**
					 * Overrides the empty implementation to return the size of the
					 * graph control.
					 */
					public mxRectangle getContainerSize()
					{
						return graphComponent.getLayoutAreaSize();
					}
				};
			}
			else if (ident.equals("circleLayout"))
			{
				layout = new mxCircleLayout(graph);
			}
		}

		return layout;
	}
//	Add by Carl
	public void setModel(mxGraphModel model){
		graphComponent.getGraph().getModel().removeListener(undoHandler, mxEvent.UNDO);
		graphComponent.getGraph().getView().removeListener(undoHandler,mxEvent.UNDO);
		
		graphComponent.getGraph().setModel(model);
		
		graphComponent.getGraph().getModel().addListener(mxEvent.UNDO, undoHandler);
		graphComponent.getGraph().getView().addListener(mxEvent.UNDO, undoHandler);
		graphComponent.refresh();
	}

	public mxIGraphModel getModel(){
		return graphComponent.getGraph().getModel();
	}

	public int getCursorLocationX(){
		return cursorLocationX;
	}

	public int getCursorLocationY(){
		return cursorLocationY;
	}

	public void addCell(mxCell cell){
        mxIGraphModel mxModel = graphComponent.getGraph().getModel();
        mxModel.beginUpdate();
        Object parent = graphComponent.getGraph().getDefaultParent();
        mxModel.add(parent, cell, mxModel.getChildCount(parent));
        mxModel.endUpdate();
    }
}
