package me.ragan262.commandmanager.context;

import org.bukkit.command.CommandSender;

import me.ragan262.commandmanager.CommandManager;

/**
 * Used by {@link CommandManager} to generate command context.
 * 
 * @author raGan
 */
public interface ContextFactory {
	
	/**
	 * <p><b>Example of context arguments and parent arguments</b></br>
	 * <u>Entered command:</u> "/example command with some arguments"</br>
	 * <u>Command method being executed:</u> "/example command with" ("with" is a subcommand of command "command")</br></br>
	 * All arguments that are part of the path to the command method being executed are parent arguments, and
	 * the rest of the arguments in entered command are context argumetns.</br>
	 * <u>Resulting parent arguments:</u> {"command", "with"}</br>
	 * <u>Resulting context arguments:</u> {"some", "arguments"}</p>
	 * 
	 * @param args arguments of this context
	 * @param parentArgs parent argumetns of this context
	 * @param sender command sender
	 * @param comMan {@link CommandManager} object handling this command
	 * @return resulting command context object of the class or subclass of the class specified by
	 * {@link ContextFactory#getContextClass() getContextClass} method
	 */
	public CommandContext getContext(final String[] args, final String[] parentArgs, final CommandSender sender, final CommandManager comMan);
	
	/**
	 * @return the class or superclass of the objects returned by 
	 * {@link ContextFactory#getContext(String[], String[], CommandSender, CommandManager) getContext} method of this class.
	 */
	public Class<? extends CommandContext> getContextClass();
}
