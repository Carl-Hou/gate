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

package org.gate.saveload.convert.tree;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gate.gui.common.TestElement;
import org.gate.gui.tree.GateTreeElement;

import org.gate.gui.tree.action.elements.Action;
import org.gate.gui.tree.action.elements.ActionSuites;
import org.gate.gui.tree.action.elements.ActionSuite;
import org.gate.gui.tree.test.elements.TestCase;
import org.gate.gui.tree.test.elements.TestSuites;
import org.gate.gui.tree.test.elements.TestSuite;

import org.gate.gui.tree.test.elements.config.HTTPHeaderManager;
import org.gate.gui.tree.test.elements.config.HTTPRequestDefaults;
import org.gate.gui.tree.test.elements.config.SeleniumDefaults;
import org.gate.gui.tree.test.elements.config.UserDefineVariables;
import org.gate.gui.tree.test.elements.dataprovider.CSVDataProvider;
import org.gate.gui.tree.test.elements.fixture.TearDown;
import org.gate.gui.tree.test.elements.fixture.SetUp;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Optional;

public class TreeElementConverterRegistry {
    private final static Logger log = LogManager.getLogger();
    private static TreeElementConverterRegistry ourInstance = new TreeElementConverterRegistry();
    private ArrayList<TreeElementConverter> treeElementConverters = new ArrayList<>();
    public static TreeElementConverterRegistry getInstance() {
        return ourInstance;
    }

    private TreeElementConverterRegistry() {

        treeElementConverters.add(new DefaultTreeElementConverter(TestSuites.class));
        treeElementConverters.add(new DefaultTreeElementConverter(TestSuite.class));

        treeElementConverters.add(new DefaultTreeElementConverter(ActionSuites.class));
        treeElementConverters.add(new DefaultTreeElementConverter(ActionSuite.class));

        treeElementConverters.add(new DefaultTreeElementConverter(UserDefineVariables.class));
        treeElementConverters.add(new DefaultTreeElementConverter(SeleniumDefaults.class));
        treeElementConverters.add(new DefaultTreeElementConverter(HTTPRequestDefaults.class));
        treeElementConverters.add(new DefaultTreeElementConverter(HTTPHeaderManager.class));
        treeElementConverters.add(new DefaultTreeElementConverter(CSVDataProvider.class));

        treeElementConverters.add(new ModelContainerConverter(TestCase.class));
        treeElementConverters.add(new ModelContainerConverter(SetUp.class));
        treeElementConverters.add(new ModelContainerConverter(TearDown.class));
        treeElementConverters.add(new TestActionConverter(Action.class));

        // super class must add after the sub class if sub class has its special covert actions
        treeElementConverters.add(new DefaultTreeElementConverter(GateTreeElement.class));
    }

    public TreeElementConverter getConverter(TestElement testElement){
        return treeElementConverters.stream().filter(c -> c.is(testElement)).findFirst().get();
    }

    public Optional<TreeElementConverter> getConverter(String tagName){
        return treeElementConverters.stream().filter(c -> c.is(tagName)).findFirst();
    }
    public Optional<TreeElementConverter> getConverter(Element element){
        return getConverter(element.getTagName());
    }
}
