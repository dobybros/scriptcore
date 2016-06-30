package chat.cache;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.ObjectExistsException;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.Configuration;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang.StringUtils;

import chat.errors.ChatErrorCodes;
import chat.errors.CoreException;
import chat.logs.LoggerEx;
import chat.utils.ChatUtils;
import chat.utils.IteratorEx;

public class CacheHandler implements MethodInterceptor {
	public static final long DEFAULT_TIMETOIDLESECONDS = 900;
	public static final long DEFAULT_TIMETOLIVESECONDS = 1800;
	public static final boolean DEFAULT_ETERNAL = false;
	public static final String DEFAULT_POLICY = "LRU";
	public static final int DEFAULT_MAXENTIRIES = 1000000;
	private static final String TAG = CacheHandler.class.getSimpleName();
	private CacheManager cacheManager;
	
	public void init() {
		Configuration config = new Configuration();
		cacheManager = CacheManager.create(config);
		LoggerEx.info(TAG, "cacheManager created " + cacheManager);
	}
	
//	@Override
	public Object invoke(MethodInvocation methodInvocation) throws Throwable {
		Class<?> clazz = methodInvocation.getThis().getClass();
		Class<?>[] interfaces = clazz.getInterfaces();
		CacheClass cacheClass = null;
		if(interfaces != null) {
			for(Class<?> interface1 : interfaces) {
				cacheClass = interface1.getAnnotation(CacheClass.class);
				if(cacheClass != null) 
					break;
			}
		}
		if(cacheClass != null)
			return handleCacheClass(cacheClass, methodInvocation);
		else {
			Method method = methodInvocation.getMethod();
			ClearCacheMethodIterator clearCacheMethod = method.getAnnotation(ClearCacheMethodIterator.class);
			if(clearCacheMethod != null) {
				return handleClearCacheMethod(methodInvocation);
			}
		}
		
		return proceed(methodInvocation);
	}

	private Object proceed(MethodInvocation methodInvocation) throws Throwable {
		try {
			return methodInvocation.proceed();
		} catch(InvocationTargetException|UndeclaredThrowableException e) {
			Throwable t = e.getCause();
			if(t != null && t instanceof UndeclaredThrowableException) 
				t = ((UndeclaredThrowableException)t).getCause();
			if(t != null) {
				throw t;
			} else 
				throw new CoreException(ChatErrorCodes.ERROR_UNKNOWN, "No cause exception for " + e.getMessage() + " while invoke method "  + methodInvocation.getMethod().getName());
		} 
	}
	
	private String getCacheId(Object[] args, Annotation[][] anns) {
		List<String> keys = new ArrayList<>();
		if(args != null && args.length > 0) {
			if(anns != null) {
				for(int i = 0; i < anns.length; i++) {
					Annotation[] ann = anns[i];
					if(ann != null && ann.length > 0) {
						for(Annotation a : ann) {
							if(a instanceof CacheKey && i < args.length) {
								Object key =  args[i];
								if(key != null)
									keys.add(key.toString());
							}
						}
					}
				}
			}
		}
		if(!keys.isEmpty()) {
			Collections.sort(keys);
			return ChatUtils.generateId(keys);
		}
		return null;
	}
	
	private void removeCache(Cache cache, String cacheId) {
		if(cache != null && cacheId != null) {
			try {
				boolean bool = cache.remove(cacheId);
				LoggerEx.info(TAG, "Remove " + cacheId + " from cache " + cache.getName() + ". " + (bool ? "removed" : "not exist") );
			} catch (Throwable e) {
				LoggerEx.error(TAG, "RemoveCache " + cacheId + " from " + cache.getName() + " failed, " + e.getMessage());
			}
		}
	}
	private void addCache(Cache cache, String cacheId, Object value) {
		if(cache != null && cacheId != null && value != null) {
			try {
				cache.put(new Element(cacheId, value));
				LoggerEx.info(TAG, "Cache a element by id " + cacheId + " in " + cache.getName() + ". " + value);
			} catch (Throwable e) {
				LoggerEx.error(TAG, "addCache " + cacheId + " from " + cache.getName() + " failed, " + e.getMessage());
			}
		}
	}
	
	@SuppressWarnings("rawtypes")
	private class IteratorWrapper implements IteratorEx {
		private IteratorEx iterator;
		private CacheHandler handler;
		
