package script.memodb.data;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

public class Keys extends MData {
	/**
	 * Id of data the Keys stand for. 
	 */
	private String id;
	
	private short idLength;
	private static final int OFFSET_IDLENGTH = 2;
	
	/**
	 * The count of keys for this data. 
	 */
	private static final int OFFSET_KEYCOUNT = 4;
	
	/**
	 * Map of each key. 
	 * kv, id : Key
	 */
	private HashMap<String, Key> keyMap;
	public class Key {
		/**
		 * Length of key
		 */
		private short keyLength;
		private static final int OFFSET_KEYMAP_KEYLENGTH = 2;
		
		/**
		 * Key in string
		 */
		private String key;
		
		/**
		 * Chunk file number
		 */
		private int chunkFileNumber;
		private static final int OFFSET_KEYMAP_CHUNKFILENUMBER = 4;
		
		/**
		 * Chunk address in Chunk file specified by chunkFileName. 
		 */
		private int chunkOffset;
		private static final int OFFSET_KEYMAP_CHUNKOFFSET = 4;
		
		/**
		 * The count of all chunks. 
		 * This chunk could be first chunk, other chunks could be in different chunk file. 
		 */
		private int chunkCount;
		private static final int OFFSET_KEYMAP_CHUNKCOUNT = 4;
		
		public int resurrect(MemoryMappedFile memoFile, int offsetInc) throws IOException {
			keyLength = memoFile.getShort(offsetInc);
			offsetInc += OFFSET_KEYMAP_KEYLENGTH;
			
			if(keyLength > 0) {
				byte[] keyBytes = new byte[keyLength];
				memoFile.getBytes(offsetInc, keyBytes, 0, keyLength);
				key = new String(keyBytes, "utf8");
				offsetInc += keyLength;
			}
			
			chunkFileNumber = memoFile.getInt(offsetInc);
			offsetInc += OFFSET_KEYMAP_CHUNKFILENUMBER;
			
			chunkOffset = memoFile.getInt(offsetInc);
			offsetInc += OFFSET_KEYMAP_CHUNKOFFSET;
			
			chunkCount = memoFile.getInt(offsetInc);
			offsetInc += OFFSET_KEYMAP_CHUNKCOUNT;
			return offsetInc;
		}
		
		public int persistent(MemoryMappedFile memoFile, int offsetInc) throws IOException {
			if(keyLength > 0) {
				memoFile.putShort(offsetInc, keyLength);
				offsetInc += OFFSET_KEYMAP_KEYLENGTH;
				
				byte[] keyBytes = key.getBytes("utf8");
				memoFile.setBytes(offsetInc, keyBytes, 0, keyLength);
				offsetInc += keyLength;
			} else {
				memoFile.putShort(offsetInc, (short)0);
				offsetInc += OFFSET_KEYMAP_KEYLENGTH;
			}
			
			memoFile.putInt(offsetInc, chunkFileNumber);
			offsetInc += OFFSET_KEYMAP_CHUNKFILENUMBER;
			
			memoFile.putInt(offsetInc, chunkOffset);
			offsetInc += OFFSET_KEYMAP_CHUNKOFFSET;
			
			memoFile.putInt(offsetInc, chunkCount);
			offsetInc += OFFSET_KEYMAP_CHUNKCOUNT;
			return offsetInc;
		}
		
		protected int length() {
			return OFFSET_KEYMAP_CHUNKCOUNT + 
					OFFSET_KEYMAP_CHUNKOFFSET +
					OFFSET_KEYMAP_CHUNKFILENUMBER + 
					OFFSET_KEYMAP_KEYLENGTH +
					keyLength;
		}

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
			try {
				byte[] keyBytes = this.key.getBytes("utf8");
				keyLength = (short)keyBytes.length;
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}

		public int getChunkFileNumber() {
			return chunkFileNumber;
		}

		public void setChunkFileNumber(int chunkFileNumber) {
			this.chunkFileNumber = chunkFileNumber;
		}

		public int getChunkOffset() {
			return chunkOffset;
		}

		public void setChunkOffset(int chunkOffset) {
			this.chunkOffset = chunkOffset;
		}

		public int getChunkCount() {
			return chunkCount;
		}

		public void setChunkCount(int chunkCount) {
			this.chunkCount = chunkCount;
		}
	}
	
	private static final int OFFSET_LENGTH = 
			OFFSET_IDLENGTH + 
			OFFSET_KEYCOUNT;
	
	@Override
	public void resurrect(MemoryMappedFile memoFile, int offset) throws IOException {
		super.resurrect(memoFile, offset);
		int offsetInc = offset + OFFSET_MDATA;
		
		if(isCompletedData()) {
			short idLength = memoFile.getShort(offsetInc);
			offsetInc += OFFSET_IDLENGTH;
			
			byte[] idBytes = new byte[idLength];
			memoFile.getBytes(offsetInc, idBytes, 0, idLength);;
			id = new String(idBytes, "utf8");
			offsetInc += idLength;
			
			int keyCount = memoFile.getInt(offsetInc);
			offsetInc += OFFSET_KEYCOUNT;
			
			keyMap = new HashMap<>();
			for(int i = 0; i < keyCount; i++) {
				Key key = new Key();
				offsetInc = key.resurrect(memoFile, offsetInc);
				keyMap.put(key.key, key);
			}
		}
	}
	
	@Override
	public void persistent(MemoryMappedFile memoFile, int offset) throws IOException {
		if(StringUtils.isBlank(id))
			throw new IOException("id couldn't be null");
		super.persistent(memoFile, offset);
		int offsetInc = offset + OFFSET_MDATA;
		try {
			byte[] idBytes = id.getBytes("utf8");
			
			memoFile.putInt(offsetInc, idBytes.length);
			offsetInc += OFFSET_IDLENGTH;
			
			memoFile.setBytes(offsetInc, idBytes, 0, idBytes.length);;
			offsetInc += idBytes.length;
			
			if(keyMap == null || keyMap.isEmpty()) {
				memoFile.putInt(offsetInc, 0);
				offsetInc += OFFSET_KEYCOUNT;
			} else {
				memoFile.putInt(offsetInc, keyMap.size());
				offsetInc += OFFSET_KEYCOUNT;
				
				Set<Entry<String, Key>> entries = keyMap.entrySet();
				for(Entry<String, Key> entry : entries) {
					Key key = entry.getValue();
					offsetInc = key.persistent(memoFile, offsetInc);
				}
			}
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
		int length = OFFSET_LENGTH;
		length += idLength;
		if(keyMap != null) {
			Collection<Key> values = keyMap.values();
			for(Key value : values) {
				length += value.length();
			}
		}
		return length;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
		try {
			byte[] idBytes = this.id.getBytes("utf8");
			idLength = (short) idBytes.length;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	public HashMap<String, Key> getKeyMap() {
		return keyMap;
	}

	public void setKeyMap(HashMap<String, Key> keyMap) {
		this.keyMap = keyMap;
	}
}
