package db

import script.groovy.annotation.Bean
import connectors.mongodb.MongoCollectionHelper
import connectors.mongodb.annotations.DBCollection

@DBCollection(name = "user", databaseClass = "db.UserDatabase")
@Bean
class UserCollection extends MongoCollectionHelper {

}
