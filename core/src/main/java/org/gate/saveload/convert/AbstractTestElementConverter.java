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

package org.gate.saveload.convert;

import org.gate.gui.common.TestElement;
import org.gate.saveload.utils.DocumentHelper;
import org.gate.saveload.utils.exceptions.TestElementDecodeException;
import org.gate.varfuncs.property.GateProperty;
import org.gate.varfuncs.property.StringProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

/*
 *  This class use to transform TestElement object to DocumentElement and Document Element to TestElement
 * */
public abstract class AbstractTestElementConverter implements TestElementConverter {


    final public static String Properties = "ElementProperties";
    final public static String NAME = "name";
    final public static String VALUE = "value";
    final public static String NAMESPACE = "NameSpace";
    final public static String PROPERTY = "ElementProperty";

    //    protected Document doc = null;
    protected DocumentHelper documentHelper = null;
    String tagName;
    Class testElementClass;

    protected Logger log = LogManager.getLogger(this);

    public AbstractTestElementConverter(Class testElementClass, String tagName) {
        this.tagName = tagName;
        this.testElementClass = testElementClass;
    }

    public AbstractTestElementConverter(Class testElementClass) {
        this.tagName = testElementClass.getSimpleName();
        this.testElementClass = testElementClass;
    }

    public boolean is(TestElement testElement) {
        return this.testElementClass.isInstance(testElement);
    }

    public boolean is(String tagName) {
        return this.tagName.equals(tagName);
    }

    public void setDocumentHelper(DocumentHelper documentHelper) {
        this.documentHelper = documentHelper;
    }

    public Element createObjectElement() {
        return documentHelper.createElement(tagName);
    }


    public Element getMap(TestElement testElement) {

        HashMap<String, LinkedList<GateProperty>> propsMap = testElement.getPropsMap();
        Element mapRoot = documentHelper.createElement(Properties);

        for (Map.Entry<String, LinkedList<GateProperty>> entry : propsMap.entrySet()) {
            Element nameSpaceRoot = documentHelper.createElement(NAMESPACE);
            nameSpaceRoot.setAttribute(NAME, entry.getKey());
            for (GateProperty prop : entry.getValue()) {
                Element entryElement = documentHelper.createElement(PROPERTY);
                entryElement.setAttribute(NAME, prop.getName());
                entryElement.setAttribute(VALUE, prop.getStringValue());
                nameSpaceRoot.appendChild(entryElement);
            }
            mapRoot.appendChild(nameSpaceRoot);
        }

        return mapRoot;
    }

    public HashMap<String, HashMap<String, String>> getSavedMap(Element element) {
        HashMap<String, HashMap<String, String>> propsMap = new HashMap<>();
        Optional<Element> mapElementOptional = documentHelper.getChildrenElements(element).stream().
                filter(e -> e.getNodeName().equals(Properties)).findFirst();

        if (mapElementOptional.isPresent()) {
            LinkedList<Element> elements = documentHelper.getChildrenElements(mapElementOptional.get());
            for (Element nameSpaceElement : elements) {
                String nameSpace = nameSpaceElement.getAttribute(NAME);
                HashMap<String, String> props = new HashMap<>();
                LinkedList<Element> elementsOfNameSpace = documentHelper.getChildrenElements(nameSpaceElement);
                elementsOfNameSpace.forEach(e -> {
                    props.put(e.getAttribute(NAME), e.getAttribute(VALUE));
                });
                propsMap.put(nameSpace, props);
            }
        } else {
            log.fatal("Element is not an Properties: " + element.getNodeName());
        }
        return propsMap;
    }


    public Element marshalByDefault(TestElement testElement) {
        Element objectElement = createObjectElement();
        Element mapElement = getMap(testElement);
        objectElement.appendChild(mapElement);
        return objectElement;
    }


    public TestElement unmarshalByDefault(Element element) throws TestElementDecodeException {
        TestElement testElement = null;
        try {
            testElement = (TestElement) testElementClass.newInstance();
            HashMap<String, HashMap<String, String>> savedPropsMap = getSavedMap(element);

            for (String nameSpace : savedPropsMap.keySet()) {
                for (Map.Entry<String, String> prop : savedPropsMap.get(nameSpace).entrySet()) {
                    if (nameSpace.equals(TestElement.NS_ARGUMENT)) {
                        testElement.putProp(nameSpace, prop.getKey(), prop.getValue());
                    } else {
                        testElement.setProp(nameSpace, prop.getKey(), prop.getValue());
                    }

                }
            }
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            throw new TestElementDecodeException(e);
        }
        return testElement;
    }
}
