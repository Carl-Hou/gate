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

import com.mxgraph.util.mxResources;
import org.gate.common.util.GateException;
import org.gate.gui.GuiPackage;
import org.gate.gui.common.OptionPane;
import org.gate.gui.MainFrame;
import org.gate.gui.tree.GateTreeModel;
import org.gate.gui.tree.GateTreeSupport;
import org.gate.gui.tree.action.ActionTree;
import org.gate.gui.tree.test.TestTree;
import org.gate.saveload.utils.DocumentHelper;
import org.gate.saveload.utils.GateLoader;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class Load extends AbstractGateAction{

    private static final Set<String> commands = new HashSet<>();
    static {
        commands.add(ActionNames.OPEN);
    }

    protected static String lastDir;

    @Override
    public void doAction(ActionEvent e) throws IllegalUserActionException {

        if(e.getActionCommand().equals(ActionNames.OPEN)){
            String wd = (lastDir != null) ? lastDir : System.getProperty("user.dir");
            JFileChooser fileChooser = new JFileChooser(wd);
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            // Adds file filter for supported file format
            fileChooser.setFileFilter(new FileFilter() {
                @Override
                public boolean accept(File f) {
                    if(f.getName().contains(".gmx") || f.isDirectory()){
                        return true;
                    }
                    return false;
                }
                @Override
                public String getDescription() {
                    return "Gate [.gmx]";
                }
            });

            int responseCode = fileChooser.showDialog(null, mxResources.get("openTreeFile"));
            if (responseCode == JFileChooser.APPROVE_OPTION) {
                lastDir = fileChooser.getSelectedFile().getPath();
                load(lastDir);
            }
        }
    }

    public void load(String modelFile){
        File testFile = new File(modelFile);
        if(!testFile.exists()){
            OptionPane.showErrorMessageDialog("Error on load project: ", "File not found: ".concat(testFile.getAbsolutePath()));
        }
        GuiPackage.getIns().getMainFrame().setTestFile(modelFile);
        GuiPackage.getIns().getMainFrame().closeModelEditor();
        GuiPackage.getIns().getMainFrame().updateTitle();

        GateTreeSupport.load(new File(modelFile));

    }

    @Override
    public Set<String> getActionNames() {
        return commands;
    }


}
