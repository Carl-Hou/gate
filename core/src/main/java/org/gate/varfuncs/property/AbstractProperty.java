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

import java.util.Collection;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class AbstractProperty implements GateProperty {
    private static final long serialVersionUID = 240L;

    protected final Logger log = LogManager.getLogger(this);

    private String name;

    private transient boolean runningVersion = false;

    public AbstractProperty(String name) {
        if (name == null) {
            throw new IllegalArgumentException("NAME cannot be null");
        }
        this.name = name;
    }

    public AbstractProperty() {
        this("");
    }

    protected boolean isEqualType(GateProperty prop) {
        if (this.getClass().equals(prop.getClass())) {
            return true;
        } else {
            return false;
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean isRunningVersion() {
        return runningVersion;
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return name;
    }

    /** {@inheritDoc} */
    @Override
    public void setName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("NAME cannot be null");
        }
        this.name = name;
    }

    /** {@inheritDoc} */
    @Override
    public void setRunningVersion(boolean runningVersion) {
        this.runningVersion = runningVersion;
    }

    protected PropertyIterator getIterator(Collection<GateProperty> values) {
        return new PropertyIteratorImpl(values);
    }

    /** {@inheritDoc} */
    @Override
    public AbstractProperty clone() {
        try {
            AbstractProperty prop = (AbstractProperty) super.clone();
            prop.name = name;
            prop.runningVersion = runningVersion;
            return prop;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e); // clone should never return null
        }
    }

    /**
     * Returns 0 if string is invalid or null.
     *
     * @see GateProperty#getIntValue()
     */
    @Override
    public int getIntValue() {
        String val = getStringValue();
        if (val == null || val.length()==0) {
            return 0;
        }
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Returns 0 if string is invalid or null.
     *
     * @see GateProperty#getLongValue()
     */
    @Override
    public long getLongValue() {
        String val = getStringValue();
        if (val == null || val.length()==0) {
            return 0;
        }
        try {
            return Long.parseLong(val);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Returns 0 if string is invalid or null.
     *
     * @see GateProperty#getDoubleValue()
     */
    @Override
    public double getDoubleValue() {
        String val = getStringValue();
        if (val == null || val.length()==0) {
            return 0;
        }
        try {
            return Double.parseDouble(val);
        } catch (NumberFormatException e) {
            log.error("Tried to parse a non-number string to an integer", e);
            return 0;
        }
    }

    /**
     * Returns 0 if string is invalid or null.
     *
     * @see GateProperty#getFloatValue()
     */
    @Override
    public float getFloatValue() {
        String val = getStringValue();
        if (val == null || val.length()==0) {
            return 0;
        }
        try {
            return Float.parseFloat(val);
        } catch (NumberFormatException e) {
            log.error("Tried to parse a non-number string to an integer", e);
            return 0;
        }
    }

    /**
     * Returns false if string is invalid or null.
     *
     * @see GateProperty#getBooleanValue()
     */
    @Override
    public boolean getBooleanValue() {
        String val = getStringValue();
        if (val == null || val.length()==0) {
            return false;
        }
        return Boolean.parseBoolean(val);
    }

    /**
     * Determines if the two objects are equal by comparing names and values
     *
     * @return true if names are equal and values are equal (or both null)
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof GateProperty)) {
            return false;
        }
        if (this == o) {
            return true;
        }
        GateProperty jpo = (GateProperty) o;
        if (!name.equals(jpo.getName())) {
            return false;
        }
        Object o1 = getObjectValue();
        Object o2 = jpo.getObjectValue();
        return o1 == null ? o2 == null : o1.equals(o2);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        int result = 17;
        result = result * 37 + name.hashCode();// name cannot be null
        Object o = getObjectValue();
        result = result * 37 + (o == null ? 0 : o.hashCode());
        return result;
    }

    /**
     * Compares two JMeterProperty object values. N.B. Does not compare names
     *
     * @param arg0
     *            JMeterProperty to compare against
     * @return 0 if equal values or both values null; -1 otherwise
     * @see Comparable#compareTo(Object)
     */
    @Override
    public int compareTo(GateProperty arg0) {
        // We don't expect the string values to ever be null. But (as in
        // bug 19499) sometimes they are. So have null compare less than
        // any other VALUE. Log a warning so we can try to find the root
        // cause of the null VALUE.
        String val = getStringValue();
        String val2 = arg0.getStringValue();
        if (val == null) {
            log.warn("Warning: Unexpected null VALUE for property: " + name);

            if (val2 == null) {
                // Two null values -- return equal
                return 0;
            } else {
                return -1;
            }
        }
        return val.compareTo(val2);
    }

    /**
     * Get the property type for this property. Used to convert raw values into
     * JMeterProperties.
     *
     * @return property type of this property
     */
    protected Class<? extends GateProperty> getPropertyType() {
        return getClass();
    }

    /**
     * Provides the string representation of the property.
     *
     * @return the string VALUE
     */
    @Override
    public String toString() {
        // N.B. Other classes rely on this returning just the string.
        return getStringValue();
    }

    /** {@inheritDoc} */
    @Override
    public void mergeIn(GateProperty prop) {
        // NOOP
    }
}
