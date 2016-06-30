package db

import java.util.List;

import connectors.mongodb.annotations.DocumentField
import connectors.mongodb.annotations.MongoDocument
import connectors.mongodb.codec.BaseObject

@MongoDocument
class Account extends BaseObject{
	@DocumentField(key = "acc")
	private String loginAccount;
	
	@DocumentField(key = "res")
	private MediaResource mediaResource;
	
	@DocumentField(key = "rids")
	private List<MediaResource> resources;
	
	@DocumentField(key = "strs")
	private List<String> strs;
}
