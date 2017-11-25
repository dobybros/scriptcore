package com.docker.script.annotations;

import com.docker.script.BaseRuntime;
import script.groovy.runtime.GroovyRuntime;

public interface ServiceNotFoundListener {
	public BaseRuntime getRuntimeWhenNotFound(String service);
}
