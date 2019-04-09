package com.docker.annotations;

import java.lang.annotation.*;

@Target(value = {ElementType.METHOD})
@Retention(value = RetentionPolicy.RUNTIME)
@Documented
public @interface PeriodicInvocation {
	public String id();
	public long periodSeconds(); //10 seconds
	public String startTime(); //2018-1-3 11:00:00
	public String timezone() default "0800";
}
