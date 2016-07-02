package db

import connectors.mongodb.annotations.DBDocument
import connectors.mongodb.annotations.DocumentField
import connectors.mongodb.codec.BaseObject

@DBDocument
class MediaResource extends BaseObject{
	@DocumentField(key = "rid")
	private String resourceId;
	
	@DocumentField(key = "trid")
	private String thumbnailResourceId;
	
}
