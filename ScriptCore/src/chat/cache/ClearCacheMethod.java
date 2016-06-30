package chat.cache;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)  
@Retention(RetentionPolicy.RUNTIME)       
@Documented      
@Inherited   
public @interface ClearCacheMethod {
	/**
	 * Specify another cache name which different from current CacheClass or current class don't specify as a CacheClass.
	 * The another cache will not force creating by this usage, but only create by specified CacheClass.
	 * 
	 * @return
	 */
	public String otherCache() default "";
}
