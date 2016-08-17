package script.memodb.data;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;

import script.memodb.data.Keys.Key;


public class IndexMDataFile extends MDataFile<Index<?>> {
	private static final int OFFSET_INDEXMDATAFILE_UNIQUE = 1;
	public IndexMDataFile(String path) {
		super(path, MDATAFILE_MAXSIZE_INDEX);
	}
	
	public static class IndexInfo {
		private static final int OFFSET_INDEXINFO_LENGTH = 1;
		private static final int OFFSET_INDEXINFO_SORT = 1;
		public IndexInfo(String key, byte sort) {
			if(StringUtils.isBlank(key))
				throw new IllegalArgumentException("Index key couldn't be blank.");
			if(sort != SORT_ASC && sort != SORT_DESC)
				throw new IllegalArgumentException("Sort parameter is illegal, " + sort + " expecting " + SORT_ASC + " or " + SORT_DESC);
			this.key = key;
			this.sort = sort;
		}
		private String key;
		public static final byte SORT_ASC = 1;
		public static final byte SORT_DESC = -1;
		private byte sort;
	}
	private IndexInfo[] indexes;
	private boolean unique;
	
	@Override
	public synchronized void open() throws IOException {
		if(status.compareAndSet(STATUS_STANDBY, STATUS_OPENNING)) {
			try {
				openFile();
				
				if(offset < MDATAFILE_RESERVED) {
					throw new IOException("Offset should not be less than MDATAFILE_RESERVED " + MDATAFILE_RESERVED + ", offset " + offset + ". Path " + path);
				} else if(offset == MDATAFILE_RESERVED) {
					//Mean index file is just created. not prepare index information yet.
					if(indexes == null || indexes.length == 0)
						throw new IOException("Index information is needed to create index file. Path " + path);
					if(indexes.length > Byte.MAX_VALUE) 
						throw new IOException("The number of index information couldn't exceed 127. Path " + path);
					
					memFile.putByte(offset, (byte) (unique ? 1 : 0));
					offset += OFFSET_INDEXMDATAFILE_UNIQUE;
					
					memFile.putByte(offset, (byte) indexes.length);
					offset += IndexInfo.OFFSET_INDEXINFO_LENGTH;
					
					for(IndexInfo indexInfo : indexes) {
						memFile.putByte(offset, indexInfo.sort);
						offset += IndexInfo.OFFSET_INDEXINFO_SORT;
						
						byte[] keyBytes = indexInfo.key.getBytes("utf8");
						memFile.putShort(offset, (short) keyBytes.length);
						offset += Key.OFFSET_KEYMAP_KEYLENGTH;
						
						memFile.setBytes(offset, keyBytes, 0, keyBytes.length);
						offset += keyBytes.length;
					}
				} else {
					//Mean index file is created already. Need read the index information.  
					byte uniqueByte = memFile.getByte(offset);
					unique = (uniqueByte == 0 ? false : true);
					offset += OFFSET_INDEXMDATAFILE_UNIQUE;
					
					byte indexLength = memFile.getByte(offset);
					offset += IndexInfo.OFFSET_INDEXINFO_LENGTH;
					
					indexes = new IndexInfo[indexLength];
					for(int i = 0; i < indexLength; i++) {
						byte sort = memFile.getByte(offset);
						offset += IndexInfo.OFFSET_INDEXINFO_SORT;
						
						short keyLength = memFile.getShort(offset);
						offset += Key.OFFSET_KEYMAP_KEYLENGTH;
						
						byte[] keyBytes = new byte[keyLength];
						memFile.getBytes(offset, keyBytes, 0, keyLength);
						offset += keyLength;
						String key = new String(keyBytes, "utf8");
						
						IndexInfo indexInfo = new IndexInfo(key, sort);
						indexes[i] = indexInfo;
					}
				}
				status.compareAndSet(STATUS_OPENNING, STATUS_OPENED);
			} catch (Throwable e) {
				e.printStackTrace();
				status.set(STATUS_CLOSED);
				throw new IOException(this.getClass().getSimpleName() + " open " + path + " size " + length + " failed, " + e.getMessage(), e);
			}
		}
	}

	public IndexInfo[] getIndexes() {
		return indexes;
	}
	public void setIndexes(IndexInfo... indexes) {
		this.indexes = indexes;
	}

	public boolean isUnique() {
		return unique;
	}

	public void setUnique(boolean unique) {
		this.unique = unique;
	}
}
