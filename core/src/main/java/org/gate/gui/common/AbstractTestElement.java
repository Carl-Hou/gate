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

import org.gate.varfuncs.property.GateProperty;
import org.gate.varfuncs.property.StringProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;

abstract public class AbstractTestElement implements TestElement, Serializable {

	protected Logger log = LogManager.getLogger(this.getClass());

	public final static String Version = "0.1";

	protected HashMap<String, LinkedList<GateProperty>> propsMap = new HashMap();

	public AbstractTestElement(){
		LinkedList<GateProperty> name = new LinkedList<>();
		name.add(new StringProperty(NP_NAME, this.getClass().getSimpleName()));
        LinkedList<GateProperty> props = new LinkedList<>();
		propsMap.put(NS_DEFAULT, props);
		propsMap.put(NS_NAME, name);
	}

	protected GateProperty getProp(HashMap<String, LinkedList<GateProperty>> propsMap, String scope, String name){
		if(propsMap.containsKey(scope)){
			for(GateProperty property : propsMap.get(scope)){
				if(property.getName().equals(name)){
					return property;
				}
			}
		}
		return null;
	}

	protected void setProp(HashMap<String, LinkedList<GateProperty>> propsMap, String scope, String name, String value){
		for(GateProperty property :propsMap.get(scope)){
			if(property.getName().equals(name)){
				property.setObjectValue(value);
				return;
			}
		}
		return;
	}
	/**
	 * update if exist. add if not exist
	 */

	protected void putProp(HashMap<String, LinkedList<GateProperty>> propsMap, String scope, String name, String value){

		for(GateProperty property :propsMap.get(scope)){
			if(property.getName().equals(name)){
				property.setObjectValue(value);
				return;
			}
		}
		propsMap.get(scope).add(new StringProperty(name, value));

	}

	protected boolean addProp(HashMap<String, LinkedList<GateProperty>> propsMap, String scope, String name, String value){
		for(GateProperty property :propsMap.get(scope)){
			if(property.getName().equals(name)){
				return false;
			}
		}
		propsMap.get(scope).add(new StringProperty(name, value));
		return true;
	}

	protected void addNameSpace(String nameSpace){
		propsMap.put(nameSpace, new LinkedList<GateProperty>());
	}

	protected void clearNameSpace(String nameSpace){
		propsMap.get(nameSpace).clear();
	}

	protected boolean addProp(String nameSpace, String name, String value){
		return addProp(propsMap, nameSpace, name, value);
	}
	
	public String toString(){return getName();}

    @Override
    public String getName(){
        return getProp(NS_NAME, NP_NAME).getStringValue();
    }

    @Override
    public void setName(String name){
        setProp(NS_NAME, NP_NAME, name);
    }

    @Override
    public void setProps(String nameSpace, LinkedList<GateProperty> props){
		// http Get query could have duplicate name. don't check if the name is unique.
		propsMap.get(nameSpace).clear();
        propsMap.put(nameSpace,props);
    }

	@Override
	public GateProperty getProp(String nameSpace, String name){
		return getProp(propsMap, nameSpace, name);
	}

	@Override
	public void setProp(String nameSpace, String name, String value) {
		setProp(propsMap, nameSpace, name, value);
	}
	@Override
	public void putProp(String nameSpace, String name, String value) {
		putProp(propsMap, nameSpace, name, value);
	}
	@Override
	public LinkedList<GateProperty> getProps(String nameSpace){
		return propsMap.get(nameSpace);
	}

    @Override
	public HashMap<String, LinkedList<GateProperty>> getPropsMap(){
		return propsMap;
	}

	@Override
	public void setPropsMap(HashMap<String, LinkedList<GateProperty>> propsMap){
		this.propsMap = propsMap;
	}

}
