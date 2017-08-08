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

package org.axonframework.commandhandling.interceptors;

import org.axonframework.commandhandling.CommandHandlerInterceptor;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.commandhandling.InterceptorChain;
import org.axonframework.domain.DomainEventMessage;
import org.axonframework.domain.EventMessage;
import org.axonframework.serializer.SerializationAwareDomainEventMessage;
import org.axonframework.serializer.SerializationAwareEventMessage;
import org.axonframework.unitofwork.UnitOfWork;
import org.axonframework.unitofwork.UnitOfWorkListenerAdapter;

/**
 * Interceptor that register a unit of work listener that wraps each EventMessage in a SerializationAware message. This
 * allows for performance optimizations in cases where storage (in the event store) and publication (on the event bus)
 * use the same serialization mechanism.
 *
 * @author Allard Buijze
 * @since 2.0
 */
public class SerializationOptimizingInterceptor implements CommandHandlerInterceptor {

    private final SerializationOptimizingListener listener = new SerializationOptimizingListener();

    @Override
    public Object handle(CommandMessage<?> commandMessage, UnitOfWork unitOfWork,
                         InterceptorChain interceptorChain)
            throws Throwable {
        unitOfWork.registerListener(listener);
        return interceptorChain.proceed();
    }

    private static final class SerializationOptimizingListener extends UnitOfWorkListenerAdapter {

        @Override
        public <T> EventMessage<T> onEventRegistered(UnitOfWork unitOfWork, EventMessage<T> event) {
            if (event instanceof DomainEventMessage) {
                return SerializationAwareDomainEventMessage.wrap((DomainEventMessage<T>) event);
            } else {
                return SerializationAwareEventMessage.wrap(event);
            }
        }
    }
}
