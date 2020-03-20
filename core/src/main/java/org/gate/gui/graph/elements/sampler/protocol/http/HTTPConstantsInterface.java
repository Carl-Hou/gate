package org.gate.gui.graph.elements.sampler.protocol.http;

import org.gate.gui.tree.test.elements.config.HTTPHeaderManager;
import org.gate.gui.tree.test.elements.config.HTTPRequestDefaults;

import java.nio.charset.StandardCharsets;

public interface HTTPConstantsInterface {

    // Unique name in modelContext of GateContext.
    String DefaultConfigName = HTTPRequestDefaults.class.getName();
    String HeaderManagerName = HTTPHeaderManager.class.getName();
    String ApacheHttpClientContextName = HttpRequestContext.class.getName();

    String PN_Protocol = "protocol";
    String PN_ServerNameIP = "Server name or IP";
    String PN_PortNumber = "Port Number";
    String PN_Method = "Method";
    String PN_Path = "Path";
    String PN_ContentEncoding = "Content encoding";
    String PN_UseKeepAlive = "Use KeepAlive";
    String PN_ConnectTimeout = "Connect Timeout";
    String PN_ResponseTimeout = "Response Timeout";



    // method
    String DEFAULT_HTTPS_PORT = "443"; // $NON-NLS-1$
    String DEFAULT_HTTP_PORT = "80"; // $NON-NLS-1$
    String PROTOCOL_HTTP = "http"; // $NON-NLS-1$
    String PROTOCOL_HTTPS = "https"; // $NON-NLS-1$
    String HEAD = "HEAD"; // $NON-NLS-1$
    String POST = "POST"; // $NON-NLS-1$
    String PUT = "PUT"; // $NON-NLS-1$
    String GET = "GET"; // $NON-NLS-1$
    String OPTIONS = "OPTIONS"; // $NON-NLS-1$
    String TRACE = "TRACE"; // $NON-NLS-1$
    String DELETE = "DELETE"; // $NON-NLS-1$
    String PATCH = "PATCH"; // $NON-NLS-1$

    String PROPFIND = "PROPFIND"; // $NON-NLS-1$
    String PROPPATCH = "PROPPATCH"; // $NON-NLS-1$
    String MKCOL = "MKCOL"; // $NON-NLS-1$
    String COPY = "COPY"; // $NON-NLS-1$
    String MOVE = "MOVE"; // $NON-NLS-1$
    String LOCK = "LOCK"; // $NON-NLS-1$
    String UNLOCK = "UNLOCK"; // $NON-NLS-1$
    String CONNECT = "CONNECT"; // $NON-NLS-1$
    String REPORT = "REPORT"; // $NON-NLS-1$
    String MKCALENDAR = "MKCALENDAR"; // $NON-NLS-1$
    String SEARCH = "SEARCH"; // $NON-NLS-1$


    // header
    String HEADER_CONTENT_TYPE = "Content-Type"; // $NON-NLS-1$
    String HEADER_COOKIE = "Cookie"; // $NON-NLS-1$
    String HEADER_CONTENT_LENGTH = "Content-Length"; // $NON-NLS-1$
    //name
    String Default_URL_ARGUMENT_ENCODING = StandardCharsets.UTF_8.name();

    //Other
    String HEADER_CONNECTION = "Connection"; // $NON-NLS-1$
    String KEEP_ALIVE = "keep-alive"; // $NON-NLS-1$
    String CONNECTION_CLOSE = "close"; // $NON-NLS-1$

}
