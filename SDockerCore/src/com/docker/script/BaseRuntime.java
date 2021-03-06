package com.docker.script;

import chat.errors.CoreException;
import chat.logs.LoggerEx;
import com.docker.script.i18n.I18nHandler;
import com.docker.script.i18n.MessageProperties;
import com.docker.script.servlet.WebServiceAnnotationHandler;
import com.docker.storage.kafka.KafkaConfCenter;
import com.docker.storage.kafka.KafkaProducerHandler;
import com.docker.storage.redis.RedisHandler;
import com.docker.utils.ReflectionUtil;
import com.docker.utils.SpringContextUtil;
import connectors.mongodb.MongoClientHelper;
import connectors.mongodb.annotations.handlers.MongoCollectionAnnotationHolder;
import connectors.mongodb.annotations.handlers.MongoDBHandler;
import connectors.mongodb.annotations.handlers.MongoDatabaseAnnotationHolder;
import connectors.mongodb.annotations.handlers.MongoDocumentAnnotationHolder;
import org.apache.commons.io.FileUtils;
import script.filter.JsonFilterFactory;
import script.groovy.runtime.*;
import script.groovy.servlets.GroovyServletDispatcher;
import com.docker.script.servlet.GroovyServletManagerEx;
import script.groovy.servlets.RequestPermissionHandler;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public abstract class BaseRuntime extends GroovyRuntime {
	public static final String TAG = BaseRuntime.class.getSimpleName();
	private ConcurrentHashMap<String, Object> memoryCache = new ConcurrentHashMap<>();

	private MongoDBHandler mongoDBHandler;
	private RedisHandler redisHandler;
	private KafkaProducerHandler kafkaProducerHandler;
	private KafkaConfCenter kafkaConfCenter;
	private I18nHandler i18nHandler;

	private String service;

	private String serviceName;
	private Integer serviceVersion;

	private Properties config;

	public void prepare(String service, Properties properties, String rootPath) {
		LoggerEx.info(TAG,"prepare service: " + service + " properties: " + properties + " rootPath: " + rootPath);
	    this.service = service.toLowerCase();
	    this.config = properties;
        String enableGroovyMVC = null;
        addClassAnnotationHandler(new GroovyBeanFactory());
        if(properties != null) {
			Object rpcServerHandler = SpringContextUtil.getBean("rpcServer");
			if(rpcServerHandler != null && rpcServerHandler instanceof ClassAnnotationHandler)
        		addClassAnnotationHandler((ClassAnnotationHandler) rpcServerHandler);
			Object rpcServerSslHandler = SpringContextUtil.getBean("rpcServerSsl");
			if(rpcServerSslHandler != null && rpcServerSslHandler instanceof ClassAnnotationHandler)
				addClassAnnotationHandler((ClassAnnotationHandler) rpcServerSslHandler);
			Object upStreamAnnotationHandler = SpringContextUtil.getBean("upStreamAnnotationHandler");
			if(upStreamAnnotationHandler != null && upStreamAnnotationHandler instanceof ClassAnnotationHandler)
				addClassAnnotationHandler((ClassAnnotationHandler) upStreamAnnotationHandler);

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
            String produce = properties.getProperty("db.kafka.produce");
			kafkaConfCenter = new KafkaConfCenter();
			kafkaConfCenter.filterKafkaConf(properties,KafkaConfCenter.FIELD_PRODUCE,KafkaConfCenter.FIELD_CONSUMER);
			if(produce != null){
				kafkaProducerHandler = new KafkaProducerHandler(kafkaConfCenter);
				kafkaProducerHandler.connect();
			}
			String i18nFolder = properties.getProperty("i18n.folder");
			String name = properties.getProperty("i18n.name");
			if (i18nFolder != null && name != null) {
				i18nHandler = new I18nHandler();
				File messageFile = new File(rootPath + i18nFolder);
				if (messageFile != null) {
					File[] files = messageFile.listFiles();
					if (files != null) {
						for (File file : files) {
							String fileName = file.getName();
							fileName = fileName.replace(name + "_", "");
							fileName = fileName.replace(".properties", "");
							MessageProperties messageProperties = new MessageProperties();
//							messageProperties.setAbsolutePath(file.getAbsolutePath());
							try {
								messageProperties.load(FileUtils.openInputStream(file));
//								messageProperties.init();
							} catch (IOException e) {
								e.printStackTrace();
							}
							i18nHandler.getMsgPropertyMap().put(fileName, messageProperties);
						}
					}
				}
			}
		}

        if(enableGroovyMVC != null && enableGroovyMVC.trim().equals("true")) {
            GroovyServletManagerEx servletManagerEx = new GroovyServletManagerEx(this.serviceName, this.serviceVersion);
            addClassAnnotationHandler(servletManagerEx);
            GroovyServletDispatcher.addGroovyServletManagerEx(this.service, servletManagerEx);
			addClassAnnotationHandler(new WebServiceAnnotationHandler());
        } else {
            GroovyServletDispatcher.removeGroovyServletManagerEx(this.service);
        }

		addClassAnnotationHandler(new GroovyTimerTaskHandler());
		addClassAnnotationHandler(new GroovyRedeployMainHandler());
		addClassAnnotationHandler(new ServerLifeCircleHandler());
		addClassAnnotationHandler(new JsonFilterFactory());
		addClassAnnotationHandler(new RequestPermissionHandler());
	}

	@Override
    public void close() {
	    if(service != null) {
			GroovyServletDispatcher.removeGroovyServletManagerEx(service.toLowerCase());
		}
		try {
			if(mongoDBHandler != null) {
				MongoClientHelper helper = mongoDBHandler.getMongoClientHelper();
				if(helper != null) {
					helper.disconnect();
				}
			}
		} catch(Throwable t) {
		}
		try {
			if(redisHandler != null) {
				redisHandler.disconnect();
			}
		} catch(Throwable t) {
		}
		super.close();
		clear();
	}


	public Object executeBeanMethod(Object caller, String name, Object... args) throws CoreException, InvocationTargetException, IllegalAccessException {
		GroovyBeanFactory beanFactory = (GroovyBeanFactory) getClassAnnotationHandler(GroovyBeanFactory.class);
		if(beanFactory != null) {
            script.groovy.object.GroovyObjectEx objectEx = beanFactory.getBean(caller.getClass());
            if(objectEx == null)
            	return null;
			Object obj = objectEx.getObject();
			if(obj != null) {
                Class<?>[] argClasses = null;
				if(args != null && args.length > 0) {
					argClasses = new Class[args.length];
					for(int i = 0; i < args.length; i++) {
						argClasses[i] = args[i].getClass();
					}
				}
				Method method = null;
                try {
                    method = obj.getClass().getMethod(name, argClasses);
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                    LoggerEx.error(TAG, "NoSuchMethod while executeBeanMethod " + name + " args " + Arrays.toString(args));
                }
                if(method != null) {
                    return method.invoke(obj, args);
                }
            }
		}
		return null;
	}

	public MongoDBHandler getMongoDBHandler() {
		return mongoDBHandler;
	}

	public RedisHandler getRedisHandler() {
		return redisHandler;
	}

	public KafkaProducerHandler getKafkaProducerHandler(){ return kafkaProducerHandler;}

    public KafkaConfCenter getKafkaConfCenter() { return kafkaConfCenter; }

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

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public Properties getConfig() {
		return config;
	}

	public void setConfig(Properties config) {
		this.config = config;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public Integer getServiceVersion() {
		return serviceVersion;
	}

	public void setServiceVersion(Integer serviceVersion) {
		this.serviceVersion = serviceVersion;
	}
}
