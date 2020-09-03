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

import org.apache.http.*;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.FormBodyPartBuilder;
import org.apache.http.entity.mime.MIME;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.gate.gui.graph.elements.sampler.protocol.http.gui.HttpFileUploaderGui;
import org.gate.varfuncs.property.GateProperty;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public class HttpFileUploader extends HTTPHCAbstractImpl {

    public static final String PN_FilePath = "file_path";
    public static final String PN_ParameterName = "parameter_name";
    public static final String PN_MIMEType = "mime_type";
    public static final String PN_BrowserCompatibleHeaders = "browser-compatible_headers";

    public HttpFileUploader() {
    }

    @Override
    void addProps(){
        addProp(NS_DEFAULT, PN_FilePath, "");
        addProp(NS_DEFAULT, PN_ParameterName, "");
        addProp(NS_DEFAULT, PN_MIMEType, "");
        addProp(NS_DEFAULT, PN_BrowserCompatibleHeaders, "false");
    }

    @Override
    public String getGUI() {
        return HttpFileUploaderGui.class.getName();
    }

    /**
     * @param post {@link HttpPost}
     * @return String posted body if computable
     * @throws IOException if sending the data fails due to I/O
     */
    protected String sendPostData(HttpPost post) throws IOException {
        // Buffer to hold the post body, except file content
        StringBuilder postedBody = new StringBuilder(1000);
        String contentEncoding = getRunTimeProp(NS_DEFAULT, PN_ContentEncoding);
        final boolean haveContentEncoding = !contentEncoding.isEmpty();
        Charset charset = null;
        if(haveContentEncoding) {
            charset = Charset.forName(contentEncoding);
        } else {
            charset = MIME.DEFAULT_CHARSET;
        }
        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create()
                .setCharset(charset);
        if(Boolean.parseBoolean(getRunTimeProp(NS_DEFAULT, PN_BrowserCompatibleHeaders))){
            multipartEntityBuilder.setLaxMode();
        }else{
            multipartEntityBuilder.setStrictMode();
        }

        for (GateProperty argument : getRunTimeProps(NS_ARGUMENT)) {
            if (argument.getName().trim().isEmpty()) {
                continue;
            }
            StringBody stringBody = new StringBody(argument.getStringValue(), ContentType.create("text/plain", charset));
            FormBodyPart formPart = FormBodyPartBuilder.create(argument.getName(), stringBody).build();
            multipartEntityBuilder.addPart(formPart);
        }
        FileBody fileBody = new FileBody(new File(getRunTimeProp(NS_DEFAULT, PN_FilePath)), getRunTimeProp(NS_DEFAULT, PN_MIMEType));
        multipartEntityBuilder.addPart(getRunTimeProp(NS_DEFAULT, PN_ParameterName), fileBody);

        HttpEntity entity = multipartEntityBuilder.build();
        post.setEntity(entity);
        if (entity.isRepeatable()){
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            entity.writeTo(bos);
            bos.flush();
            // We get the posted bytes using the encoding used to create it
            postedBody.append(bos.toString(
                    contentEncoding.isEmpty() ? "US-ASCII" // $NON-NLS-1$ this igetSendParameterValuesAsPostBodys the default used by HttpClient
                            : contentEncoding));
            bos.close();
        } else {
            postedBody.append("<Multipart was not repeatable, cannot view what was sent>"); // $NON-NLS-1$
        }
        return postedBody.toString();
    }

    @Override
    public String getStaticLabel() {
        return "Http File Upload";
    }



}
