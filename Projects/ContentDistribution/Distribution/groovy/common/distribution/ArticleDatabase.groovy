package common.distribution

import connectors.mongodb.MongoDatabaseHelper
import connectors.mongodb.annotations.Database

@Database(name = "articledb")
class ArticleDatabase extends MongoDatabaseHelper {
	
}
