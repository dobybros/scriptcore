package script.memodb.data;

import java.io.InputStream;

public class Chunk extends Blob{
	/**
	 * Chunk num of this chunk
	 */
	public int chunkNum;
	/**
	 * Each chunk is no more than 64m
	 */
	public int chunkLength;
	/**
	 * if data is splitted into chunks, this field specified the next chunk number
	 * if no next chunk, this field will be -1
	 */
	public int nextChunkNum;
	/**
	 * address of next chunk
	 * if no next chunk, this field will be -1
	 */
	public int nextChunkAddress;
	
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
}
