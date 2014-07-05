package me.ragan262.commandmanager.exceptions;

import org.bukkit.command.CommandSender;

/**
 * This interface allows handleCommand() in command manager to handle custom exceptions.
 * 
 * @author raGan
 */
public interface CommandExceptionHandler {

	/**
	 * This method gets called when command throws an exception that isn't 
	 * recognized by CommandManager. This method should not throw any exceptions.
	 * 
	 * @param e exception thrown by a command
	 * @param sender command sender
	 */
	public void handleException(Exception e, CommandSender sender);
	
}
