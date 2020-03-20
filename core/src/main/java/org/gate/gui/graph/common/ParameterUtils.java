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
package org.gate.gui.graph.common;

import org.gate.gui.details.results.elements.graph.ElementResult;
import org.gate.runtime.GateContextService;

import java.util.Arrays;
import java.util.List;

public class ParameterUtils {

    /*
    *  return a default value and failed result instead of throw an exception for easy to use.
    * */

    public static int getUnsinedInt(String value, ElementResult result){
        try{
            return Integer.parseUnsignedInt(value);
        }catch (NumberFormatException e){
            result.setFailure("value not an unsigned integer");
            return 0;
        }
    }

    public static int getInt(String value, ElementResult result){
        try{
            return Integer.parseInt(value);
        }catch (NumberFormatException e){
            result.setFailure("value not an unsigned integer");
            return 0;
        }
    }

    public static long getUnsignedLong(String value, ElementResult result){
        try{
            return Long.parseUnsignedLong(value);
        }catch (NumberFormatException e){
            result.setFailure("value not an unsigned integer");
            return 0;
        }
    }

    public static long getLong(String value, ElementResult result){
        try{
            return Long.parseLong(value);
        }catch (NumberFormatException e){
            result.setFailure("value not an unsigned integer");
            return 0;
        }
    }

    public static boolean getBoolean(String value, ElementResult result){
        return Boolean.valueOf(value);
    }

    // used for check parameter is not empty
    public static boolean isEmpty(String value, ElementResult result){
        if(value.isEmpty()){
            return true;
        }else{
            result.setFailure(value.concat(" : is not empty"));
            return  false;
        }
    }

    public static boolean isNotEmpty(String value, ElementResult result){
        if(value.isEmpty()){
            result.setFailure(value.concat(" : is empty"));
            return false;
        }else{
            return  true;
        }
    }



    public static boolean isJTamerVariableExist(String name, ElementResult result){
        if(GateContextService.getContext().getVariables().containsKey(name)) {
            return true;
        }else {
            result.setFailure(name.concat(": is not exit"));
            return false;
        }
    }


    // used for check parameter contained by a list
    public static boolean isContainsInList(String value, List<String> list, ElementResult result){
        if(list.contains(value)){
            return true;
        }else{
            result.setFailure(value.concat(" is not contain in: ").concat(Arrays.toString(list.toArray())));
            return false;
        }
    }


}
