/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.felix.ipojo.runtime.core.test.components;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Unbind;
import org.apache.felix.ipojo.runtime.core.test.services.FooService;

@Component
public class FilteredDependency {

    @Requires(filter="(foo=bar)")
    public FooService fs;
    
    @Unbind(filter="(foo=bar)")
    public void unbindBar() {    }
    @Bind
    public void bindBar() {    }
    
    @Unbind
    public void unbindBaz() {    }
    @Bind(filter="(foo=bar)")
    public void bindBaz() {    }
   
    @Requires(id="inv")
    public FooService fs2inv;
    @Bind(id="inv", filter="(foo=bar)")
    public void bindFS2Inv() {   }
    @Unbind(id="inv")
    public void unbindFS2Inv() {   }
    
    @Unbind(filter="(foo=bar)", id="unbindonly")
    public void unbind() {    }
    
    @Bind(filter="(foo=bar)", id="bindonly")
    public void bind() {    }
}
