package db

import script.groovy.annotation.Bean
import connectors.mongodb.MongoCollectionHelper
import connectors.mongodb.annotations.DBCollection

@DBCollection(name = "user", databaseClass = UserDatabase.class)
@Bean
class UserCollection extends MongoCollectionHelper {

}
