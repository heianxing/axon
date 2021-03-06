package com.sundy.axon.domain;

import java.util.Map;

/**
 * 携带消息的命令载体，该消息包含改变应用状态的意图
 * @author Administrator
 *
 * @param <T> 消息包含的载体类型
 */
public interface CommandMessage<T> extends Message<T> {

	/**
	 * 返回要执行命令的名称，命令需要显示表达要执行的命令意思，兵器使用载体作为执行的参数
	 * @return
	 */
	String getCommandName();
	
	/**
	 * 返回消息的副本，用给定参数替换掉元数据，但是载体不变
	 */
	CommandMessage<T> withMetaData(Map<String, ?> metaData);
	
	/**
	 * 返回消息的副本，用给定参数替合并已有元数据，但是载体不变
	 */
	CommandMessage<T> andMetaData(Map<String, ?> metaData);
	
}
