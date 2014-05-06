package me.ragan262.commandmanager.exceptions;

public class CommandException extends Exception {
	
	private static final long serialVersionUID = -5239713102121410865L;
	
	public CommandException(final String message) {
		super(message);
	}
	
	public CommandException(final Throwable cause) {
		super(cause);
	}
	
	public CommandException(final String message, final Throwable cause) {
		super(message, cause);
	}
	
}
