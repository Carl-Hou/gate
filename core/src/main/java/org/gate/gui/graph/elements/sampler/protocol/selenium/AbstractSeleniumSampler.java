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
import net.minidev.json.JSONStyle;
import org.apache.commons.io.FileUtils;
import org.gate.common.config.GateProps;
import org.gate.common.util.GateException;
import org.gate.common.util.GateRuntimeExcepiton;
import org.gate.gui.common.TestElement;
import org.gate.gui.details.results.collector.ResultCollector;
import org.gate.gui.details.results.elements.graph.ElementResult;
import org.gate.gui.graph.elements.AbstractGraphElement;
import org.gate.gui.graph.elements.sampler.protocol.selenium.gui.DefaultSeleniumElementGui;
import org.gate.runtime.GateContext;
import org.gate.runtime.GateContextService;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import javax.swing.tree.TreeNode;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.text.SimpleDateFormat;
import java.util.*;

abstract public class AbstractSeleniumSampler extends AbstractGraphElement implements SeleniumElement, SeleniumConstantsInterface{

    public AbstractSeleniumSampler(){
        addNameSpace(NS_ARGUMENT);
        addProp(NS_DEFAULT, PN_DriverId, "");
        List<String> methodSuppliers = getMethodSuppliers();
        if(!methodSuppliers.isEmpty() ){
            String  defaultMethodSupplierName = methodSuppliers.get(0);
            addProp(NS_NAME, PN_MethodSuppliersName, defaultMethodSupplierName);
            getMethodSupplierInstance(defaultMethodSupplierName).addArgumentsToProps();
        }
    }

    @Override
    protected String getContextConfigKey(){
        return DefaultConfigName;
    }

    @Override
    protected LinkedList<String> getNameSpacesToApplyDefault(){
        LinkedList<String> nameSpaces = new LinkedList<String>();
        nameSpaces.add(NS_DEFAULT);
        nameSpaces.add(NS_ARGUMENT);
        return nameSpaces;
    }

    SeleniumContext getSeleniumContext(){
        GateContext context = GateContextService.getContext();
        SeleniumContext seleniumContext = (SeleniumContext) context.getGraphElementContext().get(Selenium);
        if(seleniumContext == null){
            seleniumContext = new SeleniumContext();
            context.getGraphElementContext().put(Selenium, seleniumContext);
        }
        return  seleniumContext;
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

    void putDriver(WebDriver driver){
        getSeleniumContext().putDriver(getRunTimeProp(NS_DEFAULT, PN_DriverId), driver);
    }

    String getJSONString(JSONObject jsonObject){
        return jsonObject.toJSONString(JSONStyle.MAX_COMPRESS);
    }

    interface MethodSupplier {
        void addArgumentsToProps();
        void run(ElementResult result);
    }

    List<Class> getSuppliersClasses(){
        LinkedList<Class> suppliers = new LinkedList<>();
        for(Class<?> clazz : getClass().getDeclaredClasses()) {
            if (!clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers())
                    && MethodSupplier.class.isAssignableFrom(clazz)) {
                suppliers.add(clazz);
            }
        }
        return suppliers;
    }

    public List<String> getMethodSuppliers() {
        LinkedList<String> suppliers = new LinkedList<>();
        getSuppliersClasses().forEach(clazz -> {
            suppliers.add(clazz.getSimpleName());
        });
        Collections.sort(suppliers);
        return suppliers;
    }

    MethodSupplier getMethodSupplierInstance(String name) throws GateRuntimeExcepiton {
        Optional<Class> executorClazzOptional = getSuppliersClasses().stream().filter(clazz ->
                clazz.getSimpleName().equals(name)).findFirst();
        try {
            return (MethodSupplier) executorClazzOptional.get().getDeclaredConstructor(getClass()).newInstance(this);
        } catch (Exception e) {
            log.fatal("fatal error occur when WaitCondition", e);
            throw new GateRuntimeExcepiton(e);
        }
    }

    public void taskScreenShot(ElementResult result) {

        WebDriver driver = getDriver(result);
        if(driver == null){
            return;
        }
        // valid screen shot root location
        File screenShotLocation = new File(GateProps.getProperty(ScreenShotLocationPropName), GateProps.getGateHome() + "/screenshots");
        if(!screenShotLocation.canWrite() || !screenShotLocation.isDirectory()){
            result.setThrowable(new GateException("Gate property " + ScreenShotLocationPropName + " set to "
                    + screenShotLocation.getAbsolutePath() + "is not exit nor a directory"));
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
    public String getCurrentMethodSupplier(){
        return getProp(TestElement.NS_NAME, PN_MethodSuppliersName).getStringValue();
    }

    @Override
    public void updateByMethodSupplier(String supplierName) {
        getProp(NS_NAME, PN_MethodSuppliersName).setObjectValue(supplierName);
        clearNameSpace(NS_ARGUMENT);
        getMethodSupplierInstance(supplierName).addArgumentsToProps();
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
