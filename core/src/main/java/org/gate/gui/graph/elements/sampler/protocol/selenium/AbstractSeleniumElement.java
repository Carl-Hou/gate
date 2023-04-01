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

package org.gate.gui.graph.elements.sampler.protocol.selenium;

import org.gate.gui.details.results.elements.graph.ElementResult;
import org.gate.gui.graph.elements.sampler.protocol.selenium.gui.SeleniumElementGui;
import org.gate.gui.graph.elements.sampler.protocol.selenium.util.SeleniumConstantsInterface;
import org.gate.gui.graph.elements.sampler.protocol.selenium.util.SeleniumUtils;
import org.gate.runtime.GateContextService;
import org.gate.varfuncs.property.GateProperty;
import org.openqa.selenium.*;

public abstract class AbstractSeleniumElement extends AbstractSeleniumSampler implements SeleniumConstantsInterface {

    public AbstractSeleniumElement(){
        addProp(NS_NAME, ElementInputType, ElementInputType_Locator);
        addProp(NS_DEFAULT, PN_LocatorType, LocatorTypes[0]);
        addProp(NS_DEFAULT, PN_LocatorCondition, "");
    }

    @Override
    public String getGUI() {
        return SeleniumElementGui.class.getName();
    }

    abstract class AbstractSeleniumElementMethod extends AbstractMethodSupplier {
        WebDriver driver;
        @Override
        public void run(ElementResult result) {
            GateProperty elementInputType= getProp(NS_NAME, ElementInputType);
            driver = getDriver(result);
            if(result.isFailure()) return;

            WebElement element = null;
            if(null != elementInputType && elementInputType.getStringValue().equals(ElementInputType_Variable)){
                element = (WebElement) GateContextService.getContext()
                        .getVariables().getObject(getRunTimeProp(NS_DEFAULT, PN_VariableName));
            }else if(null != elementInputType && elementInputType.getStringValue().equals(ElementInputType_Locator)){
                By locator = SeleniumUtils.getLocator(getRunTimeProp(NS_DEFAULT, PN_LocatorType),
                        getRunTimeProp(NS_DEFAULT, PN_LocatorCondition));
                element = driver.findElement(locator);
            }else if(null != elementInputType && elementInputType.getStringValue().equals(ElementInputType_Driver)){
                // do nothing curre n   ntly
            }

            action(driver, element, result);
        }
        abstract void action(WebDriver driver, WebElement element, ElementResult result);

    }

}
