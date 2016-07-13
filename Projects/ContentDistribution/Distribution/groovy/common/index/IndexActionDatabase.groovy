package common.index

import connectors.mongodb.MongoDatabaseHelper
import connectors.mongodb.annotations.Database

@Database(name = "indexactiondb")
class IndexActionDatabase extends MongoDatabaseHelper {
	
}
