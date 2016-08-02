package script.memodb.data;

import java.util.Map;

public class Keys extends Blob{
	public String id;
	public short keyCount;
	public Map<String, Key> keyMap;
	public class Key {
		public short keyLength;
		public String key;
		public int chunkFileNumber;
		public int chunkAddress;
		public int chunkCount;
	}
}
