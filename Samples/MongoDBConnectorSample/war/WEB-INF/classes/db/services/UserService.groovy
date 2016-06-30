package db.services

import org.bson.Document

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;

import script.groovy.annotation.Bean
import script.groovy.object.GroovyObjectEx
import db.UserCollection
import db.UserData

@Bean
class UserService {
	
	@Bean
	private GroovyObjectEx<UserCollection> userCollection;
	
	public UserData getUser(String id) {
		FindIterable<UserData> iterable = userCollection.getObject().getMongoCollection().find(new Document().append("_id", id));
		MongoCursor<UserData> cursor = iterable.iterator();
		while(cursor.hasNext()) {
			UserData user = cursor.next();
			return user;
		}
		return null;
	}
	
	public String hello123() {
		return "hello";
	}
}
