package me.ragan262.commandmanager;

import org.bukkit.ChatColor;

public class CommandHelp {
	
	protected final String commandString;
	protected final String usage;
	protected final String description;
	
	protected CommandHelp(String commandString, String usage, String description) {
		this.commandString = commandString;
		this.usage = usage;
		this.description = description;
	}
	
	public String getCommandString() {
		return commandString;
	}
	
	public String getUsage() {
		return usage;
	}
	
	public String getDescription() {
		return description;
	}
	
	public String getFormattedHelp() {
		return commandString + (usage.isEmpty() ? "" : ChatColor.GOLD + " " + usage) 
				+ (description.isEmpty() ? "" : ChatColor.GRAY + " - " + description);
	}
	
}
