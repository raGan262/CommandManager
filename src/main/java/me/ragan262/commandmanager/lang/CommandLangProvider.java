package me.ragan262.commandmanager.lang;

import org.bukkit.command.CommandSender;

public interface CommandLangProvider {
	
	public CommandLang getDefaultCommandLang();
	
	public CommandLang getCommandLang(CommandSender sender);
	
}
