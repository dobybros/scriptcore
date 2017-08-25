package com.docker.rpc.remote.skeleton;

import chat.errors.ChatErrorCodes;
import chat.errors.CoreException;
import chat.logs.LoggerEx;
import chat.utils.ReflectionUtil;
import com.docker.rpc.remote.MethodMapping;
import com.docker.rpc.remote.RemoteService;
import com.docker.rpc.MethodResponse;
import script.groovy.object.GroovyObjectEx;
import script.groovy.runtime.ClassAnnotationHandler;
import script.groovy.runtime.GroovyRuntime;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceSkeletonAnnotationHandler extends ClassAnnotationHandler{
	private static final String TAG = ServiceSkeletonAnnotationHandler.class.getSimpleName();
    private ConcurrentHashMap<Long, SkelectonMethodMapping> methodMap = new ConcurrentHashMap<>();

	private String service;

    public ServiceSkeletonAnnotationHandler() {
    }

    @Override
    public Class<? extends Annotation> handleAnnotationClass(GroovyRuntime groovyRuntime) {
        return com.docker.rpc.remote.annotations.RemoteService.class;
    }

    @Override
    public void handleAnnotatedClasses(Map<String, Class<?>> annotatedClassMap, GroovyRuntime.MyGroovyClassLoader classLoader) {
        if (annotatedClassMap != null && !annotatedClassMap.isEmpty()) {
            StringBuilder uriLogs = new StringBuilder(
                    "\r\n---------------------------------------\r\n");

            ConcurrentHashMap<Long, SkelectonMethodMapping> newMethodMap = new ConcurrentHashMap<>();
            Set<String> keys = annotatedClassMap.keySet();
            for (String key : keys) {
                Class<?> groovyClass = annotatedClassMap.get(key);

                // Class<GroovyServlet> groovyClass =
                // groovyServlet.getGroovyClass();
                if (groovyClass != null) {
                    // Handle RequestIntercepting
                    com.docker.rpc.remote.annotations.RemoteService requestIntercepting = groovyClass.getAnnotation(com.docker.rpc.remote.annotations.RemoteService.class);
                    if (requestIntercepting != null) {
                        GroovyObjectEx<RemoteService> serverAdapter = getGroovyRuntime()
                                .create(groovyClass);
                        scanClass(groovyClass, serverAdapter, newMethodMap);
                    }
                }
            }
            this.methodMap = newMethodMap;
            uriLogs.append("---------------------------------------");
            LoggerEx.info(TAG, uriLogs.toString());
        }
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public class SkelectonMethodMapping extends MethodMapping {
        private GroovyObjectEx<RemoteService> remoteService;

        public SkelectonMethodMapping(Method method) {
            super(method);
        }

        public MethodResponse invoke(Long crc, Object[] rawArgs) throws CoreException {
            if(method == null)
                throw new CoreException(ChatErrorCodes.ERROR_METHODMAPPING_METHOD_NULL, "Invoke method is null");
            int argLength = rawArgs != null ? rawArgs.length : 0;
            Object[] args = null;
            if(parameterTypes.length == argLength) {
                args = rawArgs;
            } else if(parameterTypes.length < argLength) {
                args = new Object[parameterTypes.length];
                System.arraycopy(rawArgs, 0, args, 0, parameterTypes.length);
            } else {
                args = new Object[parameterTypes.length];
                System.arraycopy(rawArgs, 0, args, 0, rawArgs.length);
            }
//            if(parameterClasses != null) {
//                for(int i = 0; i < parameterClasses.length; i++) {
//                    Class<?> clazz = parameterClasses[i];
//                    if(i < rawArgs.length) {
//                        if(rawArgs[i] != null) {
//                            if(String.class.equals(clazz) && rawArgs[i] instanceof String) {
//                                args[i] = rawArgs[i];
//                            } else if(!ClassUtils.isPrimitiveOrWrapper(clazz) && rawArgs[i] instanceof JSON) {
//                                args[i] = JSON.parseObject((String) rawArgs[i], clazz);
//                            } else if(ClassUtils.isPrimitiveOrWrapper(clazz)) {
//                                args[i] = TypeUtils.cast(rawArgs[i], clazz, ParserConfig.getGlobalInstance());
//                            }
//                        }
//                    }
//                }
//            }
            Object returnObj = null;
            CoreException exception = null;
            try {
                returnObj = remoteService.invokeRootMethod(method.getName(), args);
//                returnObj = method.invoke(obj, args);
            } catch (Throwable t) {
                if(t instanceof CoreException)
                    exception = (CoreException) t;
                else
                    exception = new CoreException(ChatErrorCodes.ERROR_METHODMAPPING_INVOKE_UNKNOWNERROR, t.getMessage());
            }
            MethodResponse response = new MethodResponse(returnObj, exception);
            response.setEncode(MethodResponse.ENCODE_JAVABINARY);
            response.setCrc(crc);
            return response;
        }

        public GroovyObjectEx<RemoteService> getRemoteService() {
            return remoteService;
        }

        public void setRemoteService(GroovyObjectEx<RemoteService> remoteService) {
            this.remoteService = remoteService;
        }
    }

    public SkelectonMethodMapping getMethodMapping(Long crc) {
        return methodMap.get(crc);
    }

    public void scanClass(Class<?> clazz, GroovyObjectEx<RemoteService> serverAdapter, ConcurrentHashMap<Long, SkelectonMethodMapping> methodMap) {
        if(clazz == null)
            return;
//        Object obj = cachedInstanceMap.get(clazz);
//        if(obj == null) {
//            try {
//                obj = clazz.newInstance();
//                cachedInstanceMap.putIfAbsent(clazz, obj);
//            } catch (InstantiationException e) {
//                e.printStackTrace();
//            } catch (IllegalAccessException e) {
//                e.printStackTrace();
//            }
//        }
//        if(!ReflectionUtil.canBeInitiated(clazz)) {
//            LoggerEx.fatal(TAG, "Class " + clazz + " couldn't be initialized without parameters, it will cause the rpc call failed!");
//            return;
//        }

        Method[] methods = ReflectionUtil.getMethods(clazz);
        if(methods != null) {
            for(Method method : methods) {
                if(method.isSynthetic())
                    continue;
                SkelectonMethodMapping mm = new SkelectonMethodMapping(method);
                mm.setRemoteService(serverAdapter);
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
                LoggerEx.info("SCAN", "Mapping crc " + value + " for class " + clazz.getName() + " method " + method.getName() + " for service " + service);
            }
        }
    }
}
