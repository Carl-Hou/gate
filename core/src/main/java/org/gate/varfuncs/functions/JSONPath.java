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

package org.gate.varfuncs.functions;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import net.minidev.json.JSONStyle;
import net.minidev.json.JSONValue;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xpath.XPathAPI;
import org.apache.xpath.objects.XObject;
import org.gate.common.util.GateXMLUtils;
import org.gate.varfuncs.CompoundVariable;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

// @see org.apache.jmeter.functions.PackageTest for unit tests

/**
 * The function represented by this class allows data to be read from XML files.
 * Syntax is similar to the CVSRead function. The function allows the test to
 * line-thru the nodes in the XML file - one node per each test. E.g. inserting
 * the following in the test scripts :
 * <p>
 * ${_XPath(c:/BOF/abcd.xml,/xpath/)} // match the (first) node
 * ${_XPath(c:/BOF/abcd.xml,/xpath/)} // Go to next match of '/xpath/' expression
 * <p>
 * NOTE: A single instance of each different file/expression combination
 * is opened and used for all threads.
 *
 * @since 2.0.3
 */
public class JSONPath extends AbstractFunction {
    private static final Logger log = LogManager.getLogger();

    private static final String KEY = "__JSONPath"; // Function name //$NON-NLS-1$

    private static final List<String> desc = new LinkedList<>();

    private Object[] values; // Parameter list

    static {
        desc.add("JSON file to get values from"); //$NON-NLS-1$
        desc.add("JSONPath expression to match against"); //$NON-NLS-1$
    }

    private static final Configuration DEFAULT_CONFIGURATION =
            Configuration.defaultConfiguration().addOptions(Option.ALWAYS_RETURN_LIST);

    public JSONPath() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized String executeRecursion()
            throws InvalidVariableException {
        String myValue = ""; //$NON-NLS-1$

        String fileName = ((CompoundVariable) values[0]).execute();
        String jsonPathString = ((CompoundVariable) values[1]).execute();

        if (log.isDebugEnabled()) {
            log.debug("execute (" + fileName + " " + jsonPathString + ")   ");
        }

        try {
            File file = new File(fileName);
            if (file.exists() && file.canRead()) {

                List<Object> extractedObjects = JsonPath.compile(jsonPathString).read(
                        FileUtils.readFileToString(new File(fileName), "UTF-8"), DEFAULT_CONFIGURATION);
                if (!extractedObjects.isEmpty()) {
                    myValue = JSONValue.toJSONString(extractedObjects.get(0), JSONStyle.LT_COMPRESS);
                }
            } else {
                log.warn("Could not read open: {} ", fileName);
            }
        } catch (IOException e) {
            log.warn("Could not read file: {} {}", fileName, e.getMessage(), e);
        }

        if (log.isDebugEnabled()) {
            log.debug("execute value: " + myValue);
        }

        return myValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getArgumentDesc() {
        return desc;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getReferenceKey() {
        return KEY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void setParameters(Collection<CompoundVariable> parameters) throws InvalidVariableException {
        log.debug("setParameter - Collection.size=" + parameters.size());

        values = parameters.toArray();

        if (log.isDebugEnabled()) {
            for (int i = 0; i < parameters.size(); i++) {
                log.debug("i:" + ((CompoundVariable) values[i]).execute());
            }
        }
        checkParameterCount(parameters, 2);
    }
}
