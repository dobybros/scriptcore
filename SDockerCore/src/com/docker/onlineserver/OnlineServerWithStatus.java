package com.docker.onlineserver;

import com.docker.server.OnlineServer;

public class OnlineServerWithStatus extends OnlineServer {
	private Integer maxUsers;
	
	private Integer statusForBalancer;
	private boolean changingForBalancer;
	private String publicDomain;
	
	OnlineServerWithStatus() {
		super();
	}
	
	
	public Integer getMaxUsers() {
		return maxUsers;
	}
	
	public void setMaxUsers(Integer maxUsers) {
		this.maxUsers = maxUsers;
	}
	
	public Integer getStatusForBalancer() {
		return statusForBalancer;
	}
	
	public void setStatusForBalancer(Integer statusForBalancer) {
		this.statusForBalancer = statusForBalancer;
	}
	
	public boolean isChangingForBalancer() {
		return changingForBalancer;
	}
	
	public void setChangingForBalancer(boolean changingForBalancer) {
		this.changingForBalancer = changingForBalancer;
	}
	
	public String getPublicDomain() {
		return publicDomain;
	}
	
	public void setPublicDomain(String publicDomain) {
		this.publicDomain = publicDomain;
	}
}
