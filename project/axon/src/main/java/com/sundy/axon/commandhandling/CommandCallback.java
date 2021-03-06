package com.sundy.axon.commandhandling;

/**
 * 接口描述一个回调，当命令执行完成之后，该接口的实现类被执行
 * @author Administrator
 *
 * @param <R>
 */
public interface CommandCallback<R> {

	/**
	 * 命令执行成功后执行
	 * @param result
	 */
	void onSuccess(R result);
	
	/**
	 * 命令执行异常后执行
	 * @param cause
	 */
	void onFailure(Throwable cause);
	
}
