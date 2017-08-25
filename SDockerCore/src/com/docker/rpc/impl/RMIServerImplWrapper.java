package com.docker.rpc.impl;

import chat.errors.ChatErrorCodes;
import chat.errors.CoreException;
import chat.logs.LoggerEx;
import com.docker.rpc.*;
import com.docker.rpc.annotations.RPCServerHandler;
import org.apache.commons.lang.StringUtils;
import script.groovy.object.GroovyObjectEx;
import script.groovy.runtime.ClassAnnotationHandler;
import script.groovy.runtime.GroovyRuntime;
import script.groovy.runtime.GroovyRuntime.MyGroovyClassLoader;

import java.lang.annotation.Annotation;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class RMIServerImplWrapper {
	RMIServerHandler rmiServerHandler;
	//Server
	RPCServerMethodInvocation serverMethodInvocation = new RPCServerMethodInvocation();

	Integer port;

	RMIServer server;

	public RMIServer initServer(boolean enableSsl) {
		if(server == null) {
			try {
				if (enableSsl) {
					server = new RMIServerImpl(port + 1, this, enableSsl);
				} else {
					server = new RMIServerImpl(port + 1, this);
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return server;
	}

	public void initClient(RMIServer server) {
		this.server = server;
	}

	public RMIServer getServer() {
		return server;
	}

	public RMIServerImplWrapper(Integer port) throws RemoteException {
//		super(port + 1);
		this.port = port;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -4853473944368414096L;
	private static final String TAG = RMIServerImplWrapper.class.getSimpleName();

	public RMIServerHandler getRmiServerHandler() {
		return rmiServerHandler;
	}

	public void setRmiServerHandler(RMIServerHandler rmiServerHandler) {
		this.rmiServerHandler = rmiServerHandler;
	}
}

