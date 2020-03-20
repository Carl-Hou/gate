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
package org.gate.gui.details.results.elements;

import org.gate.common.config.GateProps;
import org.gate.common.util.GateUtils;

public class DefaultResult implements Result {

    boolean success = true;
    String name;
    StringBuffer messageBuffer = new StringBuffer();

    Throwable throwable = null;

    // Does this really required?
    public DefaultResult(String name){
        this.name = name;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getName(){
        return name;
    }

    @Override
    public boolean isSuccess() {
        return success;
    }

    @Override
    public void setSuccess() {
        this.success = true;
    }

    @Override
    public boolean isFailure() {
        return !isSuccess();
    }

    @Override
    public void setFailure() {
        this.success = false;
    }

    @Override
    public void setFailure(CharSequence msg) {
        this.success = false;
        appendMessage(msg);
    }

    @Override
    public void setMessage(CharSequence message){
        messageBuffer = new StringBuffer(message);
    }

    @Override
    public void insertMessage(CharSequence errorMessage){
        messageBuffer.insert(0,errorMessage + GateProps.LineSeparator);
    }

    @Override
    public void appendMessage(CharSequence errorMessage){
        messageBuffer.append(errorMessage).append(GateProps.LineSeparator);
    }

    @Override
    public String getMessage(){
        messageBuffer.trimToSize();
        return messageBuffer.toString();
    }

    @Override
    public void setThrowable(Throwable throwable){
        setFailure();
        if(throwable != null){
            appendMessage("Previous throwable" );
            appendMessage(GateUtils.getStackTrace(throwable));
        }
        this.throwable = throwable;
    }
    @Override
    public Throwable getThrowable(){
        return throwable;
    }

    public String toString(){
        return getName();
    }

}
