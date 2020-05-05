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

import net.minidev.json.JSONObject;
import org.gate.gui.details.results.elements.graph.ElementResult;
import org.gate.gui.graph.common.ParameterUtils;
import org.gate.gui.graph.elements.sampler.protocol.selenium.util.SeleniumConstantsInterface;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.opera.OperaDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

public class Driver extends AbstractSeleniumSampler implements SeleniumConstantsInterface {

    public Driver(){
        // keep this for new instance on save/restore
    }

    @Override
    public String getStaticLabel() {
        return "Selenium Driver";
    }

    class Create implements MethodSupplier {
        @Override
        public void addArguments() {
            addProp(NS_ARGUMENT, PN_BrowserName, "");
        }

        @Override
        public void run(ElementResult result) {
            String browserName = getRunTimeProp(NS_ARGUMENT, PN_BrowserName);
            WebDriver driver = null;
            switch (browserName) {
                case BrowserName_Chrome:
                    driver =  new ChromeDriver();
                    break;
                case BrowserName_Safari:
                    driver = new SafariDriver();
                    break;
                case BrowserName_FireFox:
                    driver = new FirefoxDriver();
                    break;
                case BrowserName_Edge:
                    driver = new EdgeDriver();
                    break;
                case BrowserName_Opera:
                    driver = new OperaDriver();
                    break;
                default:
                    result.setFailure("Browser name " + browserName + " is not support."
                            + "Support Browser names are: " + Arrays.toString(BrowserNames));
                    return;
            }
            putDriver(driver);
            result.appendMessage("Create new driver successfully");
        }
    }

    class CreateRemote implements MethodSupplier {
        @Override
        public void addArguments() {
            addProp(NS_ARGUMENT, PN_GridHubUrl, "");
            addProp(NS_ARGUMENT, PN_BrowserName, "");
            addProp(NS_ARGUMENT, PN_Platform, "");
            addProp(NS_ARGUMENT, PN_BrowserVersion, "");
            addProp(NS_ARGUMENT, PN_JavascriptEnabled, "true");
        }

        @Override
        public void run(ElementResult result) {

            URL gridHubUrl = null;
            try {
                gridHubUrl = new URL(getRunTimeProp(NS_ARGUMENT, PN_GridHubUrl));
            } catch (MalformedURLException e) {
                result.setThrowable(e);
                return;
            }
            String browserName = getRunTimeProp(NS_ARGUMENT, PN_BrowserName);
            DesiredCapabilities capability = null;
            switch (browserName) {
                case BrowserName_Chrome:
                    capability =  DesiredCapabilities.chrome();
                    break;
                case BrowserName_Safari:
                    capability =  DesiredCapabilities.safari();
                    break;
                case BrowserName_FireFox:
                    capability =  DesiredCapabilities.firefox();
                    break;
                case BrowserName_Edge:
                    capability =  DesiredCapabilities.edge();
                    break;
                case BrowserName_Opera:
                    capability =  DesiredCapabilities.operaBlink();
                    break;
                default:
                    result.setFailure("Browser name " + browserName + " is not support."
                            + "Support Browser names are: " + Arrays.toString(BrowserNames));
                    return;
            }

            capability.setPlatform(Platform.fromString(getRunTimeProp(NS_ARGUMENT, PN_Platform)));
            capability.setVersion(getRunTimeProp(NS_ARGUMENT, PN_BrowserVersion));
            String javascriptEnabled = getRunTimeProp(NS_ARGUMENT, PN_JavascriptEnabled);
            if(!javascriptEnabled.isEmpty()){
                capability.setJavascriptEnabled(Boolean.parseBoolean(javascriptEnabled));
            }

            WebDriver driver = new RemoteWebDriver(gridHubUrl, capability);

            putDriver(driver);
            result.appendMessage("Create new remote driver successfully");
        }
    }

    abstract class AbstractDriverMethod extends AbstractMethodSupplier{
        @Override
        public void run(ElementResult result) {
            WebDriver driver = getDriver(result);
            if(result.isFailure()){
                return;
            }
            exec(driver, result);
        }

        abstract void exec(WebDriver driver, ElementResult result);
    }

    class Get extends AbstractDriverMethod {
        @Override
        public void addArguments() {
            addArg(PN_URL, "");
        }

        @Override
        void exec(WebDriver driver, ElementResult result) {
            driver.get(getRTArg(PN_URL));
        }
    }

    class SwitchTo extends AbstractDriverMethod {
        @Override
        public void addArguments() {
            addArg(PN_URL, "");
        }

        @Override
        void exec(WebDriver driver, ElementResult result) {
            driver.get(getRTArg(PN_URL));
        }
    }

    class Close extends AbstractDriverMethod {
        @Override
        void exec(WebDriver driver, ElementResult result) {
            driver.close();
        }
    }

    class Quit extends AbstractDriverMethod {
        @Override
        void exec(WebDriver driver, ElementResult result) {
            driver.quit();
        }
    }

    class GetCurrentUrl extends AbstractDriverMethod {
        final static String VN_CurrentUrl = "variable_name_current_url";
        @Override
        public void addArguments(){
            addArg(VN_CurrentUrl, "currentUrl");
        }
        @Override
        void exec(WebDriver driver, ElementResult result) {
            String currentURL = driver.getCurrentUrl();
            if(null == currentURL){
                result.setFailure("Current url is empty:");
                return;
            }
            setGateVariable(VN_CurrentUrl, currentURL);
        }
    }

    class GetPageSource extends AbstractDriverMethod {
        final static String VN_PageSource = "variable_name_page_source";

        @Override
        void exec(WebDriver driver, ElementResult result) {
            String pageSource = driver.getPageSource();
            result.setResponseObject(pageSource);

        }
    }

    class GetTitle extends AbstractDriverMethod {
        final static String VN_Title = "variable_name_title";
        @Override
        public void addArguments(){
            addArg(VN_Title, "title");
        }

        @Override
        void exec(WebDriver driver, ElementResult result) {
            String title = driver.getTitle();
            setGateVariable(VN_Title,title);
        }
    }

    class GetWindowHandle extends AbstractDriverMethod {
        final static String VN_WindowsHandle = "variable_name_windows_handle";
        @Override
        public void addArguments(){
            addArg(VN_WindowsHandle, "windows_handle");
        }
        @Override
        void exec(WebDriver driver, ElementResult result) {
            String windowHandle = driver.getWindowHandle();
            setGateVariable(VN_WindowsHandle, windowHandle);
        }
    }

}
