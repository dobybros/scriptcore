package script.memodb.data;

import java.io.IOException;
import java.util.HashMap;

import script.memodb.data.Keys.Key;

public class KeysMDataFileGroupTest {
	KeysMDataFileGroupTest(String path) {
	}
	
	public static void main(String[] args) throws IOException {
		MDataFileGroup<KeysMDataFile> group = new MDataFileGroup<KeysMDataFile>("/Users/aplombchen/Desktop/data", "topic.keys");
		int length = getKey(1).dataLength() + 4;
		System.out.println("length " + length);
		long time = System.currentTimeMillis();
		int count = 10000000;
		KeysMDataFile keyFile = group.findCurrent();
		for(int i = 0; i < count;i++) {
			keyFile.add(getKey(i));
		}
		long takes = (System.currentTimeMillis() - time);
		System.out.println("takes " + takes);
		System.out.println("takes seconds " + ((float)takes / 1000));
		System.out.println("byte per second " + count * length / ((float)takes / 1000) / 1024 / 1024 + "m");
		
	}

	private static Keys getKey(int i) {
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
		
		Key k2 = keys.new Key();
		k2.setChunkFileNumber(1);
		k2.setChunkCount(1);
		k2.setChunkOffset(324234);
		k2.setKey("hello1");
		keyMap.put(k2.getKey(), k2);
		
		Key k3 = keys.new Key();
		k3.setChunkFileNumber(1);
		k3.setChunkCount(1);
		k3.setChunkOffset(324234);
		k3.setKey("hello2");
		keyMap.put(k3.getKey(), k3);
		
		Key k4 = keys.new Key();
		k4.setChunkFileNumber(1);
		k4.setChunkCount(1);
		k4.setChunkOffset(324234);
		k4.setKey("hello4");
		keyMap.put(k4.getKey(), k4);
		return keys;
	}
}
