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
package org.gate.gui.graph.elements.sampler.protocol.selenium.gui;


import org.gate.gui.common.GuiUtils;
import org.gate.gui.common.TestElement;
import org.gate.gui.graph.common.AbstractJComboBoxListener;
import org.gate.gui.graph.elements.sampler.protocol.selenium.util.SeleniumElementInterface;

import javax.swing.*;
import java.awt.*;

public class SeleniumInteractionGui extends AbstractSeleniumElementGui{

    JComboBox  categoryComboBox = new JComboBox(Interaction_Categories);
    JPanel operationCategoryPane = GuiUtils.getPanel("Operation Category", new GridLayout(1, 1));

    public SeleniumInteractionGui(){
        operationCategoryPane.add(categoryComboBox);
        topPanel.add(operationCategoryPane);
        categoryComboBox.addPopupMenuListener(new AbstractJComboBoxListener() {
            @Override
            protected void onItemSelected(String itemName) {
                seleniumElement.setProp(TestElement.NS_NAME, Interaction_Category, itemName);
                methodSuppliesComboBox.removeAllItems();
                seleniumElement.getMethodSuppliers().forEach(m -> {
                    methodSuppliesComboBox.addItem(m);
                });
            }
        });
    }

    protected void beforeSetTestElement(SeleniumElementInterface seleniumElement){
        String interactionCategory =
                seleniumElement.getProp(TestElement.NS_NAME, Interaction_Category).getStringValue();
        categoryComboBox.setSelectedItem(interactionCategory);
    }

}
