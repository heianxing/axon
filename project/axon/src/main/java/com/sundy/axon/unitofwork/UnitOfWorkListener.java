package com.sundy.axon.unitofwork;

import java.util.List;
import java.util.Set;

import com.sundy.axon.domain.AggregateRoot;
import com.sundy.axon.domain.EventMessage;

/**
 * 接口用于描述 一个监听器，UnitOfWOrk状态改变时将通知已注册的监听器
 * @author Administrator
 *
 */
public interface UnitOfWorkListener {

	/**
	 * UnitOfWOrk提交后，该方法执行。聚合被保存，并且事件将被调度分发。
	 * 在某些情况下，事件可能已经被分发
	 * 当该方法出现执行异常时，工作单元会调用{@link #onRollback(UnitOfWork, Throwable)}
	 * @param unitOfWork 执行完提交的工作单元
	 */
	void afterCommit(UnitOfWork unitOfWork);
	
	/**
	 * 工作单元回滚时执行该方法。工作单元执行失败可能选择执行该方法，
	 * @param unitOfWork 正在执行回滚的工作单元
	 * @param failureCause 失败原因
	 */
	void onRollback(UnitOfWork unitOfWork, Throwable failureCause);
	
	/**
	 * 当工作单元提交。已经注册到发布事件中的事件将会执行该方法。监听器通过修改返回新事件的信息来修改
	 * 注意，监听器，必须保证事件的所表达的意思不能改变。一般来说，这个方法一般只修改事件中的元数据
	 * <p/>
	 * 最简单的实现就是返回原事件
	 * @param unitOfWork 事件注册的工作单元
	 * @param event	事件
	 * @param <T> 事件中信息携带的载体
	 * @return
	 */
	<T> EventMessage<T> onEventRegistered(UnitOfWork unitOfWork, EventMessage<T> event);
	
	/**
	 * 聚合提交前并且在事件的发布前执行。这个阶段可以用于验证或其他行为来防止一些确定情况下的事件分发
	 * <p/>
	 * 注意，给定的事件中不包含聚合根中所有未提交领域事件。如果要获取所有事件，那么需要收集所有聚合根中事件，并将这些事件加入到事件列表中
	 * @param unitOfWork 被提交的工作单元
	 * @param aggregateRoots	需要被提交的聚合根
	 * @param events	已注册的事件，并将要被分发的事件
	 */
	void onPrepareCommit(UnitOfWork unitOfWork, Set<AggregateRoot> aggregateRoots, List<EventMessage> events);
	
	/**
	 * 在当前已绑定事务的工作单元提交前执行，但是，在所有的提交活动被执行。作为事务一部分的资源管理器有机会执行操作
	 * <p/>
	 * 注意，这些所有操作只会在该工作单元已绑定事务
	 * @param unitOfWork 潜在事务被执行的工作单元
	 * @param transaction 事务对象
	 */
	void onPrepareTransactionCommit(UnitOfWork unitOfWork, Object transaction);
	
	/**
	 * 工作单元将被清空时，通知所有已注册的监听器，，这个方法将给所有的监听器提供机会去清理掉哪些在提交或回滚时用到的资源，比如锁
	 * @param unitOfWork 将要执行清空的工作单元
	 */
	void onCleanup(UnitOfWork unitOfWork);
}
