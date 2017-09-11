package common.accounts.data

import common.accounts.services.CompanyService
import connectors.mongodb.annotations.DBDocument
import connectors.mongodb.annotations.DocumentField
import connectors.mongodb.codec.DataObject
import groovy.json.JsonBuilder

@DBDocument(collectionClass = "common.accounts.services.CompanyService")
class Company extends DataObject{
	public static final String FIELD_CREATETIME = "ctime";
	public static final String FIELD_UPDATETIME = "utime";
	public static final String FIELD_NAME = "name";
	public static final String FIELD_EMPLOYEEIDS = "yees";

	@DocumentField(key = "name")
	private String name;

	@DocumentField(key = "ctime")
	private long createTime;

	@DocumentField(key = "utime")
	private long updateTime;

	@DocumentField(key = "yers")
	private List<String> employerIds;

	@DocumentField(key = "yees")
	private List<String> employeeIds

	@DocumentField(key = "uid")
	private String userId;

	String getUserId() {
		return userId
	}

	void setUserId(String userId) {
		this.userId = userId
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	long getCreateTime() {
		return createTime
	}

	void setCreateTime(long createTime) {
		this.createTime = createTime
	}

	long getUpdateTime() {
		return updateTime
	}

	void setUpdateTime(long updateTime) {
		this.updateTime = updateTime
	}
	List<String> getEmployerIds() {
		return employerIds
	}

	void setEmployerIds(List<String> employerIds) {
		this.employerIds = employerIds
	}

	List<String> getEmployeeIds() {
		return employeeIds
	}

	void setEmployeeIds(List<String> employeeIds) {
		this.employeeIds = employeeIds
	}
	@Override
	public String toString() {
		return new JsonBuilder( this ).toPrettyString();
	}
}
