package common.accounts.data

import common.accounts.services.UserService
import connectors.mongodb.annotations.DBDocument
import connectors.mongodb.annotations.DocumentField
import connectors.mongodb.codec.DataObject
import groovy.json.JsonBuilder

@DBDocument(collectionClass = UserService.class)
class User extends DataObject{
	public static final String FIELD_TYPE = "type";
	public static final String FIELD_ACCOUNTS = "accs";
	public static final String FIELD_LOGINACCOUNTS = "la";
	public static final String FIELD_PASSWORD = "pwd";

	@DocumentField(key = "name")
	private String name;

	@DocumentField(key = "titl")
	private String title;

	public static final int TYPE_USER = 1;
	public static final int TYPE_ADMIN = 1000;
	
	@DocumentField(key = "type")
	private int type;

	@DocumentField(key = "accs")
	private List<Account> accounts;
	
	@DocumentField(key = "la")
	private List<String> loginAccounts;
	
	@DocumentField(key = "pwd")
	private String password;
	
	@DocumentField(key = "ctime")
	private long createTime;
	
	@DocumentField(key = "utime")
	private long updateTime;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public List<Account> getAccounts() {
		return accounts;
	}

	public void setAccounts(List<Account> accounts) {
		this.accounts = accounts;
	}

	public List<String> getLoginAccounts() {
		return loginAccounts;
	}

	public void setLoginAccounts(List<String> loginAccounts) {
		this.loginAccounts = loginAccounts;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public long getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(long updateTime) {
		this.updateTime = updateTime;
	}

	@Override
	public String toString() {
		return new JsonBuilder( this ).toPrettyString();
	}
}
