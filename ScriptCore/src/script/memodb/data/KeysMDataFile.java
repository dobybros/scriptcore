package script.memodb.data;

public class KeysMDataFile extends MDataFile<Keys> {
	public KeysMDataFile(String path) {
		super(path, MDATAFILE_MAXSIZE_KEYS);
	}
}
