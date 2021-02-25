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
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.message.BufferedHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.CharArrayBuffer;
import org.apache.http.util.EntityUtils;
import org.gate.common.util.GateUtils;
import org.gate.gui.common.DefaultParameters;
import org.gate.gui.details.results.elements.graph.ElementResult;
import org.gate.gui.graph.elements.AbstractGraphElement;
import org.gate.gui.graph.elements.sampler.Sampler;
import org.gate.gui.graph.elements.sampler.protocol.http.gui.HttpRequestGui;
import org.gate.gui.graph.elements.sampler.protocol.http.hc.LazyLayeredConnectionSocketFactory;
import org.gate.runtime.GateContext;
import org.gate.runtime.GateContextService;
import org.gate.varfuncs.functions.InvalidVariableException;
import org.gate.varfuncs.property.GateProperty;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public abstract class HTTPHCAbstractImpl extends AbstractGraphElement implements HTTPConstantsInterface, Sampler {

    private static final String HTTP_PREFIX = PROTOCOL_HTTP + "://"; // $NON-NLS-1$
    private static final String HTTPS_PREFIX = PROTOCOL_HTTPS + "://"; // $NON-NLS-1$

    private static final String ARG_VAL_SEP = "="; // $NON-NLS-1$
    private static final String QRY_SEP = "&"; // $NON-NLS-1$
    private static final String QRY_PFX = "?"; // $NON-NLS-1$

    public HTTPHCAbstractImpl() {
        addNameSpace(NS_ARGUMENT);
        addProp(NS_NAME, PN_Path, "");
        addProp(NS_DEFAULT, PN_Protocol, "");
        addProp(NS_DEFAULT, PN_ServerNameIP, "");
        addProp(NS_DEFAULT, PN_PortNumber, "");
        addProp(NS_DEFAULT, PN_Path, "");
        addProps();
        addProp(NS_DEFAULT, PN_Method, "");
        addProp(NS_DEFAULT, PN_ContentEncoding, "");
        addProp(NS_DEFAULT, PN_UseKeepAlive, "true");
        addProp(NS_DEFAULT, PN_ConnectTimeout, "");
        addProp(NS_DEFAULT, PN_ResponseTimeout, "");
    }

    abstract void addProps();

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
                postBody.append(new String(argument.getStringValue().getBytes(), getEncoding()));
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

    // key for get properties copy with defaults
    @Override
    protected String getContextConfigKey(){
        return DefaultConfigName;
    }

    @Override
    protected void exec(ElementResult result) {
        result.setRunTimeProps(getRunTimePropsMap());
        URL url;
        try {
            url = getUrl();
        } catch (MalformedURLException | UnsupportedEncodingException e) {
            log.error("Fail to build URL:", e);
            result.appendMessage("Fail to build URL");
            result.setThrowable(e);
            return;
        }

        result.appendRequestMessage("URL:" + url.toString());

        GateContext context = GateContextService.getContext();

        CloseableHttpClient httpClient = getHttpClient(context, url, result);
        if (result.isFailure()) {
            return;
        }
        String method = getRunTimeProp(NS_DEFAULT, PN_Method);

        HttpRequestBase httpRequest = getHttpRequest(url, method, false, 0, result);

        if(result.isFailure()){
            return;
        }

        HttpClientContext  httpContext = HttpClientContext.create();
        CloseableHttpResponse httpResponse = null;
        try {
            handleMethod(method, result, httpRequest, httpContext);
            // perform the sample. not support auth manager currently.
            httpResponse =
                    httpClient.execute(httpRequest, httpContext);
            List<URI> redirectLocations = httpContext.getRedirectLocations();
            if(redirectLocations != null && redirectLocations.size() > 0){
                HttpHost target = httpContext.getTargetHost();
                URI location = URIUtils.resolve(httpRequest.getURI(), target, redirectLocations);
                httpRequest.setURI(location);
                result.appendRequestMessage("Redirect to: ");
                result.appendRequestMessage(location.toString());
                httpResponse =
                        httpClient.execute(httpRequest, httpContext);
            }
            // Start to record response data
            result.appendRequestMessage("Request Headers:");
            result.appendRequestMessage(getConnectionHeaders(httpRequest));

            Header contentType = httpResponse.getLastHeader(HTTPConstantsInterface.HEADER_CONTENT_TYPE);
            if (contentType != null) {
                String ct = contentType.getValue();
                result.appendMessage("Content-Type:" + ct);
            }

            HttpEntity entity = httpResponse.getEntity();
            if (entity != null) {
                String content = EntityUtils.toString(entity, getEncoding());
                result.setResponseObject(content);
                result.appendMessage("Content-Length: " + String.valueOf(entity.getContentLength()));
                result.setResponseObject(content);
            }

            int statusCode = httpResponse.getStatusLine().getStatusCode();
            result.appendMessage("Response code: " + Integer.toString(statusCode));
            if (!isSuccessCode(statusCode)) {
                result.setFailure();
                return;
            }
            result.appendMessage("Response headers:");
            result.appendMessage(getResponseHeaders(httpResponse, httpContext));
            if(result.isSuccess()){
                postRequest(result);
            }
        } catch (Exception e) {
            log.error("Fail to execute http request:", e);
            result.setThrowable(e);
        } finally {
            GateUtils.closeQuietly(httpResponse);
        }
    }

    abstract void postRequest(ElementResult result);

//    @Override
//    public String getGUI() {
//        return HttpRequestGui.class.getName();
//    }

    URL getUrl() throws MalformedURLException, UnsupportedEncodingException {
        StringBuilder pathAndQuery = new StringBuilder(100);
//        String path = getRunTimeProp(NS_NAME, PN_Path);
        String path = getRunTimeProp(NS_DEFAULT, PN_Path);
        // Hack to allow entire URL to be provided in host field
        if (path.startsWith(HTTP_PREFIX)
                || path.startsWith(HTTPS_PREFIX)) {
            return new URL(path);
        }
        String host = getRunTimeProp(NS_DEFAULT, PN_ServerNameIP).toLowerCase();
        // this will be constant to http and https.
        String protocol = getRunTimeProp(NS_DEFAULT, PN_Protocol).toLowerCase();
        if (!path.startsWith("/")) {
            pathAndQuery.append("/");
        }
        pathAndQuery.append(path);
        String method = getRunTimeProp(NS_DEFAULT, PN_Method);
        // Add the query string if it is a HTTP GET or DELETE request

        if (GET.equals(method) || DELETE.equals(method)) {
            // Get the query string encoded in specified encoding
            // If no encoding is specified by user, we will get it
            // encoded in UTF-8, which is what the HTTP spec says
            String queryString = getQueryString(getRunTimeProp(NS_DEFAULT, PN_ContentEncoding));
            if (queryString.length() > 0) {
                if (path.contains(QRY_PFX)) {// Already contains a prefix
                    pathAndQuery.append(QRY_SEP);
                } else {
                    pathAndQuery.append(QRY_PFX);
                }
                pathAndQuery.append(queryString);
            }
        }

        String port = getRunTimeProp(NS_DEFAULT, PN_PortNumber);
        if(port.trim().isEmpty()){
            if(protocol.equalsIgnoreCase(PROTOCOL_HTTP)){
                port = DEFAULT_HTTP_PORT;
            }
            if(protocol.equalsIgnoreCase(PROTOCOL_HTTPS)){
                port = DEFAULT_HTTPS_PORT;
            }
        }
        return new URL(protocol, host, Integer.parseInt(port), pathAndQuery.toString());
    }

    /**
     * Gets the QueryString attribute of the UrlConfig object, using the
     * specified encoding to encode the parameter values put into the URL
     *
     * @param contentEncoding the encoding to use for encoding parameter values
     * @return the QueryString value
     */
    public String getQueryString(String contentEncoding) throws UnsupportedEncodingException {

        LinkedList<GateProperty> arguments = getRunTimeProps(NS_ARGUMENT);
        // Optimisation : avoid building useless objects if empty arguments
        if (arguments.size() == 0) {
            return "";
        }

        // Check if the sampler has a specified content encoding
        if (contentEncoding.trim().isEmpty()) {
            // We use the encoding which should be used according to the HTTP spec, which is UTF-8
            contentEncoding = Default_URL_ARGUMENT_ENCODING;
        }

        StringBuilder buf = new StringBuilder(arguments.size() * 15);

        boolean first = true;
        for(GateProperty argument : arguments){
            String encodedName = URLEncoder.encode(argument.getName(), contentEncoding);
            if (encodedName.length() == 0) {
                continue; // Skip parameters with a blank name (allows use of optional variables in parameter lists)
            }
            if (!first) {
                buf.append(QRY_SEP);
            } else {
                first = false;
            }
            buf.append(encodedName);
            //don't understand why JMeter support user MetaData (e.g use "" instead of "="). Investigate when bug occur.
            buf.append(ARG_VAL_SEP);
            buf.append(URLEncoder.encode(argument.getStringValue(), contentEncoding));
        }
        return buf.toString();
    }

    boolean hasArguments() {
        if (getRunTimeProps(NS_ARGUMENT).size() > 0) {
            return false;
        }
        return true;
    }

    // Check JMeter code. looks like this check if argument will be sent as post body.
    boolean getSendParameterValuesAsPostBody() {
//        if (getPostBodyRaw()) { TODO do we also need this?
//            return true;
//        }
        boolean noArgumentsHasName = true;
        for(GateProperty argument : getRunTimeProps(NS_ARGUMENT)){
            if (!argument.getName().isEmpty()) {
                noArgumentsHasName = false;
                break;
            }
        }
        return noArgumentsHasName;
    }

    /**
     * Setup following elements on httpRequest:
     * <ul>
     * <li>ConnRoutePNames.LOCAL_ADDRESS enabling IP-SPOOFING</li>
     * <li>Socket and connection timeout</li>
     * <li>Redirect handling</li>
     * <li>Keep Alive header or Connection Close</li>
     * <li>Calls setConnectionHeaders to setup headers</li>
     * <li>Calls setConnectionCookie to setup Cookie</li>
     * </ul>
     *
     * @param url
     *            {@link URL} of the request
     * @param httpRequest
     *            http request for the request
     * @throws IOException
     *             if hostname/ip to use could not be figured out
     */
    protected void setupRequest(URL url, HttpRequestBase httpRequest)
            throws InvalidVariableException {

        RequestConfig.Builder requestConfigBuilder = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.DEFAULT);
        if(!getRunTimeProp(NS_DEFAULT, PN_ResponseTimeout).isEmpty()){
            int responseTimeout = Integer.parseInt(getRunTimeProp(NS_DEFAULT, PN_ResponseTimeout));
            if(responseTimeout > 0){
                requestConfigBuilder.setSocketTimeout(responseTimeout);
            }
        }

        if(!getRunTimeProp(NS_DEFAULT, PN_ConnectTimeout).isEmpty()){
            int connectTimeout = Integer.parseInt(getRunTimeProp(NS_DEFAULT, PN_ConnectTimeout));
            if(connectTimeout > 0){
                requestConfigBuilder.setConnectTimeout(connectTimeout);
            }
        }

        // a well-behaved browser is supposed to send 'Connection: close'
        // with the last request to an HTTP server. Instead, most browsers
        // leave it to the server to close the connection after their
        // timeout period. Leave it to the JMeter user to decide.
        if (Boolean.parseBoolean(getRunTimeProp(NS_DEFAULT, PN_UseKeepAlive))) {
            httpRequest.setHeader(HEADER_CONNECTION, KEEP_ALIVE);
        } else {
            httpRequest.setHeader(HEADER_CONNECTION, CONNECTION_CLOSE);
        }
        //TODO implement header manager.
//        setConnectionHeaders(httpRequest, url, getHeaderManager(), getCacheManager());
        setRequestHeaders(httpRequest, url);
        // need to add cookies to result?
//        if (res != null) {
//            res.setCookies(cookies);
//        }
        httpRequest.setConfig(requestConfigBuilder.build());
    }

    HttpRequestBase getHttpRequest(URL url, String method,
                                   boolean areFollowingRedirect, int frameDepth, ElementResult res) {

        HttpRequestBase httpRequest = null;
        try {
            URI uri = url.toURI();
            if (method.equals(POST)) {
                httpRequest = new HttpPost(uri);
            } else if (method.equals(GET)) {
                // Some servers fail if Content-Length is equal to 0
                // so to avoid this we use HttpGet when there is no body (Content-Length will not be set)
                // otherwise we use HttpGetWithEntity
                if (!hasArguments()
                        || getSendParameterValuesAsPostBody()) {
                    httpRequest = new HttpGetWithEntity(uri);
                } else {
                    httpRequest = new HttpGet(uri);
                }
            } else if (method.equals(PUT)) {
                httpRequest = new HttpPut(uri);
            } else if (method.equals(HEAD)) {
                httpRequest = new HttpHead(uri);
            } else if (method.equals(TRACE)) {
                httpRequest = new HttpTrace(uri);
            } else if (method.equals(OPTIONS)) {
                httpRequest = new HttpOptions(uri);
            } else if (method.equals(DELETE)) {
                httpRequest = new HttpDelete(uri);
            } else if (method.equals(PATCH)) {
                httpRequest = new HttpPatch(uri);
            } else  {
                throw new IllegalArgumentException("Unexpected method: '" + method + "'");
            }
            setupRequest(url, httpRequest); // can throw IOException
        } catch (Exception e) {
            log.error("Error when build request: ", e);
            res.setThrowable(e);
            return null;
        }
        return httpRequest;
    }

    protected void setRequestHeaders(HttpRequestBase request, URL url) throws InvalidVariableException {

        HashMap<String, Object> modelContext = GateContextService.getContext().getConfigs();
        if(!modelContext.containsKey(HeaderManagerName)){
            return;
        }
        DefaultParameters httpHeaders = (DefaultParameters) modelContext.get(HeaderManagerName);
        for(GateProperty header : getRuntimeCopy(httpHeaders.getDefaultParameters()).get(NS_ARGUMENT)){
            if(!HEADER_CONTENT_LENGTH.equalsIgnoreCase(header.getStringValue())){
                request.setHeader(header.getName(), header.getStringValue());
            }
        }
    }

    /**
     * Creates the entity data to be sent.
     * <p>
     * If there is a file entry with a non-empty MIME type we use that to
     * set the request Content-Type header, otherwise we default to whatever
     * header is present from a Header Manager.
     * <p>
     * If the content encoding is null or empty
     * we use UTF-8 not the HC4 default provided by {@link HTTP#DEF_CONTENT_CHARSET} which is
     * ISO-8859-1.
     *
     * @param entity to be processed, e.g. PUT or PATCH
     * @return the entity content, may be empty
     * @throws  UnsupportedEncodingException for invalid charset name
     * @throws IOException cannot really occur for ByteArrayOutputStream methods
     */
    protected String sendEntityData( HttpEntityEnclosingRequestBase entity) throws IOException {
        boolean hasEntityBody = false;

        // Check for local contentEncoding (charset) override; fall back to default for content body
        // we do this here rather so we can use the same charset to retrieve the data
        // not like JMeter we use UTF-8 in default
        final String charset = getEncoding();

        // If none of the arguments have a name specified, we
        // just send all the values as the entity body
        if(getSendParameterValuesAsPostBody()) {
            hasEntityBody = true;
            StringBuilder entityBodyContent = new StringBuilder();
            for(GateProperty argument : getRunTimeProps(NS_ARGUMENT)){
                entityBodyContent.append(new String(argument.getStringValue().getBytes(), charset));
            }

            StringEntity requestEntity = new StringEntity(entityBodyContent.toString(), charset);
            entity.setEntity(requestEntity);
        }
        // Check if we have any content to send for body
        if(hasEntityBody) {
            // If the request entity is repeatable, we can send it first to
            // our own stream, so we can return it
            final HttpEntity entityEntry = entity.getEntity();
            // Buffer to hold the entity body
            StringBuilder entityBody = new StringBuilder(1000);
            if(entityEntry.isRepeatable()) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                entityEntry.writeTo(bos);
                bos.flush();
                entityBody.append(bos.toString(getEncoding()));
                bos.close();
            }
            else {
                // this probably cannot happen
                entityBody.append("<RequestEntity was not repeatable, cannot view what was sent>");
            }
            return entityBody.toString();
        }
        return ""; // may be the empty string
    }

    /**
     * <code>PUT</code> or <code>PATCH</code>
     * <p>
     * Field HTTPSampleResult#queryString of result is modified in the 2 cases
     *
     * @param method
     *            String HTTP method
     * @param result
     *            {@link ElementResult}
     * @param httpRequest
     *            {@link HttpRequestBase}
     * @param localContext
     *            {@link HttpContext}
     * @throws IOException
     *             when posting data fails due to I/O
     */
    protected void handleMethod(String method, ElementResult result,
                                HttpRequestBase httpRequest, HttpContext localContext) throws IOException {
        // Handle the various methods
        if (httpRequest instanceof HttpPost) {
            String postBody = sendPostData((HttpPost)httpRequest);
            result.appendRequestMessage("Post Body:");
            result.appendRequestMessage(postBody);
        } else if (httpRequest instanceof HttpEntityEnclosingRequestBase) {
            String entityBody = sendEntityData((HttpEntityEnclosingRequestBase) httpRequest);
            result.appendRequestMessage("Entity Body:");
            result.appendRequestMessage(entityBody);
        }
    }

    // get http client from context by implement_protocol_host_port.
    // if https. load ssl when create the client.
    CloseableHttpClient getHttpClient(GateContext context, URL url, ElementResult result) {
        HttpRequestContext httpClientContext;
        if (context.getGraphElementContext().containsKey(ApacheHttpClientContextName)) {
            httpClientContext = (HttpRequestContext) context.getGraphElementContext().get(ApacheHttpClientContextName);
        } else {
            httpClientContext = new HttpRequestContext();
            context.getGraphElementContext().put(ApacheHttpClientContextName, httpClientContext);
        }
        String clientID = url.getProtocol() + "://" + url.getHost() + url.getPort();

        CloseableHttpClient hc = httpClientContext.getClient(clientID.toLowerCase());
        if (hc == null) {
            try {
                hc = buildHttpClient();
            } catch (Throwable t) {
                log.error("Fail to get client from context", t);
                result.appendMessage("Fail to build client from context:");
                result.setThrowable(t);
            }
            httpClientContext.setClient(clientID, hc);
        }
        return hc;
    }

    CloseableHttpClient buildHttpClient() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        HttpClientBuilder httpClientBuilder = HttpClients.custom()
                .setRedirectStrategy(new LaxRedirectStrategy())
                .setSSLSocketFactory(new LazyLayeredConnectionSocketFactory());

