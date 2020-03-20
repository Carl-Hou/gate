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

import java.io.*;
import java.util.Optional;

public class CopyUtils {

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
}
