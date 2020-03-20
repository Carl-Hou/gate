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
package org.gate.gui.tree.test.elements.fixture;

import com.mxgraph.model.mxGraphModel;
import org.gate.engine.TestConstraint;
import org.gate.gui.tree.ModelContainer;
import org.gate.gui.tree.test.elements.TestTreeElement;

public abstract class FixtureElement extends TestTreeElement implements ModelContainer, TestConstraint {

    protected mxGraphModel mxModel= null;

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
