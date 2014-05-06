package me.ragan262.commandmanager;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import me.ragan262.commandmanager.annotations.Command;
import me.ragan262.commandmanager.annotations.CommandLabels;
import me.ragan262.commandmanager.annotations.NestedCommand;
import me.ragan262.commandmanager.context.CommandContext;
import me.ragan262.commandmanager.context.ContextFactory;
import me.ragan262.commandmanager.context.SimpleContextFactory;
import me.ragan262.commandmanager.exceptions.CommandException;
import me.ragan262.commandmanager.exceptions.PermissionException;
import me.ragan262.commandmanager.exceptions.UsageException;
import me.ragan262.commandmanager.lang.CommandLang;
import me.ragan262.commandmanager.lang.CommandLangProvider;
import me.ragan262.commandmanager.lang.SimpleCommandLangProvider;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandManager {
	
	Logger logger = null;
	
	String displayedCommand = "";
	String helpCommand = "help";
	Object[] arguments = null;
	Class<?>[] classes = null;
	
	private CommandLangProvider lang = new SimpleCommandLangProvider(DefaultCommandLang.instance);
	private final ContextFactory cFactory;
	
	private final Map<Method, Map<String, Method>> labels =
			new HashMap<Method, Map<String, Method>>();
	private final Map<Method, Map<String, Method>> aliases =
			new HashMap<Method, Map<String, Method>>();
	private final Map<Method, Object> instances = new HashMap<Method, Object>();
	private final Map<Method, Command> annotations = new HashMap<Method, Command>();
	
	public CommandManager(final Logger logger, final String displayedCommand, final Object... arguments) {
		this(SimpleContextFactory.instance, logger, displayedCommand, arguments);
	}
	
	public CommandManager(ContextFactory factory, final Logger logger, final String displayedCommand, final Object... arguments) {
		Validate.notNull(factory, "Context factory can't be null.");
		if(factory.getContextClass() == null) {
			throw new IllegalArgumentException("Context factory can't return null context class.");
		}
		this.cFactory = factory;
		this.logger = logger;
		this.displayedCommand = displayedCommand;
		this.arguments = arguments;
		classes = new Class<?>[arguments.length];
		for(int i = 0; i < arguments.length; i++) {
			classes[i] = arguments[i].getClass();
		}
	}
	
	public void setHelpCommand(final String helpCommand) {
		if(helpCommand != null) {
			this.helpCommand = helpCommand;
		}
	}
	
	public void setLanguageProvider(CommandLangProvider provider) {
		Validate.notNull(provider, "Language provider can't be null.");
		lang = provider;
	}
	
	public CommandLangProvider getLangProvider() {
		return lang;
	}
	
	public ContextFactory getContextFactory() {
		return cFactory;
	}
	
	public void register(final Class<?> clss) {
		registerMethods(null, clss);
	}
	
	private void registerMethods(final Method parent, final Class<?> clss) {
		final Object instance = construct(clss);
		for(final Method method : clss.getMethods()) {
			
			if(!method.isAnnotationPresent(Command.class)
					|| !method.isAnnotationPresent(CommandLabels.class)) {
				continue;
			}
			
			if(instance == null && !Modifier.isStatic(method.getModifiers())) {
				logger.warning("Failed to register command: " + method.getName() + "() in "
						+ clss.getCanonicalName() + ". Class instance is missing.");
				continue;
			}
			
			Class<?>[] paramTypes = method.getParameterTypes();
			if(paramTypes.length != 2 
					|| !paramTypes[0].isAssignableFrom(cFactory.getContextClass()) 
					|| !paramTypes[1].equals(CommandSender.class)) {
				logger.warning("Failed to register command: " + method.getName() + "() in "
						+ clss.getCanonicalName() + ". Method has incorrect parameter types.");
				continue;
			}
			
			instances.put(method, instance);
			
			final Command qCmd = method.getAnnotation(Command.class);
			annotations.put(method, qCmd);
			
			if(labels.get(parent) == null) {
				labels.put(parent, new TreeMap<String, Method>()); // sorted
			}
			if(aliases.get(parent) == null) {
				aliases.put(parent, new HashMap<String, Method>());
			}
			
			final Map<String, Method> lblMap = labels.get(parent);
			final Map<String, Method> aliMap = aliases.get(parent);
			
			final CommandLabels qCmdLbls = method.getAnnotation(CommandLabels.class);
			final String[] aliases = qCmdLbls.value();
			lblMap.put(aliases[0].toLowerCase(), method);
			for(int i = 1; i < aliases.length; i++) {
				aliMap.put(aliases[i].toLowerCase(), method);
			}
			
			if(method.isAnnotationPresent(NestedCommand.class)) {
				for(final Class<?> iCls : method.getAnnotation(NestedCommand.class).value()) {
					registerMethods(method, iCls);
				}
			}
			
		}
	}
	
	public void execute(final String[] args, final CommandSender sender) throws CommandException {
		executeMethod(args, sender, null, 0);
	}
	
	private void executeMethod(final String[] args, final CommandSender sender, final Method parent, int level) throws CommandException {
		
		CommandLang senderLang = lang.getCommandLang(sender);
		
		if(args.length <= level) {
			throw new UsageException(senderLang.notEnoughArgsMessage(), getUsage(args, level, parent));
		}
		final String label = args[level].toLowerCase();
		
		boolean execute = false;
		if(parent != null) {
			execute = annotations.get(parent).forceExecute();
		}
		
		Method method = labels.get(parent).get(label);
		if(method == null) {
			method = aliases.get(parent).get(label);
		}
		if(method == null) {
			if(execute) {
				method = parent;
				level--;
			}
			else {
				throw new UsageException(senderLang.unknownArgMessage(label), getUsage(args, level - 1,
						parent));
			}
		}
		
		// check every permission for nested command
		final Command cmd = annotations.get(method);
		if(cmd.player() && !(sender instanceof Player)) {
			throw new CommandException(senderLang.playerContextMessage());
		}
		if(sender == null || !sender.hasPermission(cmd.permission())) {
			throw new PermissionException(cmd.permission());
		}
		
		if(method != parent && labels.get(method) != null) { // going deeper
			final int numArgs = args.length - level - 1;
			if(numArgs < 1) {
				if(!cmd.forceExecute()) {
					throw new UsageException(senderLang.notEnoughArgsMessage(), getUsage(args, level, method));
				}
			}
			else {
				executeMethod(args, sender, method, level + 1);
				return; // stop here
			}
		}
		final String[] parentArgs = new String[level + 1];
		final String[] realArgs = new String[args.length - level - 1];
		System.arraycopy(args, 0, parentArgs, 0, level + 1);
		System.arraycopy(args, level + 1, realArgs, 0, args.length - level - 1);
		
		final CommandContext context = cFactory.getContext(realArgs, parentArgs, sender, this);
		
		if(context.length() < cmd.min()) {
			throw new UsageException(senderLang.notEnoughArgsMessage(), getUsage(args, level, method));
		}
		
		if(!(cmd.max() < 0) && context.length() > cmd.max()) {
			throw new UsageException(senderLang.tooManyArgsMessage(), getUsage(args, level, method));
		}
		
		invoke(method, context, sender);
	}
	
	private void invoke(final Method method, final Object... methodArgs) throws CommandException, NumberFormatException {
		Exception ex = null;
		try {
			method.invoke(instances.get(method), methodArgs);
		}
		catch (final IllegalAccessException e) {
			ex = e;
		}
		catch (final IllegalArgumentException e) {
			ex = e;
		}
		catch (final InvocationTargetException e) {
			if(e.getCause() instanceof CommandException) {
				throw (CommandException) e.getCause();
			}
			else if(e.getCause() instanceof IllegalArgumentException) {
				throw (IllegalArgumentException) e.getCause();
			}
			else {
				ex = e;
			}
		}
		if(ex != null) {
			logger.log(Level.SEVERE, "Failed to execute command.", ex);
		}
	}

	public void handleCommand(final CommandSender sender, final String[] args) {
		CommandLang senderLang = lang.getCommandLang(sender);
		try {
			execute(args, sender);
		}
		catch (final CommandException e) {
			if(e instanceof UsageException) {
				sender.sendMessage(ChatColor.RED + e.getMessage());
				sender.sendMessage(ChatColor.RED + senderLang.usageMessage(((UsageException) e).getUsage()));
			}
			else if(e instanceof PermissionException) {
				sender.sendMessage(ChatColor.RED + senderLang.permissionMessage(e.getMessage()));
			}
			else {
				sender.sendMessage(ChatColor.RED + e.getMessage());
			}
		}
		catch (final NumberFormatException e) {
			sender.sendMessage(ChatColor.RED + senderLang.invalidNumberMessage(e.getMessage().replaceFirst(".+ \"", "\"")));
		}
		catch (final IllegalArgumentException e) {
			sender.sendMessage(ChatColor.RED + senderLang.invalidArgMessage(e.getMessage()));
		}
	}
	
	public Map<String, List<String>> getHelp(final String[] args, final CommandSender sender, final boolean deep) {
		final Map<String, List<String>> result = new HashMap<String, List<String>>();
		Method m = null;
		for(final String s : args) {
			if(labels.get(m).containsKey(s)) {
				m = labels.get(m).get(s);
			}
			else if(aliases.get(m).containsKey(s)) {
				m = aliases.get(m).get(s);
			}
			else {
				throw new IllegalArgumentException(s);
			}
			if(!sender.hasPermission(annotations.get(m).permission())) {
				return result;
			}
		}
		addHelpToMap(sender, m, args, result, deep);
		return result;
	}
	
	private void addHelpToMap(final CommandSender sender, final Method method, final String[] arguments, final Map<String, List<String>> resultMap, final boolean deep) {
		Command command = null;
		final Map<String, Method> lbls = labels.get(method);
		if(lbls == null || deep) {
			if(method != null) {
				command = annotations.get(method);
				if(lbls != null && !command.forceExecute()) {
					// we don't want to display command groups
				}
				else if(command != null && sender.hasPermission(command.permission())) {
					if(resultMap.get(command.section()) == null) {
						resultMap.put(command.section(), new ArrayList<String>());
					}
					final StringBuilder cmdString = new StringBuilder();
					final String usage = command.usage();
					cmdString.append(displayedCommand);
					if(arguments.length > 0) {
						cmdString.append(' ').append(implode(arguments));
					}
					if(!usage.isEmpty()) {
						cmdString.append(' ').append(ChatColor.GOLD).append(usage);
					}
					cmdString.append(ChatColor.GRAY).append(" - ").append(command.desc());
					resultMap.get(command.section()).add(cmdString.toString());
				}
			}
		}
		if(lbls != null) {
			for(final String label : lbls.keySet()) {
				final Method innerMethod = lbls.get(label);
				command = annotations.get(innerMethod);
				if(command != null && sender.hasPermission(command.permission())) {
					if(deep) {
						final String[] newArguments = new String[arguments.length + 1];
						for(int i = 0; i < arguments.length; i++) {
							newArguments[i] = arguments[i];
						}
						newArguments[arguments.length] = label;
						addHelpToMap(sender, innerMethod, newArguments, resultMap, deep);
					}
					else {
						if(resultMap.get(command.section()) == null) {
							resultMap.put(command.section(), new ArrayList<String>());
						}
						final StringBuilder cmdString = new StringBuilder();
						final String usage = command.usage();
						cmdString.append(displayedCommand);
						if(arguments.length > 0) {
							cmdString.append(' ').append(implode(arguments));
						}
						cmdString.append(' ').append(label);
						if(!usage.isEmpty()) {
							cmdString.append(' ').append(ChatColor.GOLD).append(usage);
						}
						cmdString.append(ChatColor.GRAY).append(" - ").append(command.desc());
						resultMap.get(command.section()).add(cmdString.toString());
					}
				}
			}
		}
	}
	
	private String getUsage(final String[] args, final int level, final Method method) {
		
		final StringBuilder usage = new StringBuilder();
		
		usage.append(displayedCommand);
		
		if(method != null) {
			for(int i = 0; i <= level; i++) {
				usage.append(' ').append(args[i]);
			}
			final Map<String, Method> lbls = labels.get(method);
			if(lbls == null) {
				usage.append(' ').append(annotations.get(method).usage());
			}
			else {
				boolean first = true;
				usage.append(" <");
				for(final String key : lbls.keySet()) {
					if(first) {
						first = false;
					}
					else {
						usage.append('|');
					}
					usage.append(key);
				}
				usage.append(">");
			}
		}
		else {
			usage.append(' ').append(helpCommand);
		}
		
		return usage.toString();
	}
	
	public String getUsage(final String[] args) {
		final StringBuilder usage = new StringBuilder();
		usage.append(displayedCommand);
		
		Method method = null;
		Method oldMethod = null;
		
		for(final String arg : args) {
			final String lcArg = arg.toLowerCase();
			method = labels.get(oldMethod).get(lcArg);
			if(method == null) {
				method = aliases.get(oldMethod).get(lcArg);
			}
			if(method != null) {
				usage.append(' ').append(lcArg);
				oldMethod = method;
			}
			else {
				break;
			}
		}
		if(labels.get(oldMethod) == null) {
			usage.append(' ').append(annotations.get(oldMethod).usage());
		}
		else {
			final Map<String, Method> lbls = labels.get(oldMethod);
			boolean first = true;
			usage.append(" <");
			for(final String key : lbls.keySet()) {
				if(first) {
					first = false;
				}
				else {
					usage.append('|');
				}
				usage.append(key);
			}
			usage.append(">");
		}
		return usage.toString();
	}
	
	private Object construct(final Class<?> clss) {
		Exception ex = null;
		try {
			Constructor<?> constr = null;
			try {
				constr = clss.getConstructor(classes);
			}
			catch (NoSuchMethodException ignore) {}
			if(constr != null) {
				constr.setAccessible(true);
				return constr.newInstance(arguments);
			}
			else {
				return clss.newInstance();
			}
		}
		catch (final SecurityException e) {
			ex = e;
		}
		catch (final InstantiationException e) {
			ex = e;
		}
		catch (final IllegalAccessException e) {
			ex = e;
		}
		catch (final IllegalArgumentException e) {
			ex = e;
		}
		catch (final InvocationTargetException e) {
			ex = e;
		}
		if(ex != null) {
			logger.log(Level.WARNING, "CommandManager could not create an instance of a class '" + clss.getCanonicalName() + "'.");
		}
		return null;
	}
	
	private String implode(final String[] strs) {
		final StringBuilder result = new StringBuilder();
		final char gl = ' ';
		boolean first = true;
		for(int i = 0; i < strs.length; i++) {
			if(first) {
				first = false;
			}
			else {
				result.append(gl);
			}
			result.append(strs[i]);
		}
		return result.toString();
	}
}
