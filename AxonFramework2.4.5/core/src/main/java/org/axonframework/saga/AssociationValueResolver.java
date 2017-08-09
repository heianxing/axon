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

package org.axonframework.saga;

import org.axonframework.domain.EventMessage;

import java.util.Set;

/**
 * Interface describing the mechanism that resolves Association Values from events. The Association Values are used to
 * find Saga's potentially interested in this Event.
 *
 * @author Allard Buijze
 * @since 0.7
 */
public interface AssociationValueResolver {

    /**
     * Extracts an Association Value from the given <code>event</code>.
     *
     * @param event The event to extract Association Value from
     * @return The Association Value extracted from the Event, or <code>null</code> if none found.
     */
    Set<AssociationValue> extractAssociationValues(EventMessage event);
}
