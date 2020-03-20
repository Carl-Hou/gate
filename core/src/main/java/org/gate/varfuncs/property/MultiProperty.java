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

/**
 * For JMeterProperties that hold multiple properties within, provides a simple
 * interface for retrieving a property iterator for the sub values.
 *
 * @version $Revision: 1730225 $
 */
public abstract class MultiProperty extends AbstractProperty implements Iterable<GateProperty> {
    private static final long serialVersionUID = 240L;

    public MultiProperty() {
        super();
    }

    public MultiProperty(String name) {
        super(name);
    }

    /**
     * Get the property iterator to iterate through the sub-values of this
     * JMeterProperty.
     *
     * @return an iterator for the sub-values of this property
     */
    @Override
    public abstract PropertyIterator iterator();

    /**
     * Add a property to the collection.
     *
     * @param prop the {@link GateProperty} to add
     */
    public abstract void addProperty(GateProperty prop);

    /**
     * Clear away all values in the property.
     */
    public abstract void clear();

    @Override
    public void setRunningVersion(boolean running) {
        super.setRunningVersion(running);
        for (GateProperty jMeterProperty : this) {
            jMeterProperty.setRunningVersion(running);
        }
    }

    @Override
    public void mergeIn(GateProperty prop) {
        if (prop.getObjectValue() == getObjectValue()) {
            return;
        }
        log.debug("merging in " + prop.getClass());
        if (prop instanceof MultiProperty) {
            for (GateProperty item : ((MultiProperty) prop)) {
                addProperty(item);
            }
        } else {
            addProperty(prop);
        }
    }
}
