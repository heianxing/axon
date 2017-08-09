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

import org.axonframework.commandhandling.annotation.CommandHandler;
import org.axonframework.domain.GenericEventMessage;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.repository.Repository;
import org.axonframework.unitofwork.UnitOfWork;

/**
 * @author Allard Buijze
 */
public class StubAggregateCommandHandler {

    private Repository<StubAggregate> repository;
    private EventBus eventBus;

    @CommandHandler
    public void handleStubAggregateCreated(CreateStubAggregateCommand command) {
        repository.add(new StubAggregate(command.getAggregateId()));
    }

    @CommandHandler
    public void handleStubAggregateUpdated(UpdateStubAggregateCommand command) {
        StubAggregate aggregate = repository.load(command.getAggregateId(), command.getAggregateVersion());
        aggregate.makeAChange();
    }

    @CommandHandler
    public void handleStubAggregateUpdatedWithExtraEvent(UpdateStubAggregateWithExtraEventCommand command,
                                                         UnitOfWork unitOfWork) {
        StubAggregate aggregate = repository.load(command.getAggregateId());
        aggregate.makeAChange();
        unitOfWork.publishEvent(new GenericEventMessage<MyEvent>(new MyEvent()), eventBus);
        aggregate.makeAChange();
    }

    @CommandHandler
    public void handleStubAggregateLooping(LoopingCommand command) {
        StubAggregate aggregate = repository.load(command.getAggregateId());
        aggregate.makeALoopingChange();
    }

    @CommandHandler
    public void handleProblematicCommand(ProblematicCommand command) {
        StubAggregate aggregate = repository.load(command.getAggregateId(), command.getAggregateVersion());
        aggregate.causeTrouble();
    }

    public void setRepository(Repository<StubAggregate> repository) {
        this.repository = repository;
    }

    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }
}
