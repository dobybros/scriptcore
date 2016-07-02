package db

import connectors.mongodb.annotations.DBDocument
import connectors.mongodb.annotations.DocumentField
import connectors.mongodb.codec.BaseObject

@DBDocument
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
