package com.docker.storage.adapters;

import chat.errors.CoreException;

import com.docker.data.DockerStatus;
import com.docker.data.Service;


/**
 * 管理服务器在线状态的接口
 * 
 * 使用Lan里的MongoDB数据库
 * 
 * @author aplombchen
 *
 */
public interface DockerStatusService {

	void deleteDockerStatus(String server) throws CoreException;

	void addDockerStatus(DockerStatus serverStatus)
			throws CoreException;

	void addService(String server, Service service)
			throws CoreException;

	void deleteService(String server, String service, Integer version) throws CoreException;

	void update(String server, DockerStatus serverStatus)
			throws CoreException;

	DockerStatus getDockerStatusByServer(String server)
			throws CoreException;
}
