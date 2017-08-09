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

package org.axonframework.contextsupport.spring;

import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.SimpleCommandBus;
import org.junit.*;
import org.junit.runner.*;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:contexts/axon-namespace-support-context.xml"})
public class SimpleCommandBusBeanDefinitionParserTest {

    @Autowired
    private DefaultListableBeanFactory beanFactory;

    @Test
    public void embeddedRefInterceptorDefinitionTest() {
        BeanDefinition commandBusEmbeddedRef = beanFactory.getBeanDefinition("commandBus-embedded-ref");
        assertNotNull("Bean definition not created correctly", commandBusEmbeddedRef);
        PropertyValue handlerInterceptors = commandBusEmbeddedRef.getPropertyValues().getPropertyValue("handlerInterceptors");
        assertNotNull("No definition for the interceptor", handlerInterceptors);
        ManagedList<?> list = (ManagedList<?>) handlerInterceptors.getValue();
        assertTrue(RuntimeBeanReference.class.isInstance(list.get(0)));
        RuntimeBeanReference beanReference = (RuntimeBeanReference) list.get(0);
        assertEquals("commandBusInterceptor", beanReference.getBeanName());

        PropertyValue dispatchInterceptors = commandBusEmbeddedRef.getPropertyValues().getPropertyValue("dispatchInterceptors");
        assertNotNull("No definition for the interceptor", dispatchInterceptors);
        list = (ManagedList<?>) dispatchInterceptors.getValue();
        assertTrue(RuntimeBeanReference.class.isInstance(list.get(0)));
        beanReference = (RuntimeBeanReference) list.get(0);
        assertEquals("dispatchInterceptor", beanReference.getBeanName());

        CommandBus commandBus = beanFactory.getBean("commandBus-embedded-ref", CommandBus.class);
        assertNotNull(commandBus);
    }

    @Test
    public void embeddedInterceptorBeanInterceptorDefinitionTest() {
        BeanDefinition commandBusEmbeddedBean = beanFactory.getBeanDefinition("commandBus-embedded-interceptor-bean");
        assertNotNull("Bean definition not created correctly", commandBusEmbeddedBean);
        PropertyValue handlerInterceptors = commandBusEmbeddedBean.getPropertyValues().getPropertyValue("handlerInterceptors");
        assertNotNull("No definition for the interceptor", handlerInterceptors);
        ManagedList<?> list = (ManagedList<?>) handlerInterceptors.getValue();
        assertTrue(BeanDefinitionHolder.class.isInstance(list.get(0)));

        PropertyValue dispatchInterceptors = commandBusEmbeddedBean.getPropertyValues().getPropertyValue("dispatchInterceptors");
        assertNotNull("No definition for the interceptor", dispatchInterceptors);
        list = (ManagedList<?>) handlerInterceptors.getValue();
        assertTrue(BeanDefinitionHolder.class.isInstance(list.get(0)));

        CommandBus commandBus = beanFactory.getBean("commandBus-embedded-interceptor-bean", CommandBus.class);
        assertNotNull(commandBus);
    }

    @Test
    public void commandBusElementTrueMBeans() {
        BeanDefinition beanDefinition = beanFactory.getBeanDefinition("commandBus-simple");
        assertNotNull("Bean definition not created", beanDefinition);
        assertEquals("Wrong bean class", SimpleCommandBus.class.getName(), beanDefinition.getBeanClassName());
        assertEquals("wrong amount of constructor arguments"
                , 0, beanDefinition.getConstructorArgumentValues().getArgumentCount());
        SimpleCommandBus commandBus = beanFactory.getBean("commandBus-simple", SimpleCommandBus.class);
        assertNotNull(commandBus);
    }
}
