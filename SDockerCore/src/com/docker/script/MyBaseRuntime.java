package com.docker.script;

import chat.errors.CoreException;
import chat.logs.LoggerEx;
import com.docker.data.Lan;
import com.docker.rpc.remote.skeleton.ServiceSkeletonAnnotationHandler;
import com.docker.rpc.remote.stub.ServiceStubManager;
import com.docker.server.OnlineServer;
import com.docker.storage.adapters.LansService;
import com.docker.utils.SpringContextUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import script.groovy.runtime.GroovyRuntime;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MyBaseRuntime extends BaseRuntime {
	private static final String TAG = MyBaseRuntime.class.getSimpleName();
	private String remoteServiceHost;
	private ServiceStubManager serviceStubManager;
	private ConcurrentHashMap<String, ServiceStubManager> stubManagerForLanIdMap = new ConcurrentHashMap<>();

	private LansService lansService = (LansService) SpringContextUtil.getBean("lansService");

	public void resetServiceStubManager(Class<?> proxyClass) {
		if(serviceStubManager != null) {
			serviceStubManager.setServiceStubProxyClass(proxyClass);
			serviceStubManager.clearCache();
			serviceStubManager.init();
		}
	}

	public void resetServiceStubManagerForLans(Class<?> proxyClass) {
		Collection<ServiceStubManager> managers = stubManagerForLanIdMap.values();
		for(ServiceStubManager manager : managers) {
			manager.setServiceStubProxyClass(proxyClass);
			manager.clearCache();
			manager.init();
		}
	}

	public ServiceStubManager getServiceStubManager(String lanId) {
		ServiceStubManager manager = stubManagerForLanIdMap.get(lanId);
		if (manager == null) {
			if (lanId.equals(OnlineServer.getInstance().getLanId())) {
				// 本地访问
				return serviceStubManager;
			}
			synchronized (stubManagerForLanIdMap) {
				manager = stubManagerForLanIdMap.get(lanId);
				if(manager == null) {
					manager = new ServiceStubManager();
					manager.setUsePublicDomain(true);
					OnlineServer onlineServer = OnlineServer.getInstance();
					manager.setClientTrustJksPath(onlineServer.getRpcSslClientTrustJksPath());
					manager.setJksPwd(onlineServer.getRpcSslJksPwd());
					manager.setServerJksPath(onlineServer.getRpcSslServerJksPath());
					manager.setServiceStubProxyClass(serviceStubManager.getServiceStubProxyClass());

					if(lansService == null)
					    return null;
					Lan lan = null;
					try {
						lan = lansService.getLan(lanId);
					} catch (CoreException e) {
						e.printStackTrace();
						LoggerEx.error(TAG, "Read lan " + lanId + " information failed, " + e.getMessage());
					}
					if(lan == null)
						throw new NullPointerException("Lan is null for lanId " + lanId);
					if(lan.getDomain() != null && lan.getPort() != null && lan.getProtocol() != null)
						throw new NullPointerException("Lan " + lan + " is illegal for lanId " + lanId);
					manager.setHost(lan.getProtocol() + "://" + lan.getDomain() + ":" + lan.getPort());
                    manager.init();
					stubManagerForLanIdMap.putIfAbsent(lanId, manager);
					manager = stubManagerForLanIdMap.get(lanId);
				}
			}
		}
		return manager;
	}

	@Override
	public void prepare(String service, Properties properties, String localScriptPath) {
		super.prepare(service, properties, localScriptPath);
		ServiceSkeletonAnnotationHandler serviceSkeletonAnnotationHandler = new ServiceSkeletonAnnotationHandler();
		serviceSkeletonAnnotationHandler.setService(getServiceName());
		addClassAnnotationHandler(serviceSkeletonAnnotationHandler);

		remoteServiceHost = properties.getProperty("remote.service.host");
		if(remoteServiceHost != null) {
//			ServiceStubManager serviceStubManager = ServiceStubManager.getInstance();
			serviceStubManager = new ServiceStubManager(service);
			serviceStubManager.setHost(remoteServiceHost);
		}
	}
	public void beforeDeploy() {
		if(remoteServiceHost != null) {
			String code =
					"package script.groovy.runtime\n" +
					"@script.groovy.annotation.RedeployMain\n" +
					"class ServiceStubProxy extends com.docker.rpc.remote.stub.Proxy implements GroovyInterceptable{\n" +
					"    private Class<?> remoteServiceStub;\n" +
					"    ServiceStubProxy() {\n" +

					"        super(null, null);\n" +
					"    }\n" +
					"    ServiceStubProxy(com.docker.rpc.remote.stub.RemoteServiceDiscovery remoteServiceDiscovery, Class<?> remoteServiceStub, com.docker.rpc.remote.stub.ServiceStubManager serviceStubManager) {\n" +
					"        super(remoteServiceDiscovery, serviceStubManager)\n" +
					"        this.remoteServiceStub = remoteServiceStub;\n" +
					"    }\n" +
					"    def methodMissing(String methodName,methodArgs) {\n" +
					"        Long crc = chat.utils.ReflectionUtil.getCrc(remoteServiceStub, methodName, remoteServiceDiscovery.getServiceName());\n" +
					"        return invoke(crc, methodArgs);\n" +
					"    }\n" +
					"    public static def getProxy(com.docker.rpc.remote.stub.RemoteServiceDiscovery remoteServiceDiscovery, Class<?> remoteServiceStub, com.docker.rpc.remote.stub.ServiceStubManager serviceStubManager) {\n" +
					"        ServiceStubProxy proxy = new ServiceStubProxy(remoteServiceDiscovery, remoteServiceStub, serviceStubManager)\n" +
					"        def theProxy = proxy.asType(proxy.remoteServiceStub)\n" +
					"        return theProxy\n" +
					"    }\n" +
					"    public void main() {\n" +
					"        com.docker.script.MyBaseRuntime baseRuntime = (com.docker.script.MyBaseRuntime) GroovyRuntime.getCurrentGroovyRuntime(this.getClass().getClassLoader());\n" +
					"        baseRuntime.resetServiceStubManagerForLans(script.groovy.runtime.ServiceStubProxy.class); " +
					"        baseRuntime.resetServiceStubManager(script.groovy.runtime.ServiceStubProxy.class); " +
// 					"        com.docker.rpc.remote.stub.ServiceStubManager serviceStubManager = baseRuntime.getServiceStubManager();\n" +
//					"        serviceStubManager.setServiceStubProxyClass(script.groovy.runtime.ServiceStubProxy.class)\n" +
//					"        serviceStubManager.clearCache()\n" +
//					"        serviceStubManager.init()\n" +
					"    }\n" +
					"}";
			try {
				FileUtils.writeStringToFile(new File(path + "/script/groovy/runtime/ServiceStubProxy.groovy"), code, "utf8");
			} catch (IOException e) {
				e.printStackTrace();
				LoggerEx.error(TAG, "write ServiceStubProxy.groovy file on " + (path + "/script/groovy/runtime/ServiceStubProxy.groovy") + " in service " + getService() + " failed, " + e.getMessage());
			}
		}
	}
	@Override
	public void close() {
		super.close();
        if(serviceStubManager != null) {
            serviceStubManager.shutdown();
        }
        if(stubManagerForLanIdMap != null) {
            Collection<ServiceStubManager> managers = stubManagerForLanIdMap.values();
            for(ServiceStubManager manager : managers) {
                manager.shutdown();
            }
        }
	}

	public ServiceStubManager getServiceStubManager() {
		return serviceStubManager;
	}

	public void setServiceStubManager(ServiceStubManager serviceStubManager) {
		this.serviceStubManager = serviceStubManager;
	}
}
