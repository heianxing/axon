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

package org.axonframework.integrationtests.loopbacktest;

import org.axonframework.eventsourcing.annotation.AbstractAnnotatedAggregateRoot;
import org.axonframework.eventsourcing.annotation.AggregateIdentifier;

import java.util.UUID;

/**
 * @author Allard Buijze
 */
public class Message extends AbstractAnnotatedAggregateRoot<UUID> {

    @AggregateIdentifier
    private UUID identifier;

    public Message(String messageContents) {
        identifier = UUID.randomUUID();
        apply(new MessageCreatedEvent(messageContents));
    }

    public Message() {
    }
}