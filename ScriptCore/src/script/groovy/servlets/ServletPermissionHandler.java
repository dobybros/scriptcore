package script.groovy.servlets;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import chat.logs.LoggerEx;
import chat.utils.HashTree;
import script.groovy.object.GroovyObjectEx;
import script.groovy.runtime.ClassAnnotationHandler;
import script.groovy.runtime.GroovyRuntime;
import script.groovy.runtime.GroovyRuntime.MyGroovyClassLoader;
import script.groovy.servlet.annotation.ControllerMapping;
import script.groovy.servlet.annotation.PermissionIntercepter;
import script.groovy.servlet.annotation.RequestMapping;
public class ServletPermissionHandler implements ClassAnnotationHandler {

	public ServletPermissionHandler() {
	}

	@Override
	public Class<? extends Annotation> handleAnnotationClass(GroovyRuntime groovyRuntime) {
		return PermissionIntercepter.class;
	}

	@Override
	public void handleAnnotatedClasses(Map<String, Class<?>> annotatedClassMap,
			MyGroovyClassLoader classLoader) {
		GroovyRuntime groovyRuntime = GroovyRuntime.getInstance();
		if(annotatedClassMap != null && !annotatedClassMap.isEmpty()) {
			StringBuilder uriLogs = new StringBuilder("\r\n---------------------------------------\r\n");
			HashTree<String, RequestURIWrapper> tree = new HashTree<String, RequestURIWrapper>();
			HashMap<String, GroovyObjectEx<RequestIntercepter>> iMap = new HashMap<String, GroovyObjectEx<RequestIntercepter>>();
			
			Set<String> keys = annotatedClassMap.keySet();
			for (String key : keys) {
				Class<?> groovyClass = annotatedClassMap.get(key);
				RequestURI requestUri = null;
				GroovyObjectEx<script.groovy.servlets.GroovyServletManager.PermissionIntercepter> intercepter = groovyRuntime
						.create(groovyClass);
				
				GroovyServletManager.getInstance().setPermissionIntercepter(intercepter);
			}
			this.servletTree = tree;
			this.interceptorMap = iMap;
			uriLogs.append("---------------------------------------");
			LoggerEx.info(TAG, uriLogs.toString());
		}
	}

}
