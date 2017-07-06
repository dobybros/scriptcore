package connectors.mongodb;

import java.util.HashMap;

import com.mongodb.client.MongoCollection;

import connectors.mongodb.annotations.handlers.MongoDBHandler;
import connectors.mongodb.annotations.handlers.MongoDBHandler.CollectionHolder;
import connectors.mongodb.codec.DataObject;
import script.groovy.runtime.GroovyRuntime;

public class MongoCollectionHelper {
	protected MongoCollection<DataObject> getMongoCollection() {
		ClassLoader classLoader = this.getClass().getClassLoader().getParent();
		if(classLoader != null && classLoader instanceof GroovyRuntime.MyGroovyClassLoader) {
			GroovyRuntime.MyGroovyClassLoader myGroovyClassLoader = (GroovyRuntime.MyGroovyClassLoader) classLoader;
			GroovyRuntime runtime = myGroovyClassLoader.getGroovyRuntime();
			if(runtime != null) {
				MongoDBHandler handler = (MongoDBHandler) runtime.getClassAnnotationHandler(MongoDBHandler.class);
				if(handler != null) {
					HashMap<Class<?>, CollectionHolder> map = handler.getCollectionMap();
					if(map != null) {
						CollectionHolder holder = map.get(this.getClass());
						if(holder != null)
							return holder.getCollection();
					}
				}
			}
		}

		return null;
	};
	
	
}
