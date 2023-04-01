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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gate.common.config.GateProps;
import org.gate.gui.graph.elements.ElementContext;
import org.openqa.selenium.WebDriver;

import java.util.HashMap;
import java.util.LinkedList;

// Selenium context for model execute
public class SeleniumContext implements ElementContext {

    Logger log = LogManager.getLogger(this.getClass());
    boolean closeBrowsers = GateProps.getProperty("gate.selenium.context.browsers.close", true);
    HashMap<String, WebDriver> drivers = new HashMap<>();
    LinkedList<String> driverIDRequireToClose = new LinkedList<>();

    public void putDriver(String name, WebDriver dirver){
        if(drivers.containsKey(name)){
            log.warn("Driver id already exist. do nothing");
        }
        drivers.put(name, dirver);
    }

    public WebDriver getDriver(String name){
        return drivers.get(name);
    }

    // set driver to close after test
    public void closeBrowserAfterTest(String driverID, boolean isClose){
        if(isClose){
            driverIDRequireToClose.add(driverID);
        }else{
            driverIDRequireToClose.remove(driverID);
        }
    }

    @Override
    public void close() {
        driverIDRequireToClose.forEach( driverId ->{
            WebDriver driver = drivers.get(driverId);
            if(null != driver){
                driver.quit();
            }
        });
        if(closeBrowsers){
            drivers.values().forEach( driver ->{
                driver.quit();
            });
        }
    }

}
