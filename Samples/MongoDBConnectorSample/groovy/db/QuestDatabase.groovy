package db

import connectors.mongodb.MongoDatabaseHelper
import connectors.mongodb.annotations.Database

@Database(name = "chat_quest")
class QuestDatabase extends MongoDatabaseHelper {
	
}