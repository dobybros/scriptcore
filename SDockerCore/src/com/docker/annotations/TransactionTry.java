package com.docker.annotations;

import java.lang.annotation.*;

@Target(value = {ElementType.METHOD})
@Retention(value = RetentionPolicy.RUNTIME)
@Documented
public @interface TransactionTry {
	public String id();
	public int order();
}
