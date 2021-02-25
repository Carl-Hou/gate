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
package org.gate.gui.details.properties;

import org.gate.gui.common.GuiUtils;
import org.gate.gui.common.TestElement;
import org.gate.varfuncs.property.GateProperty;
import org.gate.varfuncs.property.StringProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.LinkedList;

// refert to org.apache.jmeter.Config.gui.ArgumentsPane

public class ArgumentsPane extends JPanel {

    Logger log = LogManager.getLogger(this.getClass());
    JTable table = null;
    JButton add = new JButton("Add");
    JButton addFromClipboard = new JButton("Add From ClipBoard");
    JButton delete  = new JButton("Delete");
    JButton up = new JButton("Up");
    JButton down = new JButton("Down");
    // Maeke sure setTestElement was called before use this component
    TestElement testElement = null;
    String nameSpace = TestElement.NS_ARGUMENT;

    public ArgumentsPane(){
        PropertiesTableModel tableModel = new PropertiesTableModel(getNameColumnHeader(), getValueColumnHeader());
        table = new JTable(tableModel);
        setLayout(new BorderLayout());
        add(table, BorderLayout.CENTER);
        add(table.getTableHeader(), BorderLayout.NORTH);
        JPanel buttons = makeButtonPanel();
        add(buttons, BorderLayout.SOUTH);
    }

    public ArgumentsPane(String borderName){
        setBorder(GuiUtils.getBorder(borderName));
        PropertiesTableModel tableModel = new PropertiesTableModel(getNameColumnHeader(), getValueColumnHeader());
        table = new JTable(tableModel);
        setLayout(new BorderLayout());
        add(table, BorderLayout.CENTER);
        add(table.getTableHeader(), BorderLayout.NORTH);
        JPanel buttons = makeButtonPanel();
        add(buttons, BorderLayout.SOUTH);

    }

    public void setNameSpace(String nameSpace){
        this.nameSpace = nameSpace;
    }

    String getNameColumnHeader(){
        return "name";
    }

    String getValueColumnHeader(){
        return "value";
    }


    protected void checkButtonsStatus() {
        // Disable DELETE if there are no rows in the table to delete.
        if (getModel().getRowCount() == 0) {
            delete.setEnabled(false);
//            showDetail.setEnabled(false);
        } else {
            delete.setEnabled(true);
//            showDetail.setEnabled(true);
        }

//        if(enableUpDown) {  did not see this is used anywhere.
            if(getModel().getRowCount()>1) {
                up.setEnabled(true);
                down.setEnabled(true);
            }
            else {
                up.setEnabled(false);
                down.setEnabled(false);
            }
//        }
    }

    /**
     * @param table {@link JTable}
     * @return number of visible rows
     */
    private static int getNumberOfVisibleRows(JTable table) {
        Rectangle vr = table.getVisibleRect();
        int first = table.rowAtPoint(vr.getLocation());
        vr.translate(0, vr.height);
        return table.rowAtPoint(vr.getLocation()) - first;
    }


    /**
     * ensure that a row is visible in the viewport
     * @param rowIndx row index
     */
    private void scrollToRowIfNotVisible(int rowIndx) {
        if(table.getParent() instanceof JViewport) {
            Rectangle visibleRect = table.getVisibleRect();
            final int cellIndex = 0;
            Rectangle cellRect = table.getCellRect(rowIndx, cellIndex, false);
            if (visibleRect.y > cellRect.y) {
                table.scrollRectToVisible(cellRect);
            } else {
                Rectangle rect2 = table.getCellRect(rowIndx + getNumberOfVisibleRows(table), cellIndex, true);
                int width = rect2.y - cellRect.y;
                table.scrollRectToVisible(new Rectangle(cellRect.x, cellRect.y, cellRect.width, cellRect.height + width));
            }
        }
    }

