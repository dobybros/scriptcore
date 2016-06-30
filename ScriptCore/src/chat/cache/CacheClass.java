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
public @interface CacheClass {
	/**
	 * The max time to idle
	 * 
	 * default is 900
	 * 
	 * @return
	 */
	public long timeToIdleSeconds() default CacheHandler.DEFAULT_TIMETOIDLESECONDS;
	/**
	 * The max time to live include idle.
	 * 
	 * default is 1800
	 * 
	 * @return
	 */
	public long timeToLiveSeconds() default CacheHandler.DEFAULT_TIMETOLIVESECONDS;
	/**
	 * Name need be unique
	 * 
	 * @return
	 */
	public String name();
	/**
	 * LRU - least recently used
	 * LFU - least frequently used
	 * FIFO - first in first out, the oldest element by creation time
	 * 
	 * default is LRU	
	 * 
	 * @return
	 */
	public String policy() default CacheHandler.DEFAULT_POLICY;
	/**
	 * cache will never be expired
	 * 
	 * @return
	 */
	public boolean eternal() default CacheHandler.DEFAULT_ETERNAL;
	/**
	 * MaxEntries for caching in memory. 
	 * 
	 * default is 1,000,000
	 * 
	 * @return
	 */
	public int maxEntries() default CacheHandler.DEFAULT_MAXENTIRIES;
}
