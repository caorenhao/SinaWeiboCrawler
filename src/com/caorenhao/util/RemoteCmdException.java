package com.caorenhao.util;

/**
 * 执行远程命令的异常.
 *
 * @author renhao.cao.
 *         Created 2015-1-21.
 */
public class RemoteCmdException extends Exception {

	private static final long serialVersionUID = -6223329442812958667L;

	/** TODO Put here a description of what this constructor does.*/
	public RemoteCmdException() {
	}

	/**
	 * TODO Put here a description of what this constructor does.
	 *
	 * @param message
	 */
	public RemoteCmdException(String message) {
		super(message);
	}

	/**
	 * TODO Put here a description of what this constructor does.
	 *
	 * @param cause
	 */
	public RemoteCmdException(Throwable cause) {
		super(cause);
	}

	/**
	 * TODO Put here a description of what this constructor does.
	 *
	 * @param message
	 * @param cause
	 */
	public RemoteCmdException(String message, Throwable cause) {
		super(message, cause);
	}

}
