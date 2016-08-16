package script.memodb.utils;

import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;

import script.memodb.data.Index;
import chat.utils.IteratorEx;

public abstract class MDataIndexMap<T> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6275321239875889685L;

	private ConcurrentSkipListMap<T, Index<T>> indexMap;
	
	public MDataIndexMap(Comparator<T> comparator) {
		indexMap = new ConcurrentSkipListMap<>(comparator);
	}
	
	public synchronized Index<T> put(T key, Index<T> value) {
		Index<T> index = indexMap.putIfAbsent(key, value);
		if(index != null) {
			Index<T> duplicatedIndex = null;
			if(index.getType() == Index.TYPE_DUPLICATED) {
				duplicatedIndex = index;
			} else {
				duplicatedIndex = new Index<T>();
				duplicatedIndex.enableDuplicatedSet();
				duplicatedIndex.add(index);
				indexMap.put(key, duplicatedIndex);
			}
			duplicatedIndex.add(value);
		}
		return value;
    }
	
	public synchronized Index<T> remove(T key) {
		return indexMap.remove(key);
	}
	
	public synchronized boolean remove(T key, Index<T> value) {
		boolean bool = indexMap.remove(key, value);
		if(!bool) {
			Index<T> index = indexMap.get(key);
			if(index != null && index.getType() == Index.TYPE_DUPLICATED) {
				bool = index.remove(value);
				if(bool && index.isEmpty()) {
					indexMap.remove(key, index);
				}
			}
		}
		return bool;
	}
	
	/**
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException if {@code fromKey} or {@code toKey} is null
     * @throws IllegalArgumentException {@inheritDoc}
     */
    public void subMap(T fromKey,
	                      boolean fromInclusive,
	                      T toKey,
	                      boolean toInclusive) {
    	ConcurrentNavigableMap<T, Index<T>> subMap = indexMap.subMap(fromKey, fromInclusive, toKey, toInclusive);
    	
    }

    public void query(T key, IteratorEx<Index<T>> iterator) {
    	if(iterator == null)
    		return;
    	Index<T> index = indexMap.get(key);
    	if(index != null) {
    		if(index.getType() == Index.TYPE_DUPLICATED) {
    			ConcurrentSkipListSet<Index<T>> duplicatedSet = index.getDuplicatedSet();
    			if(duplicatedSet != null && !duplicatedSet.isEmpty()) {
    				Iterator<Index<T>> indexIterator = duplicatedSet.iterator();
    				while(indexIterator.hasNext()) {
    					Index<T> i = indexIterator.next();
    					if(!iterator.iterate(i))
    						break;
    				}
    			}
    		} else {
    			iterator.iterate(index);
    		}
    	}
    	return;
    }
    
    public Index<T> queryOne(T key) {
    	Index<T> index = indexMap.get(key);
    	if(index != null) {
    		if(index.getType() == Index.TYPE_DUPLICATED) {
    			ConcurrentSkipListSet<Index<T>> duplicatedSet = index.getDuplicatedSet();
    			if(duplicatedSet != null && !duplicatedSet.isEmpty()) {
    				return duplicatedSet.first();
    			}
    		} else {
    			return index;
    		}
    	}
    	return null;
    }
    
	public static void main(String[] args) {
		System.out.println(new Long(50).equals(new Long(50)));
		System.out.println(50 == new Long(50));
		System.out.println(new Long(50) > new Long(50));
	}
}
