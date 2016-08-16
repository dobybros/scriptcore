package script.memodb.data;

import java.io.IOException;


public class IndexMDataFile extends MDataFile<Index<?>> {
	private IndexMDataFile(String path) {
		super(path, MDATAFILE_MAXSIZE_INDEX);
	}
	
	public class IndexInfo {
		private String key;
		public static final byte SORT_ASC = 1;
		public static final byte SORT_DESC = -1;
		private byte sort;
	}
	private IndexInfo[] indexes;
	private boolean unique;
	
	@Override
	public void open() throws IOException {
		super.open();
		
		int dataLength = mdata.dataLength();
		int nextOffset = offset + dataLength;
		boolean acquired = memFile.compareAndSwapInt(RESERVED_CURSORADDRESS, offset, nextOffset);
		if(acquired) {
			offset = nextOffset;
			memFile.putByte(offset, MData.VERSION_UPDATING);
			memFile.putInt(offset + MData.OFFSET_VERSION, dataLength);
			
			memFile.putIntVolatile(RESERVED_CURSORADDRESS, offset);
			return true;
		}
		return false;
	}
}
