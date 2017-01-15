package db

import connectors.mongodb.MongoDatabaseHelper
import connectors.mongodb.annotations.Database

@Database(name = "usertestdb")
class UserDatabase extends MongoDatabaseHelper {
	
}
