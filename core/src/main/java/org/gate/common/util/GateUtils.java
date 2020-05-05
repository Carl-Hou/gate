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
package org.gate.common.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gate.common.config.GateProps;
import org.gate.runtime.GateVariables;
import org.w3c.dom.Document;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.*;

public class GateUtils {

    static Logger log = LogManager.getLogger(GateUtils.class);

    public static LinkedList<String> getParameterList(String parameters){
        return getParameterList(parameters, ",");
    }

    public static LinkedList<String> getParameterList(String parameters, String delimiter){
        LinkedList<String> nameList = new LinkedList<>();
        if(parameters == null){
            return nameList;
        }
        nameList.addAll(Arrays.asList(parameters.trim().split(delimiter)));
        for (String name : nameList) {
            name.trim();
        }
        if(nameList.size() == 1){
            if(nameList.getFirst().isEmpty()){
                nameList.clear();
            }
        }
        return nameList;
    }

    /*
     * Use for deep copy isolate objects. A temp solution for clone.
     * Don't use this if not have to.
     * */
    public static <T> Optional<T> deepCopy(T source)  {
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream(); // this stream don't need to be closed
            out = new ObjectOutputStream(bos);
            out.writeObject(source);
            ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
            in = new ObjectInputStream(bis);
            T deepCopyClone = (T) in.readObject();
            return Optional.of(deepCopyClone);
        }catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }finally {
            if(out !=null){
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(in !=null){
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return Optional.empty();
    }

    public static String getStackTrace(Throwable e) {
        StringWriter stringWriter = new StringWriter();
        e.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
    }

    // do read only option on variables
    public static String dumpToString(GateVariables variables){
        StringBuffer sb = new StringBuffer();
        sb.append("Variables: ").append(GateProps.LineSeparator);
        variables.entrySet().forEach( v ->{
            sb.append(v.getKey()).append(":").append(v.getValue()).append(GateProps.LineSeparator);
        });
        sb.trimToSize();
        return sb.toString();
    }
    // from JMeter
    /**
     * Close a Closeable with no error thrown
     * @param cl - Closeable (may be null)
     */
    public static void closeQuietly(Closeable cl){
        try {
            if (cl != null) {
                cl.close();
            }
        } catch (IOException ignored) {
            log.fatal("Ignore exception when close closeable", ignored);
            // NOOP
        }
    }

    /**
     * Convert binary byte array to hex string.
     *
     * @param ba input binary byte array
     * @return hex representation of binary input
     */
    public static String baToHexString(byte[] ba) {
        StringBuilder sb = new StringBuilder(ba.length*2);
        for (byte b : ba) {
            int j = b & 0xff;
            if (j < 16) {
                sb.append('0'); // $NON-NLS-1$ add zero padding
            }
            sb.append(Integer.toHexString(j));
        }
        return sb.toString();
    }

    /**
     * Takes a String and a tokenizer character string, and returns a new array of
     * strings of the string split by the tokenizer character(s).
     *
     * Trailing delimiters are significant (unless the default = null)
     *
     * @param splittee
     *            String to be split.
     * @param delims
     *            Delimiter character(s) to split the string on
     * @param def
     *            Default value to place between two split chars that have
     *            nothing between them. If null, then ignore omitted elements.
     *
     * @return Array of all the tokens.
     *
     * @throws NullPointerException if splittee or delims are null
     *
     * @see #split(String, String, boolean)
     * @see #split(String, String)
     *
     * This is a rewritten version of JMeterUtils.split()
     */
    public static String[] split(String splittee, String delims, String def) {
        StringTokenizer tokens = new StringTokenizer(splittee,delims,def!=null);
        boolean lastWasDelim=false;
        List<String> strList = new ArrayList<>();
        while (tokens.hasMoreTokens()) {
            String tok=tokens.nextToken();
            if (   tok.length()==1 // we have a single character; could be a token
                    && delims.contains(tok)) // it is a token
            {
                if (lastWasDelim) {// we saw a delimiter last time
                    strList.add(def);// so add the default
                }
                lastWasDelim=true;
            } else {
                lastWasDelim=false;
                strList.add(tok);
            }
        }
        if (lastWasDelim) {
            strList.add(def);
        }
        return strList.toArray(new String[strList.size()]);
    }

    /**
     * This is _almost_ equivalent to the String.split method in JDK 1.4. It is
     * here to enable us to support earlier JDKs.
     *
     * Note that unlike JDK1.4 split(), it optionally ignores leading split Characters,
     * and the splitChar parameter is not a Regular expression
     *
     * <P>
     * This piece of code used to be part of JMeterUtils, but was moved here
     * because some JOrphan classes use it too.
     *
     * @param splittee
     *            String to be split
     * @param splitChar
     *            Character(s) to split the string on, these are treated as a single unit
     * @param truncate
     *            Should adjacent and leading/trailing splitChars be removed?
     *
     * @return Array of all the tokens; empty if the input string is null or the splitChar is null
     *
     * @see #split(String, String, String)
     *
     */
    public static String[] split(String splittee, String splitChar,boolean truncate) {
        if (splittee == null || splitChar == null) {
            return new String[0];
        }
        final String EMPTY_ELEMENT = "";
        int spot;
        final int splitLength = splitChar.length();
        final String adjacentSplit = splitChar + splitChar;
        final int adjacentSplitLength = adjacentSplit.length();
        if(truncate) {
            while ((spot = splittee.indexOf(adjacentSplit)) != -1) {
                splittee = splittee.substring(0, spot + splitLength)
                        + splittee.substring(spot + adjacentSplitLength, splittee.length());
            }
            if(splittee.startsWith(splitChar)) {
                splittee = splittee.substring(splitLength);
            }
            if(splittee.endsWith(splitChar)) { // Remove trailing splitter
                splittee = splittee.substring(0,splittee.length()-splitLength);
            }
        }
        List<String> returns = new ArrayList<>();
        final int length = splittee.length(); // This is the new length
        int start = 0;
        spot = 0;
        while (start < length && (spot = splittee.indexOf(splitChar, start)) > -1) {
            if (spot > 0) {
                returns.add(splittee.substring(start, spot));
            }
            else
            {
                returns.add(EMPTY_ELEMENT);
            }
            start = spot + splitLength;
        }
        if (start < length) {
            returns.add(splittee.substring(start));
        } else if (spot == length - splitLength){// Found splitChar at end of line
            returns.add(EMPTY_ELEMENT);
        }
        return returns.toArray(new String[returns.size()]);
    }

    public static String[] split(String splittee,String splitChar)
    {
        return split(splittee,splitChar,true);
    }

}
