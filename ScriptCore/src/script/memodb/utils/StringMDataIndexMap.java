package script.memodb.utils;

import java.util.Comparator;

public class StringMDataIndexMap extends MDataIndexMap<String>{

	public StringMDataIndexMap() {
		super(new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				int result = o1.compareTo(o2);
				return result;
			}
		});
	}

}
