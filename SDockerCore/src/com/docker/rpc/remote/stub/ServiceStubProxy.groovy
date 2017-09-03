//package com.docker.rpc.remote.stub
//
//class ServiceStubProxy extends com.docker.rpc.remote.stub.Proxy implements GroovyInterceptable{
//    private Class<?> remoteServiceStub;
//    ServiceStubProxy(RemoteServiceDiscovery remoteServiceDiscovery, Class<?> remoteServiceStub) {
//        super(remoteServiceDiscovery)
//        this.remoteServiceStub = remoteServiceStub;
//    }
//    def methodMissing(String methodName,methodArgs) {
//        Long crc = chat.utils.ReflectionUtil.getCrc(remoteServiceStub, methodName, remoteServiceDiscovery.getService());
//        return invoke(crc, methodArgs);
//    }
//
//    public static def getProxy(RemoteServiceDiscovery remoteServiceDiscovery, Class<?> remoteServiceStub) {
//        ServiceStubProxy proxy = new ServiceStubProxy(remoteServiceDiscovery, remoteServiceStub)
//        def stubProxy = new ServiceStubProxy()
//        def theProxy = stubProxy.asType(stubProxy.remoteServiceStub)
//        return theProxy
//    }
//}
//
//
