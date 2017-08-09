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

package org.axonframework.auditing;

import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.commandhandling.GenericCommandMessage;
import org.junit.*;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author Allard Buijze
 */
public class CorrelationAuditDataProviderTest {

    @Test
    public void testDefaultName() throws Exception {
        CommandMessage<?> command = new GenericCommandMessage<Object>("Mock");
        Map<String, Object> actual = new CorrelationAuditDataProvider().provideAuditDataFor(command);

        assertEquals(1, actual.size());
        assertEquals(command.getIdentifier(), actual.get("command-identifier"));
    }

    @Test
    public void testCustomName() throws Exception {
        CommandMessage<?> command = new GenericCommandMessage<Object>("Mock");
        Map<String, Object> actual = new CorrelationAuditDataProvider("bla").provideAuditDataFor(command);

        assertEquals(1, actual.size());
        assertEquals(command.getIdentifier(), actual.get("bla"));
    }
}
