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
package org.gate.gui.tree.test.elements.config;

import org.gate.gui.tree.test.elements.TestTreeElement;
import org.gate.runtime.GateContext;

public abstract class ConfigElement extends TestTreeElement {

    /*
     * update variables or modelContext of GateContext. don't change to parameter stored in the ConfigElement.
     * Throw runtime exception on configure failure to make the test fail.
     * */
    public void updateContext(GateContext context){
        // is check enable needed?
        if(isEnable()){
            update(context);
        }
    }
    abstract protected void update(GateContext context);

}
