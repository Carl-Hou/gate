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
package org.gate.gui.common;

import jdk.nashorn.internal.scripts.JO;
import org.gate.common.util.GateUtils;

import javax.swing.*;
import java.io.PrintWriter;
import java.io.StringWriter;

public class OptionPane {

    private static final boolean SKIP_CONFIRM = false; // $NON-NLS-1$

    public static void  showErrorMessageDialog(String title, String errorMessage){
        JOptionPane.showMessageDialog(null, errorMessage, title, JOptionPane.ERROR_MESSAGE);
    }

    public static void  showErrorMessageDialog(String title, Throwable t){
        JOptionPane.showMessageDialog(null, GateUtils.getStackTrace(t), title, JOptionPane.ERROR_MESSAGE);
    }

    public static void  showErrorMessageDialog(Throwable t){
        JOptionPane.showMessageDialog(null, GateUtils.getStackTrace(t), "Fatal Error:", JOptionPane.ERROR_MESSAGE);
    }

    public static void showMessageDialog(String message){
        JOptionPane.showMessageDialog(null, message, "Message:", JOptionPane.INFORMATION_MESSAGE);
    }

    public static boolean  showConfirmMessageDialog(String title, String message){

        int isConfirm = SKIP_CONFIRM ? JOptionPane.YES_OPTION :
                JOptionPane.showConfirmDialog(null, message, title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if(isConfirm == JOptionPane.YES_OPTION){
            return true;
        }else{
            return false;
        }
    }


}
