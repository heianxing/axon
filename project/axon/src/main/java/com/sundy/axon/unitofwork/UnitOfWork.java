package com.sundy.axon.unitofwork;

import com.sundy.axon.domain.AggregateRoot;
import com.sundy.axon.domain.EventMessage;
import com.sundy.axon.eventhandling.EventBus;

/**
 * 该类代表一个能够修改聚合根的工作单元，一个典型的工作单元的范围是一个命令的执行。一个工作单元主要用于，防止在聚合根被处理完成前，单个的事件被发布出去。
 * 它也能通过仓库来管理资源，比如 锁。事务，比如锁在事件已提交或回滚后才能释放
 * <p/>
 * 获取当前工作单元 可以通过 {@link CurrentUnitOfWork#get()}
 * @author Administrator
 *
 */
public interface UnitOfWork {

	/**
	 * 提交工作单元，所有已注册但没有注册到相对应仓库的聚合根，缓存事件将会发送到对应的事件总线，所有已注册的工作单元监听器都将被通知
	 * <p/>
	 * 不管是否成功提交，该工作单元将会从CurrentUnitOfWork移除，并清理掉所有已占用资源，这也意味着，如果提交失败，所有东西被清掉
	 */
	void commit();
	
	/**
	 * 清理掉所有的缓存，所有的缓存事件和已注册的聚合根将会被丢弃  并且通知所有注册到该工作单元的监听器
	 * <p/>
	 * 如果 引起回滚的是因为异常，那么建议使用 {@link UnitOfWork#rollback(Throwable)}来代替
	 */
	void rollback();
	
	/**
	 *  清理掉所有的缓存，所有的缓存事件和已注册的聚合根将会被丢弃  并且通知所有注册到该工作单元的监听器
	 * @param cause 引起回退的异常
	 */
	void rollback(Throwable cause);
	
	/**
	 * 启动当前工作单元，准备注册聚合根。并将当前工作单元注册到 CurrentUnitOfWork
	 */
	void start();
	
	/**
	 * 判断工作单元是否已经启动，并且该工作单元没有提交和回滚
	 * @return
	 */
	boolean isStarted();
	
	/**
	 * 判断UnitOfWork是否必须有事务
	 * @return
	 */
	boolean isTransactional();
	
	/**
	 * 注册UnitOfWork监听器，用于监听UnitOfWork的状态改变，一般用于允许组件清理资源，比如锁，当UnitOfWork提交或者回滚。 如果一个工作单元部分提交。
	 * 那么只有关联到这个已提交聚合根的监听器被通知
	 * @param listener
	 */
	void registerListener(UnitOfWorkListener listener);
	
	/**
	 * 注册一个聚合根到UnitOfWork，当UnitOfWork提交时该聚合根将被保存到仓库。该方法的返回聚合根实例将会作为该工作单元处理进程的组成
	 * <p/>
	 * 如果一个聚合已有一个一样的已经被注册，一下两件有可能发生
	 * <ul>
	 * <li>1,返回已被注册的聚合<code>SaveAggregateCallback<code/>将被忽略<li/>
	 * <li>2,抛出一个异常IllegalStateException，提示非法尝试注册一个重复的聚合，<li/>
	 * <ul/>
	 * @param aggregateRoot 被注册的聚合根
	 * @param eventBus		聚合根将会发布到的事件总线
	 * @param saveAggregateCallback 当UnitOfWork将要保存这个聚合根的时候，回调函数将会被执行
	 * @return
	 */
	<T extends AggregateRoot> T registerAggregate(T aggregateRoot, EventBus eventBus, SaveAggregateCallback<T> saveAggregateCallback);
	
	/**
	 * 请求将一个事件发布到事件总线上。UnitOfWork可以立即发布也可以缓存事件直到UnitOfWork提交再发布
	 * @param event			将要发布到事件总线的事件
	 * @param eventBus		事件总线
	 */
	void publishEvent(EventMessage<?> event, EventBus eventBus);
	
	/**
	 * 将资源通过name附加到UnitOfWork。这些资源不能够被继承
	 * <p/>
	 * 如果该UnitOfWork已存在该资源，那么资源会被覆盖
	 * <p/>
	 * 为了方便，一般将资源名称命名为该资源的全类型名，如果有两个同一类型的资源，将会通过其他方式来区分
	 * @param name
	 * @param resource
	 */
	void attachResource(String name, Object resource);
	
	/**
	 * 将资源通过name附加到UnitOfWork。这些资源通过判断来是否继承
	 * <p/>
	 * 如果该UnitOfWork已存在该资源，那么资源会被覆盖
	 * <p/>
	 * 为了方便，一般将资源名称命名为该资源的全类型名，如果有两个同一类型的资源，将会通过其他方式来区分
	 * @param name
	 * @param resource
	 * @param inherited
	 */
	void attachResource(String name, Object resource, boolean inherited);
	
	/**
	 * 通过资源名称来获取资源。如果没有改资源则返回null
	 * @param name 资源名
	 * @param T 资源类型
	 * @return
	 */
	<T> T getResource(String name);
	
	/**
	 * 将所有可继承的资源附加到给定的工作单元
	 * @param inheritingUnitOfWork
	 */
	void attachInheritedResources(UnitOfWork inheritingUnitOfWork);
	
}
