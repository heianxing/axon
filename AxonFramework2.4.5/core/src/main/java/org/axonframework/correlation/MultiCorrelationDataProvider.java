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

package org.axonframework.correlation;

import org.axonframework.domain.Message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CorrelationDataProvider that combines the data of multiple other correlation providers. When multiple instance
 * provide the same keys, a delegate will override the entries provided by previously resolved delegates.
 *
 * @param <T> The type of message this correlation handler can handle
 * @author Allard Buijze
 * @since 2.3
 */
public class MultiCorrelationDataProvider<T extends Message> implements CorrelationDataProvider<T> {

    private final List<? extends CorrelationDataProvider<? super T>> delegates;

    /**
     * Initialize the correlation data provider, delegating to given <code>correlationDataProviders</code>.
     *
     * @param correlationDataProviders the providers to delegate to.
     */
    public MultiCorrelationDataProvider(
            List<? extends CorrelationDataProvider<? super T>> correlationDataProviders) {
        delegates = new ArrayList<CorrelationDataProvider<? super T>>(correlationDataProviders);
    }

    @Override
    public Map<String, ?> correlationDataFor(T message) {
        Map<String, Object> correlationData = new HashMap<String, Object>();
        for (CorrelationDataProvider<? super T> delegate : delegates) {
            final Map<String, ?> extraData = delegate.correlationDataFor(message);
            if (extraData != null) {
                correlationData.putAll(extraData);
            }
        }
        return correlationData;
    }
}
