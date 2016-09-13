package script.groovy.servlets;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import script.groovy.object.GroovyObjectEx;
import script.groovy.runtime.ClassAnnotationHandler;
import script.groovy.runtime.GroovyRuntime;
import script.groovy.runtime.GroovyRuntime.MyGroovyClassLoader;
import script.groovy.servlet.annotation.RequestPermission;
import script.groovy.servlets.GroovyServletManager.PermissionIntercepter;
import chat.logs.LoggerEx;
import chat.utils.ChatUtils;
import chat.utils.HashTree;
public class RequestPermissionHandler implements ClassAnnotationHandler {

	private static final String TAG = RequestPermissionHandler.class.getSimpleName();

	public RequestPermissionHandler() {
	}

	@Override
	public Class<? extends Annotation> handleAnnotationClass(GroovyRuntime groovyRuntime) {
		return RequestPermission.class;
	}

	@Override
	public void handleAnnotatedClasses(Map<String, Class<?>> annotatedClassMap,
			MyGroovyClassLoader classLoader) {
		GroovyRuntime groovyRuntime = GroovyRuntime.getInstance();
		if(annotatedClassMap != null && !annotatedClassMap.isEmpty()) {
			StringBuilder uriLogs = new StringBuilder("\r\n---------------------------------------\r\n");
			
			Set<String> keys = annotatedClassMap.keySet();
			GroovyObjectEx<PermissionIntercepter> intercepter = null;
			
			for (String key : keys) {
				if(intercepter == null) {
					Class<?> groovyClass = annotatedClassMap.get(key);
					intercepter = groovyRuntime.create(groovyClass);
					
					uriLogs.append("Mapped " + key + " | " + groovyClass + " to request permission intercepter." + "\r\n");
					GroovyServletManager.getInstance().setPermissionIntercepter(intercepter);
				} else {
					uriLogs.append("Ignored " + key + " to request permission intercepter." + "\r\n");
				}
			}
			uriLogs.append("---------------------------------------");
			LoggerEx.info(TAG, uriLogs.toString());
		}
	}

}
