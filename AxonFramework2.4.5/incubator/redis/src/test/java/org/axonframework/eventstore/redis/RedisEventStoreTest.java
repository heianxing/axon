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

package org.axonframework.eventstore.redis;

import org.apache.commons.pool.impl.GenericObjectPool;
import org.axonframework.domain.GenericDomainEventMessage;
import org.axonframework.domain.SimpleDomainEventStream;
import org.axonframework.integrationtests.commandhandling.StubDomainEvent;
import org.axonframework.serializer.xml.XStreamSerializer;
import org.junit.*;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.List;

/**
 * @author Allard Buijze
 */
public class RedisEventStoreTest {

    private RedisEventStore testSubject;
    private Jedis jedis;

    @Before
    public void setUp() {
        jedis = new Jedis("localhost");
        testSubject = new RedisEventStore();
        testSubject.setEventSerializer(new XStreamSerializer());
        testSubject.setRedisConnectionProvider(new RedisConnectionProvider() {
            @Override
            public Jedis newConnection() {
                return new Jedis("localhost");
            }

            @Override
            public void closeConnection(Jedis toClose) {
                toClose.disconnect();
            }
        });
        try {
            jedis.ping();
        } catch (Exception e) {
            Assume.assumeNoException(e);
        }
    }

    @Test
    public void testAppend() {
        String id = "blabla";
        String dbKey = "TEST." + id;
        jedis.del(dbKey);
        testSubject.appendEvents("TEST", new SimpleDomainEventStream(
                new GenericDomainEventMessage<StubDomainEvent>(id, 0, new StubDomainEvent()),
                new GenericDomainEventMessage<StubDomainEvent>(id, 1, new StubDomainEvent()),
                new GenericDomainEventMessage<StubDomainEvent>(id, 2, new StubDomainEvent()),
                new GenericDomainEventMessage<StubDomainEvent>(id, 3, new StubDomainEvent()),
                new GenericDomainEventMessage<StubDomainEvent>(id, 4, new StubDomainEvent()),
                new GenericDomainEventMessage<StubDomainEvent>(id, 5, new StubDomainEvent())));

        List<String> events = jedis.lrange(dbKey, 0, -1);
        for (String key : events) {
            System.out.println(key);
        }
    }

    private static class PooledRedisConnectionProvider implements RedisConnectionProvider {

        private JedisPool pool;

        private PooledRedisConnectionProvider() {
            GenericObjectPool.Config poolConfig = new GenericObjectPool.Config();
            poolConfig.maxActive = 100;
            pool = new JedisPool(poolConfig, "192.168.56.101");
        }

        @Override
        public Jedis newConnection() {
            return pool.getResource();
        }

        public void closeConnection(Jedis toClose) {
            pool.returnResource(toClose);
        }
    }
}
