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

import org.gate.varfuncs.property.GateProperty;
import org.gate.varfuncs.property.StringProperty;

import javax.swing.*;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.util.LinkedList;


public class ValueListArgumentsPane extends ArgumentsPane {

    @Override
    LinkedList<GateProperty> getArgumentProptiesElement(PropertiesTableModel tableModel) {
        LinkedList<GateProperty> argumentProps = new LinkedList<>();
        for(int i=0; i< tableModel.getRowCount(); i++){
            argumentProps.add(new StringProperty(String.valueOf(i), tableModel.getValue(i)));
        }
        return argumentProps;
    }

    @Override
    String getValueColumnHeader(){
        return "Patterns to Test";
    }

    // will change to value only display after setElement to it.
    @Override
    protected void customizeArgumentsTable(JTable argumentTabe){
        TableColumnModel tcm = table.getColumnModel();
        TableColumn tc = tcm.getColumn(0);
        table.removeColumn(tc);
    }

}
