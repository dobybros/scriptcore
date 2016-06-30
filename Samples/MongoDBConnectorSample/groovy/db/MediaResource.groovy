package db

import connectors.mongodb.annotations.DocumentField
import connectors.mongodb.annotations.MongoDocument
import connectors.mongodb.codec.BaseObject

@MongoDocument
class MediaResource extends BaseObject{
	@DocumentField(key = "rid")
	private String resourceId;
	
	@DocumentField(key = "trid")
	private String thumbnailResourceId;
	
}
