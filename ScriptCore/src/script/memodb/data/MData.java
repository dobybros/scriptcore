package script.memodb.data;

import java.io.IOException;

public abstract class MData implements MMFileSerializable {
	public static int VERSION_DELETED = -100;
	public static int VERSION_UPDATING = -1;
	
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
	
	@Override
	public void persistent(MemoryMappedFile memoFile, long address) throws IOException {
		memoFile.putByte(address, version);
		memoFile.putInt(address + OFFSET_VERSION, length());
	}
	
	protected abstract int length();
}
