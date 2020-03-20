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

package org.gate.varfuncs;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.gate.varfuncs.functions.InvalidVariableException;
import org.gate.varfuncs.property.GateProperty;
import org.gate.varfuncs.property.MultiProperty;
import org.gate.varfuncs.property.NumberProperty;
import org.gate.varfuncs.property.PropertyIterator;
import org.gate.varfuncs.property.PropertyIteratorImpl;
import org.gate.varfuncs.property.StringProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gate.varfuncs.property.*;

/**
 * Perfom replacement of ${variable} references.
 */
public class ValueReplacer {
    private  final Logger log = LogManager.getLogger(this);

    private final CompoundVariable masterFunction = new CompoundVariable();

//    private Map<String, String> variables = v1 HashMap<>();
   
//    /**
//     * Set this {@link ValueReplacer}'s variable map
//     * @param variables Map which stores the variables
//     */
//    Don't know when will this used so mark this down
//    public void setUserDefinedVariables(Map<String, String> variables) {
//        this.variables = variables;
//    }
    
    public LinkedList<GateProperty> replaceValues(LinkedList<GateProperty> props) throws InvalidVariableException {
    	
    	PropertyIterator iter = new PropertyIteratorImpl(props);
    	LinkedList<GateProperty> result = new LinkedList<>();
    	
//    	result.addAll(replaceValues(iter, v1 ReplaceStringWithFunctions(masterFunction)));
    	
    	for(GateProperty p : replaceValues(iter, new ReplaceStringWithFunctions(masterFunction))){
    		p.setRunningVersion(true);
    		result.add(new StringProperty(p.getName(), p.getStringValue()));
    	}
    	return result;
    	
    }
    
    public GateProperty replaceValue(GateProperty prop) throws InvalidVariableException {
    	
    	LinkedList<GateProperty>vals = new LinkedList<>();
		vals.add(prop);
		PropertyIterator iter = new PropertyIteratorImpl(vals);
		Collection<GateProperty>results =  replaceValues(iter, new ReplaceStringWithFunctions(masterFunction));
		
		Optional<GateProperty> valueOptional = results.stream().findFirst();
		if(valueOptional.isPresent() && results.size() ==1){
			GateProperty runtimeProp = valueOptional.get();
			runtimeProp.setRunningVersion(true);
//			return v1 StringProperty(runtimeProp.getCaseName(), runtimeProp.getStringValue());
			return runtimeProp;
		}else{
			log.fatal("Error occur when replace VALUE of JTamer properites, nothing changed");
			return prop;
		}
		
    }
    
    
    /**
     * Replaces a {@link StringProperty} containing functions with their Function properties equivalent.
     * <p>For example:
     * <code>${__time()}_${__threadNum()}_${__machineName()}</code> will become a
     * {@link FunctionProperty} of
     * a {@link CompoundVariable} containing three functions
     * @param iter the {@link PropertyIterator} over all properties, in which the values should be replaced
     * @param transform the {@link ValueTransformer}, that should do transformation
     * @return a v1 {@link Collection} with all the transformed {@link GateProperty}s
     * @throws InvalidVariableException when <code>transform</code> throws an {@link InvalidVariableException} while transforming a VALUE
     */
    protected Collection<GateProperty> replaceValues(PropertyIterator iter, ValueTransformer transform) throws InvalidVariableException {
        List<GateProperty> props = new LinkedList<>();
        while (iter.hasNext()) {
            GateProperty val = iter.next();
//            if (log.isDebugEnabled()) {
//                log.debug("About to replace in property of type: " + val.getClass() + ": " + val);
//            }
            if (val instanceof StringProperty) {
                
                    val = transform.transformValue(val);
//                    if (log.isDebugEnabled()) {
//                        log.debug("Replacement result: " + val);
//                    }
                
            } else if (val instanceof NumberProperty) {
                val = transform.transformValue(val);
                if (log.isDebugEnabled()) {
                    log.debug("Replacement result: " + val);
                }
            } else if (val instanceof MultiProperty) {
                MultiProperty multiVal = (MultiProperty) val;
                Collection<GateProperty> newValues = replaceValues(multiVal.iterator(), transform);
                multiVal.clear();
                for (GateProperty jmp : newValues) {
                    multiVal.addProperty(jmp);
                }
                if (log.isDebugEnabled()) {
                    log.debug("Replacement result: " + multiVal);
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Won't replace " + val);
                }
            }
            props.add(val);
        }
        return props;
    }
}
