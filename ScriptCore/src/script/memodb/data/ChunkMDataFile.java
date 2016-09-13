package script.memodb.data;


public class ChunkMDataFile extends MDataFile<Chunk> {
	ChunkMDataFile(String path) {
		super(path, MDATAFILE_MAXSIZE_CHUNK);
	}
}
