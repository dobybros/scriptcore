package com.docker.script;

import chat.logs.LoggerEx;
import com.docker.rpc.remote.skeleton.ServiceSkeletonAnnotationHandler;
import com.docker.rpc.remote.stub.ServiceStubManager;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class MyBaseRuntime extends BaseRuntime {
	private static final String TAG = MyBaseRuntime.class.getSimpleName();

	@Override
	public void prepare(String service, Properties properties, String localScriptPath) {
		super.prepare(service, properties, localScriptPath);
		final MyBaseRuntime instance = this;
		ServiceSkeletonAnnotationHandler serviceSkeletonAnnotationHandler = new ServiceSkeletonAnnotationHandler();
		serviceSkeletonAnnotationHandler.setService(service);
		addClassAnnotationHandler(serviceSkeletonAnnotationHandler);

		String remoteServiceHost = properties.getProperty("remote.service.host");
		if(remoteServiceHost != null) {
			ServiceStubManager serviceStubManager = ServiceStubManager.getInstance();
			serviceStubManager.setHost(remoteServiceHost);
			String code =
					"package script.groovy.runtime\n" +
					"@script.groovy.annotation.RedeployMain\n" +
					"class ServiceStubProxy extends com.docker.rpc.remote.stub.Proxy implements GroovyInterceptable{\n" +
					"    private Class<?> remoteServiceStub;\n" +
					"    ServiceStubProxy() {\n" +
					"        super(null);\n" +
					"    }\n" +
					"    ServiceStubProxy(com.docker.rpc.remote.stub.RemoteServiceDiscovery remoteServiceDiscovery, Class<?> remoteServiceStub) {\n" +
					"        super(remoteServiceDiscovery)\n" +
					"        this.remoteServiceStub = remoteServiceStub;\n" +
					"    }\n" +
					"    def methodMissing(String methodName,methodArgs) {\n" +
					"        Long crc = chat.utils.ReflectionUtil.getCrc(remoteServiceStub, methodName, remoteServiceDiscovery.getService());\n" +
					"        return invoke(crc, methodArgs);\n" +
					"    }\n" +
					"    public static def getProxy(com.docker.rpc.remote.stub.RemoteServiceDiscovery remoteServiceDiscovery, Class<?> remoteServiceStub) {\n" +
					"        ServiceStubProxy proxy = new ServiceStubProxy(remoteServiceDiscovery, remoteServiceStub)\n" +
					"        def theProxy = proxy.asType(proxy.remoteServiceStub)\n" +
					"        return theProxy\n" +
					"    }\n" +
					"    public void main() {\n" +
					"        com.docker.rpc.remote.stub.ServiceStubManager serviceStubManager = com.docker.rpc.remote.stub.ServiceStubManager.getInstance();\n" +
					"        serviceStubManager.setServiceStubProxyClass(script.groovy.runtime.ServiceStubProxy.class)\n" +
					"        serviceStubManager.clearCache()\n" +
					"        serviceStubManager.init()\n" +
					"    }\n" +
					"}";
			try {
				FileUtils.writeStringToFile(new File(path + "/script/groovy/runtime/ServiceStubProxy.groovy"), code, "utf8");
			} catch (IOException e) {
				e.printStackTrace();
				LoggerEx.error(TAG, "write ServiceStubProxy.groovy file on " + (path + "/script/groovy/runtime/ServiceStubProxy.groovy") + " in service " + service + " failed, " + e.getMessage());
			}
		}
	}

	@Override
	public void close() {
		super.close();

	}
}
