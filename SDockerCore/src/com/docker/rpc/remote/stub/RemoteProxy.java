package com.docker.rpc.remote.stub;

import chat.errors.ChatErrorCodes;
import chat.errors.CoreException;
import chat.utils.ReflectionUtil;
import com.docker.rpc.MethodResponse;
import com.docker.rpc.MethodRequest;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

public class RemoteProxy implements MethodInterceptor {
    private static final String TAG = RemoteProxy.class.getSimpleName();
    private RemoteServiceDiscovery remoteServiceDiscovery;

    Enhancer enhancer = new Enhancer();

    public RemoteProxy(RemoteServiceDiscovery remoteServiceDiscovery) {
        this.remoteServiceDiscovery = remoteServiceDiscovery;
    }

    public Object getProxy(Class clazz) {
        //设置需要创建的子类
        enhancer.setSuperclass(clazz);
        enhancer.setCallback(this);
        //通过字节码技术动态创建子类实例
        return enhancer.create();
    }

    @Override
    public Object intercept(Object obj, Method method, Object[] args,
                            MethodProxy proxy) throws Throwable {
        // TODO Auto-generated method stub
        if(method.getDeclaringClass().equals(Object.class)) {
            return method.invoke(obj, args);
        }
        MethodRequest request = new MethodRequest();
        request.setEncode(MethodRequest.ENCODE_JAVABINARY);
        request.setArgs(args);
        //TODO should consider how to optimize get CRC too often.
        Long crc = ReflectionUtil.getCrc(method, remoteServiceDiscovery.getService());
        request.setCrc(crc);
        request.setService(remoteServiceDiscovery.getService());
        RemoteServiceDiscovery.RemoteServers lanServers = remoteServiceDiscovery.getRemoteServers();
        if(lanServers == null)
            throw new CoreException(ChatErrorCodes.ERROR_LANSERVERS_NOSERVERS, "RemoteService " + remoteServiceDiscovery.getService() + " doesn't be found while invoke method " + method);
        MethodResponse response = (MethodResponse) lanServers.call(request);
        if(response != null) {
            CoreException e = response.getException();
            if(e != null) {
                throw e;
            }
            Object returnObject = response.getReturnObject();
            return returnObject;
        }
        throw new CoreException(ChatErrorCodes.ERROR_METHODRESPONSE_NULL, "Method response is null for request " + request);
//        request.setMethodMapping(new RMIServerImpl.MethodMapping(method));
//        RMIClientHandler rmiHandler = SpringContextUtil.getBean("");
        //目标方法调用
//        Object result = proxy.invokeSuper(obj, args);
        //目标方法后执行
//        return null;
    }    
}