//        if ("https".equals(getRunTimeProp(NS_DEFAULT, PN_Protocol).toLowerCase())) {
//            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
//                // Trust all
//                @Override
//                public boolean isTrusted(X509Certificate[] chain,
//                                         String authType) throws CertificateException {
//                    return true; // NOT CHECK
//                }
//            }).build();
//            String[] supportedProtocols =  GateUtils.getParameterList(
//                    GateProps.getProperties().getProperty("gate.http.ssl.protocols")).toArray(new String[0]);
//
//            SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContext, supportedProtocols,
//                    null, new HostnameVerifier() {
//                @Override
//                public boolean verify(String hostname, SSLSession session) {
//                    return true; // NOT CHECK
//                }
//            });

//            httpClientBuilder.setSSLSocketFactory(sslConnectionSocketFactory);
//        }
        return httpClientBuilder.build();
    }

    /**
     * Write responseHeader to headerBuffer in an optimized way
     * @param headerBuffer {@link StringBuilder}
     * @param responseHeader {@link Header}
     */
    private void writeResponseHeader(StringBuilder headerBuffer, Header responseHeader) {
        if(responseHeader instanceof BufferedHeader) {
            CharArrayBuffer buffer = ((BufferedHeader)responseHeader).getBuffer();
            headerBuffer.append(buffer.buffer(), 0, buffer.length()).append('\n'); // $NON-NLS-1$;
        }
        else {
            headerBuffer.append(responseHeader.getName())
                    .append(": ") // $NON-NLS-1$
                    .append(responseHeader.getValue())
                    .append('\n'); // $NON-NLS-1$
        }
    }

    /**
     * Get all the request headers for the <code>HttpMethod</code>
     *
     * @param method
     *            <code>HttpMethod</code> which represents the request
     * @return the headers as a string
     */
    private String getConnectionHeaders(HttpRequest method) {
        if(method != null) {
            // Get all the request headers
            StringBuilder hdrs = new StringBuilder(150);
            Header[] requestHeaders = method.getAllHeaders();
            for (Header requestHeader : requestHeaders) {
                // Exclude the COOKIE header, since cookie is reported separately in the sample
                if (!HTTPConstantsInterface.HEADER_COOKIE.equalsIgnoreCase(requestHeader.getName())) {
                    writeResponseHeader(hdrs, requestHeader);
                }
            }

            return hdrs.toString();
        }
        return ""; ////$NON-NLS-1$
    }

    /**
     * Set any default request headers to include
     *
     * @param request the HttpRequest to be used
     */
    protected void setDefaultRequestHeaders(HttpRequest request) {
        // Method left empty here, but allows subclasses to override
    }

    /**
     * Gets the ResponseHeaders
     *
     * @param response
     *            containing the headers
     * @param localContext {@link HttpContext}
     * @return string containing the headers, one per line
     */
    private String getResponseHeaders(HttpResponse response, HttpContext localContext) {
        Header[] rh = response.getAllHeaders();

        StringBuilder headerBuf = new StringBuilder(40 * (rh.length+1));
        headerBuf.append(response.getStatusLine());// header[0] is not the status line...
        headerBuf.append("\n"); // $NON-NLS-1$

        for (Header responseHeader : rh) {
            writeResponseHeader(headerBuf, responseHeader);
        }
        return headerBuf.toString();
    }

    /**
     * Determine if the HTTP status code is successful or not
     * i.e. in range 200 to 399 inclusive
     *
     * @param code status code to check
     * @return whether in range 200-399 or not
     */
    protected boolean isSuccessCode(int code) {
        return code >= 200 && code <= 399;
    }
    String getEncoding() {
        String contentEncoding;
        if(getRunTimeProp(NS_DEFAULT, PN_ContentEncoding).isEmpty()) {
            contentEncoding = Default_URL_ARGUMENT_ENCODING;
        }else {
            contentEncoding = getRunTimeProp(NS_DEFAULT, PN_ContentEncoding);
        }
        return contentEncoding;
    }

    @Override
    public String getStaticLabel() {
        return "Http Request";
    }

    /**
     * Implementation that allows GET method to have a body
     */
    public static final class HttpGetWithEntity extends HttpEntityEnclosingRequestBase {
        public HttpGetWithEntity(final URI uri) {
            super();
            setURI(uri);
        }
        @Override
        public String getMethod() {
            return GET;
        }
    }
    // we use apache http delete

}
