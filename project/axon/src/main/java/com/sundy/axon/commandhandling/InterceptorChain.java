package com.sundy.axon.commandhandling;

import com.sundy.axon.domain.CommandMessage;

/**
 * 拦截链，用于管理所有的命令通过一个链条的拦截器，并最终到达命令处理器。
 * 拦截器通过处理这些命令通过{@link #proceed()} 或者 {@link #proceed(CommandMessage)} 或者通过返回来阻止处理
 * @author Administrator
 *
 */
public interface InterceptorChain {

	/**
	 * 指示拦截器，继续拦截后续命令
	 * @return
	 * @throws Throwable
	 */
	Object proceed() throws Throwable;
	
	/**
	 * 指示拦截器，继续拦截后续命令
	 * @return
	 * @throws Throwable
	 */
	Object proceed(CommandMessage<?> command) throws Throwable;
	
}
