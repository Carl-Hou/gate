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

package org.gate.saveload.convert.graph;

import org.gate.gui.graph.elements.GraphElement;
import org.gate.saveload.convert.TestElementConverter;
import org.gate.saveload.utils.exceptions.ConvertException;
import org.w3c.dom.Element;

public interface GraphElementConverter extends TestElementConverter {

	/*
	*  Make sure this don't return null;throw exception once abnormal error occur.
	*  Decode and encode operation all handled in this program, so any issue should be handled.
	*  Any abnormal issue cause an exception
	* */
	public Element marshal(GraphElement testElement) throws ConvertException;

	public GraphElement unmarshal(Element element) throws ConvertException;

}
