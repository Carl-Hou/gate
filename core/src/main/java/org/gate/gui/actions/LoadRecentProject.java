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

import org.gate.common.util.GateException;
import org.gate.gui.GuiPackage;
import org.gate.gui.MainFrame;
import org.gate.gui.common.OptionPane;
import org.gate.gui.tree.GateTreeModel;
import org.gate.gui.tree.GateTreeSupport;
import org.gate.gui.tree.action.ActionTree;
import org.gate.gui.tree.test.TestTree;
import org.gate.saveload.utils.DocumentHelper;
import org.gate.saveload.utils.GateLoader;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;

public class LoadRecentProject extends AbstractGateAction{

    /** Prefix for the user preference key */
    private static final String USER_PREFS_KEY = "recent_file_"; //$NON-NLS-1$
    /** The number of menu items used for recent files */
    private static final int NUMBER_OF_MENU_ITEMS = 9;

    private static final Set<String> commands = new HashSet<>();
    static {
        commands.add(ActionNames.OPEN_RECENT);
    }

    private static final Preferences prefs = Preferences.userNodeForPackage(LoadRecentProject.class);
    // Note: Windows user preferences are stored relative to: HKEY_CURRENT_USER\Software\JavaSoft\Prefs

    @Override
    public Set<String> getActionNames() {
        return commands;
    }

    @Override
    public void doAction(ActionEvent e) throws IllegalUserActionException {
        // TODO :  ask the user if it is ok to close current project
        File file = getRecentFile(e);
        // load file
        load(file.getAbsolutePath());

    }

    void load(String modelFile) {
        File testFile = new File(modelFile);
        if (!testFile.exists()) {
            OptionPane.showErrorMessageDialog("Error on load project: ", "File not found: ".concat(testFile.getAbsolutePath()));
            return;
        }

        GuiPackage.getIns().getMainFrame().setTestFile(modelFile);
        GuiPackage.getIns().getMainFrame().closeModelEditor();
        GuiPackage.getIns().getMainFrame().updateTitle();
        GateTreeSupport.load(new File(modelFile));
        updateRecentFiles(modelFile);
    }

    private static LinkedList<String> getRecentFiles(){
        LinkedList<String> recentFiles = new LinkedList<>();
        for(int i=0; i< NUMBER_OF_MENU_ITEMS; i++){
            String value = getRecentFile(i);
            if(value != null){
                recentFiles.add(getRecentFile(i));
            }
        }
        return recentFiles;
    }

    public static void updateRecentFiles(String loadedFileName){
        LinkedList<String> newRecentFiles = new LinkedList<>();
        // Check if the new file is already in the recent list
        boolean alreadyExists = false;
        for(int i = 0; i < NUMBER_OF_MENU_ITEMS; i++) {
            String recentFilePath = getRecentFile(i);
            if(!loadedFileName.equals(recentFilePath)) {
                newRecentFiles.add(recentFilePath);
            }
            else {
                alreadyExists = true;
            }
        }
        // Add the new file at the start of the list
        newRecentFiles.add(0, loadedFileName);
        // Remove the last item from the list if it was a brand new file
        if(!alreadyExists) {
            newRecentFiles.removeLast();
        }
        // Store the recent files
        for(int i = 0; i < NUMBER_OF_MENU_ITEMS; i++) {
            String fileName = newRecentFiles.get(i);
            if(fileName != null) {
                setRecentFile(i, fileName);
            }
        }
    }

    /**
     * Get the recent file for the menu item
     */
    private File getRecentFile(ActionEvent e) {
        JMenuItem menuItem = (JMenuItem)e.getSource();
        // Get the preference for the recent files
        return new File(getRecentFile(Integer.parseInt(menuItem.getName())));
    }

    /**
     * Get the menu items to add to the menu bar, to get recent file functionality
     *
     * @return a List of JMenuItem and a JSeparator, representing recent files
     */
    public static List<JComponent> getRecentFileMenuItems() {
        LinkedList<JComponent> menuItems = new LinkedList<>();
        // Get the preference for the recent files
        for(int i = 0; i < NUMBER_OF_MENU_ITEMS; i++) {
            String recentFilePath = getRecentFile(i);
            if(recentFilePath != null){

                // Create the menu item
                JMenuItem recentFile = new JMenuItem();
                // Use the index as the name, used when processing the action
                recentFile.setName(Integer.toString(i));
                recentFile.addActionListener(ActionRouter.getInstance());
                recentFile.setActionCommand(ActionNames.OPEN_RECENT);


                File file = new File(recentFilePath);
                StringBuilder sb = new StringBuilder(60);
                if (i<9) {
                    sb.append(i+1).append(" "); //$NON-NLS-1$
                }
                sb.append(getMenuItemDisplayName(file));
                recentFile.setText(sb.toString());
                recentFile.setToolTipText(recentFilePath);
                recentFile.setEnabled(true);
                recentFile.setVisible(true);
                // Add the menu item
                menuItems.add(recentFile);
            }
        }

        return menuItems;
    }

    /**
     * Get the name to display in the menu item, it will chop the file name
     * if it is too long to display in the menu bar
     */
    private static String getMenuItemDisplayName(File file) {
        // Limit the length of the menu text if needed
        final int maxLength = 40;
        String menuText = file.getName();
        if(menuText.length() > maxLength) {
            menuText = "..." + menuText.substring(menuText.length() - maxLength, menuText.length()); //$NON-NLS-1$
        }
        return menuText;
    }



    /**
     * Get the full path to the recent file where index 0 is the most recent
     * @param index the index of the recent file
     * @return full path to the recent file at <code>index</code>
     */
    public static String getRecentFile(int index) {
        return prefs.get(USER_PREFS_KEY + index, null);
    }

    /**
     * Set the full path to the recent file where index 0 is the most recent
     */
    private static void setRecentFile(int index, String fileName) {
        prefs.put(USER_PREFS_KEY + index, fileName);
    }
}
