/*
 * Copyright (c) 2010-2014. Axon Framework
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

import org.axonframework.domain.DomainEventStream;

import java.util.Collection;

/**
 * EventStreamDecorator implementation that delegates to several other decorator instances.
 *
 * @author Allard Buijze
 * @since 2.2.1
 */
public class CompositeEventStreamDecorator implements EventStreamDecorator {

    private final EventStreamDecorator[] eventStreamDecorators;

    /**
     * Initialize the decorator, delegating to the given <code>eventStreamDecorators</code>. The decorators are
     * invoked in the iterator's order on {@link #decorateForRead(String, Object, org.axonframework.domain.DomainEventStream)},
     * and in revese order on {@link #decorateForAppend(String, EventSourcedAggregateRoot,
     * org.axonframework.domain.DomainEventStream)}.
     *
     * @param eventStreamDecorators The decorators to decorate Event Streams with
     */
    public CompositeEventStreamDecorator(Collection<EventStreamDecorator> eventStreamDecorators) {
        this.eventStreamDecorators = eventStreamDecorators.toArray(new EventStreamDecorator[eventStreamDecorators
                .size()]);
    }

    @Override
    public DomainEventStream decorateForRead(String aggregateType, Object aggregateIdentifier,
                                             DomainEventStream eventStream) {
        DomainEventStream events = eventStream;
        for (EventStreamDecorator decorator : eventStreamDecorators) {
            events = decorator.decorateForRead(aggregateType, aggregateIdentifier, events);
        }
        return events;
    }

    @Override
    public DomainEventStream decorateForAppend(String aggregateType, EventSourcedAggregateRoot aggregate,
                                               DomainEventStream eventStream) {
        DomainEventStream events = eventStream;
        for (int i = eventStreamDecorators.length - 1; i >= 0; i--) {
            events = eventStreamDecorators[i].decorateForAppend(aggregateType, aggregate, events);
        }
        return events;
    }
}
