package com.github.theholywaffle.lolchatapi;

public class LoginException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5849853116089056873L;

	public LoginException( String msg, Throwable cause) {
		super(msg, cause);
	}
}
