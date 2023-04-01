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
package org.gate.gui.graph.elements.extractor.gui;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gate.gui.common.TestElement;
import org.gate.gui.details.properties.graph.GraphElementPropertiesGui;
import org.gate.gui.graph.elements.extractor.AbstractExtractor;
import org.gate.gui.graph.elements.extractor.ExtractorConstantsInterface;

import javax.swing.*;

public class TextExtractorGui extends DefaultExtractorGui implements GraphElementPropertiesGui, ExtractorConstantsInterface {

    protected Logger log = LogManager.getLogger(this.getName());

    void onInputSourceSelected(AbstractExtractor extractor){
        extractor.putProp(TestElement.NS_DEFAULT, TEXT_Extractor_Template, "");
        extractor.putProp(TestElement.NS_DEFAULT, Extractor_Type, TEXT_ExtractorType_Regex);
    }

    void updateTableEditors(){
        defaultPropertiesTable.constraintReadOnly(Extractor_Type);
    }


}
