package com.sundy.axon.auditing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sundy.axon.common.Assert;
import com.sundy.axon.domain.CommandMessage;
import com.sundy.axon.domain.EventMessage;
import com.sundy.axon.unitofwork.UnitOfWork;
import com.sundy.axon.unitofwork.UnitOfWorkListenerAdapter;

public class AuditUnitOfWorkListener extends UnitOfWorkListenerAdapter {

	private final AuditDataProvider auditDataProvider;
	
	private final AuditLogger auditLogger;
	
	private final CommandMessage<?> command;
	
	private final List<EventMessage> recordedEvents = new ArrayList<EventMessage>();
	
	private volatile Object returnValue;
	
	public AuditUnitOfWorkListener(CommandMessage<?> command, AuditDataProvider auditDataProvider, AuditLogger auditLogger){
		Assert.notNull(command, "command may not be null");
	    Assert.notNull(auditDataProvider, "auditDataProvider may not be null");
	    Assert.notNull(auditLogger, "auditLogger may not be null");
	    this.auditDataProvider = auditDataProvider;
	    this.auditLogger = auditLogger;
	    this.command = command;
	}
	
	@Override
	public void afterCommit(UnitOfWork unitOfWork){
		auditLogger.logSuccessful(command, unitOfWork, recordedEvents);
	}
	
	@Override
	public void onRollback(UnitOfWork unitOfWork, Throwable failureCause){
		auditLogger.logFailed(command, failureCause, recordedEvents);
	}


	@Override
	public <T> EventMessage<T> onEventRegistered(UnitOfWork unitOfWork,
			EventMessage<T> event) {
		Map<String, ?> auditData = auditDataProvider.provideAuditDataFor(command);
		if(!auditData.isEmpty()){
			event = event.andMetaData(auditData);
		}
		recordedEvents.add(event);
		return event;
	}

	public void setReturnValue(Object returnValue) {
		this.returnValue = returnValue;
	}
	
	
	
	
	
	
}
