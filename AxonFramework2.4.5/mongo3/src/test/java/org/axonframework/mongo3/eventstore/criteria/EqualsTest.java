/*
 * Copyright (c) 2010-2016. Axon Framework
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.axonframework.mongo3.eventstore.criteria;

import org.bson.conversions.Bson;
import org.junit.*;

/**
 * @author Allard Buijze
 */
public class EqualsTest {

    @Test
    public void testEqualsToValue() {
        Bson actual = new Equals(new MongoProperty("property"), "someValue").asBson();
       // assertEquals(actual.keySet(), Collections.singleton("property"));
        //assertEquals(actual.get("property"), "someValue");
        // TODO
        System.out.println(actual);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEqualsBetweenProperties() {
        new Equals(new MongoProperty("property"), new MongoProperty("bla"));
    }
}
