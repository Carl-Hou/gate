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

package org.gate.gui.details.results.elements.graph;

import org.gate.gui.details.results.elements.Result;
import org.gate.varfuncs.property.GateProperty;

import java.util.HashMap;
import java.util.LinkedList;

public interface ElementResult extends Result {

//    void setRunTimeProps(HashMap<String, HashMap<String, String>> props);
    void setRunTimeProps(HashMap<String, LinkedList<GateProperty>> props);
    void setResponseObject(Object responseObject);
    // for functions ?
//    Object getResponseObject();
    String getResponseAsString();

//    void setThrowable(Throwable t);
//    Throwable getThrowable();

    String getResult();
    void appendRequestMessage(CharSequence message);
    String getRequestMessage();

}
