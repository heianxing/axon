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

package org.axonframework.commandhandling.annotation;

import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.GenericCommandMessage;
import org.axonframework.commandhandling.NoHandlerForCommandException;
import org.axonframework.common.annotation.ClasspathParameterResolverFactory;
import org.axonframework.common.annotation.ParameterResolverFactory;
import org.axonframework.unitofwork.CurrentUnitOfWork;
import org.axonframework.unitofwork.UnitOfWork;
import org.junit.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Allard Buijze
 */
public class AnnotationCommandHandlerAdapterTest {

    private AnnotationCommandHandlerAdapter testSubject;
    private CommandBus mockBus;
    private MyCommandHandler mockTarget;
    private UnitOfWork mockUnitOfWork;
    private ParameterResolverFactory parameterResolverFactory;

    @Before
    public void setUp() {
        mockBus = mock(CommandBus.class);
        mockTarget = new MyCommandHandler();
        parameterResolverFactory = ClasspathParameterResolverFactory.forClass(getClass());
        testSubject = new AnnotationCommandHandlerAdapter(mockTarget, parameterResolverFactory);
        mockUnitOfWork = mock(UnitOfWork.class);
        CurrentUnitOfWork.set(mockUnitOfWork);
    }

    @After
    public void tearDown() {
        CurrentUnitOfWork.clear(mockUnitOfWork);
    }

    @Test
    public void testHandlerDispatching_VoidReturnType() throws Throwable {
        Object actualReturnValue = testSubject.handle(GenericCommandMessage.asCommandMessage(""), mockUnitOfWork);
        assertEquals(null, actualReturnValue);
        assertEquals(1, mockTarget.voidHandlerInvoked);
        assertEquals(0, mockTarget.returningHandlerInvoked);

        verify(mockUnitOfWork).attachResource(ParameterResolverFactory.class.getName(), parameterResolverFactory);
    }

    @Test
    public void testHandlerDispatching_WithReturnType() throws Throwable {
        Object actualReturnValue = testSubject.handle(GenericCommandMessage.asCommandMessage(1L), mockUnitOfWork);
        assertEquals(1L, actualReturnValue);
        assertEquals(0, mockTarget.voidHandlerInvoked);
        assertEquals(1, mockTarget.returningHandlerInvoked);

        verify(mockUnitOfWork).attachResource(ParameterResolverFactory.class.getName(), parameterResolverFactory);
    }

    @Test
    public void testHandlerDispatching_WithCustomCommandName() throws Throwable {
        Object actualReturnValue = testSubject.handle(new GenericCommandMessage("almostLong", 1L, null), mockUnitOfWork);
        assertEquals(1L, actualReturnValue);
        assertEquals(0, mockTarget.voidHandlerInvoked);
        assertEquals(0, mockTarget.returningHandlerInvoked);
        assertEquals(1, mockTarget.almostDuplicateReturningHandlerInvoked);

        verify(mockUnitOfWork).attachResource(ParameterResolverFactory.class.getName(), parameterResolverFactory);
    }

    @Test
    public void testHandlerDispatching_ThrowingException() throws Throwable {
        try {
            testSubject.handle(GenericCommandMessage.asCommandMessage(new HashSet()), mockUnitOfWork);
            fail("Expected exception");
        } catch (Exception ex) {
            assertEquals(Exception.class, ex.getClass());
        }
        verify(mockUnitOfWork).attachResource(ParameterResolverFactory.class.getName(), parameterResolverFactory);
    }

    @Test
    public void testSubscribe() {
        AnnotationCommandHandlerAdapter.subscribe(testSubject, mockBus);

        verify(mockBus).subscribe(Long.class.getName(), testSubject);
        verify(mockBus).subscribe(String.class.getName(), testSubject);
        verify(mockBus).subscribe(HashSet.class.getName(), testSubject);
        verify(mockBus).subscribe(ArrayList.class.getName(), testSubject);
        verify(mockBus).subscribe("almostLong", testSubject);
        verifyNoMoreInteractions(mockBus);
    }

    @Deprecated
    @Test
    public void testSelfSubscribe() {
        testSubject = new AnnotationCommandHandlerAdapter(mockTarget, mockBus);
        testSubject.subscribe();

        verify(mockBus).subscribe(Long.class.getName(), testSubject);
        verify(mockBus).subscribe(String.class.getName(), testSubject);
        verify(mockBus).subscribe(HashSet.class.getName(), testSubject);
        verify(mockBus).subscribe(ArrayList.class.getName(), testSubject);
        verify(mockBus).subscribe("almostLong", testSubject);
        verifyNoMoreInteractions(mockBus);
    }

    @Test(expected = NoHandlerForCommandException.class)
    public void testHandle_NoHandlerForCommand() throws Throwable {
        testSubject.handle(GenericCommandMessage.asCommandMessage(new LinkedList()), null);
        verify(mockUnitOfWork, never()).attachResource(ParameterResolverFactory.class.getName(),
                                                       parameterResolverFactory);
    }

    private static class MyCommandHandler {

        private int voidHandlerInvoked;
        private int returningHandlerInvoked;
        private int almostDuplicateReturningHandlerInvoked;

        @SuppressWarnings({"UnusedDeclaration"})
        @CommandHandler
        public void myVoidHandler(String stringCommand, UnitOfWork unitOfWork) {
            voidHandlerInvoked++;
        }

        @CommandHandler(commandName = "almostLong")
        public Long myAlmostDuplicateReturningHandler(Long longCommand, UnitOfWork unitOfWork) {
            assertNotNull("The UnitOfWork was not passed to the command handler", unitOfWork);
            almostDuplicateReturningHandlerInvoked++;
            return longCommand;
        }

        @CommandHandler
        public Long myReturningHandler(Long longCommand, UnitOfWork unitOfWork) {
            assertNotNull("The UnitOfWork was not passed to the command handler", unitOfWork);
            returningHandlerInvoked++;
            return longCommand;
        }

        @SuppressWarnings({"UnusedDeclaration"})
        @CommandHandler
        public void exceptionThrowingHandler(HashSet o) throws Exception {
            throw new Exception("Some exception");
        }

        @SuppressWarnings({"UnusedDeclaration"})
        @CommandHandler
        public void exceptionThrowingHandler(ArrayList o) throws Exception {
            throw new RuntimeException("Some exception");
        }
    }
}
