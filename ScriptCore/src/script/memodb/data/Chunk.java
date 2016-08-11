package script.memodb.data;

import java.io.IOException;
import java.io.InputStream;

public class Chunk extends MData{
	/**
	 * Chunk num of this chunk
	 */
	private int chunkNum;
	private static final int OFFSET_CHUNKNUM = 4;
	/**
	 * Each chunk is no more than 64m
	 */
	private int chunkLength;
	private static final int OFFSET_CHUNKLENGTH = 4;
	/**
	 * if data is splitted into chunks, this field specified the next chunk number
	 * if no next chunk, this field will be -1
	 */
	private int nextChunkNum;
	private static final int OFFSET_NEXTCHUNKNUM = 4;
	/**
	 * address of next chunk
	 * if no next chunk, this field will be -1
	 */
	private int nextChunkOffset;
	private static final int OFFSET_NEXTCHUNKOFFSET = 4;
	
	private static final int OFFSET_LENGTH = 
			OFFSET_CHUNKNUM + 
			OFFSET_CHUNKLENGTH + 
			OFFSET_NEXTCHUNKNUM + 
			OFFSET_NEXTCHUNKOFFSET;
	
	/**
	 * Data in byte array
	 * This is alternative with dataInputStream 
	 */
	private byte[] dataBytes;
	/**
	 * Data in InputStream
	 * This is alternative with dataBytes
	 */
	private InputStream dataInputStream;
	@Override
	public void resurrect(MemoryMappedFile memoFile, int offset) throws IOException {
		super.resurrect(memoFile, offset);
		int offsetInc = offset + OFFSET_MDATA;
		
		if(isCompletedData()) {
			chunkNum = memoFile.getInt(offsetInc);
			offsetInc += OFFSET_CHUNKNUM;
			
			nextChunkNum = memoFile.getInt(offsetInc);
			offsetInc += OFFSET_NEXTCHUNKNUM;
			
			nextChunkOffset = memoFile.getInt(offsetInc);
			offsetInc += OFFSET_NEXTCHUNKOFFSET;
			
			chunkLength = memoFile.getInt(offsetInc);
			offsetInc += OFFSET_CHUNKLENGTH;
			
			dataBytes = new byte[chunkLength];
			memoFile.getBytes(offsetInc, dataBytes, 0, chunkLength);
		}
	}
	
	@Override
	public void persistent(MemoryMappedFile memoFile, int offset) throws IOException {
		super.persistent(memoFile, offset);
		int offsetInc = offset + OFFSET_MDATA;
		try {
			memoFile.putInt(offsetInc, chunkNum);
			offsetInc += OFFSET_CHUNKNUM;
			
			memoFile.putInt(offsetInc, nextChunkNum);
			offsetInc += OFFSET_NEXTCHUNKNUM;
			
			memoFile.putInt(offsetInc, nextChunkOffset);
			offsetInc += OFFSET_NEXTCHUNKOFFSET;
			
			if(dataBytes != null) {
				memoFile.putInt(offsetInc, chunkLength);
				offsetInc += OFFSET_CHUNKLENGTH;
				
				memoFile.setBytes(offsetInc, dataBytes, 0, chunkLength);
			} else if(dataInputStream != null){
				memoFile.putInt(offsetInc, chunkLength);
				offsetInc += OFFSET_CHUNKLENGTH;
				
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
						memoFile.setBytes(offsetInc, buffer, 0, read);
						totalRead += read;
						offsetInc += read;
						if(totalRead == chunkLength)
							break;
					}
				} while((read = dataInputStream.read(buffer)) != -1);
			} else {
				memoFile.putInt(offsetInc, 0);
				offsetInc += OFFSET_CHUNKLENGTH;
			}
			persistentDone(memoFile, offset);
		} catch(Throwable t) {
			t.printStackTrace();
//			persistentCorrupted(memoFile, address);
			if(t instanceof IOException) {
				throw t;
			} else {
				throw new IOException(t.getMessage(), t);
			}
		}
	}

	@Override
	protected int length() {
		return OFFSET_LENGTH + chunkLength;
	}
	
	public int getChunkNum() {
		return chunkNum;
	}

	public void setChunkNum(int chunkNum) {
		this.chunkNum = chunkNum;
	}

	public int getNextChunkNum() {
		return nextChunkNum;
	}

	public void setNextChunkNum(int nextChunkNum) {
		this.nextChunkNum = nextChunkNum;
	}

	public int getNextChunkOffset() {
		return nextChunkOffset;
	}

	public void setNextChunkOffset(int nextChunkOffset) {
		this.nextChunkOffset = nextChunkOffset;
	}

	public byte[] getDataBytes() {
		return dataBytes;
	}

	public void setDataBytes(byte[] dataBytes) {
		this.dataBytes = dataBytes;
		if(this.dataBytes != null)
			this.chunkLength = dataBytes.length;
	}

	public InputStream getDataInputStream() {
		return dataInputStream;
	}

	public void setDataInputStream(InputStream dataInputStream, int chunkLength) {
		this.dataInputStream = dataInputStream;
		this.chunkLength = chunkLength;
	}
}
