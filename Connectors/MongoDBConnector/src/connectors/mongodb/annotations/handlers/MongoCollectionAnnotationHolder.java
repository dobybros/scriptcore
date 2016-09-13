package connectors.mongodb.annotations.handlers;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import script.groovy.runtime.ClassAnnotationHandler;
import script.groovy.runtime.GroovyRuntime;
import script.groovy.runtime.GroovyRuntime.MyGroovyClassLoader;
import connectors.mongodb.annotations.DBCollection;

public class MongoCollectionAnnotationHolder implements ClassAnnotationHandler {
	private static final String TAG = MongoCollectionAnnotationHolder.class.getSimpleName();

	private Map<Class<?>, DBCollection> collectionClassMap = new LinkedHashMap<>();
	
	private static MongoCollectionAnnotationHolder instance;
	public MongoCollectionAnnotationHolder() {
		instance = this;
	}
	
	public static MongoCollectionAnnotationHolder getInstance() {
		return instance;
	}
	
	public void init() {
	}
	
	@Override
	public Class<? extends Annotation> handleAnnotationClass(GroovyRuntime groovyRuntime) {
		return DBCollection.class;
	}

	@Override
	public void handleAnnotatedClasses(Map<String, Class<?>> annotatedClassMap,
			MyGroovyClassLoader classLoader) {
		if(annotatedClassMap != null) {
			Collection<Class<?>> values = annotatedClassMap.values();
			for(Class<?> groovyClass : values) {
				DBCollection mongoCollection = groovyClass.getAnnotation(DBCollection.class);
				if(mongoCollection != null) {
					collectionClassMap.put(groovyClass, mongoCollection);
				}
			}
		}
	}

	public Map<Class<?>, DBCollection> getCollectionClassMap() {
		return collectionClassMap;
	}

}
