package script.memodb.data;

import java.io.IOException;

public class ChunkMDataFile extends MDataFile<Chunk> {
	private ChunkMDataFile(String path) {
		super(path, MDATAFILE_MAXSIZE_CHUNK);
	}
	
	public static void main(String[] args) throws IOException {
		ChunkMDataFile file = new ChunkMDataFile("/Users/aplombchen/Desktop/1.chunk");
		file.open();
	}
}
