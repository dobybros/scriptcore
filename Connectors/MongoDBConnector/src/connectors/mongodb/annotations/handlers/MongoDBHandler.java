package connectors.mongodb.annotations.handlers;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.types.Binary;

import script.groovy.runtime.ClassAnnotationHandler;
import script.groovy.runtime.GroovyRuntime.MyGroovyClassLoader;
import chat.errors.CoreException;
import chat.logs.LoggerEx;
import chat.utils.HashTree;

import com.mongodb.MongoClient;

import connectors.mongodb.MongoClientHelper;
import connectors.mongodb.annotations.DocumentField;
import connectors.mongodb.annotations.DBCollection;
import connectors.mongodb.annotations.Database;
import connectors.mongodb.annotations.DBDocument;
import connectors.mongodb.codec.BaseObjectCodecProvider;
import connectors.mongodb.codec.DataObject;
import connectors.mongodb.codec.DataObjectCodecProvider;

public class MongoDBHandler extends ClassAnnotationHandler{
	public static final String VALUE = "VALUE";
	public static final String CLASS = "CLASS";
	private static final String TAG = MongoDBHandler.class.getSimpleName();

	private HashMap<Class<?>, com.mongodb.client.MongoDatabase> databaseMap = new HashMap<>();
	private HashMap<Class<?>, CollectionHolder> collectionMap = new HashMap<>();
	private HashMap<Class<?>, FieldHolder> documentMap = new HashMap<>();
	
	private static MongoDBHandler instance;
	
	private MongoClientHelper mongoClientHelper;
	
	public static class CollectionHolder {
		private com.mongodb.client.MongoCollection<DataObject> collection;
		private HashTree<String, String> filters;
		public com.mongodb.client.MongoCollection<DataObject> getCollection() {
			return collection;
		}
		public void setCollection(com.mongodb.client.MongoCollection<DataObject> collection) {
			this.collection = collection;
		}
		public HashTree<String, String> getFilters() {
			return filters;
		}
		public void setFilters(HashTree<String, String> filters) {
			this.filters = filters;
		}
	}
	
	public static class FieldHolder {
		private HashMap<String, Field> fieldMap = new HashMap<>();
		
		public FieldHolder(Class<?> documentClass) {
			Class<?> i = documentClass;
		    while (i != null && !i.equals(Object.class)) {
		    	Field[] fields = i.getDeclaredFields();
		    	for(Field field : fields) {
					DocumentField documentField = field.getAnnotation(DocumentField.class);
					if(documentField != null) {
						String key = documentField.key();
						if(StringUtils.isNotBlank(key)) {
							fieldMap.put(key, field);
						}
					}
				}
		        i = i.getSuperclass();
		    }
		}
		
		public void assignField(Object obj, String fieldKey, Object value) {
			Field field = fieldMap.get(fieldKey);
			assignField(obj, field, value);
		}
		public void assignField(Object obj, Field field, Object value) {
			if(field == null || value == null || obj == null)
				return;
			try {
				if(!field.isAccessible())
					field.setAccessible(true);
				if(value instanceof Binary && field.getType().equals(byte[].class)) {
					field.set(obj, ((Binary)value).getData());
				} else {
					field.set(obj, value);
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
				LoggerEx.error(TAG, "Assign value " + value + " to field " + field + " for object " + obj);
			}
		}

		public HashMap<String, Field> getFieldMap() {
			return fieldMap;
		}
	}
	
	private MongoDBHandler() {
		instance = this;
	}
	
	public static MongoDBHandler getInstance() {
		return instance;
	}
	
