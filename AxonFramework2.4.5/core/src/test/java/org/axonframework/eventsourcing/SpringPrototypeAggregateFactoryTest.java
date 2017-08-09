/*
 * Copyright (c) 2010-2012. Axon Framework
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

package org.axonframework.eventsourcing;

import org.axonframework.common.annotation.ParameterResolverFactory;
import org.axonframework.domain.GenericDomainEventMessage;
import org.junit.*;
import org.junit.runner.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

/**
 * @author Allard Buijze
 */
@ContextConfiguration(locations = {"/META-INF/spring/spring-prototype-aggregate-factory.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class SpringPrototypeAggregateFactoryTest {

    @Autowired
    private SpringPrototypeAggregateFactory<SpringWiredAggregate> testSubject;

    @Autowired
    private ParameterResolverFactory parameterResolverFactory;

    @Test
    public void testContextStarts() throws Exception {
        assertNotNull(testSubject);
    }

    @Test
    public void testCreateNewAggregateInstance() {
        SpringWiredAggregate aggregate = testSubject.createAggregate("id2", new GenericDomainEventMessage<String>(
                "id2", 0, "FirstEvent"));
        assertTrue("Aggregate's init method not invoked", aggregate.isInitialized());
        assertNotNull("ContextAware method not invoked", aggregate.getContext());
        assertEquals("it's here", aggregate.getSpringConfiguredName());

        assertSame(parameterResolverFactory, aggregate.getParameterResolverFactory());
    }

    @Test
    public void testProcessSnapshotAggregateInstance() {
        SpringWiredAggregate aggregate = testSubject.createAggregate("id2",
                                                                     new GenericDomainEventMessage<SpringWiredAggregate>(
                                                                             "id2", 5, new SpringWiredAggregate()));
        assertTrue("Aggregate's init method not invoked", aggregate.isInitialized());
        assertNotNull("ContextAware method not invoked", aggregate.getContext());
        assertEquals("it's here", aggregate.getSpringConfiguredName());
    }
}
