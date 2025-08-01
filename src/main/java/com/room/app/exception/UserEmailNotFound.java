package com.room.app.exception;

public class UserEmailNotFound extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UserEmailNotFound() {
		super();
	}

	public UserEmailNotFound(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public UserEmailNotFound(String message, Throwable cause) {
		super(message, cause);
	}

	public UserEmailNotFound(String message) {
		super(message);
	}

	public UserEmailNotFound(Throwable cause) {
		super(cause);
	}

}
