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

package org.gate.gui.details.results;

import org.gate.common.config.GateProps;
import org.gate.gui.GuiPackage;
import org.gate.gui.common.GuiUtils;
import org.gate.gui.details.results.elements.Result;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gate.gui.details.results.elements.test.TestCaseResult;
import org.gate.gui.details.results.elements.test.TestSuiteResult;
import org.gate.gui.details.results.elements.test.TestSuitesResult;

import java.awt.*;
import java.util.LinkedList;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

public class ResultTree extends JPanel {

	Logger log = LogManager.getLogger();

	private static final ImageIcon imageSuccess = GuiUtils.getImage("/org/gate/images/security-high-2.png");

	private static final ImageIcon imageFailure = GuiUtils.getImage("/org/gate/images/security-low-2.png");

	private static final Border RED_BORDER = BorderFactory.createLineBorder(Color.red);

	private static final Border BLUE_BORDER = BorderFactory.createLineBorder(Color.blue);


	DefaultTreeModel model = ResultManager.getIns().getModel();
	JTree tree = new JTree(model);

	public ResultTree(){
		tree.setCellRenderer(new ResultsNodeRender());
		setLayout(new BorderLayout());
		add(tree, BorderLayout.CENTER);
		tree.setRootVisible(false);
		model.addTreeModelListener(new TreeModelListener() {
			@Override
			public void treeNodesChanged(TreeModelEvent e) {}
			@Override
			public void treeNodesInserted(TreeModelEvent e) {
				if(GateProps.getProperty("gate.gui.result.scroll", true)){
					tree.scrollPathToVisible(e.getTreePath());
				}
			}

			@Override
			public void treeNodesRemoved(TreeModelEvent e) {}

			@Override
			public void treeStructureChanged(TreeModelEvent e) {}
		});

		tree.addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				ResultTreeNode node = (ResultTreeNode) tree.getLastSelectedPathComponent();
				if(node == null){
					return;
				}
				if(node.getUserObject() instanceof Result){
					GuiPackage.getIns().getResultsPane().showResult(node.getResult());
					GuiPackage.getIns().getResultsPane().repaint();
				}else{
					log.fatal("Not result in result tree node:" + node.getUserObject().getClass().getName());
				}
			}
		});
	}

	public JTree getTree(){
		return tree;
	}

	private static class ResultsNodeRender extends DefaultTreeCellRenderer {
		private static final long serialVersionUID = 4159626601097711565L;
		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value,
													  boolean sel, boolean expanded, boolean leaf, int row, boolean focus) {
			super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, focus);

			Result userObject = ((ResultTreeNode) value).getResult();
			boolean failure = userObject.isFailure();

			// Set the status for the node
			if (failure) {
				this.setForeground(Color.red);
				this.setIcon(imageFailure);
			} else {
				this.setIcon(imageSuccess);
			}

			// Handle search related rendering
//			SearchableTreeNode node = (SearchableTreeNode) value;
//			if(node.isNodeHasMatched()) {
//				setBorder(RED_BORDER);
//			} else if (node.isChildrenNodesHaveMatched()) {
//				setBorder(BLUE_BORDER);
//			} else {
//				setBorder(null);
//			}
			return this;
		}
	}

}
