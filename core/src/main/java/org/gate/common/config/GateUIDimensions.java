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

import java.awt.*;

// this is for UI component init size.
public class GateUIDimensions {
    private static GateUIDimensions ourInstance = new GateUIDimensions();

    Dimension mainFrameSize = null;
    Dimension libraryPaneMinSize = null;
    int workspaceHorizontalDivider;
    int workspaceVerticalDivider;
    int libraryOutlineDivider;
    int graphEditorDivider;




    public static GateUIDimensions getInstance() {
        return ourInstance;
    }

    private GateUIDimensions() {
        reset();
    }

    public void reset(){
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int mainFrameWidth = (int) (screenSize.width * 1.0);
        int mainFrameHeight = (int) (screenSize.height * 0.95);
        mainFrameSize = new Dimension(mainFrameWidth, mainFrameHeight);
        workspaceHorizontalDivider = (int) (mainFrameWidth * 0.15);
        workspaceVerticalDivider = (int) (mainFrameWidth * 0.34);
        libraryOutlineDivider = (int) (mainFrameHeight * 0.63 * 0.8);
//      first 085 is the size of graphEditor, second 0.85 is the divider position between graphComponent and libaryOutline.
        graphEditorDivider = (int) (mainFrameWidth *0.85 * 0.85);
        libraryPaneMinSize = new Dimension(200, 400);

    }

    public Dimension getMainFrameSize(){return mainFrameSize;}
    public Dimension getLibraryPaneMinSize(){return libraryPaneMinSize;}
    public int getWorkspaceHorizontalDivider() {return workspaceHorizontalDivider;}
    public int getWorkspaceVerticalDivider() {return workspaceVerticalDivider;}

    public int getLibraryOutlineDivider() {return libraryOutlineDivider;}
    public int getGraphEditorDivider() {return graphEditorDivider;}

}
