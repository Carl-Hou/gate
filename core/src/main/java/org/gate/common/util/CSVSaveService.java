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

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This class provides a means for saving/reading test results as CSV files.
 */
public final class CSVSaveService {
    private static final Logger log = LogManager.getLogger();

    /**
     * Private constructor to prevent instantiation.
     */
    private CSVSaveService() {
    }


    // State of the parser
    private enum ParserState {INITIAL, PLAIN, QUOTED, EMBEDDEDQUOTE}

    public static final char QUOTING_CHAR = '"';

    /**
     * Reads from file and splits input into strings according to the delimiter,
     * taking note of quoted strings.
     * <p>
     * Handles DOS (CRLF), Unix (LF), and Mac (CR) line-endings equally.
     * <p>
     * A blank line - or a quoted blank line - both return an array containing
     * a single empty String.
     * @param infile
     *            input file - must support mark(1)
     * @param delim
     *            delimiter (e.g. comma)
     * @return array of strings, will be empty if there is no data, i.e. if the input is at EOF.
     * @throws IOException
     *             also for unexpected quote characters
     */
    public static String[] csvReadFile(BufferedReader infile, char delim)
            throws IOException {
        int ch;
        ParserState state = ParserState.INITIAL;
        List<String> list = new ArrayList<>();
        CharArrayWriter baos = new CharArrayWriter(200);
        boolean push = false;
        while (-1 != (ch = infile.read())) {
            push = false;
            switch (state) {
            case INITIAL:
                if (ch == QUOTING_CHAR) {
                    state = ParserState.QUOTED;
                } else if (isDelimOrEOL(delim, ch)) {
                    push = true;
                } else {
                    baos.write(ch);
                    state = ParserState.PLAIN;
                }
                break;
            case PLAIN:
                if (ch == QUOTING_CHAR) {
                    baos.write(ch);
                    throw new IOException(
                            "Cannot have quote-char in plain field:["
                                    + baos.toString() + "]");
                } else if (isDelimOrEOL(delim, ch)) {
                    push = true;
                    state = ParserState.INITIAL;
                } else {
                    baos.write(ch);
                }
                break;
            case QUOTED:
                if (ch == QUOTING_CHAR) {
                    state = ParserState.EMBEDDEDQUOTE;
                } else {
                    baos.write(ch);
                }
                break;
            case EMBEDDEDQUOTE:
                if (ch == QUOTING_CHAR) {
                    baos.write(QUOTING_CHAR); // doubled quote => quote
                    state = ParserState.QUOTED;
                } else if (isDelimOrEOL(delim, ch)) {
                    push = true;
                    state = ParserState.INITIAL;
                } else {
                    baos.write(QUOTING_CHAR);
                    throw new IOException(
                            "Cannot have single quote-char in quoted field:["
                                    + baos.toString() + "]");
                }
                break;
            default:
                throw new IllegalStateException("Unexpected state " + state);
            } // switch(state)
            if (push) {
                if (ch == '\r') {// Remove following \n if present
                    infile.mark(1);
                    if (infile.read() != '\n') {
                        infile.reset(); // did not find \n, put the character
                                        // back
                    }
                }
                String s = baos.toString();
                list.add(s);
                baos.reset();
            }
            if ((ch == '\n' || ch == '\r') && state != ParserState.QUOTED) {
                break;
            }
        } // while not EOF
        if (ch == -1) {// EOF (or end of string) so collect any remaining data
            if (state == ParserState.QUOTED) {
                throw new IOException("Missing trailing quote-char in quoted field:[\""
                        + baos.toString() + "]");
            }
            // Do we have some data, or a trailing empty field?
            if (baos.size() > 0 // we have some data
                    || push // we've started a field
                    || state == ParserState.EMBEDDEDQUOTE // Just seen ""
            ) {
                list.add(baos.toString());
            }
        }
        return list.toArray(new String[list.size()]);
    }

    private static boolean isDelimOrEOL(char delim, int ch) {
        return ch == delim || ch == '\n' || ch == '\r';
    }

    /**
     * Reads from String and splits into strings according to the delimiter,
     * taking note of quoted strings.
     * 
     * Handles DOS (CRLF), Unix (LF), and Mac (CR) line-endings equally.
     * 
     * @param line
     *            input line - not {@code null}
     * @param delim
     *            delimiter (e.g. comma)
     * @return array of strings
     * @throws IOException
     *             also for unexpected quote characters
     */
    public static String[] csvSplitString(String line, char delim)
            throws IOException {
        return csvReadFile(new BufferedReader(new StringReader(line)), delim);
    }
}
