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

package org.axonframework.unitofwork;

import org.axonframework.domain.AggregateRoot;

/**
 * Callback used by UnitOfWork instances to be invoked when the UnitOfWork wishes to store an aggregate. This callback
 * abstracts the actual storage mechanism away from the UnitOfWork itself.
 *
 * @param <T> The type of aggregate this callback handles
 * @author Allard Buijze
 * @since 0.6
 */
public interface SaveAggregateCallback<T extends AggregateRoot> {

    /**
     * Invoked when the UnitOfWork wishes to store an aggregate.
     *
     * @param aggregate The aggregate to store
     */
    void save(T aggregate);
}
