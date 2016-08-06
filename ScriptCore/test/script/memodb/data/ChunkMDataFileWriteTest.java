package script.memodb.data;

import java.io.IOException;

public class ChunkMDataFileWriteTest {
	ChunkMDataFileWriteTest(String path) {
	}
	
	public static void main(String[] args) throws IOException {
//		ChunkMDataFile file = new ChunkMDataFile("C:\\Dev\\tmp\\1.chunk");
		ChunkMDataFile file = new ChunkMDataFile("/Users/aplombchen/Desktop/1.chunk");
//		ChunkMDataFile file = new ChunkMDataFile("/tmp/test/1.chunk");
		file.open();
		long time = System.currentTimeMillis();
		int count = 10000000;
		for(int i = 0; i < count;i++) {
			Chunk chunk = new Chunk();
			chunk.chunkNum = 0;
			chunk.dataBytes = ("hello world " + i).getBytes("utf8");
			chunk.chunkLength = chunk.dataBytes.length;
			chunk.nextChunkOffset = -1;
			chunk.nextChunkNum = -1;
			
			file.add(chunk);
		}
		long takes = (System.currentTimeMillis() - time);
		System.out.println("takes " + takes);
		System.out.println("takes seconds " + ((float)takes / 1000));
		System.out.println("byte per second " + count * 38 / ((float)takes / 1000) / 1024 / 1024 + "m");
		
	}
}
