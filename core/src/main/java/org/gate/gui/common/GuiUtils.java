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
package org.gate.gui.common;


import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.gate.common.config.GateProps;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class GuiUtils {
    private static final Logger log = LogManager.getLogger(GuiUtils.class);
    private static String iconSize = GateProps.getProperty("gate.gui.icon.size");

    /**
     * Get pasted text from clipboard
     *
     * @return String Pasted text
     * @throws UnsupportedFlavorException
     *             if the clipboard data can not be get as a {@link String}
     * @throws IOException
     *             if the clipboard data is no longer available
     */
    public static String getPastedText() throws UnsupportedFlavorException, IOException {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable trans = clipboard.getContents(null);
        DataFlavor[] flavourList = trans.getTransferDataFlavors();
        Collection<DataFlavor> flavours = new ArrayList<>(flavourList.length);
        if (Collections.addAll(flavours, flavourList) && flavours.contains(DataFlavor.stringFlavor)) {
            return (String) trans.getTransferData(DataFlavor.stringFlavor);
        } else {
            return null;
        }
    }

    /**
     * Stop any editing that is currently being done on the table. This will
     * save any changes that have already been made.
     *
     * @param table the table to stop on editing
     */
    public static void stopTableEditing(JTable table) {
        if (table.isEditing()) {
            TableCellEditor cellEditor = table.getCellEditor(table.getEditingRow(), table.getEditingColumn());
            cellEditor.stopCellEditing();
        }
    }

    /**
     * cancel any editing that is currently being done on the table.
     *
     * @param table the table to cancel on editing
     * @since 3.1
     */
    public static void cancelEditing(JTable table) {
        // If a table cell is being edited, we must cancel the editing
        if (table != null && table.isEditing()) {
            TableCellEditor cellEditor = table.getCellEditor(table.getEditingRow(), table.getEditingColumn());
            cellEditor.cancelCellEditing();
        }
    }

    public static Border getBorder(String name){
        return BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),name);
    }

    public static JPanel getPanel(String name){
        JPanel panel = new JPanel();
        panel.setBorder(getBorder(name));
        return panel;
    }

    public static JPanel getPanel(String name, LayoutManager layoutManager){
        JPanel panel = new JPanel(layoutManager);
        panel.setBorder(getBorder(name));
        return panel;
    }

    public static JPanel getPanel(String name, JTable talbe){
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(getBorder(name));
        panel.add(talbe, BorderLayout.CENTER);
        panel.add(talbe.getTableHeader(), BorderLayout.NORTH);
        return panel;
    }

    public static RSyntaxTextArea getRSyntaxTextArea(int row, int cols){
        return getRSyntaxTextArea(row, cols, SyntaxConstants.SYNTAX_STYLE_GROOVY);
    }

    public static RSyntaxTextArea getRSyntaxTextArea(int row, int cols, String syntaxStyle){
        RSyntaxTextArea rSyntaxTextArea = new RSyntaxTextArea(row, cols);
        rSyntaxTextArea.setSyntaxEditingStyle(syntaxStyle);
        rSyntaxTextArea.setCodeFoldingEnabled(true);
        rSyntaxTextArea.setAnimateBracketMatching(true);
        rSyntaxTextArea.setAutoIndentEnabled(true);
        rSyntaxTextArea.setBracketMatchingEnabled(true);
        rSyntaxTextArea.setLineWrap(true);
        rSyntaxTextArea.setWrapStyleWord(true);
        return rSyntaxTextArea;
    }

    public static RTextScrollPane getRTextScrollPane(RSyntaxTextArea rSyntaxTextArea){
        RTextScrollPane rTextScrollPane = new RTextScrollPane(rSyntaxTextArea);
        rTextScrollPane.setFoldIndicatorEnabled(true);
        return rTextScrollPane;
    }

    public static ImageIcon getImage(String name) {
        try {
            URL url = GuiUtils.class.getResource(name.trim());
            if(url != null) {
                ImageIcon image = new ImageIcon(url);
                if(iconSize.isEmpty()){
                    Font treeFont = UIManager.getFont("Tree.font");
                    int size = (int) (treeFont.getSize()*1.25);
                    image.setImage(image.getImage().getScaledInstance(size,size,Image.SCALE_DEFAULT));
                }else{
                    int size = Integer.parseInt(iconSize);
                    image.setImage(image.getImage().getScaledInstance(size,size,Image.SCALE_DEFAULT));
                }
                return image; // $NON-NLS-1$
            } else {
                log.warn("no icon for " + name);
                return null;
            }
        } catch (NoClassDefFoundError | InternalError e) {// Can be returned by headless hosts
            log.info("no icon for " + name + " " + e.getMessage());
            return null;
        }
    }

    // from JMeter
    /**
     * Use this static method if you want to center and set its position
     * compared to the size of the current users screen size. Valid percent is
     * between +-(0-100) minus is treated as plus, bigger than 100 is always set
     * to 100.
     *
     * @param component
     *            the component you want to center and set size on
     * @param percentOfScreen
     *            the percent of the current screensize you want the component
     *            to be
     */
    public static void centerComponentInWindow(Component component, int percentOfScreen) {
        if (percentOfScreen < 0) {
            centerComponentInWindow(component, -percentOfScreen);
            return;
        }
        if (percentOfScreen > 100) {
            centerComponentInWindow(component, 100);
            return;
        }
        double percent = percentOfScreen / 100.d;
        Dimension dimension = component.getToolkit().getScreenSize();
        component.setSize((int) (dimension.getWidth() * percent), (int) (dimension.getHeight() * percent));
        centerComponentInWindow(component);
    }

    /**
     * Use this static method if you want to center a component in Window.
     *
     * @param component
     *            the component you want to center in window
     */
    public static void centerComponentInWindow(Component component) {
        Dimension dimension = component.getToolkit().getScreenSize();
        component.setLocation((int) ((dimension.getWidth() - component.getWidth()) / 2),
                (int) ((dimension.getHeight() - component.getHeight()) / 2));
        component.validate();
        component.repaint();
    }


}
