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
package org.gate.gui.graph.elements.config;

import org.gate.gui.details.results.elements.graph.ElementResult;
import org.gate.gui.graph.elements.sampler.protocol.selenium.AbstractSeleniumGraphElement;
import org.gate.gui.graph.elements.sampler.protocol.selenium.SeleniumContext;
import org.gate.gui.graph.elements.sampler.protocol.selenium.util.SeleniumUtils;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;


public class SeleniumDriverConfig extends AbstractSeleniumGraphElement implements Config{

    public SeleniumDriverConfig(){
        addProp(NS_DEFAULT, PN_DriverURL, "");
        addProp(NS_DEFAULT, PN_ImplicitlyWaitTimeoutSeconds, "");
        addProp(NS_DEFAULT, PN_PageLoadTimeoutSeconds, "");
        addProp(NS_DEFAULT, PN_SetScriptTimeoutSeconds, "");
    }

    @Override
    protected void exec(ElementResult result) {
        result.setRunTimeProps(getRunTimePropsMap());

        try {
            getMethodSupplierInstance(getCurrentMethodSupplier()).run(result);
        }catch(Throwable t){
            log.error("Fail to execute method:", t);
            result.setThrowable(t);
        }
    }

    @Override
    public String getStaticLabel() {
        return "Selenium Driver";
    }

    @Override
    public String getGUI() {
        return SeleniumDriverConfigGui.class.getName();
    }


    abstract class AbstractDriverCreator extends AbstractMethodSupplier{
        @Override
        public void addArguments() {
            addArg(PN_DriverId, "");
            addCreatorArgs();
        }

        abstract void addCreatorArgs();

        @Override
        public void run(ElementResult result) {

            WebDriver driver = getSeleniumContext().getDriver(getRTArg(PN_DriverId));
            if(driver != null){
                result.appendMessage("Apply settings to exist driver");
                return;
            }else{
                driver = createDriver();
                SeleniumContext seleniumContext = getSeleniumContext();
                String driverId = getRTArg(PN_DriverId);
                seleniumContext.putDriver(driverId, driver);
                result.appendMessage("Create new driver successfully");
            }

            String timeoutSeconds = getRunTimeProp(NS_DEFAULT, PN_ImplicitlyWaitTimeoutSeconds).trim();
            if(!timeoutSeconds.isEmpty()){
                int timeout = Integer.parseInt(timeoutSeconds);
                driver.manage().timeouts().implicitlyWait(timeout, TimeUnit.SECONDS) ;
            }

            timeoutSeconds = getRunTimeProp(NS_DEFAULT, PN_PageLoadTimeoutSeconds).trim();
            if(!timeoutSeconds.isEmpty()){
                int timeout = Integer.parseInt(timeoutSeconds);
                driver.manage().timeouts().pageLoadTimeout(timeout, TimeUnit.SECONDS) ;
            }

            timeoutSeconds = getRunTimeProp(NS_DEFAULT, PN_SetScriptTimeoutSeconds).trim();
            if(!timeoutSeconds.isEmpty()){
                int timeout = Integer.parseInt(timeoutSeconds);
                driver.manage().timeouts().setScriptTimeout(timeout, TimeUnit.SECONDS) ;
            }

            String urlToGet = getRunTimeProp(NS_DEFAULT, PN_DriverURL).trim();
            driver.get(urlToGet);

        }

        abstract WebDriver createDriver();
    }

    public class Create extends AbstractDriverCreator {

        @Override
        void addCreatorArgs() {
            addArg(PN_BrowserName, BrowserName_Chrome);
        }

        @Override
        WebDriver createDriver() {
            String browserName = getRTArg(PN_BrowserName);
            return SeleniumUtils.getDriver(browserName);
        }

    }

    public class CreateRemote extends AbstractDriverCreator{

        @Override
        void addCreatorArgs() {
            addArg(PN_GridHubUrl, "");
            addArg(PN_BrowserName, "");
            addArg(PN_Platform, "");
            addArg(PN_BrowserVersion, "");
            addArg(PN_JavascriptEnabled, "true");
        }

        @Override
        WebDriver createDriver() {
            URL gridHubUrl = null;
            try {
                gridHubUrl = new URL(getRunTimeProp(NS_ARGUMENT, PN_GridHubUrl));
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException(e);
            }
            String browserName = getRunTimeProp(NS_ARGUMENT, PN_BrowserName);
            DesiredCapabilities capability = SeleniumUtils.getDesiredCapabilities(browserName);
            capability.setPlatform(Platform.fromString(getRunTimeProp(NS_ARGUMENT, PN_Platform)));
            capability.setVersion(getRunTimeProp(NS_ARGUMENT, PN_BrowserVersion));
            String javascriptEnabled = getRunTimeProp(NS_ARGUMENT, PN_JavascriptEnabled);
            if(!javascriptEnabled.isEmpty()){
                capability.setJavascriptEnabled(Boolean.parseBoolean(javascriptEnabled));
            }

            return new RemoteWebDriver(gridHubUrl, capability);
        }
    }
}