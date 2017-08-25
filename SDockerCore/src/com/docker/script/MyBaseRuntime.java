package com.docker.script;

import com.docker.rpc.remote.skeleton.ServiceSkeletonAnnotationHandler;

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
	}

	@Override
	public void close() {
		super.close();

	}
}
