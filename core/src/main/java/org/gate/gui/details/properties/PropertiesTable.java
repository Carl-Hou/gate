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

import org.gate.common.config.GateProps;
import org.gate.gui.common.TestElement;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellEditor;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class PropertiesTable extends JTable {

    final static String OptionPaneTitle = "Parameter Constrain Alert";
    final static JComboBox booleanComboBox = new JComboBox(new String[] {GateProps.TRUE, GateProps.FALSE});
    HashMap<Integer, TableCellEditor> cellEditors = new HashMap<>();

    public PropertiesTable(PropertiesTableModel model){
        super(model);
    }

    public void setComboBox(int row, JComboBox comboBox) {
        cellEditors.put(row, new DefaultCellEditor(comboBox));
    }

    public void setComboBox(String name, JComboBox comboBox){
        PropertiesTableModel model = (PropertiesTableModel) getModel();
        for(int i=0; i< model.getRowCount(); i++){
            if(model.getName(i).equals(name)){
                setComboBox(i, comboBox);
            }
        }
    }

    void showErrorMessageDialog(String errorMessage){
        JOptionPane.showMessageDialog(null, errorMessage, OptionPaneTitle, JOptionPane.ERROR_MESSAGE);
    }

    public void constraintReadOnly(String name){
        int row = getRow(name);
        JTextField jTextField = new JTextField(name);
        jTextField.setEnabled(false);
        cellEditors.put(row, new DefaultCellEditor(jTextField));
    }

    public void constraintNotEmpty(String name){
        int row = getRow(name);
        cellEditors.put(row, new DefaultCellEditor(new JTextField()){
            @Override
            public boolean stopCellEditing(){
                String value = (String) getCellEditorValue();
                if(value.isEmpty()){
                    showErrorMessageDialog("Not accept empty value");
                    return  false;
                }
                return super.stopCellEditing();
            }
        });
    }

    public void constraintUnsignedInt(int row){
        cellEditors.put(row, new DefaultCellEditor(new JTextField()){
            @Override
            public boolean stopCellEditing(){
                String value = (String) getCellEditorValue();
                try{
                    Integer.parseUnsignedInt(value);
                }catch (NumberFormatException e){
                    showErrorMessageDialog("Require Unsigned Integer");
                    return  false;
                }
                return super.stopCellEditing();
            }
        });
    }

    public void constraintUnsignedInt(String name){
        constraintUnsignedInt(getRow(name));
    }

    public void constraintUnsignedLong(int row){
        cellEditors.put(row, new DefaultCellEditor(new JTextField()){
            @Override
            public boolean stopCellEditing(){
                String value = (String) getCellEditorValue();
                try{
                    Long.parseUnsignedLong(value);
                }catch (NumberFormatException e){
                    showErrorMessageDialog("Require Unsigned Long");
                    return  false;
                }
                return super.stopCellEditing();
            }
        });
    }

    public void constraintUnsignedLong(String name){
        constraintUnsignedLong(getRow(name));

    }

    public void constraintRegex(String name){
        cellEditors.put(getRow(name), new DefaultCellEditor(new JTextField()){
            @Override
            public boolean stopCellEditing(){
                String value = (String) getCellEditorValue();
                try{
                    Pattern.compile(value);
                }catch (PatternSyntaxException e){
                    showErrorMessageDialog("Expression's syntax is invalid");
                    return  false;
                }
                return super.stopCellEditing();
            }
        });
    }

    /*
    * return -1 when the name is not found is an expected behavior
    * constraints will be apply to -1 which will be never be used
    * */
    int getRow(String name){
        int row = -1;
        PropertiesTableModel model = (PropertiesTableModel) getModel();
        for(int i=0; i< model.getRowCount(); i++){
            if(model.getName(i).equals(name)){
                row = i;
                break;
            }
        }
        return row;
    }

    public void setBooleanOnCell(String name){
        setBooleanOnCell(getRow(name));
    }

    public void setBooleanOnCell(int row) {
        cellEditors.put(row, new DefaultCellEditor(booleanComboBox));
    }


    public void setTestElement(TestElement element){
        setTestElement(element, TestElement.NS_DEFAULT);
    }

    public void setTestElement(TestElement element, String nameSpace){
        cellEditors.clear();
        /*
        * Create a table model each time a new element is seat to avoid use a global element property for update.
        * Use a element and skip TableModelEvent.DELETE if really need to use same model in the table.
        * */
        PropertiesTableModel tableModel = new PropertiesTableModel();
        tableModel.addRows(element.getProps(nameSpace));
        tableModel.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                int r = e.getFirstRow();
                PropertiesTableModel model = (PropertiesTableModel) e.getSource();
                String key =  model.getName(r);
                String value = model.getValue(r);

                element.getProps(nameSpace).forEach(p -> {
                    if(p.getName().equals(key)){
                        p.setObjectValue(value);
                    }
                });
            }
        });
        setModel(tableModel);
    }

    @Override
    public TableCellEditor getCellEditor(int row, int column) {
        if (column ==1 && cellEditors.containsKey(row))
            return cellEditors.get(row);
        return super.getCellEditor(row, column);
    }


}
