package me.ragan262.commandmanager.lang;

import org.apache.commons.lang.Validate;
import org.bukkit.command.CommandSender;

public class SimpleCommandLangProvider implements CommandLangProvider {
	
	private final CommandLang lang;
	
	public SimpleCommandLangProvider(CommandLang lang) {
		Validate.notNull(lang, "lang cannot be null.");
		this.lang = lang;
	}
	
	public CommandLang getDefaultCommandLang() {
		return lang;
	}
	
	public CommandLang getCommandLang(CommandSender sender) {
		return lang;
	}
}
