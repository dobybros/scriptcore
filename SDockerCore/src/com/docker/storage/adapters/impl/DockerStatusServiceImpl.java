package com.docker.storage.adapters.impl;

import com.docker.data.DataObject;
import com.docker.data.DockerStatus;
import com.docker.storage.DBException;
import com.docker.utils.SpringContextUtil;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;

import chat.errors.ChatErrorCodes;
import chat.errors.CoreException;
import chat.logs.LoggerEx;
import chat.utils.ChatUtils;

import com.mongodb.client.result.DeleteResult;
import com.docker.storage.adapters.DockerStatusService;
import com.docker.storage.mongodb.daos.DockerStatusDAO;

public class DockerStatusServiceImpl implements DockerStatusService {
	private static final String TAG = DockerStatusServiceImpl.class.getSimpleName();
	private DockerStatusDAO dockerStatusDAO = (DockerStatusDAO) SpringContextUtil.getBean("dockerStatusDAO");

	@Override
	public void deleteDockerStatus(String server) throws CoreException {
		try {
			DeleteResult result = dockerStatusDAO.delete(new Document().append(DockerStatus.FIELD_DOCKERSTATUS_SERVER, server));
			if(result.getDeletedCount() <= 0)
				throw new CoreException(ChatErrorCodes.ERROR_ONLINESERVER_NOT_FOUND, "OnlineServer " + server + " doesn't be found while delete " + server);
		} catch (DBException e) {
			e.printStackTrace();
			throw new CoreException(ChatErrorCodes.ERROR_ONLINESERVER_DELETE_FAILED, "Delete online server failed, " + e.getMessage());
		}
	}

	@Override
	public void addDockerStatus(DockerStatus serverStatus)
			throws CoreException {
		try {
			dockerStatusDAO.add(serverStatus);
		} catch (DBException e) {
			e.printStackTrace();
			if(e.getType() == DBException.ERRORTYPE_DUPLICATEKEY) {
				String serverStr = ChatUtils.generateFixedRandomString();
				LoggerEx.error(TAG, "Duplicated key while adding present server s = " + serverStatus.getServer() + " regenerating... new s = " + serverStr);
				serverStatus.setServer(serverStr);
				addDockerStatus(serverStatus);
			}
			throw new CoreException(ChatErrorCodes.ERROR_ONLINESERVER_ADD_FAILED, "Add onlineServer " + serverStatus + " failed, " + e.getMessage());
		}
	}

	@Override
	public void addService(String server, String service)
			throws CoreException {
		try {
			dockerStatusDAO.updateOne(Filters.eq(DockerStatus.FIELD_DOCKERSTATUS_SERVER, server), Updates.addToSet(DockerStatus.FIELD_DOCKERSTATUS_SERVICES, service), false);
		} catch (DBException e) {
			throw new CoreException(ChatErrorCodes.ERROR_ONLINESERVER_UPDATE_FAILED, "Add service " + service + " to server " + server + " failed, " + e.getMessage());
		}
	}

	@Override
	public void deleteService(String server, String service) throws CoreException {
		try {
			dockerStatusDAO.updateOne(Filters.eq(DockerStatus.FIELD_DOCKERSTATUS_SERVER, server), Updates.pull(DockerStatus.FIELD_DOCKERSTATUS_SERVICES, service), false);
		} catch (DBException e) {
			throw new CoreException(ChatErrorCodes.ERROR_ONLINESERVER_UPDATE_FAILED, "Delete service " + service + " to server " + server + " failed, " + e.getMessage());
		}
	}

	@Override
	public void update(String server, DockerStatus serverStatus)
			throws CoreException {
		try {
			Document update = serverStatus.toDocument();
			update.remove(DataObject.FIELD_ID);
			dockerStatusDAO.updateOne(new Document().append(DockerStatus.FIELD_DOCKERSTATUS_SERVER, server), new Document().append("$set", update), false);
//			serverPresentDAO.update(new BasicDBObject().append(ServerPresent.FIELD_SERVERPRESENT_SERVER, server), new BasicDBObject().append("$set", presentServer.toDocument()), false, false);
		} catch (DBException e) {
			e.printStackTrace();
			throw new CoreException(ChatErrorCodes.ERROR_ONLINESERVER_UPDATE_FAILED, "Update onlineServer " + server + " failed, " + e.getMessage());
		}
	}

	@Override
	public DockerStatus getDockerStatusByServer(String server)
			throws CoreException {
		try {
			Document query = new Document().append(DockerStatus.FIELD_DOCKERSTATUS_SERVER, server);
			DockerStatus serverPresent = (DockerStatus) dockerStatusDAO.findOne(query);
			return serverPresent;
		} catch (DBException e) {
			e.printStackTrace();
			throw new CoreException(ChatErrorCodes.ERROR_ONLINESERVER_QUERY_FAILED, "Query server present server failed, " + e.getMessage());
		}
	}

}