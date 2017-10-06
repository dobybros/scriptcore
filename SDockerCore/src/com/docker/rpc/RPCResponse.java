package com.docker.rpc;


public abstract class RPCResponse extends RPCBase {
	private RPCRequest request;

	public RPCResponse(String type) {
		super(type);
	}

	public RPCRequest getRequest() {
		return request;
	}

	public void setRequest(RPCRequest request) {
		this.request = request;
	}
}
