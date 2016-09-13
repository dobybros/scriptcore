package script.memodb.data;

import java.io.IOException;

public class IndexMDataFileReadTest {
	
	public static void main(String[] args) throws IOException {
//		KeysMDataFile file = new KeysMDataFile("C:\\Dev\\tmp\\1.keys");
		IndexMDataFile file = new IndexMDataFile("/Users/aplombchen/Desktop/1.index");
//		ChunkMDataFile file = new ChunkMDataFile("/tmp/test/1.chunk");
		file.open();
		//1073741824
		//845130966
		int count = 10000000;
		long time = System.currentTimeMillis();
		Keys keys = new Keys();
//		file.read(0, keys);
//		file.readAll(count, keys);
		long takes = (System.currentTimeMillis() - time);
		System.out.println("takes " + takes);
		System.out.println("takes seconds " + ((float)takes / 1000));
		System.out.println("byte per second " + count * 102 / ((float)takes / 1000) / 1024 / 1024 + "m");
	}
}
