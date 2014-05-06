package me.ragan262.commandmanager;

import me.ragan262.commandmanager.lang.CommandLang;

class DefaultCommandLang implements CommandLang {
	
	static final CommandLang instance = new DefaultCommandLang();
	
	private DefaultCommandLang() {}
	
	public String invalidArgMessage(String argument) {
		return "Invalid argument: '" + argument + "'";
	}
	
	public String invalidNumberMessage(String argument) {
		return "Number expected, but " + argument + " found.";
	}
	
	public String unknownArgMessage(String argument) {
		return "Unknown argument: " + argument;
	}
	
	public String tooManyArgsMessage() {
		return "Too many argmunents.";
	}
	
	public String notEnoughArgsMessage() {
		return "Not enough arguments.";
	}
	
	public String playerContextMessage() {
		return "This command requires player context.";
	}
	
	public String usageMessage(String usage) {
		return "Usage: " + usage;
	}
	
	public String permissionMessage(String permission) {
		return "You don't have permission for this.";
	}
	
}
