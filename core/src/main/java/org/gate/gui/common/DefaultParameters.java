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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gate.varfuncs.property.GateProperty;
import org.gate.varfuncs.property.StringProperty;

import java.io.Serializable;
import java.util.*;

/**
 * Use same data structure store parameters to keep compatible with TestElement
 */

public class DefaultParameters implements Serializable {
    protected Logger log = LogManager.getLogger(this.getClass());
    protected HashMap<String, LinkedList<GateProperty>> defaultParameters = new HashMap();

    public LinkedList<GateProperty> getProps(String nameSpace){
        return defaultParameters.get(nameSpace);
    }

    public HashMap<String, LinkedList<GateProperty>> getDefaultParameters(){
        return defaultParameters;
    }

    public GateProperty getProp(String scope, String name){
        for(GateProperty property : defaultParameters.get(scope)){
            if(property.getName().equals(name)){
                return property;
            }
        }
        return  null;
    }

    public void modify(String nameSpace, LinkedList<GateProperty> parameters){
        if(!defaultParameters.containsKey(nameSpace)){
            defaultParameters.put(nameSpace, new LinkedList<>());
        }
        LinkedList<GateProperty> newDefaultParameters = new LinkedList<>(parameters);
        ListIterator<GateProperty> listIterator = newDefaultParameters.listIterator();
        while(listIterator.hasNext()){
            GateProperty newDefaultParameter = listIterator.next();
            for(GateProperty defaultParameter : defaultParameters.get(nameSpace)){
                if(newDefaultParameter.getName().equals(defaultParameter.getName())){
                    defaultParameter.setObjectValue(newDefaultParameter.getStringValue());
                    listIterator.remove();
                }
            }
        }
        defaultParameters.get(nameSpace).addAll(newDefaultParameters);
        newDefaultParameters.clear();
    }
    public void applyDefaultsInNameSpace(LinkedList<GateProperty> gateProperties){
        for(GateProperty property : gateProperties){
            for(GateProperty defaultProperty: getProps(TestElement.NS_DEFAULT)){
                if(property.getName().equals(defaultProperty.getName()) && property.getStringValue().isEmpty()){
                    property.setObjectValue(defaultProperty.getObjectValue());
                }
            }
        }
    }
}
