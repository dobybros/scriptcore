package script.memodb.data;

import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentSkipListSet;

import org.apache.commons.lang.StringUtils;

import script.memodb.data.Keys.Key;
import script.memodb.utils.MDataIndexMap;

/**
 * 
 * 
 * @author aplombchen
 *
 * @param <T>
 */
public class Index<T> extends MData{
	
	public static final byte VALUETYPE_INT = 1;
	public static final byte VALUETYPE_DOUBLE = 2;
	public static final byte VALUETYPE_FLOAT = 3;
	public static final byte VALUETYPE_STRING = 4;
	public static final byte VALUETYPE_LONG = 5;
	public static final byte VALUETYPE_SHORT = 6;
	public static final byte VALUETYPE_BYTE = 7;
	
	/**
	 * Type of index value. 
	 */
	private byte valueType;
	private static final int OFFSET_VALUETYPE = 1;
	
	/**
	 * Id of data this index stand for.
	 */
//	public String id;
	private int keyOffset;
	private static final int OFFSET_KEYOFFSET = 4;
	
	private int keyFileNumber;
	private static final int OFFSET_KEYFILENUMBER = 4;
	
	private static final int OFFSET_VALUELENGTH = 2;
	/**
	 * Index value
	 */
	private T value;
	
	private Index<?> nextIndex;
	
	//////////////////////////Runtime
	private MDataIndexMap<?> nextIndexMap;
	
	private ConcurrentSkipListSet<Index<T>> duplicatedSet;
	public static final int TYPE_DUPLICATED = 10;
	public static final int TYPE_INDEX = 1;
	private int type = TYPE_INDEX;

	public void enableDuplicatedSet() {
		type = TYPE_DUPLICATED;
		duplicatedSet = new ConcurrentSkipListSet<Index<T>>();
	}
	
	public boolean add(Index<T> index) {
		return duplicatedSet.add(index);
	}
	
	public boolean remove(Index<T> index) {
		return duplicatedSet.remove(index);
	}
	
	public boolean isEmpty() {
		return duplicatedSet.isEmpty();
	}
	
	public ConcurrentSkipListSet<Index<T>> getDuplicatedSet() {
		return duplicatedSet;
	}
	
	@Override
	public void resurrect(MemoryMappedFile memoFile, int offset) throws IOException {
		super.resurrect(memoFile, offset);
		int offsetInc = offset + OFFSET_MDATA;
		
		if(isCompletedData()) {
			valueType = memoFile.getByte(offsetInc);
			offsetInc += OFFSET_VALUETYPE;
			
			keyOffset = memoFile.getInt(offsetInc);
			offsetInc += OFFSET_KEYOFFSET;
			
			keyFileNumber = memoFile.getInt(offsetInc);
			offsetInc += OFFSET_KEYFILENUMBER;
			
//			int keyCount = memoFile.getInt(offsetInc);
//			offsetInc += OFFSET_KEYCOUNT;
//			
//			keyMap = new HashMap<>();
//			for(int i = 0; i < keyCount; i++) {
//				Key key = new Key();
//				offsetInc = key.resurrect(memoFile, offsetInc);
//				keyMap.put(key.key, key);
//			}
		}
	}
	
	@Override
	public void persistent(MemoryMappedFile memoFile, int offset) throws IOException {
//		if(StringUtils.isBlank(id))
//			throw new IOException("id couldn't be null");
		super.persistent(memoFile, offset);
		int offsetInc = offset + OFFSET_MDATA;
		try {
//			byte[] idBytes = id.getBytes("utf8");
			
//			memoFile.putInt(offsetInc, idBytes.length);
//			offsetInc += OFFSET_IDLENGTH;
//			
//			memoFile.setBytes(offsetInc, idBytes, 0, idBytes.length);;
//			offsetInc += idBytes.length;
//			
//			if(keyMap == null || keyMap.isEmpty()) {
//				memoFile.putInt(offsetInc, 0);
//				offsetInc += OFFSET_KEYCOUNT;
//			} else {
//				memoFile.putInt(offsetInc, keyMap.size());
//				offsetInc += OFFSET_KEYCOUNT;
//				
//				Set<Entry<String, Key>> entries = keyMap.entrySet();
//				for(Entry<String, Key> entry : entries) {
//					Key key = entry.getValue();
//					offsetInc = key.persistent(memoFile, offsetInc);
//				}
//			}
			persistentDone(memoFile, offset);
		} catch(Throwable t) {
			t.printStackTrace();
//			persistentCorrupted(memoFile, address);
			if(t instanceof IOException) {
				throw t;
			} else {
				throw new IOException(t.getMessage(), t);
			}
		}
	}
	
	@Override
	protected int length() {
		return 0;
	}

	public byte getValueType() {
		return valueType;
	}

	public void setValueType(byte valueType) {
		this.valueType = valueType;
	}

	public int getKeyOffset() {
		return keyOffset;
	}

	public void setKeyOffset(int keyOffset) {
		this.keyOffset = keyOffset;
	}

	public int getKeyFileNumber() {
		return keyFileNumber;
	}

	public void setKeyFileNumber(int keyFileNumber) {
		this.keyFileNumber = keyFileNumber;
	}

	public T getValue() {
		return value;
	}

	public void setValue(T value) {
		this.value = value;
	}

	public int getType() {
		return type;
	}

	public Index<?> getNextIndex() {
		return nextIndex;
	}

	public void setNextIndex(Index<?> nextIndex) {
		this.nextIndex = nextIndex;
	}

}