	@Override
	public void handleAnnotatedClasses(Map<String, Class<?>> arg0,
			MyGroovyClassLoader arg1) {
		MongoDatabaseAnnotationHolder databaseHolder = MongoDatabaseAnnotationHolder.getInstance();
		MongoCollectionAnnotationHolder collectionHolder = MongoCollectionAnnotationHolder.getInstance();
		MongoDocumentAnnotationHolder documentHolder = MongoDocumentAnnotationHolder.getInstance();
		if(databaseHolder == null || collectionHolder == null || documentHolder == null) {
			LoggerEx.info(TAG, "Information is insufficient, databaseHolder = " + databaseHolder + ", collectionHolder = " + collectionHolder + ", documentHolder = " + documentHolder);
			return;
		}
		
		Map<Class<?>, Database> databaseMap = databaseHolder.getDbClassMap();
		Map<Class<?>, DBCollection> collectionMap = collectionHolder.getCollectionClassMap();
		Map<Class<?>, DBDocument> documentMap = documentHolder.getDocumentClassMap();
		if((databaseMap == null || databaseMap.isEmpty()) || (collectionMap == null || collectionMap.isEmpty()) || (documentMap == null || documentMap.isEmpty())) {
			LoggerEx.info(TAG, "Information is insufficient, databaseHolder = " + databaseMap + ", collectionMap = " + collectionMap + ", documentMap = " + documentMap);
			return;
		}
		
		try {
			mongoClientHelper.connect();
		} catch (CoreException e) {
			e.printStackTrace();
			LoggerEx.error(TAG, "Connect mongodb failed, " + mongoClientHelper.getHosts() + " error, " + e.getMessage());
		}
		
		HashMap<Class<?>, com.mongodb.client.MongoDatabase> newDatabaseMap = new HashMap<>();
		HashMap<Class<?>, CollectionHolder> newCollectionMap = new HashMap<>();
		HashMap<Class<?>, FieldHolder> newDocumentMap = new HashMap<>();
		
		Collection<Class<?>> databaseClasses = databaseMap.keySet();
		for(Class<?> databaseClass : databaseClasses) {
			Database mongoDatabase = databaseMap.get(databaseClass);
			if(mongoDatabase != null) {
				String dbName = mongoDatabase.name();
				if(dbName != null) {
					com.mongodb.client.MongoDatabase database = mongoClientHelper.getMongoDatabase(dbName);
					newDatabaseMap.put(databaseClass, database);
				}
			}
		}
		Collection<Class<?>> collectionClasses = collectionMap.keySet();
		for(Class<?> collectionClass : collectionClasses) {
			DBCollection mongoCollection = collectionMap.get(collectionClass);
			if(mongoCollection != null) {
				String collectionName = mongoCollection.name();
				Class<?> databaseClass = mongoCollection.databaseClass();
				if(collectionName != null && databaseClass != null) {
					com.mongodb.client.MongoDatabase database = newDatabaseMap.get(databaseClass);
					if(database != null) {
						CodecRegistry codecRegistry = CodecRegistries.fromRegistries(CodecRegistries.fromProviders(new DataObjectCodecProvider(collectionClass), new BaseObjectCodecProvider()), MongoClient.getDefaultCodecRegistry());
						
						com.mongodb.client.MongoCollection<DataObject> collection = database.getCollection(collectionName, DataObject.class).withCodecRegistry(codecRegistry);
						CollectionHolder cHolder = new CollectionHolder();
						cHolder.collection = collection;
						newCollectionMap.put(collectionClass, cHolder);
					}
				}
			}
		}
		Collection<Class<?>> documentClasses = documentMap.keySet();
		for(Class<?> documentClass : documentClasses) {
			DBDocument mongoDocument = documentMap.get(documentClass);
			if(mongoDocument != null) {
				String[] filters = mongoDocument.filters();
				Class<?> collectionClass = mongoDocument.collectionClass();
				CollectionHolder holder = newCollectionMap.get(collectionClass);
				if(holder != null) {
					Object value = null;
					HashTree<String, String> tree = holder.filters;
					if(tree == null) {
						tree = new HashTree<>();
						holder.filters = tree;
					}
					for(int i = 0; i < filters.length; i++) {
						if(StringUtils.isBlank(filters[i])) 
							break;
						HashTree<String, String> children = null;
						if(i >= filters.length - 1) {
							//This is the last one in filter array. 
							value = filters[i];
							children = tree.getChildren(value.toString(), true);
						} else {
							children = tree.getChildren(filters[i], true);
						}
						tree = children;
					}
					tree.setParameter(CLASS, documentClass);
					tree.setParameter(VALUE, value);
					FieldHolder fieldHolder = new FieldHolder(documentClass);
//					tree.setParameter(FIELDS, fieldHolder);
					newDocumentMap.put(documentClass, fieldHolder);
				} else {
					FieldHolder fieldHolder = new FieldHolder(documentClass);
					newDocumentMap.put(documentClass, fieldHolder);
				}
			}
		}
		if(!newDatabaseMap.isEmpty() && !newCollectionMap.isEmpty()) {
			this.databaseMap = newDatabaseMap;
			this.collectionMap = newCollectionMap;
			this.documentMap = newDocumentMap;
		}
	}

	@Override
	public Class<? extends Annotation> handleAnnotationClass() {
		return null;
	}

	public HashMap<Class<?>, com.mongodb.client.MongoDatabase> getDatabaseMap() {
		return databaseMap;
	}

	public HashMap<Class<?>, CollectionHolder> getCollectionMap() {
		return collectionMap;
	}

	public MongoClientHelper getMongoClientHelper() {
		return mongoClientHelper;
	}

	public void setMongoClientHelper(MongoClientHelper mongoClientHelper) {
		this.mongoClientHelper = mongoClientHelper;
	}

	public HashMap<Class<?>, FieldHolder> getDocumentMap() {
		return documentMap;
	}

	public void setDocumentMap(HashMap<Class<?>, FieldHolder> documentMap) {
		this.documentMap = documentMap;
	}
}
