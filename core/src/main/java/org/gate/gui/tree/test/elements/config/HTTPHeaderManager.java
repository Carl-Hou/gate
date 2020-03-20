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

import org.gate.gui.common.DefaultParameters;
import org.gate.gui.graph.elements.sampler.protocol.http.HTTPConstantsInterface;
import org.gate.gui.tree.test.elements.config.gui.DefaultTreeConfigGui;
import org.gate.runtime.GateContext;


public class HTTPHeaderManager extends ConfigElement implements HTTPConstantsInterface {

    public HTTPHeaderManager(){
        addNameSpace(NS_ARGUMENT);
    }

    @Override
    public void update(GateContext context) {
        DefaultParameters httpHeaders;
        if(context.getConfigs().containsKey(HeaderManagerName)){
            httpHeaders = (DefaultParameters) context.getConfigs().get(HeaderManagerName);
        }else{
            httpHeaders = new DefaultParameters();
            context.getConfigs().put(HeaderManagerName, httpHeaders);
        }
        httpHeaders.modify(NS_ARGUMENT, getProps(NS_ARGUMENT));

    }

    @Override
    public String getGUI(){
        return DefaultTreeConfigGui.class.getName();
    }


}
