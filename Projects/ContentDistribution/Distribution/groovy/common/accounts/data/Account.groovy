package common.accounts.data

import connectors.mongodb.annotations.DBDocument

import java.util.List;

import connectors.mongodb.annotations.DocumentField
import connectors.mongodb.codec.BaseObject

@DBDocument
class Account extends BaseObject{
	@DocumentField(key = "acc")
	private String account;

	public Account() {
	}

	public Account(String account) {
		this.account = account;
	}

	String getAccount() {
		return account
	}

	void setAccount(String account) {
		this.account = account
	}

}
