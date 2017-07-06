package connectors.mongodb;

import java.util.HashMap;

import com.mongodb.client.MongoDatabase;

import connectors.mongodb.annotations.handlers.MongoDBHandler;
import script.groovy.runtime.GroovyRuntime;


public class MongoDatabaseHelper {

	protected MongoDatabase getMongoDatabase() {
		ClassLoader classLoader = this.getClass().getClassLoader().getParent();
		if(classLoader != null && classLoader instanceof GroovyRuntime.MyGroovyClassLoader) {
			GroovyRuntime.MyGroovyClassLoader myGroovyClassLoader = (GroovyRuntime.MyGroovyClassLoader) classLoader;
			GroovyRuntime runtime = myGroovyClassLoader.getGroovyRuntime();
			if (runtime != null) {
				MongoDBHandler handler = (MongoDBHandler) runtime.getClassAnnotationHandler(MongoDBHandler.class);
				if(handler != null) {
					HashMap<Class<?>, MongoDatabase> map = handler.getDatabaseMap();
					if(map != null) {
						MongoDatabase database = map.get(this.getClass());
						return database;
					}
				}
			}
		}
		return null;
	}
}
