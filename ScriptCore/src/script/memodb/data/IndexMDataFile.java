package script.memodb.data;

import java.io.IOException;

public class IndexMDataFile extends MDataFile<Index> {
	private IndexMDataFile(String path) {
		super(path, MDATAFILE_MAXSIZE_INDEX);
	}
}
