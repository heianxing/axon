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

package org.axonframework.common;

/**
 * Interface describing components that are able to subscribe themselves to a component that can be subscribed to, such
 * as the CommandBus and EventBus.
 *
 * @author Allard Buijze
 * @since 0.5
 * @deprecated as of 2.1. This interface will be removed in future versions. Self subscribing components give problems
 *             when wrapping components (using compositions or proxies). Therefore, subscriptions should be managed by
 *             the creating component.
 */
@Deprecated
public interface Subscribable {

    /**
     * Unsubscribe this instance from its subscribed component.
     */
    void unsubscribe();

    /**
     * Subscribe this instance with its configured component.
     */
    void subscribe();
}
