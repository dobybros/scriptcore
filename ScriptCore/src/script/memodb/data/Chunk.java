package script.memodb.data;

import java.io.IOException;
import java.io.InputStream;

public class Chunk extends MData{
	/**
	 * Chunk num of this chunk
	 */
	public int chunkNum;
	private static final int OFFSET_CHUNKNUM = 4;
	/**
	 * Each chunk is no more than 64m
	 */
	public int chunkLength;
	private static final int OFFSET_CHUNKLENGTH = 4;
	/**
	 * if data is splitted into chunks, this field specified the next chunk number
	 * if no next chunk, this field will be -1
	 */
	public int nextChunkNum;
	private static final int OFFSET_NEXTCHUNKNUM = 4;
	/**
	 * address of next chunk
	 * if no next chunk, this field will be -1
	 */
	public long nextChunkAddress;
	private static final int OFFSET_NEXTCHUNKADDRESS = 8;
	
	private static final int OFFSET_LENGTH = 
			OFFSET_CHUNKNUM + 
			OFFSET_CHUNKLENGTH + 
			OFFSET_NEXTCHUNKNUM + 
			OFFSET_NEXTCHUNKADDRESS;
	
	/**
	 * Data in byte array
	 * This is alternative with dataInputStream 
	 */
	public byte[] dataBytes;
	/**
	 * Data in InputStream
	 * This is alternative with dataBytes
	 */
	public InputStream dataInputStream;
	@Override
	public void resurrect(MemoryMappedFile memoFile, long address) throws IOException {
		super.resurrect(memoFile, address);
		int offset = OFFSET_MDATA;
		
		memoFile.putInt(address + offset, chunkNum);
		offset += OFFSET_CHUNKNUM;
		
		memoFile.putInt(address + offset, nextChunkNum);
		offset += OFFSET_NEXTCHUNKNUM;
		
		memoFile.putLong(address + offset, nextChunkAddress);
		offset += OFFSET_NEXTCHUNKADDRESS;
		
		if(dataBytes != null) {
			memoFile.putInt(address + offset, chunkLength);
			offset += OFFSET_CHUNKLENGTH;
			
			memoFile.setBytes(address + offset, dataBytes, 0, chunkLength);
		} else if(dataInputStream != null){
			memoFile.putInt(address + offset, chunkLength);
			offset += OFFSET_CHUNKLENGTH;
			
			final int BUFSIZE = 8096;
			byte[] buffer;
			
			int read = 0;
			int totalRead = 0;
			do {
				int len = chunkLength - totalRead;
				if(len > BUFSIZE) {
					buffer = new byte[BUFSIZE];
				} else {
					buffer = new byte[len];
				}
				if(read > 0) {
					memoFile.setBytes(address + offset, buffer, 0, read);
					totalRead += read;
					offset += read;
					if(totalRead == chunkLength)
						break;
				}
			} while((read = dataInputStream.read(buffer)) != -1);
		} else {
			memoFile.putInt(address + offset, 0);
			offset += OFFSET_CHUNKLENGTH;
		}
	}
	
	@Override
	public void persistent(MemoryMappedFile memoFile, long address) throws IOException {
		super.persistent(memoFile, address);
		int offset = OFFSET_MDATA;
		
		chunkNum = memoFile.getInt(offset);
		offset += OFFSET_CHUNKNUM;
		
		chunkLength = memoFile.getInt(offset);
		offset += OFFSET_CHUNKLENGTH;
		
		nextChunkNum = memoFile.getInt(offset);
		offset += OFFSET_NEXTCHUNKNUM;
		
		nextChunkAddress = memoFile.getLong(offset);
		offset += OFFSET_NEXTCHUNKADDRESS;
	}

	@Override
	protected int length() {
		return OFFSET_LENGTH;
	}
}
