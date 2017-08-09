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

import org.axonframework.common.Assert;
import org.axonframework.common.annotation.ParameterResolverFactory;
import org.axonframework.domain.DomainEventMessage;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import static java.lang.String.format;
import static org.axonframework.common.ReflectionUtils.ensureAccessible;

/**
 * Aggregate factory that uses a convention to create instances of aggregates. The type must declare a no-arg
 * constructor accepting.
 * <p/>
 * If the constructor is not accessible (not public), and the JVM's security setting allow it, the
 * GenericAggregateFactory will try to make it accessible. If that doesn't succeed, an exception is thrown.
 *
 * @param <T> The type of aggregate this factory creates
 * @author Allard Buijze
 * @since 0.7
 */
public class GenericAggregateFactory<T extends EventSourcedAggregateRoot> extends AbstractAggregateFactory<T> {

    private final String typeIdentifier;
    private final Class<T> aggregateType;
    private final Constructor<T> constructor;

    /**
     * Initialize the AggregateFactory for creating instances of the given <code>aggregateType</code>.
     *
     * @param aggregateType The type of aggregate this factory creates instances of.
     * @throws IncompatibleAggregateException if the aggregate constructor throws an exception, or if the JVM security
     * settings prevent the GenericAggregateFactory from calling the
     * constructor.
     */
    public GenericAggregateFactory(Class<T> aggregateType) {
        this(aggregateType, null);
    }

    /**
     * Initialize the AggregateFactory for creating instances of the given <code>aggregateType</code> and using the
     * given <code>parameterResolverFactory</code> to resolve parameters of annotated event handler methods.
     * <p/>
     * Note that the <code>parameterResolverFactory</code> is only used if the aggregate is an instance of {@code
     * org.axonframework.eventsourcing.annotation.AbstractAnnotatedAggregateRoot}. In other cases, this parameter is
     * ignored
     *
     * @param aggregateType            The type of aggregate this factory creates instances of.
     * @param parameterResolverFactory THe factory that resolves parameters of annotated event handlers
     * @throws IncompatibleAggregateException if the aggregate constructor throws an exception, or if the JVM security
     * settings prevent the GenericAggregateFactory from calling the
     * constructor.
     */
    public GenericAggregateFactory(Class<T> aggregateType, ParameterResolverFactory parameterResolverFactory) {
        super(parameterResolverFactory);
        Assert.isTrue(EventSourcedAggregateRoot.class.isAssignableFrom(aggregateType),
                      "The given aggregateType must be a subtype of EventSourcedAggregateRoot");
        Assert.isFalse(Modifier.isAbstract(aggregateType.getModifiers()), "Given aggregateType may not be abstract");
        this.aggregateType = aggregateType;
        this.typeIdentifier = aggregateType.getSimpleName();
        try {
            this.constructor = ensureAccessible(aggregateType.getDeclaredConstructor());
        } catch (NoSuchMethodException e) {
            throw new IncompatibleAggregateException(format("The aggregate [%s] doesn't provide a no-arg constructor.",
                                                            aggregateType.getSimpleName()), e);
        }
    }

    /**
     * {@inheritDoc}
     * <p/>
     *
     * @throws IncompatibleAggregateException if the aggregate constructor throws an exception, or if the JVM security
     * settings prevent the GenericAggregateFactory from calling the
     * constructor.
     */
    @SuppressWarnings({"unchecked"})
    @Override
    protected T doCreateAggregate(Object aggregateIdentifier, DomainEventMessage firstEvent) {
        try {
            return constructor.newInstance();
        } catch (InstantiationException e) {
            throw new IncompatibleAggregateException(format(
                    "The aggregate [%s] does not have a suitable no-arg constructor.",
                    aggregateType.getSimpleName()), e);
        } catch (IllegalAccessException e) {
            throw new IncompatibleAggregateException(format(
                    "The aggregate no-arg constructor of the aggregate [%s] is not accessible. Please ensure that "
                            + "the constructor is public or that the Security Manager allows access through "
                            + "reflection.", aggregateType.getSimpleName()), e);
        } catch (InvocationTargetException e) {
            throw new IncompatibleAggregateException(format(
                    "The no-arg constructor of [%s] threw an exception on invocation.",
                    aggregateType.getSimpleName()), e);
        }
    }

    @Override
    public String getTypeIdentifier() {
        return typeIdentifier;
    }

    @Override
    public Class<T> getAggregateType() {
        return aggregateType;
    }
}
