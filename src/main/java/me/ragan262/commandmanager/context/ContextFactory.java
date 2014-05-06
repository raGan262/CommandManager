package me.ragan262.commandmanager.context;

import org.bukkit.command.CommandSender;

import me.ragan262.commandmanager.CommandManager;

public interface ContextFactory {
	
	public CommandContext getContext(final String[] args, final String[] parentArgs, final CommandSender sender, final CommandManager comMan);
	
	public Class<? extends CommandContext> getContextClass();
}
