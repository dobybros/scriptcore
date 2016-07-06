package solr

import jdk.internal.dynalink.linker.LinkerServices.Implementation;
import groovy.json.JsonBuilder
import common.distribution.Article
import connectors.mongodb.codec.DataObject
import connectors.solr.SolrData
import connectors.solr.annotations.DocumentField
import connectors.solr.annotations.SolrDocument

@SolrDocument(core = "articles")
class ArticleIndex implements SolrData{
	public static final String FIELD_COMPANYID = "cid";
	public static final String FIELD_USERID = "uid";
	@DocumentField(key = "id")
	private String id;
	@DocumentField(key = "title")
	private String title;
	@DocumentField(key = "summary")
	private String summary;
	@DocumentField(key = "url")
	private String url;
	/**
	 * content of html url, the content is only for search. 
	 */
	@DocumentField(key = "content")
	private String content;
	@DocumentField(key = "userId")
	private String userId;

	//Consider an article can belong to multiple companies. Because user can be employees for multiple companies.
	@DocumentField(key = "companyIds")
	private List<String> companyIds;

	@DocumentField(key = "createTime")
	private Long createTime;
	@DocumentField(key = "updateTime")
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
	@Override
	public String toString() {
		return new JsonBuilder( this ).toPrettyString();
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
	
	public void fromArticle(Article article) {
		companyIds = article.getCompanyIds();
		createTime = article.getCreateTime();
		id = article.getId();
		summary = article.getSummary();
		title = article.getTitle();
		updateTime = article.getUpdateTime();
		url = article.getUrl();
		userId = article.getUserId();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
