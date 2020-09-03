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

    @Override
    public String getStaticLabel() {
        return "Selenium Window";
    }

    abstract class WindowMethod extends AbstractMethodSupplier{
        @Override
        public void addArguments(){
        }
        void addArg(String name, String value){
            addProp(NS_ARGUMENT, name, value);
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
        final static String VN_X = "variable_name_x";
        final static String VN_Y = "variable_name_y";
        @Override
        public void addArguments(){
            addArg(VN_X, "position_x");
            addArg(VN_Y, "position_y");
        }
        @Override
        void action(WebDriver driver, ElementResult result) {

            Point point = driver.manage().window().getPosition();
            setGateVariable(VN_X, point.getX());
            setGateVariable(VN_Y, point.getY());
        }
    }

    class GetSize extends WindowMethod {
        final static String VN_Width = "variable_name_width";
        final static String VN_Height = "variable_name_height";
        @Override
        public void addArguments(){
            addArg(VN_Width, "size_width");
            addArg(VN_Height, "size_height");
        }
        @Override
        void action(WebDriver driver, ElementResult result) {
            Dimension dimension = driver.manage().window().getSize();
            setGateVariable(VN_Width, dimension.getWidth());
            setGateVariable(VN_Height, dimension.getHeight());
        }
    }

    class Maximize extends WindowMethod {
        @Override
        void action(WebDriver driver, ElementResult result) {
            driver.manage().window().maximize();
        }
    }

    class SetPosition extends WindowMethod {
        final static String VN_X = "point_x";
        final static String VN_Y = "point_y";
        @Override
        public void addArguments(){
            addArg(VN_X, "0");
            addArg(VN_Y, "0");
        }
        @Override
        void action(WebDriver driver, ElementResult result) {
            int x = ParameterUtils.getInt(getRTArg(VN_X),result);
            int y = ParameterUtils.getInt(getRTArg(VN_Y),result);
            driver.manage().window().setPosition(new Point(x, y));
        }
    }

    class SetSize extends WindowMethod {
        final static String VN_Width = "width";
        final static String VN_Height = "height";
        @Override
        public void addArguments(){
            addArg(VN_Width, "");
            addArg(VN_Height, "");
        }
        @Override
        void action(WebDriver driver, ElementResult result) {
            int h = ParameterUtils.getInt(getRTArg(VN_Height),result);
            int w = ParameterUtils.getInt(getRTArg(VN_Width),result);
            driver.manage().window().setSize(new Dimension(w, h));
        }
    }

}
