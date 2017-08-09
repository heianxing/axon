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

package org.axonframework.commandhandling.disruptor;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.commandhandling.GenericCommandMessage;
import org.axonframework.commandhandling.annotation.TargetAggregateIdentifier;
import org.axonframework.domain.DomainEventMessage;
import org.axonframework.domain.DomainEventStream;
import org.axonframework.domain.EventMessage;
import org.axonframework.domain.GenericDomainEventMessage;
import org.axonframework.domain.SimpleDomainEventStream;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.EventListener;
import org.axonframework.eventsourcing.AbstractEventSourcedAggregateRoot;
import org.axonframework.eventsourcing.EventSourcedEntity;
import org.axonframework.eventsourcing.GenericAggregateFactory;
import org.axonframework.eventstore.EventStore;
import org.axonframework.repository.Repository;
import org.axonframework.unitofwork.UnitOfWork;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * @author Allard Buijze
 */
public class DisruptorCommandBusBenchmark {

    private static final int COMMAND_COUNT = 50 * 1000 * 1000;

    public static void main(String[] args) throws InterruptedException {
        CountingEventBus eventBus = new CountingEventBus();
        StubHandler stubHandler = new StubHandler();
        InMemoryEventStore inMemoryEventStore = new InMemoryEventStore();
        DisruptorCommandBus commandBus = new DisruptorCommandBus(inMemoryEventStore, eventBus);
        commandBus.subscribe(StubCommand.class.getName(), stubHandler);
        stubHandler.setRepository(commandBus.createRepository(new GenericAggregateFactory<StubAggregate>(StubAggregate.class)));
        final String aggregateIdentifier = "MyID";
        inMemoryEventStore.appendEvents(StubAggregate.class.getSimpleName(), new SimpleDomainEventStream(
                new GenericDomainEventMessage<StubDomainEvent>(aggregateIdentifier, 0, new StubDomainEvent())));

        long start = System.currentTimeMillis();
        for (int i = 0; i < COMMAND_COUNT; i++) {
            CommandMessage<StubCommand> command = new GenericCommandMessage<StubCommand>(
                    new StubCommand(aggregateIdentifier));
            commandBus.dispatch(command);
        }
        System.out.println("Finished dispatching!");

        inMemoryEventStore.countDownLatch.await(5, TimeUnit.SECONDS);
        long end = System.currentTimeMillis();
        try {
            assertEquals("Seems that some events are not published", 0, eventBus.publisherCountDown.getCount());
            assertEquals("Seems that some events are not stored", 0, inMemoryEventStore.countDownLatch.getCount());
            System.out.println("Did " + ((COMMAND_COUNT * 1000L) / (end - start)) + " commands per second");
        } finally {
            commandBus.stop();
        }
    }

    private static class StubAggregate extends AbstractEventSourcedAggregateRoot {

        private String identifier;

        @Override
        public Object getIdentifier() {
            return identifier;
        }

        public void doSomething() {
            apply(new SomethingDoneEvent());
        }

        @Override
        protected void handle(DomainEventMessage event) {
            identifier = (String) event.getAggregateIdentifier();
        }

        @Override
        protected Collection<EventSourcedEntity> getChildEntities() {
            return Collections.emptyList();
        }
    }

    private static class InMemoryEventStore implements EventStore {

        private final Map<String, DomainEventMessage> storedEvents = new HashMap<String, DomainEventMessage>();
        private final CountDownLatch countDownLatch = new CountDownLatch((int) (COMMAND_COUNT + 1L));

        @Override
        public void appendEvents(String type, DomainEventStream events) {
            if (!events.hasNext()) {
                return;
            }
            String key = events.peek().getAggregateIdentifier().toString();
            DomainEventMessage<?> lastEvent = null;
            while (events.hasNext()) {
                countDownLatch.countDown();
                lastEvent = events.next();
            }
            storedEvents.put(key, lastEvent);
        }

        @Override
        public DomainEventStream readEvents(String type, Object identifier) {
            return new SimpleDomainEventStream(Collections.singletonList(storedEvents.get(identifier.toString())));
        }
    }

    private static class StubCommand {

        @TargetAggregateIdentifier
        private Object aggregateIdentifier;

        public StubCommand(Object aggregateIdentifier) {
            this.aggregateIdentifier = aggregateIdentifier;
        }

        public Object getAggregateIdentifier() {
            return aggregateIdentifier;
        }
    }

    private static class StubHandler implements CommandHandler<StubCommand> {

        private Repository<StubAggregate> repository;

        private StubHandler() {
        }

        @Override
        public Object handle(CommandMessage<StubCommand> command, UnitOfWork unitOfWork) throws Throwable {
            repository.load(command.getPayload().getAggregateIdentifier()).doSomething();
            return null;
        }

        public void setRepository(Repository<StubAggregate> repository) {
            this.repository = repository;
        }
    }

    private static class StubDomainEvent {

    }

    private static class CountingEventBus implements EventBus {

        private final CountDownLatch publisherCountDown = new CountDownLatch(COMMAND_COUNT);

        @Override
        public void publish(EventMessage... events) {
            publisherCountDown.countDown();
        }

        @Override
        public void subscribe(EventListener eventListener) {
            throw new UnsupportedOperationException("Not implemented yet");
        }

        @Override
        public void unsubscribe(EventListener eventListener) {
            throw new UnsupportedOperationException("Not implemented yet");
        }
    }
}
