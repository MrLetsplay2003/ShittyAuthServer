package me.mrletsplay.shittyauth.util;

public class InvalidUsernameException extends Exception {

	private static final long serialVersionUID = -827594972986252869L;

	public InvalidUsernameException() {
		super();
	}

	public InvalidUsernameException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidUsernameException(String message) {
		super(message);
	}

	public InvalidUsernameException(Throwable cause) {
		super(cause);
	}

}
