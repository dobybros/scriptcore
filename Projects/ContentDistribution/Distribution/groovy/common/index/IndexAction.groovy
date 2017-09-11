package common.index

import connectors.mongodb.annotations.DBDocument
import connectors.mongodb.annotations.DocumentField
import connectors.mongodb.codec.DataObject
import groovy.json.JsonBuilder

@DBDocument(collectionClass = "common.index.IndexActionService")
class IndexAction extends DataObject{
	public static final String FIELD_CREATETIME = "ctime";
	
	public static final int ACTION_ADD = 1;
	public static final int ACTION_UPDATE = 2;
	public static final int ACTION_DELETE = 3;

	@DocumentField(key = "uid")
	private String userId;

	@DocumentField(key = "act")
	private Integer action;

	@DocumentField(key = "ctime")
	private Long createTime;

	public static final int TYPE_ARTICLE = 1;

	@DocumentField(key = "type")
	private Integer type;
	
	@DocumentField(key = "tid")
	private String targetId;
	
	/**
	 * For future optimization for multi-tasking. 
	 * targetId.hashcode() % 1000. 
	 */
	@DocumentField(key = "num")
	private Integer targetIdHashScore;
	
	@DocumentField(key = "tupd")
	private Long targetUpdateTime;

	String getUserId() {
		return userId
	}

	void setUserId(String userId) {
		this.userId = userId
	}

	Integer getAction() {
		return action
	}

	void setAction(Integer action) {
		this.action = action
	}

	Long getCreateTime() {
		return createTime
	}

	void setCreateTime(Long createTime) {
		this.createTime = createTime
	}

	Integer getType() {
		return type
	}

	void setType(Integer type) {
		this.type = type
	}

	String getTargetId() {
		return targetId
	}

	void setTargetId(String targetId) {
		this.targetId = targetId
		if(this.targetId != null) {
			targetIdHashScore = this.targetId.hashCode() % 1000;
		}
	}

	@Override
	public String toString() {
		return new JsonBuilder( this ).toPrettyString();
	}

	public Long getTargetUpdateTime() {
		return targetUpdateTime;
	}

	public void setTargetUpdateTime(Long targetUpdateTime) {
		this.targetUpdateTime = targetUpdateTime;
	}
}
