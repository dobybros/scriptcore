package script.memodb.data;

import java.io.IOException;

public abstract class MData implements MMFileSerializable {
	public static byte VERSION_DELETED = -100;
	public static byte VERSION_UPDATING = -1;
	public static byte VERSION_CORRUPTED = -10;
	
	private static final int VERSION = 1;
	protected static final int OFFSET_VERSION = 1;
	private byte version = VERSION;
	
	public byte getVersion() {
		return version;
	}

	public void setVersion(byte version) {
		this.version = version;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	protected static final int OFFSET_LENGTH = 4;
	private int length;
	
	protected static final int OFFSET_MDATA = OFFSET_VERSION + OFFSET_LENGTH;

	@Override
	public void resurrect(MemoryMappedFile memoFile, int offset) throws IOException {
		version = memoFile.getByte(offset);
		if(version != VERSION)
			throw new IOException("Illegal version " + version + " of MData, expected " + VERSION);
		length = memoFile.getInt(offset + OFFSET_VERSION);
	}
	
	public static int readDataLength(MemoryMappedFile memoFile, int offset) throws IOException {
		return memoFile.getInt(offset + OFFSET_VERSION);
	}
	
	public boolean isCompletedData() {
		return version > 0;
	}
	
	@Override
	public void persistent(MemoryMappedFile memoFile, int offset) throws IOException {
//		memoFile.putByte(offset, VERSION_UPDATING);
//		memoFile.putInt(offset + OFFSET_VERSION, length());
	}
	
	public void persistentDone(MemoryMappedFile memoFile, int offset) throws IOException {
		memoFile.putByte(offset, version);
	}
	
	public void persistentCorrupted(MemoryMappedFile memoFile, int offset) throws IOException {
		memoFile.putByte(offset, VERSION_CORRUPTED);
	}
	
	public void persistentDeleted(MemoryMappedFile memoFile, int offset) throws IOException {
		memoFile.putByte(offset, VERSION_CORRUPTED);
	}
	
	protected abstract int length();
	
	public int dataLength() {
		return length() + OFFSET_MDATA;
	}
}
