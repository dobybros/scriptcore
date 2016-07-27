package common.distribution

import connectors.mongodb.annotations.DBDocument
import connectors.mongodb.annotations.DocumentField
import connectors.mongodb.codec.DataObject
import groovy.json.JsonBuilder

@DBDocument(collectionClass = ArticleService.class)
class Article extends DataObject{
	public static final String FIELD_COMPANYID = "cid";
	public static final String FIELD_USERID = "uid";
	public static final String FIELD_CREATETIME = "ctime";

	public static final int TYPE_RAW = 1;
	public static final int TYPE_GENERATED = 10;

	@DocumentField(key = "type")
	private Integer type;

	@DocumentField(key = "titl")
	private String title;

	@DocumentField(key = "sum")
	private String summary;

	@DocumentField(key = "url")
	private String url;

	@DocumentField(key = "uid")
	private String userId;

	//Consider an article can belong to multiple companies. Because user can be employees for multiple companies.
	@DocumentField(key = "cids")
	private List<String> companyIds;

	@DocumentField(key = "ctime")
	private Long createTime;

	@DocumentField(key = "utime")
	private Long updateTime;


	String getTitle() {
		return title
	}

	void setTitle(String title) {
		this.title = title
	}

	String getSummary() {
		return summary
	}

	void setSummary(String summary) {
		this.summary = summary
	}

	String getUrl() {
		return url
	}

	void setUrl(String url) {
		this.url = url
	}

	String getUserId() {
		return userId
	}

	void setUserId(String userId) {
		this.userId = userId
	}

	Long getCreateTime() {
		return createTime
	}

	void setCreateTime(Long createTime) {
		this.createTime = createTime
	}

	Long getUpdateTime() {
		return updateTime
	}

	void setUpdateTime(Long updateTime) {
		this.updateTime = updateTime
	}
	List<String> getCompanyIds() {
		return companyIds
	}

	void setCompanyIds(List<String> companyIds) {
		this.companyIds = companyIds
	}

	Integer getType() {
		return type
	}

	void setType(Integer type) {
		this.type = type
	}

	@Override
	public String toString() {
		return new JsonBuilder( this ).toPrettyString();
	}
}
