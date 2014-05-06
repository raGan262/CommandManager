package me.ragan262.commandmanager.exceptions;


public class UsageException extends CommandException {
	
	private static final long serialVersionUID = 8119304614528165873L;
	
	private final String usage;
	
	public UsageException(final String message, final String usage) {
		super(message);
		this.usage = usage;
	}
	
	public String getUsage() {
		return usage;
	}
}
