package db

import java.lang.annotation.Documented;

import connectors.mongodb.annotations.DocumentField
import connectors.mongodb.annotations.MongoDocument
import connectors.mongodb.codec.DataObject

@MongoDocument(collectionClass = UserCollection.class, filters = ["type", "user"])
class UserData extends DataObject{
	@DocumentField(key = "name")
	private String name;
	
	@DocumentField(key = "acc")
	private Account account;
	
	@DocumentField(key = "rids")
	private List<MediaResource> resources;
	
	@DocumentField(key = "type")
	private String type;
	
	@DocumentField(key = "int")
	private int intNum;
	@DocumentField(key = "integer")
	private Integer integerNum;
	
	@DocumentField(key = "data")
	private byte[] data;
	
	@DocumentField(key = "strs")
	private List<String> strs;
	
}
