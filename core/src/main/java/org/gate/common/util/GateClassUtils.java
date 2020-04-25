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

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gate.gui.graph.elements.GraphElement;
import org.gate.gui.graph.elements.asseration.Assert;
import org.gate.gui.graph.elements.comment.Comment;
import org.gate.gui.graph.elements.config.Config;
import org.gate.gui.graph.elements.control.Controller;
import org.gate.gui.graph.elements.sampler.Sampler;
import org.gate.gui.graph.elements.extractor.Extractor;
import org.gate.gui.graph.elements.timer.Timer;

import java.lang.reflect.Modifier;
import java.util.*;

public class GateClassUtils {

    final static Logger log = LogManager.getLogger();

    FastClasspathScanner scanner = null;

    HashMap<String, List<Class>> graphElements = new HashMap<>();

    static GateClassUtils instance = new GateClassUtils();

    private GateClassUtils(){
        scanner = new FastClasspathScanner("org.gate");
        scanner.scan();
        findGraphElementImplementing();
    }

    public static GateClassUtils getIns(){
        return instance;
    }

    public synchronized void scan(){
        scanner.scan();
    }

    //filter abstract class
    public<T> List<Class> getClassesImplementing(Class<T> implementedInterface){
        return nonAbstractFilter(getClasses(scanner.getClassesImplementing(implementedInterface)));
    }

    public HashMap<String, List<Class>> getGraphElements(){
        return graphElements;
    }

    public String getGraphElementCategory(GraphElement graphElement){
        if(Assert.class.isInstance(graphElement)){
            return "Assert";
        }else if (Comment.class.isInstance(graphElement)) {
            return "Comment";
        }else if (Config.class.isInstance(graphElement)) {
            return "Config";
        }else if (Controller.class.isInstance(graphElement)) {
            return "Controller";
        }else if (Extractor.class.isInstance(graphElement)) {
            return "Extractor";
        }else if (Sampler.class.isInstance(graphElement)) {
            return "Sampler";
        }else if (Timer.class.isInstance(graphElement)) {
            return "Timer";
        }
        return null;
    }

    void findGraphElementImplementing(){
        graphElements.put(Assert.class.getName(), getClassesImplementing(Assert.class));
        graphElements.put(Comment.class.getName(), getClassesImplementing(Comment.class));
        graphElements.put(Config.class.getName(), getClassesImplementing(Config.class));
        graphElements.put(Controller.class.getName(), getClassesImplementing(Controller.class));
        graphElements.put(Extractor.class.getName(), getClassesImplementing(Extractor.class));
        graphElements.put(Sampler.class.getName(), getClassesImplementing(Sampler.class));
        graphElements.put(Timer.class.getName(), getClassesImplementing(Timer.class));

    }

    public GraphElement newGraphElementInstance(Class clazz){
        try {
            return (GraphElement) clazz.newInstance();
        }  catch (InstantiationException | IllegalAccessException e) {
            log.error("Could not instantiate " + clazz.getName(), e);
        }
        return null;
    }

    LinkedList<Class> getClasses(List<String> classNames){
        LinkedList<Class> classes = new LinkedList<>();
        classNames.forEach( className ->{
            try {
                Class clazz = Class.forName(className);
                if(isKeep(clazz)){
                    classes.add(clazz);
                }
            } catch (ClassNotFoundException e) {
                // no plan to support dynamic class. exist if class not found.
                log.fatal("Fail to find required class. Check the CLASSPATH,-cp, etc", e);
                System.exit(1);
            }
        });
        return  classes;
    }

    boolean isKeep(Class clazz){
        if(Modifier.isAbstract(clazz.getModifiers())){
            return false;
        }
        return true;
    }

    List<Class> nonAbstractFilter(LinkedList<Class> classes){
        ListIterator<Class> listIterator = classes.listIterator();
        while (listIterator.hasNext()){
            Class c = listIterator.next();
            if(Modifier.isAbstract(c.getModifiers())){
                listIterator.remove();
            }
        }
        return classes;
    }


}
