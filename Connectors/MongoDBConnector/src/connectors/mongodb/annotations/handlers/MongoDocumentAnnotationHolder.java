package connectors.mongodb.annotations.handlers;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import script.groovy.runtime.ClassAnnotationHandler;
import script.groovy.runtime.GroovyRuntime.MyGroovyClassLoader;
import connectors.mongodb.annotations.MongoDocument;

public class MongoDocumentAnnotationHolder extends ClassAnnotationHandler {
	private static final String TAG = MongoDocumentAnnotationHolder.class.getSimpleName();

	private Map<Class<?>, MongoDocument> documentClassMap = new LinkedHashMap<>();
	
	private static MongoDocumentAnnotationHolder instance;
	public MongoDocumentAnnotationHolder() {
		instance = this;
	}
	
	public static MongoDocumentAnnotationHolder getInstance() {
		return instance;
	}
	
	public void init() {
	}
	
	@Override
	public Class<? extends Annotation> handleAnnotationClass() {
		return MongoDocument.class;
	}

	@Override
	public void handleAnnotatedClasses(Map<String, Class<?>> annotatedClassMap,
			MyGroovyClassLoader classLoader) {
		if(annotatedClassMap != null) {
			Collection<Class<?>> values = annotatedClassMap.values();
			for(Class<?> groovyClass : values) {
				MongoDocument mongoDocument = groovyClass.getAnnotation(MongoDocument.class);
				if(mongoDocument != null) {
					documentClassMap.put((Class<?>) groovyClass, mongoDocument);
				}
			}
		}
	}

	public Map<Class<?>, MongoDocument> getDocumentClassMap() {
		return documentClassMap;
	}

}
