package connectors.mongodb;

import java.util.HashMap;

import com.mongodb.client.MongoCollection;

import connectors.mongodb.annotations.handlers.MongoDBHandler;
import connectors.mongodb.annotations.handlers.MongoDBHandler.CollectionHolder;
import connectors.mongodb.codec.DataObject;

public class MongoCollectionHelper {
	protected MongoCollection<DataObject> getMongoCollection() {
		MongoDBHandler handler = MongoDBHandler.getInstance();
		if(handler != null) {
			HashMap<Class<?>, CollectionHolder> map = handler.getCollectionMap();
			if(map != null) {
				CollectionHolder holder = map.get(this.getClass());
				if(holder != null)
					return holder.getCollection();
			}
		}
		return null;
	};
	
	
}
