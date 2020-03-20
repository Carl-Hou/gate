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
package org.gate.gui.graph.elements.sampler.protocol.http;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gate.common.util.GateUtils;
import org.gate.gui.graph.elements.ElementContext;

import java.util.HashMap;

public class HttpRequestContext implements ElementContext {

    Logger log = LogManager.getLogger(this.getClass());
    HashMap<String, CloseableHttpClient> httpClients = new HashMap<>();

    @Override
    public void close() {
        httpClients.values().forEach( client ->{
            GateUtils.closeQuietly(client);
        });
    }

    public CloseableHttpClient getClient(String clientId){
        CloseableHttpClient client = httpClients.get(clientId);
        if(null != client){
            return client;
        }
        return null;
    }

    public void setClient(String clientId, CloseableHttpClient httpClient){
        httpClients.put(clientId, httpClient);

    }

}
