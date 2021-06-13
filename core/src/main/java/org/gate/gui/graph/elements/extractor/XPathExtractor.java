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
package org.gate.gui.graph.elements.extractor;

import org.apache.xpath.XPathAPI;
import org.apache.xpath.objects.XObject;
import org.gate.common.util.GateXMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

public class XPathExtractor extends AbstractExtractor {

    static final String PN_DefaultValue         = "default value"; // $NON-NLS-1$

    static final String PN_ValidateXML = "valid xml";
    static final String PN_IgnoreWhiteSpace = "ignore white space";
    static final String PN_Fragment        = "entire xpath fragment"; // $NON-NLS-1$

    public XPathExtractor(){ }

    @Override
    protected void initProperties(){
        addProp(NS_DEFAULT, PN_ValidateXML, "false");
        addProp(NS_DEFAULT, PN_IgnoreWhiteSpace, "false");
        addProp(NS_DEFAULT, PN_Fragment, "false");
    }

    @Override
    protected String extract(String pattern, String content) throws Exception {
        boolean validXML = Boolean.parseBoolean(getRunTimeProp(NS_DEFAULT, PN_ValidateXML));
        boolean ignoreWhiteSpace = Boolean.parseBoolean(getRunTimeProp(NS_DEFAULT, PN_IgnoreWhiteSpace));
        boolean fragment = Boolean.parseBoolean(getRunTimeProp(NS_DEFAULT, PN_Fragment));
        Document document =  GateXMLUtils.parse(content, validXML, ignoreWhiteSpace);
        LinkedList<String> matches = new LinkedList<>();
        putValuesForXPathInList(document,pattern,matches,fragment);
        if(matches.size() > 0){
            return matches.getFirst();
        }
        return null;
    }

    @Override
    public String getGUI() {
        return DefaultExtractorGui.class.getName();
    }

    /**
     * Put in matchStrings results of evaluation
     * @param document XML document
     * @param xPathQuery XPath Query
     * @param matchStrings List of strings that will be filled
     * @param fragment return fragment
     * @throws TransformerException when the internally used xpath engine fails
     */
    void putValuesForXPathInList(Document document, String xPathQuery,
                                 List<String> matchStrings, boolean fragment) throws TransformerException {
        String val = null;
        XObject xObject = XPathAPI.eval(document, xPathQuery);
        final int objectType = xObject.getType();
        if (objectType == XObject.CLASS_NODESET) {
            NodeList matches = xObject.nodelist();
            int length = matches.getLength();
            for (int i = 0 ; i < length; i++) {
                Node match = matches.item(i);
                if ( match instanceof Element){
                    if (fragment){
                        val = GateXMLUtils.nodeToString(match);
                    } else {
                        // elements have empty nodeValue, but we are usually interested in their content
                        final Node firstChild = match.getFirstChild();
                        if (firstChild != null) {
                            val = firstChild.getNodeValue();
                        } else {
                            val = match.getNodeValue();
                        }
                    }
                } else {
                    val = match.getNodeValue();
                }
                matchStrings.add(val);
            }
        } else if (objectType == XObject.CLASS_NULL
                || objectType == XObject.CLASS_UNKNOWN
                || objectType == XObject.CLASS_UNRESOLVEDVARIABLE) {
            log.warn("Unexpected object type: "+xObject.getTypeString()+" returned for: "+xPathQuery);
        } else {
            val = xObject.toString();
            matchStrings.add(val);
        }
    }

    /*================= internal business =================*/
    /**
     * Converts (X)HTML response to DOM object Tree.
     * This version cares of charset of response.
     * @param unicodeData
     * @return the parsed document
     *
     */
    private Document parseResponse(String unicodeData, boolean validate, boolean whitespace)
            throws IOException, ParserConfigurationException, SAXException
    {
        //TODO: validate contentType for reasonable types?

        // NOTE: responseData encoding is server specific
        //       Therefore we do byte -> unicode -> byte conversion
        //       to ensure UTF-8 encoding as required by XPathUtil
        // convert unicode String -> UTF-8 bytes
        // this method assumes UTF-8 input data
        byte[] utf8data = unicodeData.getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream in = new ByteArrayInputStream(utf8data);
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setValidating(validate);
        documentBuilderFactory.setIgnoringElementContentWhitespace(whitespace);
        return documentBuilderFactory.newDocumentBuilder().parse(in);
    }

    @Override
    public String getStaticLabel() {
        return "XPath Extractor";
    }
}
