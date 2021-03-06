/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.isis.applib.services.scratchpad;

import java.util.Map;

import javax.enterprise.context.RequestScoped;

import com.google.common.collect.Maps;

import org.apache.isis.applib.annotation.Bulk.InteractionContext;
import org.apache.isis.applib.annotation.Programmatic;

@RequestScoped
public class Scratchpad {
    
    /**
     * Provides a mechanism for each object being acted upon to pass
     * data to the next object.
     */
    private final Map<Object, Object> userData = Maps.newHashMap();
    
    /**
     * Obtain user-data, as set by a previous object being acted upon.
     */
    @Programmatic
    public Object get(Object key) {
        return userData.get(key);
    }
    /**
     * Set user-data, for the use of a subsequent object being acted upon.
     */
    @Programmatic
    public void put(Object key, Object value) {
        userData.put(key, value);
    }
    
    /**
     * Clear any user data.
     * 
     * <p>
     * Note that a new instance of {@link InteractionContext} is created for
     * any given bulk action, and so it isn't required for the user data to be
     * cleared down.
     */
    @Programmatic
    public void clear() {
        userData.clear();
    }

}
