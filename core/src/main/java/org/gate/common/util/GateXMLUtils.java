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
package org.gate.common.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gate.common.config.GateProps;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.charset.StandardCharsets;


public class GateXMLUtils {
    private final static Logger log = LogManager.getLogger();

    static public String nodeToString(Node node) {
        StringWriter sw = new StringWriter();
        try {
            createTransformer().transform(new DOMSource(node), new StreamResult(sw));
        } catch (TransformerException te) {
            throw new GateRuntimeExcepiton("nodeToString Transformer Exception", te);
        }
        return sw.toString();
    }

    static public String nodesToString(NodeList nodes) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < nodes.getLength(); i++) {
            sb.append(nodeToString(nodes.item(i)));
            sb.append(GateProps.LineSeparator);
        }
        sb.trimToSize();
        return sb.toString();
    }

    public static Document parse(File xmlFile) throws GateException {
        InputStream fis = null;
        try {
            fis = new BufferedInputStream(new FileInputStream(xmlFile));
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            return builder.parse(fis);
        }catch (IOException | ParserConfigurationException | SAXException e){
            log.fatal("Fail to load Document from file:" + xmlFile.getAbsolutePath(), e);
            throw new GateException(e);
        } finally {
            GateUtils.closeQuietly(fis);
        }
    }

    /**
     * parse (X)HTML response to DOM object Tree.
     * This version cares of charset of response.
     * @param xmlString
     * @return the parsed document
     *
     */
    public static Document parse(String xmlString, boolean validate, boolean whitespace)
            throws IOException, ParserConfigurationException, SAXException
    {
        //TODO: validate contentType for reasonable types?

        // NOTE: xmlString encoding is server specific
        //       Therefore we do byte -> unicode -> byte conversion
        //       to ensure UTF-8 encoding as required by XPathUtil
        // convert unicode String -> UTF-8 bytes
        // this method assumes UTF-8 input data
        byte[] utf8data = xmlString.getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream in = new ByteArrayInputStream(utf8data);
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setValidating(validate);
        documentBuilderFactory.setIgnoringElementContentWhitespace(whitespace);
        return documentBuilderFactory.newDocumentBuilder().parse(in);
    }


    public static  void toFile(Document doc, File path) throws GateException {
        FileOutputStream out = null;
        try {
            if(!path.exists()){
                path.createNewFile();
            }
            out = new FileOutputStream(path);
            StreamResult xmlResult = new StreamResult(out);
            Transformer transformer= createTransformer();
            transformer.transform(new DOMSource(doc), xmlResult);
            out.close();
        } catch (IOException | TransformerException e) {
            log.debug("Fail to dump document to file:" + path.getAbsolutePath(), e);
            throw new GateException(e);
        }finally {
            GateUtils.closeQuietly(out);
        }
        // File output stream
    }
    // todo change this to private
    public static Transformer createTransformer() throws TransformerException {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.CDATA_SECTION_ELEMENTS, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        return transformer;
    }

}
