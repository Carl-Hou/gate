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

package org.gate.saveload.utils;

import org.gate.common.util.GateException;
import org.gate.common.util.GateXMLUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import java.io.*;
import java.util.LinkedList;

public class DocumentHelper {
    Document doc;
    protected Logger log = LogManager.getLogger();

    public DocumentHelper(){
        this.doc = createDocument();
    }

    public Document getDocument() { return doc; }

    public Element getRootElement(){
        return doc.getDocumentElement();
    }

    /*
    * hold the document once it is successfully created
    * return null once error occur when create the document
    * */
    public Document createDocument(){
        doc = null;
        try {
            doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException e) {
            log.fatal("Error on create new dom object", e);
        }
        return  doc;
    }

    public Element createRootElement(String rootElementTagName){
        if(!doc.hasChildNodes()){
            Element root = doc.createElement(rootElementTagName);
            doc.appendChild(root);
        }
        return doc.getDocumentElement();
    }

    public Element createElement(String tagName){
        return doc.createElement(tagName);
    }

    // return imported node.
    public Node importNode(Node paraent, Node nodeToImport){
        Node nodeImported = doc.importNode(nodeToImport, true);
        paraent.appendChild(nodeImported);
        return nodeImported;

    }

    public LinkedList<Element> getChildrenElements(Element parent){
        NodeList nodes = parent.getChildNodes();
        LinkedList<Element> elements = new LinkedList<>();
        for(int i=0; i< nodes.getLength(); i++){
            Node node =nodes.item(i);
            if( node.getNodeType() == Element.ELEMENT_NODE){
                elements.add((Element) node);
            }
        }
        return elements;
    }

    public LinkedList<Element> getChildrenElements(Element parent, String nodeName){
        NodeList nodes = parent.getChildNodes();
        LinkedList<Element> elements = new LinkedList<>();
        for(int i=0; i< nodes.getLength(); i++){
            Node node =nodes.item(i);
            if( node.getNodeType() == Element.ELEMENT_NODE && node.getNodeName().equals(nodeName) ){
                elements.add((Element) node);
            }
        }
        return elements;
    }

    /*
    * return Document which parse from the file.
    * set doc method to null before laod document to indicate this is an invalid object to use.
    * throw Exception once error occur.
    * */
    public Document loadFromFile(File path) throws GateException {
        doc = GateXMLUtils.parse(path);
        return doc;
    }



}
