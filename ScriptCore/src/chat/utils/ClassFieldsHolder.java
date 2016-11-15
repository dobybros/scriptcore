package chat.utils;

import java.lang.reflect.Field;
import java.util.HashMap;

import org.apache.commons.lang.StringUtils;

import chat.logs.LoggerEx;

public class ClassFieldsHolder {
	public static abstract class FieldIdentifier {
		public static final String MAPKEY = "mapkey";
		
		public abstract String getFieldKey(Field field);
		
		public FieldEx field(Field field) {
			return new FieldEx(field);
		}
	}

	public static class FieldEx extends HashMap<String, Object>{
		private Field field;
		public FieldEx(Field field) {
			this.field = field;
		}
		public Field getField() {
			return field;
		}
	}
	
	private static final String TAG = ClassFieldsHolder.class.getSimpleName();

	private HashMap<String, FieldEx> fieldMap = new HashMap<>();
	
	public ClassFieldsHolder(Class<?> documentClass, FieldIdentifier fieldIdentifier) {
		Class<?> i = documentClass;
	    while (i != null && !i.equals(Object.class)) {
	    	Field[] fields = i.getDeclaredFields();
	    	for(Field field : fields) {
	    		if(fieldIdentifier != null) {
	    			String key = fieldIdentifier.getFieldKey(field);
	    			if(StringUtils.isNotBlank(key)) {
						fieldMap.put(key, fieldIdentifier.field(field));
					}
	    		}
			}
	        i = i.getSuperclass();
	    }
	}
	
	public void assignField(Object obj, String fieldKey, Object value) {
		FieldEx field = fieldMap.get(fieldKey);
		assignField(obj, field.getField(), value);
	}
	public void assignField(Object obj, Field field, Object value) {
		if(field == null || value == null || obj == null)
			return;
		try {
			if(!field.isAccessible())
				field.setAccessible(true);
			field.set(obj, value);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
			LoggerEx.error(TAG, "Assign value " + value + " to field " + field + " for object " + obj);
		}
	}

	public HashMap<String, FieldEx> getFieldMap() {
		return fieldMap;
	}
}