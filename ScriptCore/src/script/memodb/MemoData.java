package script.memodb;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MemoData {
	public static final String ID = "_id";
	private String id;
	private Map<String, Object> dataMap = new HashMap<>();
	
	public static void main(String[] args) {
		MemoData data = new MemoData();
		data.putIntegerIterable("a", Arrays.asList(new Integer[]{1}));
	}
	
	public MemoData putIntegerIterable(String key, Iterable<Integer> value) {
		dataMap.put(key, value);
		return this;
	}
	
	public MemoData putInteger(String key, Integer value) {
		dataMap.put(key, value);
		return this;
	}
	
	public Integer getInteger(String key) {
		return (Integer) dataMap.get(key);
	}
	
	public MemoData putString(String key, String value) {
		dataMap.put(key, value);
		return this;
	}
	
	public String getString(String key, String value) {
		return (String) dataMap.get(key);
	}
	
	public MemoData putLong(String key, Long value) {
		dataMap.put(key, value);
		return this;
	}
	
	public Long getLong(String key) {
		return (Long) dataMap.get(key);
	}
	
	public MemoData putByteArray(String key, byte[] value) {
		dataMap.put(key, value);
		return this;
	}
	
	public byte[] getByteArray(String key) {
		return (byte[]) dataMap.get(key);
	}
	
	public MemoData putDouble(String key, Double value) {
		dataMap.put(key, value);
		return this;
	}
	
	public Double getDouble(String key) {
		return (Double) dataMap.get(key);
	}
	
	public MemoData putShort(String key, Short value) {
		dataMap.put(key, value);
		return this;
	}
	
	public Short getShort(String key) {
		return (Short) dataMap.get(key);
	}
	
	public MemoData putByte(String key, Byte value) {
		dataMap.put(key, value);
		return this;
	}
	
	public Byte getByte(String key) {
		return (Byte) dataMap.get(key);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		dataMap.put(ID, id);
		this.id = id;
	}
}
