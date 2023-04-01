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

import org.gate.common.util.GateRuntimeExcepiton;
import org.gate.gui.common.TestElement;
import org.gate.gui.details.results.elements.graph.ElementResult;
import org.gate.gui.graph.elements.AbstractGraphElement;
import org.gate.gui.graph.elements.sampler.protocol.selenium.util.SeleniumConstantsInterface;
import org.gate.gui.graph.elements.sampler.protocol.selenium.util.SeleniumElementInterface;
import org.gate.runtime.GateContext;
import org.gate.runtime.GateContextService;
import java.lang.reflect.Modifier;
import java.util.*;

abstract public class AbstractSeleniumGraphElement extends AbstractGraphElement implements SeleniumElementInterface, SeleniumConstantsInterface {

    public AbstractSeleniumGraphElement(){
        addNameSpace(NS_ARGUMENT);
        List<String> methodSuppliers = getMethodSuppliers();
        if(!methodSuppliers.isEmpty() ){
            String  defaultMethodSupplierName = methodSuppliers.get(0);
            addProp(NS_NAME, PN_MethodSuppliersName, defaultMethodSupplierName);
            getMethodSupplierInstance(defaultMethodSupplierName).addArguments();
        }
    }

    @Override
    protected String getContextConfigKey(){
        return DefaultConfigName;
    }

    @Override
    protected LinkedList<String> getNameSpacesToApplyDefault(){
        LinkedList<String> nameSpaces = new LinkedList();
        nameSpaces.add(NS_DEFAULT);
        nameSpaces.add(NS_ARGUMENT);
        return nameSpaces;
    }

    protected SeleniumContext getSeleniumContext(){
        GateContext context = GateContextService.getContext();
        SeleniumContext seleniumContext = (SeleniumContext) context.getGraphElementContext().get(Selenium);
        if(seleniumContext == null){
            seleniumContext = new SeleniumContext();
            context.getGraphElementContext().put(Selenium, seleniumContext);
        }
        return  seleniumContext;
    }

    protected interface MethodSupplier {
        void addArguments();
        void run(ElementResult result);
    }

    protected abstract class AbstractMethodSupplier implements MethodSupplier{
        @Override
        public void addArguments() {}

        protected void addArg(String name, String value){
            addProp(NS_ARGUMENT, name, value);
        }

        protected String getRTArg(String name){
            return getRunTimeProp(NS_ARGUMENT, name);
        }

        protected void setGateVariable(String name, int value){
            setGateVariable(name, String.valueOf(value));
        }

        protected void setGateVariable(String name, boolean value){
            setGateVariable(name, String.valueOf(value));
        }

        protected void setGateVariable(String name, String value){
            GateContextService.getContext().getVariables().put(getRTArg(name), value);
        }
    }

    protected List<Class> getSuppliersClasses(){
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


    protected MethodSupplier getMethodSupplierInstance(String name) throws GateRuntimeExcepiton {
        Optional<Class> executorClazzOptional = getSuppliersClasses().stream().filter(clazz ->
                clazz.getSimpleName().equals(name)).findFirst();
        try {
            return (MethodSupplier) executorClazzOptional.get().getDeclaredConstructor(getClass()).newInstance(this);
        } catch (Exception e) {
            log.fatal("fatal internal error occur", e);
            throw new GateRuntimeExcepiton(e);
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
        getMethodSupplierInstance(supplierName).addArguments();
    }

}
