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

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.gate.gui.graph.elements.sampler.protocol.http.gui.HttpAuthorizationGui;
import org.gate.gui.graph.elements.sampler.protocol.http.gui.HttpRequestGui;
import org.gate.varfuncs.property.GateProperty;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class HttpAuthorization extends HTTPHCAbstractImpl {

    public HttpAuthorization() {

    }

    @Override
    void addProps() {

    }


    @Override
    public String getGUI() {
        return HttpAuthorizationGui.class.getName();
    }


    /**
     * @param post {@link HttpPost}
     * @return String posted body if computable
     * @throws IOException if sending the data fails due to I/O
     */
    protected String sendPostData(HttpPost post) throws IOException {
        // Buffer to hold the post body, except file content
        StringBuilder postedBody = new StringBuilder(1000);

        final String contentEncoding = getEncoding();
        final boolean haveContentEncoding = !contentEncoding.isEmpty();


        // In a post request which is not multipart, we only support
        // parameters, no file upload is allowed

        // If none of the arguments have a name specified, we
        // just send all the values as the post body
        if (getSendParameterValuesAsPostBody()) {
            // Just append all the parameter values, and use that as the post body
            StringBuilder postBody = new StringBuilder();
            for (GateProperty argument : getRunTimeProps(NS_ARGUMENT)) {
                postBody.append(URLEncoder.encode(argument.getStringValue(), getEncoding()));
            }

            // Let StringEntity perform the encoding
            StringEntity requestEntity;
            if (haveContentEncoding) {
                requestEntity = new StringEntity(postBody.toString(), contentEncoding);
            } else {
                requestEntity = new StringEntity(postBody.toString(), Default_URL_ARGUMENT_ENCODING);
            }

            post.setEntity(requestEntity);
            postedBody.append(postBody.toString());
        } else {
            // It is a normal post request, with parameter names and values
            List<NameValuePair> nvps = new ArrayList<>();
            for (GateProperty argument : getRunTimeProps(NS_ARGUMENT)) {
                if (argument.getName().trim().isEmpty()) {
                    continue;
                }
                nvps.add(new BasicNameValuePair(argument.getName(), argument.getStringValue()));
            }
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(nvps, contentEncoding);
            post.setEntity(entity);
            if (entity.isRepeatable()) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                post.getEntity().writeTo(bos);
                bos.flush();
                // We get the posted bytes using the encoding used to create it
                postedBody.append(bos.toString(getEncoding()));
                bos.close();
            } else {
                postedBody.append("<RequestEntity was not repeatable, cannot view what was sent>");
            }
        }
        return postedBody.toString();
    }

    @Override
    public String getStaticLabel() {
        return "Http Authorization";
    }


}
