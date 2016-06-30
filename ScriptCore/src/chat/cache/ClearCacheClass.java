package chat.cache;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)  
@Retention(RetentionPolicy.RUNTIME)       
@Documented      
@Inherited   
public @interface ClearCacheClass {
	/**
	 * Cache Name
	 * 
	 * @return
	 */
	public String name();
	
	/**
	 * Can only specify one key, may in multiple fields at most time. 
	 * If any one field is a collection, then consider as multiple keys. 
	 * 
	 * The keyFields is using reflect for get* method, please remember to keep get*.
	 * 
	 * @return
	 */
	public String[] keyFields() default {};
	
	public String cacheIdCollectionField() default "";
}
