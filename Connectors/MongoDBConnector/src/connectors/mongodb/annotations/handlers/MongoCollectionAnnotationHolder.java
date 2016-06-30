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
import connectors.mongodb.annotations.MongoCollection;
import connectors.mongodb.annotations.MongoDatabase;
import connectors.mongodb.annotations.MongoDocument;
import script.groovy.annotation.Bean;
import script.groovy.object.GroovyObjectEx;
import script.groovy.runtime.ClassAnnotationHandler;
import script.groovy.runtime.GroovyRuntime;
import script.groovy.runtime.GroovyRuntime.MyGroovyClassLoader;

public class MongoCollectionAnnotationHolder extends ClassAnnotationHandler {
	private static final String TAG = MongoCollectionAnnotationHolder.class.getSimpleName();

	private Map<Class<?>, MongoCollection> collectionClassMap = new LinkedHashMap<>();
	
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
	public Class<? extends Annotation> handleAnnotationClass() {
		return MongoCollection.class;
	}

	@Override
	public void handleAnnotatedClasses(Map<String, Class<?>> annotatedClassMap,
			MyGroovyClassLoader classLoader) {
		if(annotatedClassMap != null) {
			Collection<Class<?>> values = annotatedClassMap.values();
			for(Class<?> groovyClass : values) {
				MongoCollection mongoCollection = groovyClass.getAnnotation(MongoCollection.class);
				if(mongoCollection != null) {
					collectionClassMap.put(groovyClass, mongoCollection);
				}
			}
		}
	}

	public Map<Class<?>, MongoCollection> getCollectionClassMap() {
		return collectionClassMap;
	}

}
