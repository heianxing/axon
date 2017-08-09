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

package org.axonframework.commandhandling.disruptor;

import com.lmax.disruptor.RingBuffer;
import org.axonframework.commandhandling.CommandCallback;
import org.axonframework.commandhandling.CommandMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper for command handler Callbacks that detects blacklisted aggregates and starts a cleanup process when an
 * aggregate is blacklisted.
 *
 * @param <R> The return value of the Command
 * @author Allard Buijze
 * @since 2.0
 */
public class BlacklistDetectingCallback<R>
        implements CommandCallback<R> {

    private static final Logger logger = LoggerFactory.getLogger(BlacklistDetectingCallback.class);

    private final CommandCallback<R> delegate;
    private final CommandMessage command;
    private final RingBuffer<CommandHandlingEntry> ringBuffer;
    private final DisruptorCommandBus commandBus;
    private final boolean rescheduleOnCorruptState;

    /**
     * Initializes the callback which allows the given <code>command</code> to be rescheduled on the given
     * <code>ringBuffer</code> if it failed due to a corrupt state.
     *
     * @param delegate                 The callback to invoke when an exception occurred
     * @param command                  The command being executed
     * @param ringBuffer               The RingBuffer on which an Aggregate Cleanup should be scheduled when a
     *                                 corrupted
     *                                 aggregate state was detected
     * @param commandBus               The CommandBus on which the command should be rescheduled if it was executed on
     *                                 a
     *                                 corrupt aggregate
     * @param rescheduleOnCorruptState Whether the command should be retried if it has been executed against corrupt
     *                                 state
     */
    public BlacklistDetectingCallback(CommandCallback<R> delegate, CommandMessage command,
                                      RingBuffer<CommandHandlingEntry> ringBuffer,
                                      DisruptorCommandBus commandBus, boolean rescheduleOnCorruptState) {
        this.delegate = delegate;
        this.command = command;
        this.ringBuffer = ringBuffer;
        this.commandBus = commandBus;
        this.rescheduleOnCorruptState = rescheduleOnCorruptState;
    }

    @Override
    public void onSuccess(R result) {
        if (delegate != null) {
            delegate.onSuccess(result);
        }
    }

    @Override
    public void onFailure(Throwable cause) {
        if (cause instanceof AggregateBlacklistedException) {
            long sequence = ringBuffer.next();
            CommandHandlingEntry event = ringBuffer.get(sequence);
            event.resetAsRecoverEntry(((AggregateBlacklistedException) cause).getAggregateIdentifier());
            ringBuffer.publish(sequence);
            if (delegate != null) {
                delegate.onFailure(cause.getCause());
            }
        } else if (rescheduleOnCorruptState && cause instanceof AggregateStateCorruptedException) {
            commandBus.doDispatch(command, delegate);
        } else if (delegate != null) {
            delegate.onFailure(cause);
        } else {
            logger.warn("Command {} resulted in an exception:", command.getPayloadType().getSimpleName(), cause);
        }
    }

    /**
     * Indicates whether this callback has a delegate that needs to be notified of the command handling result
     *
     * @return <code>true</code> if this callback has a delegate, otherwise <code>false</code>.
     */
    public boolean hasDelegate() {
        return delegate != null;
    }
}
