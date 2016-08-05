package script.memodb.data;

import java.io.IOException;

public abstract class MData implements MMFileSerializable {
	public static byte VERSION_DELETED = -100;
	public static byte VERSION_UPDATING = -1;
	public static byte VERSION_CORRUPTED = -10;
	
	protected static final int OFFSET_VERSION = 1;
	public byte version;
	
	protected static final int OFFSET_LENGTH = 4;
	public int length;
	
	protected static final int OFFSET_MDATA = OFFSET_VERSION + OFFSET_LENGTH;

	@Override
	public void resurrect(MemoryMappedFile memoFile, long address) throws IOException {
		version = memoFile.getByte(address);
		length = memoFile.getInt(address + OFFSET_VERSION);
	}
	
	public static int readDataLength(MemoryMappedFile memoFile, long address) {
		return memoFile.getInt(address + OFFSET_VERSION);
	}
	
	public boolean isCompletedData() {
		return version >= 0;
	}
	
	@Override
	public void persistent(MemoryMappedFile memoFile, long address) throws IOException {
		memoFile.putByte(address, VERSION_UPDATING);
		memoFile.putInt(address + OFFSET_VERSION, length());
	}
	
	public void persistentDone(MemoryMappedFile memoFile, long address) {
		memoFile.putByte(address, version);
	}
	
	public void persistentCorrupted(MemoryMappedFile memoFile, long address) {
		memoFile.putByte(address, VERSION_CORRUPTED);
	}
	
	public void persistentDeleted(MemoryMappedFile memoFile, long address) {
		memoFile.putByte(address, VERSION_CORRUPTED);
	}
	
	protected abstract int length();
	
	public int dataLength() {
		return length() + OFFSET_MDATA;
	}
}
