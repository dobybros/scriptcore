package connectors.mongodb;

import java.util.HashMap;

import com.mongodb.client.MongoDatabase;

import connectors.mongodb.annotations.handlers.MongoDBHandler;


public class MongoDatabaseHelper {
	protected MongoDatabase getMongoDatabase() {
		MongoDBHandler handler = MongoDBHandler.getInstance();
		if(handler != null) {
			HashMap<Class<?>, MongoDatabase> map = handler.getDatabaseMap();
			if(map != null) {
				MongoDatabase database = map.get(this.getClass());
				return database;
			}
		}
		return null;
	}
}
