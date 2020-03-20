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

package org.gate.common.config;

import java.util.Properties;

// This is used to store Gate app level property and name constraints
public class GateProps {
	// general
    public final static String LineSeparator = System.getProperty("line.separator");
	public final static String FileSeparator = System.getProperty("file.separator");

    //test model
	public final static String Success = "success";
	public final static String Fail = "fail";
	public final static String Next = "next";
	public final static String Note = "note";

	public final static String TRUE = "true";
	public final static String FALSE = "false";

	// used by launchers
	static boolean guiMode = false;
	static String GateHome = System.getProperty("user.dir");

	private static volatile Properties appProperties = new Properties();

	public static void setGuiMode(){
		guiMode = true;
	}

	public static boolean isGuiMode(){
		return guiMode;
	}

	public static String getGateHome(){
		return GateHome;
	}

	public static Properties getProperties(){
		return appProperties;
	}

	public static String getProperty(String name){
		return appProperties.getProperty(name, "");
	}

	public static String getProperty(String name, String defaultValue){
		return appProperties.getProperty(name, defaultValue);
	}

	public static boolean getProperty(String name, boolean defaultValue){
		String value = appProperties.getProperty(name);
		if(null == value){
			return defaultValue;
		}else{
			return Boolean.parseBoolean(value);
		}
	}

	public static int getProperty(String name, int defaultValue){
		String value = appProperties.getProperty(name);
		if(null == value){
			return defaultValue;
		}else{
			return Integer.parseInt(value);
		}
	}

	public static String setProperty(String name, String value){
		return (String) appProperties.setProperty(name, value);
	}

	public static String removeProperty(String name){
		return (String) appProperties.remove(name);
	}

}
