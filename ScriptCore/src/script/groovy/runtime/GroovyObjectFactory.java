package script.groovy.runtime;

import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;

import script.groovy.object.GroovyObjectEx;

public class GroovyObjectFactory {
	@Resource
	private GroovyRuntime groovyRuntime;
	
	private ConcurrentHashMap<String, GroovyObjectEx> beanMap = new ConcurrentHashMap<String, GroovyObjectEx>();
	
	private static GroovyObjectFactory instance;

	public static GroovyObjectFactory getInstance() {
		return instance;
	}

	public GroovyObjectFactory() {
		instance = this;
	}
	
	public <T> GroovyObjectEx<T> getObject(Class<?> c) {
		String groovyPath = GroovyRuntime.path(c);
		GroovyObjectEx<T> goe = beanMap.get(groovyPath);
		if(goe == null) {
			goe = groovyRuntime.create(groovyPath);
			GroovyObjectEx<T> oldgoe = beanMap.putIfAbsent(groovyPath, goe);
			if(oldgoe != null) 
				goe = oldgoe;
		}
		return goe;
	}
	
	public synchronized void init() {
		
	}

	public GroovyRuntime getGroovyRuntime() {
		return groovyRuntime;
	}

	public void setGroovyRuntime(GroovyRuntime groovyRuntime) {
		this.groovyRuntime = groovyRuntime;
	}
}
