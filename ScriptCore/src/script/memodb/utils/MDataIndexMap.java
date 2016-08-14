package script.memodb.utils;

import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;

import script.memodb.data.Index;

public abstract class MDataIndexMap<T> extends ConcurrentSkipListMap<T, Index<T>>{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6275321239875889685L;

	private ConcurrentHashMap<T, ConcurrentSkipListSet<Index<T>>> duplicatedMap = new ConcurrentHashMap<>();
	
	public MDataIndexMap(Comparator<T> comparator) {
		super(comparator);
	}
	
	void addDuplicatedIndexs(T key, Index<T>...indexs) {
		if(key != null) {
			ConcurrentSkipListSet<Index<T>> indexSet = duplicatedMap.get(key);
			if(indexSet == null) {
				indexSet = new ConcurrentSkipListSet<>();
				ConcurrentSkipListSet<Index<T>> theSet = duplicatedMap.putIfAbsent(key, indexSet);
				if(theSet != null) 
					indexSet = theSet;
			}
			for(Index<T> index : indexs) {
				indexSet.add(index);
			}
		}
	}
	
	void removeDuplicatedIndexs(T key, Index<T>...indexs) {
		if(key != null) {
			ConcurrentSkipListSet<Index<T>> indexSet = duplicatedMap.get(key);
			if(indexSet != null) {
				for(Index<T> index : indexs) {
					indexSet.remove(index);
					if(indexSet.isEmpty()) {
						boolean removed = duplicatedMap.remove(key, indexSet);
					}
				}
			}
		}
	}
	
	
	
	public static void main(String[] args) {
		System.out.println(new Long(50).equals(new Long(50)));
		System.out.println(50 == new Long(50));
		System.out.println(new Long(50) > new Long(50));
	}
}