		private Object getFieldValue(Object obj, Class<?> clazz, String fieldName) {
			if(obj != null && clazz != null && fieldName != null && fieldName.length() > 0){
				StringBuffer getMethdod = new StringBuffer("get");
				getMethdod.append(fieldName.substring(0, 1).toUpperCase()); 
				getMethdod.append(fieldName.substring(1));
				try {
					Method m = clazz.getMethod(getMethdod.toString());
					return m.invoke(obj);
				} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					e.printStackTrace();
					LoggerEx.error(TAG, "Get field " + fieldName + " value from " + obj + " failed, " + e.getMessage());
				}
			}
			return null;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public boolean iterate(Object t) {
			Class<?> clazz = t.getClass();
			ClearCacheClass clearCacheClass = clazz.getAnnotation(ClearCacheClass.class);
			if(clearCacheClass != null) {
				String cacheName = clearCacheClass.name();
				if(cacheName != null) { 
					Cache cache = cacheManager.getCache(cacheName);
					if(cache != null) {
						String collection = clearCacheClass.cacheIdCollectionField();
						String[] keyFields = clearCacheClass.keyFields();
						if(!StringUtils.isBlank(collection)) {
							Object fieldObj = getFieldValue(t, clazz, collection);
							if(fieldObj == null || !(fieldObj instanceof Collection)) 
								return iterator.iterate(t);
							else {
								for(Object cacheId : (Collection)fieldObj) {
									handler.removeCache(cache, cacheId.toString());
								}
							}
						} else if(keyFields.length > 0){
							if(keyFields != null && keyFields.length > 0) {
								List<String> keys = new ArrayList<>();
								for(String keyField : keyFields) {
									Object fieldObj = getFieldValue(t, clazz, keyField);
									if(fieldObj == null) 
										return iterator.iterate(t);
									else 
										keys.add(fieldObj.toString());
								}
								if(!keys.isEmpty()) {
									Collections.sort(keys);
									String cacheId = ChatUtils.generateId(keys);
									handler.removeCache(cache, cacheId);
								}
							}
						}
					}
					return iterator.iterate(t);
				}
			}
			return iterator.iterate(t);
		}
		
	}
	
	@SuppressWarnings("rawtypes")
	private Object handleClearCacheMethod(MethodInvocation methodInvocation) throws Throwable {
		Method method = methodInvocation.getMethod();
		Object[] args = methodInvocation.getArguments();
		Annotation[][] anns = method.getParameterAnnotations();
		if(args != null && args.length > 0) {
			if(anns != null) {
				for(int i = 0; i < anns.length; i++) {
					Annotation[] ann = anns[i];
					if(ann != null && ann.length > 0) {
						for(Annotation a : ann) {
							if(a instanceof CacheIterator && i < args.length) {
								if(args[i] instanceof IteratorEx) {
									IteratorWrapper wrapper = new IteratorWrapper();
									wrapper.iterator = (IteratorEx) args[i];
									wrapper.handler = this;
									args[i] = wrapper;
								} else {
									LoggerEx.warn(TAG, "Expect CacheIterator is IteratorEx, but " + args[i].getClass());
								}
							}
						}
					}
				}
			}
		}
		Object result = proceed(methodInvocation);
		return result;
	}
	
	private Object handleCacheClass(CacheClass cacheClass, MethodInvocation methodInvocation) throws Throwable {
		Cache cache = null;
		String cacheId = null;
		Method method = methodInvocation.getMethod();
		String cacheName = null;
		if(cacheClass != null)
			cacheName = cacheClass.name();
		CacheMethod cacheMethod = method.getAnnotation(CacheMethod.class);
		ClearCacheMethod clearCacheMethod = method.getAnnotation(ClearCacheMethod.class);
		if(cacheMethod != null || clearCacheMethod != null) {
			String otherCacheName = null;
			if(cacheMethod != null)
				otherCacheName = cacheMethod.otherCache();
			else if(clearCacheMethod != null) 
				otherCacheName = clearCacheMethod.otherCache();
			if(!StringUtils.isBlank(otherCacheName)) {
				cacheClass = null;
				cacheName = otherCacheName;
			}
		}
		if(cacheName != null) 
			cache = cacheManager.getCache(cacheName);
		if(cacheMethod != null && cache == null && cacheClass != null) {
			synchronized (cacheManager) {
				cache = cacheManager.getCache(cacheName);
				if(cache == null) {
					CacheConfiguration cacheConfiguration = new CacheConfiguration();
					cacheConfiguration.setEternal(cacheClass.eternal());
					cacheConfiguration.setLogging(false);
					cacheConfiguration.setName(cacheName);
					cacheConfiguration.setTimeToIdleSeconds(cacheClass.timeToIdleSeconds());
					cacheConfiguration.setTimeToLiveSeconds(cacheClass.timeToLiveSeconds());
					cacheConfiguration.setMemoryStoreEvictionPolicy(cacheClass.policy());
					cacheConfiguration.setMaxEntriesLocalHeap(cacheClass.maxEntries());
					cache = new Cache(cacheConfiguration);
					try {
						cacheManager.addCache(cache);
						LoggerEx.info(TAG, "Cache " + cache.getName() + " created.");
					} catch (ObjectExistsException e) {
						cache = cacheManager.getCache(cacheName);
					}
				}
			}
		} 
		if(cache != null) {
			cacheId = getCacheId(methodInvocation.getArguments(), method.getParameterAnnotations());
			if(cacheId != null) {
				if(clearCacheMethod != null) {
					removeCache(cache, cacheId);
				} else {
					Element element = cache.get(cacheId);
					if(element != null) {
//						AcuLogger.info(TAG, method.getName() + " read " + cacheId + " from cache " + cache.getName() + ". " + element.getObjectValue());
						return element.getObjectValue();
					}
				}
			}
		}
		Object result = proceed(methodInvocation);
		if(cache != null) {
			if(clearCacheMethod == null && cacheMethod != null) {
				addCache(cache, cacheId, result);
			}
		}
		return result;
	}

}
