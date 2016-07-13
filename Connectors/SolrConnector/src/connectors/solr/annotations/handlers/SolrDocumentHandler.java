package connectors.solr.annotations.handlers;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import script.groovy.runtime.ClassAnnotationHandler;
import script.groovy.runtime.GroovyRuntime;
import script.groovy.runtime.GroovyRuntime.MyGroovyClassLoader;
import chat.utils.ClassFieldsHolder;
import chat.utils.ClassFieldsHolder.FieldEx;
import chat.utils.ClassFieldsHolder.FieldIdentifier;
import connectors.solr.annotations.DocumentField;
import connectors.solr.annotations.SolrDocument;

public class SolrDocumentHandler implements ClassAnnotationHandler {
	private static final String TAG = SolrDocumentHandler.class.getSimpleName();
	private HashMap<Class<?>, SolrClassFieldsHolder> documentMap = new HashMap<>();
	
	private static SolrDocumentHandler instance;
	
	public SolrDocumentHandler() {
		instance = this;
	}
	
	public static SolrDocumentHandler getInstance() {
		return instance;
	}
	
	public void init() {
	}
	
	@Override
	public Class<? extends Annotation> handleAnnotationClass(GroovyRuntime groovyRuntime) {
		return SolrDocument.class;
	}

	@Override
	public void handleAnnotatedClasses(Map<String, Class<?>> annotatedClassMap,
			MyGroovyClassLoader classLoader) {
		if(annotatedClassMap != null) {
			Map<Class<?>, SolrDocument> documentClassMap = new LinkedHashMap<>();
			Collection<Class<?>> values = annotatedClassMap.values();
			for(Class<?> groovyClass : values) {
				SolrDocument mongoDocument = groovyClass.getAnnotation(SolrDocument.class);
				if(mongoDocument != null) {
					documentClassMap.put((Class<?>) groovyClass, mongoDocument);
				}
			}
			
			HashMap<Class<?>, SolrClassFieldsHolder> newDocumentMap = new HashMap<>();
			
			Collection<Class<?>> documentClasses = documentClassMap.keySet();
			for(Class<?> documentClass : documentClasses) {
				SolrDocument mongoDocument = documentClassMap.get(documentClass);
				String core = mongoDocument.core();
				if(mongoDocument != null && StringUtils.isNotBlank(core)) {
					SolrClassFieldsHolder fieldHolder = new SolrClassFieldsHolder(documentClass, new SolrFieldIdentifier(), core);
					newDocumentMap.put(documentClass, fieldHolder);
				}
			}
			this.documentMap = newDocumentMap;
		}
	}
	public HashMap<Class<?>, SolrClassFieldsHolder> getDocumentMap() {
		return documentMap;
	}

	public class SolrClassFieldsHolder extends ClassFieldsHolder {
		private String core;
		public SolrClassFieldsHolder(Class<?> documentClass,
				FieldIdentifier fieldIdentifier, String core) {
			super(documentClass, fieldIdentifier);
			this.core = core;
		}
		public String getCore() {
			return core;
		}
		public void setCore(String core) {
			this.core = core;
		}
		
	}
	
	public class SolrFieldIdentifier extends FieldIdentifier {
		public static final String KEY = "key";
		public static final String BOOST = "boost";
		
		@Override
		public String getFieldKey(Field field) {
			DocumentField documentField = field.getAnnotation(DocumentField.class);
			if(documentField != null) 
				return documentField.key();
			return null;
		}
		
		@Override
		public FieldEx field(Field field) {
			DocumentField documentField = field.getAnnotation(DocumentField.class);
			if(documentField != null) {
				String key = documentField.key();
				float boost = documentField.boost();
				FieldEx fieldEx = new FieldEx(field);
				fieldEx.put(KEY, key);
				fieldEx.put(BOOST, boost);
				return fieldEx;
			}
			return super.field(field);
		}
	}
	
}
