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

package org.gate.gui.tree.test.elements;

import com.mxgraph.model.mxGraphModel;
import org.gate.engine.TestConstraint;
import org.gate.gui.tree.ModelContainer;

public class TestCase extends TestTreeElement implements ModelContainer, TestConstraint {

    public static final String Depends = "depends";
    public static final String TestCaseTimeout = "test case timeout";
    protected mxGraphModel mxModel= null;

    public TestCase(){
        addProp(NS_DEFAULT, Depends, "");
        addProp(NS_DEFAULT, TestCaseTimeout, ""); // no timeout if this set to empty
    }

    public String getDepends(){
        return getProp(NS_DEFAULT, Depends).getStringValue();
    }

    public String getTimeout(){
        return getProp(NS_DEFAULT, TestCaseTimeout).getStringValue();
    }



    @Override
    public mxGraphModel getMxModel() {
        if(mxModel == null){
            mxModel = new mxGraphModel();
        }
        return mxModel;
    }

    @Override
    public void setMxModel(mxGraphModel model) {
        this.mxModel = model;
    }
}
