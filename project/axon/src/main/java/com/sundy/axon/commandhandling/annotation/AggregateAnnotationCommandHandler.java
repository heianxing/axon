package com.sundy.axon.commandhandling.annotation;

import com.sundy.axon.commandhandling.CommandBus;
import com.sundy.axon.commandhandling.CommandHandler;
import com.sundy.axon.common.Subscribable;
import com.sundy.axon.domain.AggregateRoot;
import com.sundy.axon.domain.CommandMessage;
import com.sundy.axon.unitofwork.UnitOfWork;

public class AggregateAnnotationCommandHandler<T extends AggregateRoot> implements Subscribable,CommandHandler<Object> {

	private final CommandBus commandBus;
	
	public Object handle(CommandMessage<Object> commandMessage,
			UnitOfWork unitOfWork) throws Throwable {
		// TODO Auto-generated method stub
		return null;
	}

	public void unsubscribe() {
		// TODO Auto-generated method stub
		
	}

	public void subscribe() {
		// TODO Auto-generated method stub
		
	}

}
