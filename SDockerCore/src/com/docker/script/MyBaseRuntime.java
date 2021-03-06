package com.docker.script;

import chat.errors.CoreException;
import chat.logs.LoggerEx;
import com.docker.annotations.*;
import com.docker.data.Lan;
import com.docker.rpc.remote.skeleton.ServiceSkeletonAnnotationHandler;
import com.docker.rpc.remote.stub.ServiceStubManager;
import com.docker.script.annotations.ServiceNotFound;
import com.docker.script.annotations.ServiceNotFoundListener;
import com.docker.server.OnlineServer;
import com.docker.storage.adapters.LansService;
import com.docker.utils.SpringContextUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import script.groovy.object.GroovyObjectEx;
import script.groovy.runtime.ClassAnnotationHandler;
import script.groovy.runtime.GroovyRuntime;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class MyBaseRuntime extends BaseRuntime {
	private static final String TAG = MyBaseRuntime.class.getSimpleName();
	private String remoteServiceHost;
	private ServiceStubManager serviceStubManager;
	private ConcurrentHashMap<String, ServiceStubManager> stubManagerForLanIdMap = new ConcurrentHashMap<>();

	private LansService lansService = (LansService) SpringContextUtil.getBean("lansService");


    private List<GroovyObjectEx<ServiceNotFoundListener>> serviceNotFoundListeners;

    private ReadWriteLock serviceNotFoundLock = new ReentrantReadWriteLock();

	public void resetServiceStubManager(Class<?> proxyClass) {
		if(serviceStubManager != null) {
			serviceStubManager.setServiceStubProxyClass(proxyClass);
//			serviceStubManager.clearCache();
//			serviceStubManager.init();
		}
	}

	public void resetServiceStubManagerForLans(Class<?> proxyClass) {
		Collection<ServiceStubManager> managers = stubManagerForLanIdMap.values();
		for(ServiceStubManager manager : managers) {
			manager.setServiceStubProxyClass(proxyClass);
//			manager.clearCache();
//			manager.init();
		}
	}

	public ServiceStubManager getServiceStubManager(String lanId) {
	    if(StringUtils.isBlank(lanId))
	        return null;
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
					if(lan.getDomain() == null || lan.getPort() == null || lan.getProtocol() == null)
						throw new NullPointerException("Lan " + lan + " is illegal for lanId " + lanId + " domain " + lan.getDomain() + " port " + lan.getPort() + " protocol " + lan.getProtocol());
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
		serviceSkeletonAnnotationHandler.setServiceVersion(getServiceVersion());
		serviceSkeletonAnnotationHandler.addExtraAnnotation(PeriodicTask.class);
		serviceSkeletonAnnotationHandler.addExtraAnnotation(OneTimeTask.class);
		serviceSkeletonAnnotationHandler.addExtraAnnotation(Summaries.class);
		serviceSkeletonAnnotationHandler.addExtraAnnotation(Summary.class);
		serviceSkeletonAnnotationHandler.addExtraAnnotation(Transactions.class);
		serviceSkeletonAnnotationHandler.addExtraAnnotation(Transaction.class);
		serviceSkeletonAnnotationHandler.addExtraAnnotation(TransactionResultNotify.class);
		addClassAnnotationHandler(serviceSkeletonAnnotationHandler);

        final MyBaseRuntime instance = this;
        addClassAnnotationHandler(new ClassAnnotationHandler() {
            @Override
            public Class<? extends Annotation> handleAnnotationClass(GroovyRuntime runtime) {
                return ServiceNotFound.class;
            }

            @Override
            public void handleAnnotatedClasses(Map<String, Class<?>> annotatedClassMap,
                                               MyGroovyClassLoader cl) {
                if (annotatedClassMap != null && !annotatedClassMap.isEmpty()) {
                    StringBuilder uriLogs = new StringBuilder(
                            "\r\n---------------------------------------\r\n");

                    List<GroovyObjectEx<ServiceNotFoundListener>> newServiceNotFoundMap = new ArrayList<>();
                    Set<String> keys = annotatedClassMap.keySet();
                    GroovyRuntime groovyRuntime = instance;
                    for (String key : keys) {
                        Class<?> groovyClass = annotatedClassMap.get(key);
                        if (groovyClass != null) {
                            ServiceNotFound messageReceivedAnnotation = groovyClass.getAnnotation(ServiceNotFound.class);
                            if (messageReceivedAnnotation != null) {
                                GroovyObjectEx<ServiceNotFoundListener> messageReceivedObj = groovyRuntime
                                        .create(groovyClass);
                                if (messageReceivedObj != null) {
                                    uriLogs.append("ServiceNotFoundListener #" + groovyClass + "\r\n");
                                    newServiceNotFoundMap.add(messageReceivedObj);
                                }
                            }
                        }
                    }
                    instance.serviceNotFoundLock.writeLock().lock();
                    try {
                        instance.serviceNotFoundListeners = newServiceNotFoundMap;
                    } finally {
                        instance.serviceNotFoundLock.writeLock().unlock();
                    }
                    uriLogs.append("---------------------------------------");
                    LoggerEx.info(TAG, uriLogs.toString());
                }
            }
        });

		remoteServiceHost = properties.getProperty("remote.service.host");
		if(remoteServiceHost != null) {
//			ServiceStubManager serviceStubManager = ServiceStubManager.getInstance();
			serviceStubManager = new ServiceStubManager(service);
			serviceStubManager.setHost(remoteServiceHost);
		}
		String libs = properties.getProperty("libs");
		if(libs != null) {
			String[] libArray = libs.split(",");
			for(String lib : libArray) {
				addLibPath(lib);
			}
		}
	}
	ClassHolder serviceStubProxyClass = null;
	public void prepareServiceStubProxy() {
		MyGroovyClassLoader classLoader = getClassLoader();
		if(classLoader != null && serviceStubProxyClass == null) {
			serviceStubProxyClass = classLoader.getClass("script.groovy.runtime.ServiceStubProxy");
			resetServiceStubManagerForLans(serviceStubProxyClass.getParsedClass());
			resetServiceStubManager(serviceStubProxyClass.getParsedClass());
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
					"        baseRuntime.prepareServiceStubProxy();" +
					"    }\n" +
					"    public void shutdown(){}\n" +
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
        if(serviceNotFoundListeners != null) {
            serviceNotFoundListeners.clear();
        }
	}

	public ServiceStubManager getServiceStubManager() {
		return serviceStubManager;
	}

	public void setServiceStubManager(ServiceStubManager serviceStubManager) {
		this.serviceStubManager = serviceStubManager;
	}

    public BaseRuntime getRuntimeWhenNotFound(String service) {
        serviceNotFoundLock.readLock().lock();
        try {
            if(serviceNotFoundListeners != null) {
                for(GroovyObjectEx<ServiceNotFoundListener> listener : serviceNotFoundListeners) {
                    try {
                        return listener.getObject().getRuntimeWhenNotFound(service);
                    } catch (Throwable t) {
                        t.printStackTrace();
                        LoggerEx.error(TAG, "Handle getRuntime service " + service + " failed, " + t.getMessage());
                    }
                }
            }
        } finally {
            serviceNotFoundLock.readLock().unlock();
        }
        return null;
    }
}
