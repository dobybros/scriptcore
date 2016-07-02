package db

import connectors.mongodb.MongoDatabaseHelper
import connectors.mongodb.annotations.Database

@Database(name = "userdb")
class UserDatabase extends MongoDatabaseHelper {
	
}
