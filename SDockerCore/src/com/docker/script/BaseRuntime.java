package com.docker.script;

import com.docker.script.i18n.I18nHandler;
import com.docker.script.i18n.MessageProperties;
import com.docker.storage.redis.RedisHandler;
import connectors.mongodb.MongoClientHelper;
import connectors.mongodb.annotations.handlers.MongoCollectionAnnotationHolder;
import connectors.mongodb.annotations.handlers.MongoDBHandler;
import connectors.mongodb.annotations.handlers.MongoDatabaseAnnotationHolder;
import connectors.mongodb.annotations.handlers.MongoDocumentAnnotationHolder;
import script.filter.JsonFilterFactory;
import script.groovy.runtime.*;
import script.groovy.servlets.GroovyServletDispatcher;
import script.groovy.servlets.GroovyServletManagerEx;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public abstract class BaseRuntime extends GroovyRuntime {
	private ConcurrentHashMap<String, Object> memoryCache = new ConcurrentHashMap<>();

	private MongoDBHandler mongoDBHandler;
	private RedisHandler redisHandler;
	private I18nHandler i18nHandler;

	private String service;

	public void prepare(String service, Properties properties, String rootPath) {
	    this.service = service;
        String enableGroovyMVC = null;
        if(properties != null) {
            enableGroovyMVC = properties.getProperty("web.groovymvc.enable");
			String mongodbHost = properties.getProperty("db.mongodb.uri");
			if(mongodbHost != null) {
				addClassAnnotationHandler(new MongoDatabaseAnnotationHolder());
				addClassAnnotationHandler(new MongoCollectionAnnotationHolder());
				addClassAnnotationHandler(new MongoDocumentAnnotationHolder());
				mongoDBHandler = new MongoDBHandler();
				MongoClientHelper helper = new MongoClientHelper();
				helper.setHosts(mongodbHost);
				mongoDBHandler.setMongoClientHelper(helper);
				addClassAnnotationHandler(mongoDBHandler);
			}

			String redisHost = properties.getProperty("db.redis.uri");
			if(redisHost != null) {
				redisHandler = new RedisHandler(redisHost);
				redisHandler.connect();
			}

			String i18nFolder = properties.getProperty("i18n.folder");
			String name = properties.getProperty("i18n.name");
			if (i18nFolder != null && name != null) {
				i18nHandler = new I18nHandler();
				i18nHandler.setMsgPropertyMap(new HashMap<>());
				File messageFile = new File(rootPath + i18nFolder);
				if (messageFile != null) {
					File[] files = messageFile.listFiles();
					if (files != null) {
						for (File file : files) {
							String fileName = file.getName();
							fileName = fileName.replace(name + "_", "");
							fileName = fileName.replace(".properties", "");
							MessageProperties messageProperties = new MessageProperties();
							messageProperties.setAbsolutePath(file.getAbsolutePath());
							try {
								messageProperties.init();
							} catch (IOException e) {
								e.printStackTrace();
							}
							i18nHandler.getMsgPropertyMap().put(fileName, messageProperties);
						}
					}
				}
			}
		}

		addClassAnnotationHandler(new GroovyBeanFactory());
        if(enableGroovyMVC != null && enableGroovyMVC.trim().equals("true")) {
            GroovyServletManagerEx servletManagerEx = new GroovyServletManagerEx();
            addClassAnnotationHandler(servletManagerEx);
            GroovyServletDispatcher.addGroovyServletManagerEx(service.toLowerCase(), servletManagerEx);
        } else {
            GroovyServletDispatcher.removeGroovyServletManagerEx(service.toLowerCase());
        }

		addClassAnnotationHandler(new GroovyTimerTaskHandler());
		addClassAnnotationHandler(new GroovyRedeployMainHandler());
		addClassAnnotationHandler(new ServerLifeCircleHandler());
		addClassAnnotationHandler(new JsonFilterFactory());
	}

	@Override
    public void close() {
	    super.close();
	    if(service != null)
	        GroovyServletDispatcher.removeGroovyServletManagerEx(service.toLowerCase());
	    try {
			if(mongoDBHandler != null) {
				MongoClientHelper helper = mongoDBHandler.getMongoClientHelper();
				if(helper != null)
					helper.disconnect();
			}
		} catch(Throwable t) {
		}
	    try {
			if(redisHandler != null) {
				redisHandler.disconnect();
			}
		} catch(Throwable t) {
		}
    }

	public MongoDBHandler getMongoDBHandler() {
		return mongoDBHandler;
	}

	public RedisHandler getRedisHandler() {
		return redisHandler;
	}

	public I18nHandler getI18nHandler() {
		return i18nHandler;
	}

	public Object get(String key) {
		return memoryCache.get(key);
	}

	public Object put(String key, Object value) {
		return memoryCache.put(key, value);
	}

	public Object remove(String key) {
		return memoryCache.remove(key);
	}

	public void clear() {
		memoryCache.clear();
	}
}