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

package org.axonframework.auditing;

import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.common.Assert;
import org.axonframework.domain.EventMessage;
import org.axonframework.unitofwork.UnitOfWork;
import org.axonframework.unitofwork.UnitOfWorkListenerAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Listener implementation that adds auditing information to events being tracked by the unit of work this listener is
 * registered to.
 *
 * @author Allard Buijze
 * @since 0.7
 */
public class AuditingUnitOfWorkListener extends UnitOfWorkListenerAdapter {

    private final AuditDataProvider auditDataProvider;
    private final AuditLogger auditLogger;
    private final CommandMessage<?> command;
    private final List<EventMessage> recordedEvents = new ArrayList<EventMessage>();
    private volatile Object returnValue;

    /**
     * Initialize a listener for the given <code>command</code>. The <code>auditDataProvider</code> is called before
     * the Unit Of Work is committed to provide the auditing information. The <code>auditLogger</code> is invoked after
     * the Unit Of Work is successfully committed.
     *
     * @param command           The command being audited
     * @param auditDataProvider The instance providing the information to attach to the events
     * @param auditLogger       The logger writing the audit
     */
    public AuditingUnitOfWorkListener(CommandMessage<?> command, AuditDataProvider auditDataProvider,
                                      AuditLogger auditLogger) {
        Assert.notNull(command, "command may not be null");
        Assert.notNull(auditDataProvider, "auditDataProvider may not be null");
        Assert.notNull(auditLogger, "auditLogger may not be null");
        this.auditDataProvider = auditDataProvider;
        this.auditLogger = auditLogger;
        this.command = command;
    }

    @Override
    public void afterCommit(UnitOfWork unitOfWork) {
        auditLogger.logSuccessful(command, returnValue, recordedEvents);
    }

    @Override
    public void onRollback(UnitOfWork unitOfWork, Throwable failureCause) {
        auditLogger.logFailed(command, failureCause, recordedEvents);
    }

    @Override
    public <T> EventMessage<T> onEventRegistered(UnitOfWork unitOfWork, EventMessage<T> event) {
        Map<String, ?> auditData = auditDataProvider.provideAuditDataFor(command);
        if (!auditData.isEmpty()) {
            event = event.andMetaData(auditData);
        }
        recordedEvents.add(event);
        return event;
    }

    /**
     * Registers the return value of the command handler with the auditing context.
     *
     * @param returnValue The return value of the command handler, if any. May be <code>null</code>.
     */
    void setReturnValue(Object returnValue) {
        this.returnValue = returnValue;
    }
}
