package script.memodb;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MemoData {
	public static final String ID = "id";
	private String id;
	private Map<String, Object> dataMap = new LinkedHashMap<>();

	public MemoData putShortList(String key, List<Short> shorts) {
		dataMap.put(key, shorts);
		return this;
	}

	@SuppressWarnings("unchecked")
	public List<Short> getShortList(String key) {
		return (List<Short>) dataMap.get(key);
	}

	public MemoData putDoubleList(String key, List<Double> doubles) {
		dataMap.put(key, doubles);
		return this;
	}

	@SuppressWarnings("unchecked")
	public List<Double> getDoubleList(String key) {
		return (List<Double>) dataMap.get(key);
	}

	public MemoData putLongList(String key, List<Long> longs) {
		dataMap.put(key, longs);
		return this;
	}
	@SuppressWarnings("unchecked")
	public List<Long> getLongList(String key) {
		return (List<Long>) dataMap.get(key);
	}

	public MemoData putString(String key, List<String> strings) {
		dataMap.put(key, strings);
		return this;
	}
	@SuppressWarnings("unchecked")
	public List<String> getStringList(String key) {
		return (List<String>) dataMap.get(key);
	}

	public MemoData putIntegerList(String key, List<Integer> integers) {
		dataMap.put(key, integers);
		return this;
	}
	@SuppressWarnings("unchecked")
	public List<Integer> getIntegerList(String key) {
		return (List<Integer>) dataMap.get(key);
	}

	public MemoData putMemoDataList(String key, List<MemoData> mData) {
		dataMap.put(key, mData);
		return this;
	}
	@SuppressWarnings("unchecked")
	public List<MemoData> getMemoDataList(String key) {
		return (List<MemoData>) dataMap.get(key);
	}

	public MemoData putMemoData(String key, MemoData mData) {
		dataMap.put(key, mData);
		return this;
	}

	public MemoData getMemoData(String key) {
		return (MemoData) dataMap.get(key);
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
