package connectors.mongodb.annotations.handlers;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;

import connectors.mongodb.MongoClientHelper;
import connectors.mongodb.MongoCollectionHelper;
import connectors.mongodb.annotations.DBCollection;
import connectors.mongodb.annotations.Database;
import connectors.mongodb.annotations.DBDocument;
import script.groovy.annotation.Bean;
import script.groovy.object.GroovyObjectEx;
import script.groovy.runtime.ClassAnnotationHandler;
import script.groovy.runtime.GroovyRuntime;
import script.groovy.runtime.GroovyRuntime.MyGroovyClassLoader;

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
