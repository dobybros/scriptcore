package db

import connectors.mongodb.MongoDatabaseHelper
import connectors.mongodb.annotations.MongoDatabase

@MongoDatabase(name = "userdb")
class UserDatabase extends MongoDatabaseHelper {
	
}
