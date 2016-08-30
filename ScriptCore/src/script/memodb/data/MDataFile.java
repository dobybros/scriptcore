package script.memodb.data;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class MDataFile<T extends MData> {
	public static final int MDATAFILE_MAXSIZE_CHUNK = 1024 * 1024 * 1024;
	public static final int MDATAFILE_MAXSIZE_INDEX = 256 * 1024 * 1024;
	public static final int MDATAFILE_MAXSIZE_KEYS = 1024 * 1024 * 1024;
	
//	public static final int MDATAFILE_MAXSIZE_CHUNK = 11 * 1024 * 1024;
//	public static final int MDATAFILE_MAXSIZE_INDEX = 11 * 1024 * 1024;
//	public static final int MDATAFILE_MAXSIZE_KEYS = 11 * 1024 * 1024;
	MemoryMappedFile memFile;
	
	/**
	 * Reserved size for a mdatafile. 
	 */
	static final int MDATAFILE_RESERVED = 256;
	static final int RESERVED_CURSORADDRESS = 0;
	int offset;
	
	public static final int STATUS_STANDBY = 0;
	public static final int STATUS_OPENNING = 1;
	public static final int STATUS_OPENED = 5;
	public static final int STATUS_CLEANFRAGMENT = 10;
	public static final int STATUS_CLOSED = 100;
	AtomicInteger status = new AtomicInteger(STATUS_STANDBY);
	
	String path;
	int length;
	public MDataFile(String path, int length) {
		this.path = path;
		this.length = length;
	}
	
	public synchronized void open() throws IOException {
		if(status.compareAndSet(STATUS_STANDBY, STATUS_OPENED)) {
			try {
				openFile();
			} catch (Throwable e) {
				e.printStackTrace();
				status.set(STATUS_CLOSED);
				throw new IOException(this.getClass().getSimpleName() + " open " + path + " size " + length + " failed, " + e.getMessage(), e);
			}
		}
	}
	
	void openFile() throws Exception {
		memFile = new MemoryMappedFile(path, length);
		//read reserved parameters for a mdatafile. 
		offset = memFile.getIntVolatile(RESERVED_CURSORADDRESS);
		if(offset == 0) {
			offset = MDATAFILE_RESERVED;
			memFile.putIntVolatile(RESERVED_CURSORADDRESS, offset);
		}		
	}
	
	void check() throws IOException {
		if(status.get() != STATUS_OPENED)
			throw new IOException("Path " + path + " hasn't been openned yet, status " + status.get());
	}
	
	public int add(T mdata) throws IOException {
		check();
		int pos = acquireAdd(mdata);
		if(pos >= 0) {
			mdata.persistent(memFile, pos);
			return ACQUIREADD_SUCCESSFULLY;
		} 
		return pos;
	}
	
	public static final int ACQUIREADD_SUCCESSFULLY = 1;
	public static final int ACQUIREADD_FAILED = -1;
	public static final int ACQUIREADD_NOTENOUGHSPACE = -2;
	
	private synchronized int acquireAdd(T mdata) throws IOException {
		int pos;
		int dataLength = mdata.dataLength();
		int nextOffset = offset + dataLength;
		if(!memFile.isEnough(offset, dataLength)) {
			return ACQUIREADD_NOTENOUGHSPACE;//Not enough space for this file. 
		}
		boolean acquired = memFile.compareAndSwapInt(RESERVED_CURSORADDRESS, offset, nextOffset);
		if(acquired) {
			pos = offset;
			offset = nextOffset;
			memFile.putByte(pos, MData.VERSION_UPDATING);
			memFile.putInt(pos + MData.OFFSET_VERSION, dataLength);
			
//			memFile.putIntVolatile(RESERVED_CURSORADDRESS, offset);
			return pos;
		} 
		return ACQUIREADD_FAILED;
	}
	
	public void read(int pos, T mdata) throws IOException {
		check();
		int start = MDATAFILE_RESERVED;
		for(int i = 0; i < pos; i++) {
			int length = MData.readDataLength(memFile, start);
			start += length;
		}
		mdata.resurrect(memFile, start);
	}
	
	public void readAll(int limit, T mdata) throws IOException {
		check();
		int start = MDATAFILE_RESERVED;
		for(int i = 0; i < limit; i++) {
			mdata.resurrect(memFile, start);

			int length = MData.readDataLength(memFile, start);
			start += length;
		}
		
	}
}
