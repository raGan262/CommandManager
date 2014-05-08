package me.ragan262.commandmanager.context;

import org.bukkit.command.CommandSender;

import me.ragan262.commandmanager.CommandManager;

/**
 * Default context factory for command manager objects. This is a singleton class.
 * 
 * @author raGan
 */
public class SimpleContextFactory implements ContextFactory {
	
	/**
	 * SimpleContextFactory instance.
	 */
	public static final ContextFactory instance = new SimpleContextFactory();
	
	private SimpleContextFactory() {}
	
	/**
	 * Creates and returns new instance of {@link CommandContext} class.
	 */
	@Override
	public CommandContext getContext(String[] args, String[] parentArgs, CommandSender sender, CommandManager comMan) {
		return new CommandContext(args, parentArgs, sender, comMan);
	}
	
	/**
	 * Returns the {@link CommandContext} class.
	 */
	@Override
	public Class<? extends CommandContext> getContextClass() {
		return CommandContext.class;
	}
}
