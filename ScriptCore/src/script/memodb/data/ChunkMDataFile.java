package script.memodb.data;

import java.io.IOException;

public class ChunkMDataFile extends MDataFile<Chunk> {
	private ChunkMDataFile(String path) {
		super(path, MDATAFILE_MAXSIZE_CHUNK);
	}
	
	public static void main(String[] args) throws IOException {
		ChunkMDataFile file = new ChunkMDataFile("C:\\Dev\\tmp\\1.chunk");
//		ChunkMDataFile file = new ChunkMDataFile("/Users/aplombchen/Desktop/1.chunk");
		file.open();
		
		Chunk chunk = new Chunk();
		chunk.chunkNum = 0;
		chunk.dataBytes = "hello world".getBytes("utf8");
		chunk.chunkLength = chunk.dataBytes.length;
		chunk.nextChunkAddress = -1;
		chunk.nextChunkNum = -1;
		
		file.add(chunk);
		
		Chunk readChunk = new Chunk();
		file.read(0, readChunk);
		
	}
}
