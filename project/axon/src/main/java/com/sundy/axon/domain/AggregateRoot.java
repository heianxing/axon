package com.sundy.axon.domain;

/**
 * 接口用于定义一些聚合根实体的约定
 * @author Administrator
 *
 * @param <I> 聚合的唯一标识符类型
 */
public interface AggregateRoot<I> {

	/**
	 * 返回聚合的唯一标识符
	 * @return
	 */
	I getIdentifier();
	
	/**
	 * 清理掉一些被标记为未提交的事件，以及清理掉一些一致的事件回调方法
	 */
	void commitEvents();
	
	/**
	 * 获取当前可用的有效的未提交事件的个数
	 * @return
	 */
	int getUncommittedEventCount();
	
	/**
	 * 获取所有从聚合创建到最近提交以来所有的未提交事件
	 * @return
	 */
	DomainEventStream getUncommittedEvents();
	
	/**
	 * 返回当前聚合的版本，为null表示该聚合时最近创建的。这个版本号反应聚合发生了什么改变
	 * <p/>
	 * 聚合的没次修改和保存到仓库，都会更新一个版本号，该版本号可以作为一个乐观锁的策略来发现当前的修改冲突
	 * <p/>
	 * 一般来说，将之前时间提交的序列化作为聚合的版本号
	 * @return
	 */
	Long getVersion();
	
	/**
	 * 用于判断该聚合是否被删除。如果该聚合标记为删除(返回true) 那么在适当的时候将发送一个命令个仓库去执行删除操作
	 * @return
	 */
	boolean isDeleted();
	
	/**
	 * 添加EventRegistrationCallback，用于一个已注册到发布列表中事件 当聚合被提交，这个回调将被清空
	 * @param eventRegistrationCallback
	 */
	void addEventRegistrationCallback(EventRegistrationCallback eventRegistrationCallback);
	
}
