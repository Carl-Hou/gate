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
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;

public class Window extends AbstractSeleniumSampler {

    public Window(){
    }

    abstract class WindowMethod implements MethodSupplier{
        @Override
        public void addArgumentsToProps(){
        }

        @Override
        public void run(ElementResult result) {
            WebDriver driver = getDriver(result);

            if(result.isFailure()) return;
            action(driver, result);
        }
        abstract void action(WebDriver driver, ElementResult result);
    }

    class FullScreen extends WindowMethod {
        @Override
        void action(WebDriver driver, ElementResult result) {
            driver.manage().window().fullscreen();
        }
    }

    class GetPosition extends WindowMethod {
        @Override
        void action(WebDriver driver, ElementResult result) {
            Point point = driver.manage().window().getPosition();
            JSONObject returnValue = new JSONObject();
            returnValue.put("x", point.getX());
            returnValue.put("y", point.getY());
            result.setResponseObject(getJSONString(returnValue));
        }
    }

    class GetSize extends WindowMethod {
        @Override
        void action(WebDriver driver, ElementResult result) {
            Dimension dimension = driver.manage().window().getSize();
            JSONObject returnValue = new JSONObject();
            returnValue.put("height", dimension.getHeight());
            returnValue.put("width", dimension.getWidth());
            result.setResponseObject(getJSONString(returnValue));
        }
    }

    class Maximize extends WindowMethod {
        @Override
        void action(WebDriver driver, ElementResult result) {
            driver.manage().window().maximize();
        }
    }

    class SetPosition extends WindowMethod {
        @Override
        void action(WebDriver driver, ElementResult result) {
            int x = ParameterUtils.getInt(getRunTimeProp(NS_ARGUMENT, "Point_X"),result);
            int y = ParameterUtils.getInt(getRunTimeProp(NS_ARGUMENT, "Point_Y"),result);
            driver.manage().window().setPosition(new Point(x, y));
        }
    }

    class SetSize extends WindowMethod {
        @Override
        void action(WebDriver driver, ElementResult result) {
            int h = ParameterUtils.getInt(getRunTimeProp(NS_ARGUMENT, "Dimension_Height"),result);
            int w = ParameterUtils.getInt(getRunTimeProp(NS_ARGUMENT, "Dimension_Width"),result);
            driver.manage().window().setSize(new Dimension(w, h));
        }
    }

}
