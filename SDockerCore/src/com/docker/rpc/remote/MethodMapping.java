package com.docker.rpc.remote;

import java.lang.reflect.Method;

public class MethodMapping {
    protected Method method;
    protected Class<?>[] parameterTypes;
    protected Class<?> returnClass;

    public MethodMapping(Method method) {
        this.method = method;
    }

    public Method getMethod() {
        return method;
    }

    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(Class<?>[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public Class<?> getReturnClass() {
        return returnClass;
    }

    public void setReturnClass(Class<?> returnClass) {
        this.returnClass = returnClass;
    }

}