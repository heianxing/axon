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

package org.axonframework.eventhandling;

import org.axonframework.eventhandling.Cluster;
import org.axonframework.eventhandling.DefaultClusterSelector;
import org.axonframework.eventhandling.EventListener;
import org.junit.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Allard Buijze
 */
public class DefaultClusterSelectorTest {

    private DefaultClusterSelector testSubject;

    @Before
    public void setUp() throws Exception {
        testSubject = new DefaultClusterSelector();
    }

    @Test
    public void testSameInstanceIsReturned() {
        Cluster cluster1 = testSubject.selectCluster(mock(EventListener.class));
        Cluster cluster2 = testSubject.selectCluster(mock(EventListener.class));
        Cluster cluster3 = testSubject.selectCluster(mock(EventListener.class));

        assertSame(cluster1, cluster2);
        assertSame(cluster2, cluster3);
    }

    @Test
    public void testProvidedInstanceIsReturned() {
        Cluster mock = mock(Cluster.class);
        testSubject = new DefaultClusterSelector(mock);
        assertSame(mock, testSubject.selectCluster(mock(EventListener.class)));
    }
}
