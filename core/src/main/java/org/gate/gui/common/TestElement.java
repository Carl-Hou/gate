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

import java.util.HashMap;
import java.util.LinkedList;

public interface TestElement {

    String NS_NAME = "name";
    String NP_NAME = "name";
    String NS_DEFAULT = "default";
    String NS_ARGUMENT = "argument";
    String NS_TEXT = "text";

    String getName();
    void setName(String name);

    void setProps(String nameSpace, LinkedList<GateProperty> props);
    LinkedList<GateProperty> getProps(String nameSpace);
    GateProperty getProp(String nameSpace, String name);
    void setProp(String nameSpace, String name, String value);
    void putProp(String nameSpace, String name, String value);
    HashMap<String, LinkedList<GateProperty>> getPropsMap();
    void setPropsMap(HashMap<String, LinkedList<GateProperty>> propsMap);
    String getGUI();
}
