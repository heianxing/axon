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

package org.axonframework.test;

import org.axonframework.commandhandling.annotation.CommandHandler;
import org.axonframework.domain.GenericEventMessage;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.repository.Repository;
import org.axonframework.unitofwork.CurrentUnitOfWork;

/**
 * @author Allard Buijze
 */
class MyCommandHandler {

    private Repository<StandardAggregate> repository;
    private EventBus eventBus;

    MyCommandHandler(Repository<StandardAggregate> repository, EventBus eventBus) {
        this.repository = repository;
        this.eventBus = eventBus;
    }

    MyCommandHandler() {
    }

    @CommandHandler
    public void createAggregate(CreateAggregateCommand command) {
        repository.add(new StandardAggregate(0, command.getAggregateIdentifier()));
    }

    @CommandHandler
    public void handleTestCommand(TestCommand testCommand) {
        StandardAggregate aggregate = repository.load(testCommand.getAggregateIdentifier(), null);
        aggregate.doSomething();
    }

    @CommandHandler
    public void handleStrangeCommand(StrangeCommand testCommand) {
        StandardAggregate aggregate = repository.load(testCommand.getAggregateIdentifier(), null);
        aggregate.doSomething();
        eventBus.publish(new GenericEventMessage<MyApplicationEvent>(new MyApplicationEvent()));
        CurrentUnitOfWork.get().publishEvent(new GenericEventMessage<MyApplicationEvent>(new MyApplicationEvent()),
                                             eventBus);
        throw new StrangeCommandReceivedException("Strange command received");
    }

    @CommandHandler
    public void handleIllegalStateChange(IllegalStateChangeCommand command) {
        StandardAggregate aggregate = repository.load(command.getAggregateIdentifier());
        aggregate.doSomethingIllegal(command.getNewIllegalValue());
    }

    @CommandHandler
    public void handleDeleteAggregate(DeleteCommand command) {
        repository.load(command.getAggregateIdentifier()).delete(command.isAsIllegalChange());
    }

    public void setRepository(Repository<StandardAggregate> repository) {
        this.repository = repository;
    }
}
