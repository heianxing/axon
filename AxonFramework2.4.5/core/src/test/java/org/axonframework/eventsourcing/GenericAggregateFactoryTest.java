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
import org.axonframework.domain.DomainEventMessage;
import org.axonframework.domain.GenericDomainEventMessage;
import org.axonframework.domain.StubAggregate;
import org.axonframework.eventsourcing.annotation.AggregateIdentifier;
import org.axonframework.testutils.MockException;
import org.junit.*;

import java.util.Collection;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Allard Buijze
 */
public class GenericAggregateFactoryTest {

    @Test(expected = IncompatibleAggregateException.class)
    public void testInitializeRepository_NoSuitableConstructor() {
        new GenericAggregateFactory<UnsuitableAggregate>(UnsuitableAggregate.class);
    }

    @Test
    public void testInitializeRepository_ConstructorNotCallable() {
        GenericAggregateFactory<ExceptionThrowingAggregate> factory =
                new GenericAggregateFactory<ExceptionThrowingAggregate>(ExceptionThrowingAggregate.class);
        try {
            factory.createAggregate(UUID.randomUUID(), new GenericDomainEventMessage(new Object(), 0, new Object()));
            fail("Expected IncompatibleAggregateException");
        } catch (IncompatibleAggregateException e) {
            // we got it
        }
    }

    @Test
    public void testAggregateTypeIsSimpleName() {
        GenericAggregateFactory<StubAggregate> factory = new GenericAggregateFactory<StubAggregate>(StubAggregate.class);
        assertEquals("StubAggregate", factory.getTypeIdentifier());
    }

    @Test
    public void testParameterResolverIsRegisteredWithCreatedAggregate() {
        final ParameterResolverFactory parameterResolverFactory = mock(ParameterResolverFactory.class);
        GenericAggregateFactory<SpringWiredAggregate> factory =
                new GenericAggregateFactory<SpringWiredAggregate>(SpringWiredAggregate.class,
                                                                  parameterResolverFactory);
        final SpringWiredAggregate aggregate = factory.createAggregate("test",
                                                                       new GenericDomainEventMessage<Object>("test", 0,
                                                                                                             "test"));
        assertSame(parameterResolverFactory, aggregate.getParameterResolverFactory());
    }

    @Test
    public void testInitializeFromAggregateSnapshot() {
        StubAggregate aggregate = new StubAggregate("stubId");
        aggregate.doSomething();
        aggregate.commitEvents();
        DomainEventMessage<StubAggregate> snapshotMessage = new GenericDomainEventMessage<StubAggregate>(
                aggregate.getIdentifier(), aggregate.getVersion(), aggregate);
        GenericAggregateFactory<StubAggregate> factory = new GenericAggregateFactory<StubAggregate>(StubAggregate.class);
        assertEquals("StubAggregate", factory.getTypeIdentifier());
        assertSame(aggregate, factory.createAggregate(aggregate.getIdentifier(), snapshotMessage));
    }

    private static class UnsuitableAggregate extends AbstractEventSourcedAggregateRoot {

        private UnsuitableAggregate(Object uuid) {
        }

        @Override
        protected Collection<EventSourcedEntity> getChildEntities() {
            return null;
        }

        @Override
        protected void handle(DomainEventMessage event) {
        }

        @Override
        public Object getIdentifier() {
            return "unsuitableAggregateId";
        }
    }

    private static class ExceptionThrowingAggregate extends AbstractEventSourcedAggregateRoot {

        @AggregateIdentifier
        private String identifier;

        private ExceptionThrowingAggregate() {
            throw new MockException();
        }

        @Override
        protected void handle(DomainEventMessage event) {
        }

        @Override
        public Object getIdentifier() {
            throw new UnsupportedOperationException("Not implemented yet");
        }

        @Override
        protected Collection<EventSourcedEntity> getChildEntities() {
            return null;
        }
    }
}
