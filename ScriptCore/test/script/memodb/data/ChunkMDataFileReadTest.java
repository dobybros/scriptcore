package script.memodb.data;

import java.io.IOException;

public class ChunkMDataFileReadTest extends MDataFile<Index> {
	private ChunkMDataFileReadTest(String path) {
		super(path, MDATAFILE_MAXSIZE_INDEX);
	}
	
	public static void main(String[] args) throws IOException {
//		ChunkMDataFile file = new ChunkMDataFile("C:\\Dev\\tmp\\1.chunk");
		ChunkMDataFile file = new ChunkMDataFile("/Users/aplombchen/Desktop/1.chunk");
//		ChunkMDataFile file = new ChunkMDataFile("/tmp/test/1.chunk");
		file.open();
		//1073741824
		//845130966
		int count = 10000000;
		long time = System.currentTimeMillis();
		file.readAll(count, new Chunk());
		long takes = (System.currentTimeMillis() - time);
		System.out.println("takes " + takes);
		System.out.println("takes seconds " + ((float)takes / 1000));
		System.out.println("byte per second " + count * 38 / ((float)takes / 1000) / 1024 / 1024 + "m");
	}
}
