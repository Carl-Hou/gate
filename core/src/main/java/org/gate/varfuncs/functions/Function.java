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

package org.gate.varfuncs.functions;

import java.util.Collection;
import java.util.List;

import org.gate.gui.details.results.elements.graph.SamplerResult;
import org.gate.varfuncs.CompoundVariable;


/**
 * Methods that a function must implement
 */
public interface Function {
    /**
     * Given the previous SampleResult and the current Sampler, return a string
     * to use as a replacement value for the function call. Assume
     * "setParameter" was previously called.
     *
     * This method must be threadsafe - multiple threads will be using the same
     * object.
     *
     * @return The replacement value, which was generated by the function
     * @throws InvalidVariableException - when the variables for the function call can't be evaluated
     */
    // result is different with JMeter, remove them.
    // same with JMeter's execute(previousSampler, currentSampler). rename this to resolve name conflict
    String executeRecursion() throws InvalidVariableException;

    /**
     * A collection of the parameters used to configure your function. Each
     * parameter is a CompoundVariable and can be resolved by calling the
     * execute() method of the CompoundVariable (which should be done at
     * execution.)
     *
     * @param parameters The parameters for the function call
     * @throws InvalidVariableException - when the variables for the function call can't be evaluated
     */
    void setParameters(Collection<CompoundVariable> parameters) throws InvalidVariableException;

    /**
     * Return the name of your function. Convention is to prepend "__" to the
     * name (ie "__regexFunction")
     * @return The name of the funtion
     */
    String getReferenceKey();

    /**
     * Return a list of strings briefly describing each parameter your function
     * takes. Please use JMeterUtils.getResString(resource_name) to grab a
     * resource string. Otherwise, your help text will be difficult to
     * internationalize.
     *
     * This list is not optional. If you don't wish to write help, you must at
     * least return a List containing the correct number of blank strings, one
     * for each argument.
     * @return List with brief descriptions for each parameter the function takes
     */
    List<String> getArgumentDesc();
}
