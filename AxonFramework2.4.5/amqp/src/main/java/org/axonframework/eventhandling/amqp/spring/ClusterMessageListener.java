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

package org.axonframework.eventhandling.amqp.spring;

import org.axonframework.domain.EventMessage;
import org.axonframework.eventhandling.Cluster;
import org.axonframework.eventhandling.amqp.AMQPMessageConverter;
import org.axonframework.serializer.UnknownSerializedTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * MessageListener implementation that deserializes incoming messages and forwards them to one or more clusters. The
 * <code>byte[]</code> making up the message payload must the format as used by the {@link SpringAMQPTerminal}.
 *
 * @author Allard Buijze
 * @since 2.0
 */
public class ClusterMessageListener implements MessageListener {

    private static final Logger logger = LoggerFactory.getLogger(ClusterMessageListener.class);

    private final List<Cluster> clusters = new CopyOnWriteArrayList<Cluster>();
    private final AMQPMessageConverter messageConverter;

    /**
     * Initializes a ClusterMessageListener with given <code>initialCluster</code> that uses given
     * <code>serializer</code> to deserialize the message's contents into an EventMessage.
     *
     * @param initialCluster   The first cluster to assign to the listener
     * @param messageConverter The message converter to use to convert AMQP Messages to Event Messages
     */
    public ClusterMessageListener(Cluster initialCluster, AMQPMessageConverter messageConverter) {
        this.messageConverter = messageConverter;
        this.clusters.add(initialCluster);
    }

    @Override
    public void onMessage(Message message) {
        try {
            EventMessage eventMessage = messageConverter.readAMQPMessage(message.getBody(),
                                                                         message.getMessageProperties().getHeaders());
            if (eventMessage != null) {
                for (Cluster cluster : clusters) {
                    cluster.publish(eventMessage);
                }
            }
        } catch (UnknownSerializedTypeException e) {
            logger.warn("Unable to deserialize an incoming message. Ignoring it. {}", e.toString());
        }
    }

    /**
     * Registers an additional cluster. This cluster will receive messages once registered.
     *
     * @param cluster the cluster to add to the listener
     */
    public void addCluster(Cluster cluster) {
        clusters.add(cluster);
    }
}
