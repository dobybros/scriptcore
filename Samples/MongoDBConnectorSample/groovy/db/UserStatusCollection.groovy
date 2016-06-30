package db

import script.groovy.annotation.Bean
import connectors.mongodb.MongoCollectionHelper
import connectors.mongodb.annotations.MongoCollection

@MongoCollection(name = "userStatus", databaseClass = UserDatabase.class)
@Bean
class UserStatusCollection extends MongoCollectionHelper {

}