    JPanel makeButtonPanel(){
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        add.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GuiUtils.stopTableEditing(table);
                String[] rowData = {"",""};
                getModel().addRow(rowData);
                checkButtonsStatus();
                // Highlight (select) and scroll to the appropriate row.
                int rowToSelect = getModel().getRowCount() - 1;
                table.setRowSelectionInterval(rowToSelect, rowToSelect);
                table.scrollRectToVisible(table.getCellRect(rowToSelect, 0, true));
                updateElement();
            }
        });


        addFromClipboard.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GuiUtils.stopTableEditing(table);
                int rowCount = table.getRowCount();
                try {
                    String clipboardContent = GuiUtils.getPastedText();
                    if(clipboardContent == null) {
                        return;
                    }
                    String[] clipboardLines = clipboardContent.split("\n");
                    for (String clipboardLine : clipboardLines) {
                        String[] clipboardCols = clipboardLine.split("\t");
                        if (clipboardCols.length > 0) {
//                            Argument argument = createArgumentFromClipboard(clipboardCols);
//                            tableModel.addRow(argument);
                            getModel().addRow(clipboardCols);
                        }
                    }
                    if (table.getRowCount() > rowCount) {
                        checkButtonsStatus();

                        // Highlight (select) and scroll to the appropriate rows.
                        int rowToSelect = getModel().getRowCount() - 1;
                        table.setRowSelectionInterval(rowCount, rowToSelect);
                        table.scrollRectToVisible(table.getCellRect(rowCount, 0, true));
                    }
                    updateElement();
                } catch (IOException ioe) {
                    JOptionPane.showMessageDialog(null,
                            "Could not add read arguments from clipboard:\n" + ioe.getLocalizedMessage(), "Error",
                            JOptionPane.ERROR_MESSAGE);
                } catch (UnsupportedFlavorException ufe) {
                    JOptionPane.showMessageDialog(null,
                            "Could not add retrieve " + DataFlavor.stringFlavor.getHumanPresentableName()
                                    + " from clipboard" + ufe.getLocalizedMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }

            }
        });

        delete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GuiUtils.cancelEditing(table);
                int[] rowsSelected = table.getSelectedRows();
                int anchorSelection = table.getSelectionModel().getAnchorSelectionIndex();
                table.clearSelection();
                if (rowsSelected.length > 0) {
                    for (int i = rowsSelected.length - 1; i >= 0; i--) {
                        getModel().removeRow(rowsSelected[i]);
                    }

                    // Table still contains one or more rows, so highlight (select)
                    // the appropriate one.
                    if (getModel().getRowCount() > 0) {
                        if (anchorSelection >= getModel().getRowCount()) {
                            anchorSelection = getModel().getRowCount() - 1;
                        }
                        table.setRowSelectionInterval(anchorSelection, anchorSelection);
                    }
                    checkButtonsStatus();
                }
                updateElement();
            }
        });


        up.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //get the selected rows before stopping editing
                // or the selected rows will be unselected
                int[] rowsSelected = table.getSelectedRows();
                GuiUtils.stopTableEditing(table);

                if (rowsSelected.length > 0 && rowsSelected[0] > 0) {
                    table.clearSelection();
                    for (int rowSelected : rowsSelected) {
                        //tableModel.moveRow(rowSelected, rowSelected + 1, rowSelected - 1);
                        getModel().moveRow(rowSelected, rowSelected, rowSelected - 1);
                    }

                    for (int rowSelected : rowsSelected) {
                        table.addRowSelectionInterval(rowSelected - 1, rowSelected - 1);
                    }

                    scrollToRowIfNotVisible(rowsSelected[0]-1);
                }
                updateElement();
            }
        });

        down.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //get the selected rows before stopping editing
                // or the selected rows will be unselected
                int[] rowsSelected = table.getSelectedRows();
                GuiUtils.stopTableEditing(table);

                if (rowsSelected.length > 0 && rowsSelected[rowsSelected.length - 1] < table.getRowCount() - 1) {
                    table.clearSelection();
                    for (int i = rowsSelected.length - 1; i >= 0; i--) {
                        int rowSelected = rowsSelected[i];
//                        tableModel.moveRow(rowSelected, rowSelected + 1, rowSelected + 1);
                        getModel().moveRow(rowSelected, rowSelected , rowSelected + 1);
                    }
                    for (int rowSelected : rowsSelected) {
                        table.addRowSelectionInterval(rowSelected + 1, rowSelected + 1);
                    }
                    scrollToRowIfNotVisible(rowsSelected[0]+1);
                }
                updateElement();
            }
        });

        buttonPanel.add(add);
        buttonPanel.add(addFromClipboard);
        buttonPanel.add(delete);
        buttonPanel.add(up);
        buttonPanel.add(down);
        checkButtonsStatus();
        return buttonPanel;
    }



    public void setTestElement(TestElement element){
        testElement = element;
        PropertiesTableModel tableModel = new PropertiesTableModel(getNameColumnHeader(), getValueColumnHeader());
        tableModel.setNameEditalbe();
        /*
        * Create a new model each time set the element and addRows must be in front of the add Model Listener
        * because get model from table have issue with Model listener for current implementation
        * change the implementation later if have to.
        * */
        tableModel.addRows(element.getProps(nameSpace));
        tableModel.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                updateElement();
            }
        });


        table.setModel(tableModel);
        customizeArgumentsTable(table);
        checkButtonsStatus();
    }

    protected void customizeArgumentsTable(JTable table){

    }
    void updateElement(){

        PropertiesTableModel model = getModel();
        LinkedList<GateProperty> argumentProps = getArgumentProptiesElement(model);
        testElement.setProps(nameSpace, argumentProps);

    }

    void updateElement(String nameSpace){
        PropertiesTableModel model = getModel();
        LinkedList<GateProperty> argumentProps = getArgumentProptiesElement(model);
        testElement.setProps(nameSpace, argumentProps);

    }

    LinkedList<GateProperty> getArgumentProptiesElement(PropertiesTableModel tableModel) {
        LinkedList<GateProperty> argumentProps = new LinkedList<>();
        for(int i=0; i< tableModel.getRowCount(); i++){
            argumentProps.add(new StringProperty(tableModel.getName(i), tableModel.getValue(i)));
        }
        return argumentProps;
    }


    PropertiesTableModel getModel(){
        return (PropertiesTableModel) table.getModel();
    }

}
