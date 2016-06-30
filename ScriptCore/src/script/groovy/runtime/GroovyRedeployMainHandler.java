package script.groovy.runtime;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Map;

import script.groovy.annotation.RedeployMain;
import script.groovy.object.GroovyObjectEx;
import script.groovy.runtime.GroovyRuntime.MyGroovyClassLoader;
import chat.logs.LoggerEx;

public class GroovyRedeployMainHandler extends ClassAnnotationHandler {

	private static final String TAG = GroovyRedeployMainHandler.class.getSimpleName();

	@Override
	public Class<? extends Annotation> handleAnnotationClass() {
		return RedeployMain.class;
	}

	@Override
	public void handleAnnotatedClasses(Map<String, Class<?>> annotatedClassMap,
			MyGroovyClassLoader classLoader) {
		if(annotatedClassMap != null) {
			Collection<Class<?>> values = annotatedClassMap.values();
			for(Class<?> groovyClass : values) {
				GroovyObjectEx<?> groovyObj = getGroovyRuntime().create(groovyClass);
				try {
					groovyObj.invokeRootMethod("main");
				} catch (Throwable t) {
					t.printStackTrace();
					LoggerEx.error(TAG, "Execute redeploy main for " + groovyClass + " failed, " + t.getMessage());
				}
			}
		}
	}

}
