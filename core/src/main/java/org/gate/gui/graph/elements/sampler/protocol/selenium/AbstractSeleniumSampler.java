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

import org.apache.commons.io.FileUtils;
import org.gate.common.config.GateProps;
import org.gate.common.util.GateException;
import org.gate.gui.details.results.collector.ResultCollector;
import org.gate.gui.details.results.elements.graph.ElementResult;
import org.gate.gui.graph.elements.sampler.Sampler;
import org.gate.gui.graph.elements.sampler.protocol.selenium.gui.DefaultSeleniumElementGui;
import org.gate.gui.graph.elements.sampler.protocol.selenium.util.SeleniumConstantsInterface;
import org.gate.gui.graph.elements.sampler.protocol.selenium.util.SeleniumElementInterface;
import org.gate.runtime.GateContextService;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import javax.swing.tree.TreeNode;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

abstract public class AbstractSeleniumSampler extends AbstractSeleniumGraphElement
        implements SeleniumElementInterface, SeleniumConstantsInterface, Sampler {

    public AbstractSeleniumSampler(){
        addProp(NS_DEFAULT, PN_DriverId, "");
    }

    WebDriver getDriver(){
        String driverId = getRunTimeProp(NS_DEFAULT, PN_DriverId);
        return getSeleniumContext().getDriver(driverId);
    }

    /*
    * Driver should not be null in most case. add exception to result to make the test fail on driver not found
    * use getDriver() to avoid test fail in case driver allow to be null
    * */
    WebDriver getDriver(ElementResult result){
        WebDriver driver = getDriver();
        if(driver == null) {
            result.setThrowable(new GateException("Driver not found by id: ".concat(getRunTimeProp(NS_DEFAULT, PN_DriverId))));
        }
        return driver;
    }

    public void taskScreenShot(ElementResult result) {

        WebDriver driver = getDriver(result);
        if(driver == null){
            return;
        }
        // valid screen shot root location
        File screenShotLocation = new File(GateProps.getProperty(ScreenShotLocationPropName, GateProps.getGateHome() + GateProps.FileSeparator + "screenshots"));

        if(!screenShotLocation.canWrite() || !screenShotLocation.isDirectory()){
            result.setThrowable(new GateException("Gate property " + ScreenShotLocationPropName + " set to "
                    + screenShotLocation.getAbsolutePath() + " is not exit nor a directory"));
            return;
        }
        // create screen shot file
        SimpleDateFormat df = new SimpleDateFormat("_yyyy-MM-dd_HH-mm-ss-SS" );
        String screenShotName = getName() + df.format(new Date()) + ".png";
        ResultCollector resultCollector = GateContextService.getContext().getResultCollector();
        StringBuilder sb = new StringBuilder();
        // skip first 2
        TreeNode[] path = resultCollector.getResultNode().getPath();
        for(int i =2; i< path.length ;i ++){
            sb.append(GateProps.FileSeparator).append(path[i].toString());
        }

        sb.append(df.format(new Date())).append(".png");
        sb.trimToSize();
        File screenFileOnStorage = new File(screenShotLocation, sb.toString());

        screenFileOnStorage.getParentFile().mkdirs();

        File screenFileInMemory = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        try {
            FileUtils.copyFile(screenFileInMemory, screenFileOnStorage);
        } catch (IOException e) {
            result.setThrowable(e);
        }
    }

    @Override
    protected void exec(ElementResult result) {

        result.setRunTimeProps(getRunTimePropsMap());

        try {
            getMethodSupplierInstance(getCurrentMethodSupplier()).run(result);
        }catch(Throwable t){
            log.error("Fail to execute selenium method:", t);
            result.setThrowable(t);
        }

        switch (GateProps.getProperty(ScreenShotConditionPropName)) {
            case ScreenShotConditionNever:
                result.appendMessage("Screen shot condition is: " + ScreenShotConditionNever);
                break;
            case ScreenShotConditionAlways:
                result.appendMessage("Screen shot condition is: " + ScreenShotConditionAlways);
                taskScreenShot(result);
                break;
            case ScreenShotConditionFail:
                result.appendMessage("Screen shot condition is: " + ScreenShotConditionFail);
                if (result.isFailure()) {
                    taskScreenShot(result);
                }
                break;
            default:
                result.setThrowable(new GateException("Setting of Gate property " + ScreenShotConditionPropName
                        + " is not supported. Check setting on gate.properties "));
        }

        return;
    }

    @Override
    public String getGUI() {
        return DefaultSeleniumElementGui.class.getName();
    }
}
