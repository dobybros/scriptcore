package db

import script.groovy.annotation.Bean
import connectors.mongodb.MongoCollectionHelper
import connectors.mongodb.annotations.DBCollection

@DBCollection(name = "userStatus", databaseClass = "db.UserDatabase")
@Bean
class UserStatusCollection extends MongoCollectionHelper {

}
