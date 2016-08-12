package script.memodb.index;

import java.io.IOException;
import java.util.Comparator;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.BiFunction;

public class IndexMapTest {
	IndexMapTest(String path) {
	}
	
	public static void main(String[] args) throws IOException {
//		TreeMap<String, String> treeMap = new TreeMap<>();
		int count = 10;
		long time = System.currentTimeMillis();
//		for(int i = 0; i < count; i++) {
//			treeMap.put("hello" + i, "hi");
//		}
//		System.out.println("TreeMap insert " + count + " takes " + ((float)(System.currentTimeMillis() - time) / 1000) + "s");
//		
//		HashMap<String, String> hashMap = new HashMap<>();
//		time = System.currentTimeMillis();
//		for(int i = 0; i < count; i++) {
//			hashMap.put("hello" + i, "hi");
//		}
//		System.out.println("HashMap insert " + count + " takes " + ((float)(System.currentTimeMillis() - time) / 1000) + "s");
//		
//		ConcurrentSkipListMap<String, String> skipMap = new ConcurrentSkipListMap<>();
//		time = System.currentTimeMillis();
//		for(int i = 0; i < count; i++) {
//			skipMap.put("hello" + i, "hi");
//		}
//		System.out.println("ConcurrentSkipListMap insert " + count + " takes " + ((float)(System.currentTimeMillis() - time) / 1000) + "s");
		ConcurrentSkipListMap<String, String> skipComparatorMap = null;
		Comparator<String> comparator = new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				int c = o1.compareTo(o2);
//				if(c == 0) {
//					String v1 = skipComparatorMap.get(o1);
//				}
				return c;
			}
		};
		skipComparatorMap = new ConcurrentSkipListMap<>(comparator);
		time = System.currentTimeMillis();
		for(int i = 0; i < count; i++) {
			skipComparatorMap.put("hello" + i, "hi");
			skipComparatorMap.put("hello" + i, "hi");
		}
		System.out.println("ConcurrentSkipListMap w/ comparator insert " + count + " actual " + skipComparatorMap.size() + " takes " + ((float)(System.currentTimeMillis() - time) / 1000) + "s");
		
		time = System.currentTimeMillis();
		ConcurrentNavigableMap<String, String> subMap = skipComparatorMap.subMap("hello3", "hello5");
		for(String key : subMap.keySet()) {
			System.out.println("subMap key = " + key);
		}
		System.out.println("subMap " + subMap.size() + " takes " + ((float)(System.currentTimeMillis() - time) / 1000) + "s");
		
//		time = System.currentTimeMillis();
//		ConcurrentNavigableMap<String, String> headSubMap = skipComparatorMap.headMap("hello5");
//		for(String key : headSubMap.keySet()) {
//			System.out.println("headSubMap key = " + key);
//		}
//		System.out.println("headMap takes " + ((float)(System.currentTimeMillis() - time) / 1000) + "s");
		
//		String value = skipComparatorMap.compute("hello2", new BiFunction<String, String, String>() {
//			@Override
//			public String apply(String t, String u) {
//				System.out.println("t " + t + " u " + u);
//				return "aa";
//			}
//		});
//		System.out.println("value " + value);
		
//		for(String key : headSubMap.keySet()) {
//			System.out.println("headSubMap key = " + key);
//		}
	}
}
