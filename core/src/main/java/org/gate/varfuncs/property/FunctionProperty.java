/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 */

package org.gate.varfuncs.property;

import org.gate.runtime.GateContext;
import org.gate.runtime.GateContextService;
import org.gate.varfuncs.CompoundVariable;

/**
 * Class that implements the Function property
 */
public class FunctionProperty extends AbstractProperty {
    private static final long serialVersionUID = 233L;

    private transient CompoundVariable function;

    private int testIteration = -1;

    private String cacheValue;

    public FunctionProperty(String name, CompoundVariable func) {
        super(name);
        function = func;
    }

    public FunctionProperty() {
        super();
    }

    @Override
    public void setObjectValue(Object v) {
        if (v instanceof CompoundVariable && !isRunningVersion()) {
            function = (CompoundVariable) v;
        } else {
            cacheValue = v.toString();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof FunctionProperty) {
            if (function != null) {
                return function.equals(((GateProperty) o).getObjectValue());
            }
        }
        return false;
    }

    @Override
    public int hashCode(){
        int hash = super.hashCode();
        if (function != null) {
            hash = hash*37 + function.hashCode();
        }
        return hash;
    }

    /**
     * Executes the function (and caches the value for the duration of the tree
     * iteration) if the property is a running version. Otherwise, the raw
     * string representation of the function is provided.
     *
     * @see GateProperty#getStringValue()
     */
    @Override
    public String getStringValue() {
        GateContext ctx = GateContextService.getContext();
        if (!isRunningVersion() ) {
            log.debug("Not running version, return raw function string");
            return function.getRawParameters();            
        }
        return function.execute();
    }

    /**
     * @see GateProperty#getObjectValue()
     */
    @Override
    public Object getObjectValue() {
        return function;
    }

    @Override
    public FunctionProperty clone() {
        FunctionProperty prop = (FunctionProperty) super.clone();
        prop.cacheValue = cacheValue;
        prop.testIteration = testIteration;
        prop.function = function;
        return prop;
    }


}
