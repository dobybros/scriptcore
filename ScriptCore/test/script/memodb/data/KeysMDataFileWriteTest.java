package script.memodb.data;

import java.io.IOException;
import java.util.HashMap;

import script.memodb.data.Keys.Key;

public class KeysMDataFileWriteTest {
	KeysMDataFileWriteTest(String path) {
	}
	
	public static void main(String[] args) throws IOException {
//		ChunkMDataFile file = new ChunkMDataFile("C:\\Dev\\tmp\\1.chunk");
		KeysMDataFile file = new KeysMDataFile("/Users/aplombchen/Desktop/1.keys");
//		ChunkMDataFile file = new ChunkMDataFile("/tmp/test/1.chunk");
		file.open();
		long time = System.currentTimeMillis();
		int count = 10000000;
		for(int i = 0; i < count;i++) {
			Keys keys = new Keys();
			keys.setId("123key " + i);
			HashMap<String, Key> keyMap = new HashMap<>();
			keys.setKeyMap(keyMap);
			Key k1 = keys.new Key();
			k1.setChunkFileNumber(1);
			k1.setChunkCount(1);
			k1.setChunkOffset(324234);
			k1.setKey("hello");
			keyMap.put(k1.getKey(), k1);
			keyMap.put(k1.getKey(), k1);
			keyMap.put(k1.getKey(), k1);
			keyMap.put(k1.getKey(), k1);
			file.add(keys);
		}
		long takes = (System.currentTimeMillis() - time);
		System.out.println("takes " + takes);
		System.out.println("takes seconds " + ((float)takes / 1000));
		System.out.println("byte per second " + count * 38 / ((float)takes / 1000) / 1024 / 1024 + "m");
		
	}
}
