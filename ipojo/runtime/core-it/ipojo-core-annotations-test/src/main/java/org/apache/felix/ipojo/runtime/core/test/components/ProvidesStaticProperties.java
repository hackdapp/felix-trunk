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

import java.util.Properties;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.ipojo.annotations.StaticServiceProperty;
import org.apache.felix.ipojo.runtime.core.test.services.BarService;
import org.apache.felix.ipojo.runtime.core.test.services.BarService;
import org.apache.felix.ipojo.runtime.core.test.services.FooService;

@Component
@Provides(
		specifications= {FooService.class, BarService.class},
		properties= {
				@StaticServiceProperty(name="prop1", value="prop1", type="java.lang.String"),
				@StaticServiceProperty(name="prop2", type="java.lang.String"),
				@StaticServiceProperty(name="props", value="{prop1, prop2}", type="string[]"),
				@StaticServiceProperty(name="mandatory1", mandatory=true, type="string")
		})
public class ProvidesStaticProperties implements FooService, BarService {

    @ServiceProperty(name = "foo")
    public int m_foo = 0;

    @ServiceProperty(value = "4", mandatory=true)
    public int bar;

    @ServiceProperty(name="baz")
    int m_baz;

    @ServiceProperty
    public int boo;

    @ServiceProperty(name="baa", value="5")
    public int m_baa;

    public boolean foo() {
        return false;
    }

    public Properties fooProps() {
        return null;
    }

    public boolean getBoolean() {
        return false;
    }

    public double getDouble() {
        return 0;
    }

    public int getInt() {
        return 0;
    }

    public long getLong() {
        return 0;
    }

    public Boolean getObject() {
        return null;
    }

    public boolean bar() {
        return false;
    }

    public Properties getProps() {
        return null;
    }

}
