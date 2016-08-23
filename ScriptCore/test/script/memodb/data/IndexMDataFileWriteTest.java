package script.memodb.data;

import java.io.IOException;
import java.util.HashMap;

import script.memodb.data.IndexMDataFile.IndexInfo;
import script.memodb.data.Keys.Key;

public class IndexMDataFileWriteTest {
	IndexMDataFileWriteTest(String path) {
	}
	
	public static void main(String[] args) throws IOException {
//		KeysMDataFile file = new KeysMDataFile("C:\\Dev\\tmp\\1.keys");
		IndexMDataFile file = new IndexMDataFile("/Users/aplombchen/Desktop/1.index");
//		ChunkMDataFile file = new ChunkMDataFile("/tmp/test/1.chunk");
		file.setIndexes(new IndexInfo("a", IndexInfo.SORT_ASC), new IndexInfo("hello", IndexInfo.SORT_DESC));
		file.setUnique(true);
		file.open();
		int length = getKey(1).dataLength() + 4;
		System.out.println("length " + length);
		long time = System.currentTimeMillis();
		int count = 1;
		for(int i = 0; i < count;i++) {
			file.add(getKey(i));
		}
		long takes = (System.currentTimeMillis() - time);
		System.out.println("takes " + takes);
		System.out.println("takes seconds " + ((float)takes / 1000));
		System.out.println("byte per second " + count * length / ((float)takes / 1000) / 1024 / 1024 + "m");
		
	}

	private static Index<String> getIndex() {
		Index<String> index = new Index<String>();
		index.setKeyFileNumber(2);
		index.setKeyOffset(23432);
		index.setValue("hello");
		return index;
	}
}
