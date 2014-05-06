package me.ragan262.commandmanager.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Command {
	
	String desc() default "";
	
	int min() default 0;
	
	int max() default -1;
	
	String usage() default "";
	
	String permission() default "";
	
	String section() default "";
	
	boolean player() default false;
	
	boolean forceExecute() default false;
}
