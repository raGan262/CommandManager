package me.ragan262.commandmanager.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Command {
	
	/**
	 * @return description of the command, used to generate help
	 */
	String desc() default "";
	
	/**
	 * @return minimal number of arguments for this command
	 */
	int min() default 0;
	
	/**
	 * @return maximal number of arguments for this command
	 */
	int max() default -1;
	
	/**
	 * @return usage or command arguments (e.g. "[player] [item]" in case of 2 arguments)
	 */
	String usage() default "";
	
	/**
	 * Also supports vertical bars as permission separators. I case there are more permissions 
	 * specified that way, at least one is required for the command to be executed.
	 * @return permission required to run the command
	 */
	String permission() default "";
	
	/**
	 * Commands with the same section will be grouped under the same key in a help map generated by
	 * CommandManager. Section names are case sensitive. Empty string is a valid section name.
	 * @return command section
	 */
	String section() default "";
	
	/**
	 * @return true is command can only be executed by a player
	 */
	boolean player() default false;
	
	/**
	 * Forces execution of a non-leaf command even if unknown or no additional arguments are specified.
	 * Example: Imagine command: /example arg1 arg2 &lt;option1, option2, option3&gt;
	 * Normally, when user inputs "/example arg1 arg2 otherOption", command manager sends
	 * unknown argument error message. But if forceExecute() for "arg2" command is true, comamnd
	 * manager would execute command arg2 instead and pass it whatever arguments were entered. Note
	 * that it will still respect min() and max() specicfic to that paticular command.
	 * 
	 * @return true if this command should be executed even if it's not a command tree leaf
	 */
	boolean forceExecute() default false;
}
