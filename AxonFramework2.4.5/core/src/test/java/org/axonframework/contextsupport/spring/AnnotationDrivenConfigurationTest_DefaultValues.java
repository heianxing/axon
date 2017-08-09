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

package org.axonframework.contextsupport.spring;

import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.SimpleCommandBus;
import org.axonframework.commandhandling.annotation.CommandHandler;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.EventListener;
import org.axonframework.eventhandling.SimpleEventBus;
import org.axonframework.eventhandling.annotation.EventHandler;
import org.junit.*;
import org.junit.runner.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

/**
 * @author Allard Buijze
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AnnotationDrivenConfigurationTest_DefaultValues.Context.class)
public class AnnotationDrivenConfigurationTest_DefaultValues {

    @Autowired
    private ApplicationContext applicationContext;


    @Test
    public void testAnnotationConfigurationAnnotationWrapsBeans() throws Exception {
        Object eventHandler = applicationContext.getBean("eventHandler");
        Object commandHandler = applicationContext.getBean("commandHandler");


        assertTrue(eventHandler instanceof EventListener);
        assertTrue(commandHandler instanceof org.axonframework.commandhandling.CommandHandler);
    }

    @AnnotationDriven()
    @Configuration
    public static class Context {

        @Bean
        public AnnotatedEventHandler eventHandler() {
            return new AnnotatedEventHandler();
        }

        @Bean
        public AnnotatedCommandHandler commandHandler() {
            return new AnnotatedCommandHandler();
        }

        @Bean
        public CommandBus commandBus() {
            return new SimpleCommandBus();
        }

        @Bean
        public EventBus eventBus() {
            return new SimpleEventBus();
        }
    }

    public static class AnnotatedEventHandler {

        @EventHandler
        public void on(String someEvent) {
        }
    }

    public static class AnnotatedCommandHandler {

        @CommandHandler
        public void on(String someEvent) {
        }
    }
}
