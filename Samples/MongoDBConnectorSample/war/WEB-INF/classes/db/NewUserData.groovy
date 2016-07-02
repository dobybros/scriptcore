package db

import connectors.mongodb.annotations.DBDocument
import connectors.mongodb.annotations.DocumentField
import connectors.mongodb.codec.DataObject

@DBDocument(collectionClass = UserCollection.class, filters = ["type", "user1"])
class NewUserData extends DataObject{
	@DocumentField(key = "name")
	private String name;

	@DocumentField(key = "type")
	private String type;
}
