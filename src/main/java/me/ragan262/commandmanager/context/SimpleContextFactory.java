package me.ragan262.commandmanager.context;

import org.bukkit.command.CommandSender;

import me.ragan262.commandmanager.CommandManager;

public class SimpleContextFactory implements ContextFactory {
	
	public static final ContextFactory instance = new SimpleContextFactory();
	
	private SimpleContextFactory() {}
	
	public CommandContext getContext(String[] args, String[] parentArgs, CommandSender sender, CommandManager comMan) {
		return new CommandContext(args, parentArgs, sender, comMan);
	}
	
	public Class<? extends CommandContext> getContextClass() {
		return CommandContext.class;
	}
}
