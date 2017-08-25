package com.docker.rpc.remote.stub;

import chat.logs.LoggerEx;
import chat.utils.ReflectionUtil;
import com.docker.rpc.RPCClientAdapterMap;
import com.docker.rpc.remote.MethodMapping;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceStubManager {
	private static final String TAG = ServiceStubManager.class.getSimpleName();
	private static ServiceStubManager instance;
//	private OnlineServer onlineServer = (OnlineServer) SpringContextUtil.getBean("onlineServer");
//	private RMIServerImpl rpcServer = (RMIServerImpl) SpringContextUtil.getBean("rpcServer");
    private ConcurrentHashMap<Long, MethodMapping> methodMap = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String, RemoteServiceDiscovery> discoveryMap = new ConcurrentHashMap<>();
//	private static ConcurrentHashMap<Class<? extends RemoteService>, Class<? extends RemoteService>> storageAdapterClassMap = new ConcurrentHashMap<>();
//	private static ConcurrentHashMap<Class<? extends RemoteService>, String> globalLanForAdapterMap = new ConcurrentHashMap<>();
//	static {
//		globalLanForAdapterMap.put(UserInPresenceAdapter.class, "frankfurt");
//	}

    private ConcurrentHashMap<String, Object> remoteServiceMap = new ConcurrentHashMap<>();
    private String clientTrustJksPath;
    private String serverJksPath;
    private String jksPwd;
    private String host;
    private boolean inited = false;

    public static ServiceStubManager getInstance() {
        if(instance == null) {
            synchronized (ServiceStubManager.class) {
                if(instance == null) {
                    instance = new ServiceStubManager();
                }
            }
        }
        return instance;
    }

    public MethodMapping getMethodMapping(Long crc) {
        return methodMap.get(crc);
    }
    public void scanClass(Class<?> clazz, String service) {
        if(clazz == null || service == null)
            return;
//        if(service == null) {
//            String[] paths = clazz.getName().split(".");
//            if(paths.length >= 2) {
//                service = paths[paths.length - 2];
//            }
//        }
        Method[] methods = ReflectionUtil.getMethods(clazz);
        if(methods != null) {
            for(Method method : methods) {
                MethodMapping mm = new MethodMapping(method);
                long value = ReflectionUtil.getCrc(method, service);
                if(methodMap.contains(value)) {
                    LoggerEx.fatal(TAG, "Don't support override methods, please rename your method " + method + " for crc " + value + " and existing method " + methodMap.get(value).getMethod());
                    continue;
                }
                Class<?>[] parameterTypes = method.getParameterTypes();
                if(parameterTypes != null) {
                    boolean failed = false;
                    for(int i = 0; i < parameterTypes.length; i++) {
                        parameterTypes[i] = ReflectionUtil.getInitiatableClass(parameterTypes[i]);
                        Class<?> parameterType = parameterTypes[i];
                        if(!ReflectionUtil.canBeInitiated(parameterType)) {
                            failed = true;
                            LoggerEx.fatal(TAG, "Parameter " + parameterType + " in method " + method + " couldn't be initialized. ");
                            break;
                        }
                    }
                    if(failed)
                        continue;
                }
                mm.setParameterTypes(parameterTypes);

                Class<?> returnType = method.getReturnType();
                returnType = ReflectionUtil.getInitiatableClass(returnType);
                mm.setReturnClass(returnType);
                methodMap.put(value, mm);
//                RemoteProxy.cacheMethodCrc(method, value);
                LoggerEx.info("SCAN", "Mapping crc " + value + " for class " + clazz.getName() + " method " + method.getName() + " for service " + service);
            }
        }
    }

	private ServiceStubManager() {

	}

	public synchronized void init() {
        if(inited)
            return;
        if(host == null) {
            throw new NullPointerException("Discovery host is null, ServiceStubManager initialize failed!");
        }

        inited = true;
    }

    private RemoteServiceDiscovery getRemoteServiceDiscovery(String service) {
        RemoteServiceDiscovery remoteServiceDiscovery = discoveryMap.get(service);
        if(remoteServiceDiscovery == null) {
            synchronized (discoveryMap) {
                remoteServiceDiscovery = discoveryMap.get(service);
                if(remoteServiceDiscovery == null) {
                    remoteServiceDiscovery = new RemoteServiceDiscovery();
                    remoteServiceDiscovery.setHost(host);
                    RPCClientAdapterMap clientAdapterMap = new RPCClientAdapterMap();
                    if(clientTrustJksPath != null && serverJksPath != null &&  jksPwd != null) {
                        clientAdapterMap.setEnableSsl(true);
                        clientAdapterMap.setRpcSslClientTrustJksPath(clientTrustJksPath);
                        clientAdapterMap.setRpcSslJksPwd(jksPwd);
                        clientAdapterMap.setRpcSslServerJksPath(serverJksPath);
                    }
                    remoteServiceDiscovery.setService(service);
                    remoteServiceDiscovery.setRpcClientAdapterMap(clientAdapterMap);
                    remoteServiceDiscovery.setShutdownListener(new RemoteServiceDiscovery.ShutdownListener() {
                        @Override
                        public void shutdownNow() {
                            discoveryMap.remove(service);
                        }
                    });
                    remoteServiceDiscovery.update();
                    new Thread(remoteServiceDiscovery).start();
                }
            }
        }
        return remoteServiceDiscovery;
    }

	public void shutdown() {
        if(discoveryMap != null) {
            Collection<String> keys = discoveryMap.keySet();
            for(String key : keys) {
                RemoteServiceDiscovery serviceDiscovery = discoveryMap.get(key);
                serviceDiscovery.shutdown(RemoteServiceDiscovery.ShutdownListener.TYPE_SERVER_SHUTDOWN);
            }
        }
    }

    public <T> T getService(String service, Class<T> adapterClass) {
        if(!inited)
            throw new NullPointerException("ServiceSubManager hasn't been initialized yet, please call init method first.");
        if(service == null)
            throw new NullPointerException("Service can not be nulll");
        String key = adapterClass.getSimpleName() + "#" + service;
        T adapterService = (T) remoteServiceMap.get(key);
        if(adapterService == null) {
            synchronized (adapterClass) {
                if(adapterService == null) {
                    try {
                        scanClass(adapterClass, service);
//                        if(service == null)
//                            throw new NullPointerException("Service for adapterClass " + key + " doesn't be found");
                        RemoteProxy proxy = new RemoteProxy(getRemoteServiceDiscovery(service));
                        adapterService = (T) proxy.getProxy(adapterClass);
                    } catch (Throwable e) {
                        e.printStackTrace();
                        LoggerEx.warn(TAG, "Initiate moduleClass " + adapterClass + " failed, " + e.getMessage());
                    }

                    if(adapterService != null) {
                        T associatedModule = (T) remoteServiceMap.putIfAbsent(key, adapterService);
                        if(associatedModule != null)
                            adapterService = associatedModule;
                    }
                }
            }
        }
        return adapterService;
    }

    public String getClientTrustJksPath() {
        return clientTrustJksPath;
    }

    public void setClientTrustJksPath(String clientTrustJksPath) {
        this.clientTrustJksPath = clientTrustJksPath;
    }

    public String getServerJksPath() {
        return serverJksPath;
    }

    public void setServerJksPath(String serverJksPath) {
        this.serverJksPath = serverJksPath;
    }

    public String getJksPwd() {
        return jksPwd;
    }

    public void setJksPwd(String jksPwd) {
        this.jksPwd = jksPwd;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }
}
