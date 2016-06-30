package script.groovy.servlets;

import java.util.HashMap;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import script.groovy.object.GroovyObjectEx;
import script.groovy.runtime.GroovyRuntime;
import chat.errors.ChatErrorCodes;
import chat.errors.CoreException;
import chat.logs.LoggerEx;

public class RequestHolder {
	private static final String TAG = null;
	private RequestURIWrapper requestUriWrapper;
	private HttpServletRequest request;
	private HttpServletResponse response;
	private HashMap<String, String> pathVariables;
	private GroovyObjectEx<RequestIntercepter> interceptor;

	RequestHolder(RequestURIWrapper requestUriWrapper,
			HttpServletRequest request, HttpServletResponse response,
			HashMap<String, String> pathVariables, GroovyObjectEx<RequestIntercepter> interceptor) {
		this.requestUriWrapper = requestUriWrapper;
		this.request = request;
		this.response = response;
		this.pathVariables = pathVariables;
		this.interceptor = interceptor;
	}

	public String getResponseType() {
		if(requestUriWrapper != null)
			return requestUriWrapper.getResponseType();
		return null;
	}
	
	public String getPathVariable(String key) throws CoreException {
		return getPathVariable(key, true);
	}
	
	public String getPathVariable(String key, boolean required) throws CoreException {
		if(pathVariables == null)
			if(required) {
				throw new CoreException(ChatErrorCodes.ERROR_URL_VARIABLE_NULL, "PathVariable is null.");
			} else {
				return null;
			}
		return pathVariables.get(key);
	}
	
	public Set<String> variablesKeySet() {
		if(pathVariables == null)
			return null;
		return pathVariables.keySet();
	}
	
	public HashMap<String, String> getPathVariables() {
		return pathVariables;
	}

	public HttpServletResponse getResponse() {
		return response;
	}

	public HttpServletRequest getRequest() {
		return request;
	}
	
	public Integer getIntegerParam(String key) throws CoreException {
		return getIntegerParam(key, false);
	}
	
	public Integer getIntegerParam(String key, boolean required) throws CoreException {
		String para = request.getParameter(key);
		if(para != null) {
			try {
				return Integer.parseInt(para);
			} catch (Exception e) {
			}
			return null;
		} else {
			if(required) {
				throw new CoreException(ChatErrorCodes.ERROR_URL_PARAMETER_NULL, "URL parameter is required but null.");
			} else {
				return null;
			}
		}
	}
	
	public Double getDoubleParam(String key) throws CoreException {
		return getDoubleParam(key, false);
	}
	
	public Double getDoubleParam(String key, boolean required) throws CoreException {
		String para = request.getParameter(key);
		if(para != null) {
			try {
				return Double.parseDouble(para);
			} catch (Exception e) {
			}
			return null;
		} else {
			if(required) {
				throw new CoreException(ChatErrorCodes.ERROR_URL_PARAMETER_NULL, "URL parameter is required but null.");
			} else {
				return null;
			}
		}
	}
	
	public Boolean getBooleanParam(String key) throws CoreException {
		return getBooleanParam(key, false);
	}
	
	public Boolean getBooleanParam(String key, boolean required) throws CoreException {
		String para = request.getParameter(key);
		if(para != null) {
			try {
				return Boolean.parseBoolean(para);
			} catch (Exception e) {
			}
			return null;
		} else {
			if(required) {
				throw new CoreException(ChatErrorCodes.ERROR_URL_PARAMETER_NULL, "URL parameter is required but null.");
			} else {
				return null;
			}
		}
	}
	
	public Long getLongParam(String key) throws CoreException {
		return getLongParam(key, false);
	}
	
	public Long getLongParam(String key, boolean required) throws CoreException {
		String para = request.getParameter(key);
		if(para != null) {
			try {
				return Long.parseLong(para);
			} catch (Exception e) {
			}
			return null;
		} else {
			if(required) {
				throw new CoreException(ChatErrorCodes.ERROR_URL_PARAMETER_NULL, "URL parameter is required but null.");
			} else {
				return null;
			}
		}
	}
	
	public String getParam(String key) throws CoreException {
		return getParam(key, false);
	}
	
	public String getParam(String key, boolean required) throws CoreException {
		String para = request.getParameter(key);
		if(para != null) {
			return para;
		} else {
			if(required) {
				throw new CoreException(ChatErrorCodes.ERROR_URL_PARAMETER_NULL, "URL parameter " + key + " is required but null.");
			} else {
				return null;
			}
		}
	}
	
	/*
	public abstract class RequestInterceptor{
		protected void proceed() {
			invoke(servlet, groovyMethod);
		}
		public abstract void invoke(RequestHolder holder);
	}
	
	public class MyInterceptor extends RequestInterceptor{

		@Override
		public void invoke(RequestHolder holder) {
			super.proceed();
		}
		
	}
	*/
	
	public void handleRequest() throws CoreException {
		GroovyObjectEx<GroovyServlet> servletObj = requestUriWrapper.getGroovyObject();
		String groovyMethod = requestUriWrapper.getMethod();
		if (servletObj != null && groovyMethod != null) {
			RequestIntercepter theInterceptor = null;
			if(interceptor != null) {
				GroovyRuntime groovyRuntime = interceptor.getGroovyRuntime();
				if(groovyRuntime != null) {
					groovyRuntime.registerClassLoaderOnThread();
					try {
						theInterceptor = interceptor.getObject();
						if(theInterceptor != null) {
							theInterceptor.invokeInternal(this);
							return;
						}
					} catch (Throwable e) {
						e.printStackTrace();
						if(e instanceof CoreException) 
							throw e;
						throw new CoreException(ChatErrorCodes.ERROR_GROOVY_UNKNOWN, "Unknown error while executing controller intercepter " + servletObj.getGroovyPath() + " : " + e.getMessage());
					} finally {
						groovyRuntime.unregisterClassLoaderOnThread();
					}
				}
			}
//			servletObj.invokeRootMethod(groovyMethod, this);
			invokeMethod(groovyMethod, servletObj);
		} else {
			LoggerEx.error(TAG,
					"Handle request failed by illegal paramenters, servlet "
							+ servletObj + " groovyMethod " + groovyMethod
							+ " for uri " + request.getRequestURI());
		}
	}
	
	public GroovyObjectEx<RequestIntercepter> getInterceptor() {
		return interceptor;
	}

	RequestURIWrapper getRequestUriWrapper() {
		return requestUriWrapper;
	}

	public void invokeMethod(String groovyMethod,
			GroovyObjectEx<GroovyServlet> servletObj) throws CoreException {
		//TODO annotation
		Object[] args = requestUriWrapper.getActualParameters(this);
		servletObj.invokeMethod(groovyMethod, args);
	}

}
