package script.memodb.data;

public class IndexMDataFile extends MDataFile<Index> {
	private IndexMDataFile(String path) {
		super(path, MDATAFILE_MAXSIZE_INDEX);
	}
}
