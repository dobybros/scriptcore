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

public class RemoteProxy extends Proxy implements MethodInterceptor {
    private static final String TAG = RemoteProxy.class.getSimpleName();

    Enhancer enhancer = new Enhancer();

    public RemoteProxy(RemoteServiceDiscovery remoteServiceDiscovery) {
        super(remoteServiceDiscovery);
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
        Long crc = ReflectionUtil.getCrc(method, remoteServiceDiscovery.getService());
        return invoke(crc, args);
    }
}