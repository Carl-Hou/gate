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

package org.gate.runtime;

/**
 * Provides context service for JMeter threads.
 * Keeps track of active and total thread counts.
 */
public final class GateContextService {
    private static final ThreadLocal<GateContext> threadContext = new ThreadLocal<GateContext>() {
        @Override
        public GateContext initialValue() {
            return new GateContext();
        }
    };

    /**
     * Private constructor to prevent instantiation.
     */
    private GateContextService() {
    }

    /**
     * Gives access to the current thread context.
     * 
     * @return the current thread Context
     */
    public static GateContext getContext() {
        return threadContext.get();
    }

    /**
     * Allows the thread Context to be completely cleared.
     * <br/>
     * Invokes {@link ThreadLocal#remove()}.
     */
    static void removeContext(){ // Currently only used by JMeterThread
        threadContext.remove();
    }

    /**
     * Replace Thread Context by the parameter. Currently only used by the
     * private class <code>ASyncSample</code> in
     * HTTPSamplerBase}
     *
     * @param context
     *            {@link GateContext}
     */
    public static void replaceContext(GateContext context) {
        threadContext.remove();
        threadContext.set(context);
    }


}
