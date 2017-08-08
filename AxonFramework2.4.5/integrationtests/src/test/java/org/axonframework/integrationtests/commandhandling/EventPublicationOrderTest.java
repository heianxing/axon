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

package org.axonframework.integrationtests.commandhandling;

import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.SimpleCommandBus;
import org.axonframework.commandhandling.annotation.AnnotationCommandHandlerAdapter;
import org.axonframework.domain.DomainEventMessage;
import org.axonframework.domain.EventMessage;
import org.axonframework.domain.GenericDomainEventMessage;
import org.axonframework.domain.SimpleDomainEventStream;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.SimpleEventBus;
import org.axonframework.eventsourcing.EventSourcingRepository;
import org.axonframework.eventstore.EventStore;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.*;
import org.mockito.invocation.*;
import org.mockito.stubbing.*;

import java.util.UUID;

import static org.axonframework.commandhandling.GenericCommandMessage.asCommandMessage;
import static org.mockito.Mockito.*;

/**
 * @author Allard Buijze
 */
public class EventPublicationOrderTest {

    private CommandBus commandBus;
    private EventBus eventBus;
    private EventSourcingRepository<StubAggregate> repository;
    private EventStore eventStore;

    @Before
    public void setUp() {
        this.commandBus = new SimpleCommandBus();
        this.eventBus = spy(new SimpleEventBus());
        eventStore = mock(EventStore.class);
        this.repository = new EventSourcingRepository<StubAggregate>(StubAggregate.class, eventStore);
        repository.setEventBus(eventBus);
        StubAggregateCommandHandler target = new StubAggregateCommandHandler();
        target.setRepository(repository);
        target.setEventBus(eventBus);
        AnnotationCommandHandlerAdapter.subscribe(target, commandBus);
    }

    @Test
    public void testPublicationOrderIsMaintained_AggregateAdded() {
        UUID aggregateId = UUID.randomUUID();
        when(eventStore.readEvents("StubAggregate", aggregateId))
                .thenReturn(new SimpleDomainEventStream(
                        new GenericDomainEventMessage<Object>(aggregateId, 0,
                                                              new StubAggregateCreatedEvent(aggregateId))));
        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                System.out.println("Published event: " + invocation.getArguments()[0].toString());
                return Void.class;
            }
        }).when(eventBus).publish(isA(EventMessage.class));
        commandBus.dispatch(asCommandMessage(new UpdateStubAggregateWithExtraEventCommand(aggregateId)));
        verify(eventBus).publish(isA(DomainEventMessage.class),
                                 argThat(new NotADomainEventMatcher()),
                                 isA(DomainEventMessage.class));
    }

    private static class NotADomainEventMatcher extends BaseMatcher<EventMessage> {

        @Override
        public boolean matches(Object o) {
            return !(o instanceof DomainEventMessage);
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("Not a DomainEventMessage");
        }
    }
}
