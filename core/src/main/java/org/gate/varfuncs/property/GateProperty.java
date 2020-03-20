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

package org.gate.varfuncs.property;

import java.io.Serializable;


public interface GateProperty extends Serializable, Cloneable, Comparable<GateProperty> {
    /**
     * Returns whether the property is a running version.
     *
     * @return flag whether this property is a running version
     */
    boolean isRunningVersion();

    /**
     * The name of the property. Typically this should match the name that keys
     * the property's location in the jTamerTree elements Map.
     *
     * @return the name of the property
     */
    String getName();

    /**
     * Set the property name.
     *
     * @param name the name of the property
     */
    void setName(String name);

    /**
     * Make the property a running version or turn it off as the running
     * version. A property that is made a running version will preserve the
     * current state in such a way that it is retrievable by a future call to
     * 'recoverRunningVersion()'. Additionally, a property that is a running
     * version will resolve all functions prior to returning it's property
     * VALUE. A non-running version property will return functions as their
     * uncompiled string representation.
     * 
     * This must be called before getXXXXValue()
     *
     * @param runningVersion flag whether this property is a running version
     */
    void setRunningVersion(boolean runningVersion);

    
    /**
     * Take the given property object and merge it's VALUE with the current
     * property object. For most property types, this will simply be ignored.
     * But for collection properties and jTamerTree elements properties, more complex
     * behavior is required.
     *
     * @param prop the property object to merge into this property
     */
    void mergeIn(GateProperty prop);

    int getIntValue();

    long getLongValue();

    double getDoubleValue();

    float getFloatValue();

    boolean getBooleanValue();

    String getStringValue();

    Object getObjectValue();

    void setObjectValue(Object value);

    GateProperty clone();
}
