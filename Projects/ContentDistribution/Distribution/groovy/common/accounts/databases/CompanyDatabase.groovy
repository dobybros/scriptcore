package common.accounts.databases

import connectors.mongodb.MongoDatabaseHelper
import connectors.mongodb.annotations.Database

@Database(name = "companydb")
class CompanyDatabase extends MongoDatabaseHelper {
	
}
