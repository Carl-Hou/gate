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

import java.util.LinkedList;

import javax.swing.table.DefaultTableModel;

import org.gate.varfuncs.property.GateProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PropertiesTableModel extends DefaultTableModel {
	
     Logger log = LogManager.getLogger(this.getClass());
     int begin = 0;
 	 int end = 0;
 	 boolean nameNotEditalbe = true;
 	 
     public PropertiesTableModel(){
    	addColumn("name");
    	addColumn("value");   	
    	setRowCount(0);
     }

	public PropertiesTableModel(String nameColumnHeader, String valueColumnHeader){
		addColumn(nameColumnHeader);
		addColumn(valueColumnHeader);
		setRowCount(0);
	}

	public void setNameEditalbe(){
     	nameNotEditalbe = false;
	}

     public void addRow(GateProperty prop){
    	 addRow(prop.getName(), prop.getStringValue());
     }
     
     public void addRows(LinkedList<GateProperty> props){
    	 props.forEach(p -> { addRow(p);});
     }
     
     public void addRow(String name, Object value){
 		Object[] rowData ={name, value};
 		addRow(rowData);
 	}

 	public void removeAllRows(){
		 getDataVector().clear();
    }
     
     public void setEditableRange(int begin, int end){
    	 this.begin = begin;
    	 this.end = end;
     }
     
     public String getName(int row){
    	 return (String) getValueAt(row, 0);
     }

     public String getValue(int row){
    	 return  (String) getValueAt(row, 1);
     }
     
     @Override
 	public boolean isCellEditable(int row, int column) {
 		if(column == 0 && nameNotEditalbe){
 			return false;
 		}
 		if(end ==0 && begin ==0 ){
 			return true;
 		}
 		if(row <=end && row >= begin){
 			return true;
 		}
 		return false;
 	}

}
