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

import org.axonframework.eventsourcing.annotation.AbstractAnnotatedAggregateRoot;
import org.axonframework.eventsourcing.annotation.AggregateIdentifier;
import org.axonframework.eventsourcing.annotation.EventSourcingHandler;

/**
 * @author Allard Buijze
 */
public class StubAggregate extends AbstractAnnotatedAggregateRoot {

    private int changeCounter;

    @AggregateIdentifier
    private Object identifier;

    public StubAggregate(Object aggregateId) {
        apply(new StubAggregateCreatedEvent(aggregateId));
    }

    StubAggregate() {
    }

    public void makeAChange() {
        apply(new StubAggregateChangedEvent());
    }

    public void causeTrouble() {
        throw new RuntimeException("That's problematic");
    }

    @Override
    public void markDeleted() {
        super.markDeleted();
    }

    @EventSourcingHandler
    private void onCreated(StubAggregateCreatedEvent event) {
        this.identifier = event.getAggregateIdentifier();
        changeCounter = 0;
    }

    @EventSourcingHandler
    private void onChange(StubAggregateChangedEvent event) {
        changeCounter++;
    }

    public void makeALoopingChange() {
        apply(new LoopingChangeDoneEvent(getIdentifier()));
    }
}
