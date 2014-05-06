package me.ragan262.commandmanager.lang;

public interface CommandLang {
	
	public String invalidArgMessage(String argument);
	
	public String invalidNumberMessage(String argument);
	
	public String unknownArgMessage(String argument);
	
	public String tooManyArgsMessage();
	
	public String notEnoughArgsMessage();
	
	public String playerContextMessage();
	
	public String usageMessage(String usage);
	
	public String permissionMessage(String permission);
	
}
