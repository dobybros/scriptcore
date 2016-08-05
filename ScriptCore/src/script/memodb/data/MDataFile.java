package script.memodb.data;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class MDataFile<T extends MData> {
	public static final long MDATAFILE_MAXSIZE_CHUNK = 1024 * 1024 * 1024;
	public static final long MDATAFILE_MAXSIZE_INDEX = 256 * 1024 * 1024;
	public static final long MDATAFILE_MAXSIZE_KEYS = 512 * 1024 * 1024;
	private MemoryMappedFile memFile;
	
	/**
	 * Reserved size for a mdatafile. 
	 */
	private static final int MDATAFILE_RESERVED = 256;
	private static final int RESERVED_CURSORADDRESS = 0;
	private long cursorAddress;
	
	public static final int STATUS_STANDBY = 0;
	public static final int STATUS_OPENED = 1;
	public static final int STATUS_CLEANFRAGMENT = 10;
	public static final int STATUS_CLOSED = 100;
	private AtomicInteger status = new AtomicInteger(STATUS_STANDBY);
	
	private String path;
	private long length;
	public MDataFile(String path, long length) {
		this.path = path;
		this.length = length;
	}
	
	public void open() throws IOException {
		if(status.compareAndSet(STATUS_STANDBY, STATUS_OPENED)) {
			try {
				memFile = new MemoryMappedFile(path, length);
				//read reserved parameters for a mdatafile. 
				cursorAddress = memFile.getLongVolatile(RESERVED_CURSORADDRESS);
				if(cursorAddress == 0) {
					cursorAddress = memFile.getAddress() + MDATAFILE_RESERVED;
					memFile.putLongVolatile(RESERVED_CURSORADDRESS, cursorAddress);
				}
			} catch (Throwable e) {
				e.printStackTrace();
				status.set(STATUS_CLOSED);
				throw new IOException(this.getClass().getSimpleName() + " open " + path + " size " + length + " failed, " + e.getMessage(), e);
			}
		}
	}
	
	public void add(T mdata) throws IOException {
		mdata.persistent(memFile, cursorAddress);
		cursorAddress += mdata.dataLength();
		memFile.putLongVolatile(RESERVED_CURSORADDRESS, cursorAddress);
	}
	
	public void read(int pos, T mdata) throws IOException {
		long start = memFile.getAddress() + MDATAFILE_RESERVED;
		for(int i = 0; i < pos; i++) {
			int length = MData.readDataLength(memFile, start);
			start += length;
		}
		mdata.resurrect(memFile, start);
	}
}
