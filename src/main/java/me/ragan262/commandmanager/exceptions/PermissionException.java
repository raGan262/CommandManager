package me.ragan262.commandmanager.exceptions;


public class PermissionException extends CommandException {
	
	private static final long serialVersionUID = -1992835899555259331L;
	
	public PermissionException(final String permission) {
		super(permission);
	}
	
}
