package core.utils


/**
 * For output json, if value is null, then don't append entry into json object.
 *
 * Created by aplombchen on 9/10/16.
 */
class CleanLinkedHashMap<K, V> extends LinkedHashMap<K, V> {
    public V put(K key, V value) {
        if(value != null)
            return super.put(key, value);
        return null;
    }

    /**
     * @param key
     * @param clazz Only support DBObjectable
     * @return clazz instance
     */
    @SuppressWarnings("unchecked")
    public <V> V get(String key, Class<? super V> clazz) {
        Object value = super.get(key);
        if (value == null) {
            return null;
        } else {
            return (V) value;
        }
    }

    /** Returns the value of a field as an <code>integer</code>.
     * @param key the field to look for
     * @return the field value or null
     */
    public Integer getInteger( String key ){
        Object o = get(key);
        if ( o == null )
            return null;

		if(o instanceof Number) {
			return ((Number)o).intValue();
		} else if(o instanceof String){
			try {
				return Integer.parseInt(o);
			} catch(Throwable t) {}
		}
        return null;
    }

    /** Returns the value of a field as an <code>Integer</code>.
     * @param key the field to look for
     * @param def the default to return
     * @return the field value (or default)
     */
    public Integer getInteger( String key , Integer i ){
        Object foo = get( key );
        if ( foo == null )
            return i;

        if(o instanceof Number) {
			return ((Number)o).intValue();
		} else if(o instanceof String){
			try {
				return Integer.parseInt(o);
			} catch(Throwable t) {}
		}
        return null;
    }

    /**
     * Returns the value of a field as a <code>Long</code>.
     *
     * @param key the field to return
     * @return the field value or null
     */
    public Long getLongObject( String key){
        Object foo = get( key );
        if (foo == null)
            return null;
        return ((Number)foo).longValue();
    }

    /**
     * Returns the value of a field as an <code>Long</code>.
     * @param key the field to look for
     * @param def the default to return
     * @return the field value (or default)
     */
    public Long getLongObject( String key , Long l ) {
        Object foo = get( key );
        if ( foo == null )
            return l;

        return ((Number)foo).longValue();
    }

    /**
     * Returns the value of a field as a <code>Double</code>.
     *
     * @param key the field to return
     * @return the field value or null
     */
    public Double getDoubleObject( String key){
        Object foo = get( key );
        if (foo == null)
            return null;
        return ((Number)foo).doubleValue();
    }

    /**
     * Returns the value of a field as an <code>Double</code>.
     * @param key the field to look for
     * @param def the default to return
     * @return the field value (or default)
     */
    public Double getDoubleObject( String key , Double d ) {
        Object foo = get( key );
        if ( foo == null )
            return d;

        return ((Number)foo).doubleValue();
    }
}