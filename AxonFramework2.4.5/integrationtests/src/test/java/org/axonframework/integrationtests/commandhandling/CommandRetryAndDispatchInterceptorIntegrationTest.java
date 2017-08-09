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

package org.axonframework.integrationtests.commandhandling;

import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.CommandDispatchInterceptor;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.commandhandling.SimpleCommandBus;
import org.axonframework.commandhandling.gateway.AbstractCommandGateway;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.commandhandling.gateway.DefaultCommandGateway;
import org.axonframework.commandhandling.gateway.IntervalRetryScheduler;
import org.axonframework.commandhandling.gateway.RetryScheduler;
import org.axonframework.commandhandling.gateway.RetryingCallback;
import org.axonframework.domain.MetaData;
import org.axonframework.repository.ConcurrencyException;
import org.axonframework.unitofwork.CurrentUnitOfWork;
import org.axonframework.unitofwork.UnitOfWork;
import org.junit.*;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static junit.framework.Assert.assertEquals;

/**
 * @author <a href="https://github.com/davispw">Peter Davis</a>
 */
public class CommandRetryAndDispatchInterceptorIntegrationTest {

    private SimpleCommandBus commandBus;
    private CommandGateway commandGateway;
	private ScheduledExecutorService scheduledThreadPool;
	private IntervalRetryScheduler retryScheduler;

    @Before
    public void setUp() {
        this.commandBus = new SimpleCommandBus();
        scheduledThreadPool = Executors.newScheduledThreadPool(1);
        retryScheduler = new IntervalRetryScheduler(scheduledThreadPool, 0, 1);
    }

    @After
    public void tearDown() throws Exception {
    	scheduledThreadPool.shutdownNow();
        while (CurrentUnitOfWork.isStarted()) {
            CurrentUnitOfWork.get().rollback();
        }
    }

    /**
	 * Tests that exceptions thrown by dispatch interceptors on another thread
	 * are handled properly.
	 * <p>
	 * Documentation states, <blockquote> Exceptions have the following effect:<br>
	 * Any declared checked exception will be thrown if the Command Handler (or
	 * an interceptor) threw an exceptions of that type. If a checked exception
	 * is thrown that has not been declared, it is wrapped in a
	 * CommandExecutionException, which is a RuntimeException.<br>
	 * &hellip; </blockquote>
	 */
    @Test(expected=
    		SecurityException.class, // per documentation, an unchecked exception (theoretically
    			// the only kind throwable by an interceptor) is returned unwrapped
    		timeout=10000) // bug is that the caller waits forever for a CommandCallback.onFailure that never comes...
    public void testCommandDipatchInterceptorExceptionOnRetryThreadIsThrownToCaller() {
		commandGateway = new DefaultCommandGateway(commandBus, retryScheduler);

		// trigger retry
        commandBus.subscribe(String.class.getName(), new CommandHandler<String>() {
            @Override
            public Object handle(CommandMessage<String> commandMessage, UnitOfWork unitOfWork) throws Throwable {
                throw new ConcurrencyException("some retryable exception");
            }
        });

        // say we have a dispatch interceptor that expects to get the user's session from a ThreadLocal...
        // yes, this should be configured on the gateway instead of the command bus, but still...
        final Thread testThread = Thread.currentThread();
        commandBus.setDispatchInterceptors(Collections.singletonList(new CommandDispatchInterceptor() {
			@Override
			public CommandMessage<?> handle(CommandMessage<?> commandMessage) {
				if (Thread.currentThread() == testThread) {
					return commandMessage; // ok
				} else {
					// also, nothing is logged!
					LoggerFactory.getLogger(getClass()).info("throwing exception from dispatcher...");
					throw new SecurityException("test dispatch interceptor exception");
				}
			}
		}));

        // wait, but hopefully not forever...
        commandGateway.sendAndWait("command");
    }
    
    /**
	 * Tests that metadata added by a
	 * {@linkplain AbstractCommandGateway#AbstractCommandGateway(CommandBus,RetryScheduler,List)
	 * command gateway's dispatch interceptors} is preserved on retry.
	 * <p>
	 * It'd be nice if metadata added by a
	 * {@linkplain SimpleCommandBus#setDispatchInterceptors(List) command bus's
	 * dispatch interceptors} could be preserved, too, but that doesn't seem to
	 * be possible given how {@link RetryingCallback} works, so verify that it
	 * is not preserved.
	 */
    @Test(timeout=10000)
    public void testCommandGatewayDispatchInterceptorMetaDataIsPreservedOnRetry() {
    	final Thread testThread = Thread.currentThread();
		commandGateway = new DefaultCommandGateway(commandBus, retryScheduler, new CommandDispatchInterceptor() {
			@Override
			public CommandMessage<?> handle(CommandMessage<?> commandMessage) {
				if (Thread.currentThread() == testThread) {
					return commandMessage.andMetaData(Collections.singletonMap("gatewayMetaData", "myUserSession"));
				} else {
					// gateway interceptor should only be called from the caller's thread
					throw new SecurityException("test dispatch interceptor exception");
				}
			}
		});

    	// trigger retry, then return metadata for verification
        commandBus.subscribe(String.class.getName(), new CommandHandler<String>() {
            @Override
            public MetaData handle(CommandMessage<String> commandMessage, UnitOfWork unitOfWork) throws Throwable {
            	if (Thread.currentThread() == testThread) {
            		throw new ConcurrencyException("some retryable exception");
            	} else {
            		return commandMessage.getMetaData();
            	}
            }
        });

        assertEquals("myUserSession",
        		((MetaData) commandGateway.sendAndWait("command")).get("gatewayMetaData"));
    }
    

    /**
	 * It'd be nice if metadata added by a
	 * {@linkplain SimpleCommandBus#setDispatchInterceptors(List) command bus's
	 * dispatch interceptors} could be preserved, too, but that doesn't seem to
	 * be possible given how {@link RetryingCallback} works, so verify that it
	 * behaves as designed (if not as "expected").
	 */
    @Test(timeout=10000)
    public void testCommandBusDispatchInterceptorMetaDataIsNotPreservedOnRetry() {
    	final Thread testThread = Thread.currentThread();
		commandGateway = new DefaultCommandGateway(commandBus, retryScheduler);

    	// trigger retry, then return metadata for verification
        commandBus.subscribe(String.class.getName(), new CommandHandler<String>() {
            @Override
            public MetaData handle(CommandMessage<String> commandMessage, UnitOfWork unitOfWork) throws Throwable {
            	if (Thread.currentThread() == testThread) {
            		throw new ConcurrencyException("some retryable exception");
            	} else {
            		return commandMessage.getMetaData();
            	}
            }
        });

        commandBus.setDispatchInterceptors(Collections.singletonList(new CommandDispatchInterceptor() {
			@Override
			public CommandMessage<?> handle(CommandMessage<?> commandMessage) {
				if (Thread.currentThread() == testThread) {
					return commandMessage.andMetaData(Collections.singletonMap("commandBusMetaData", "myUserSession"));
				} else {
					// say the security interceptor example
					// from #testCommandDipatchInterceptorExceptionOnRetryThreadIsThrownToCaller
					// has been "fixed" -- on the retry thread, there's no security context
					return commandMessage.andMetaData(Collections.singletonMap("commandBusMetaData", "noUserSession"));
				}
			}
		}));

        assertEquals("noUserSession", ((MetaData) commandGateway.sendAndWait("command")).get("commandBusMetaData"));
    }
}
