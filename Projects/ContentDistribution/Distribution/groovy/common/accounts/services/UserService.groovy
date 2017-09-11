package common.accounts.services

import common.accounts.data.User
import common.accounts.databases.UserDatabase
import org.bson.Document
import org.bson.conversions.Bson
import org.bson.types.ObjectId

import script.groovy.annotation.Bean

import com.mongodb.client.FindIterable
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoCursor

import connectors.mongodb.MongoCollectionHelper
import connectors.mongodb.annotations.DBCollection

@DBCollection(name = "user", databaseClass = "common.accounts.databases.UserDatabase")
@Bean
class UserService extends MongoCollectionHelper {
	private User findUser(Bson query) {
		MongoCollection<User> collection = this.getMongoCollection();
		FindIterable<User> iterable = collection.find(query);
		if(iterable != null) {
			MongoCursor<User> cursor = iterable.iterator();
			if(cursor.hasNext()) {
				return cursor.next();
			}
		}
		return null;
	}
	
	public User getUser(String id) {
		return findUser(new Document().append("_id", id));
	}

	public void addUser(User user) {
		if(user.getId() == null)
			user.setId(ObjectId.get().toString());
		user.setCreateTime(System.currentTimeMillis());
		user.setUpdateTime(user.getCreateTime());
		MongoCollection<User> collection = this.getMongoCollection();
		collection.insertOne(user);
	}

	public User getUserFromLogin(String account, String passwordMd5) {
		return findUser(new Document().append(User.FIELD_LOGINACCOUNTS, account).append(User.FIELD_PASSWORD, passwordMd5));
	}
	
}
