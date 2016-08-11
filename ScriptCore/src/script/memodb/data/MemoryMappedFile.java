/* 
* This class was inspired from an entry in Bryce Nyeggen's blog 
*/
package script.memodb.data;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.channels.FileChannel;

import sun.misc.Unsafe;
import sun.nio.ch.FileChannelImpl;
 
/**
 * Class for direct access to a memory mapped file. 
 *
 */
@SuppressWarnings("restriction")
public class MemoryMappedFile {
 
	private static final Unsafe unsafe;
	private static final Method mmap;
	private static final Method unmmap;
	private static final int BYTE_ARRAY_OFFSET;
 
	private long addr, size;
	private final String loc;
 
	static {
		try {
			Field singleoneInstanceField = Unsafe.class.getDeclaredField("theUnsafe");
			singleoneInstanceField.setAccessible(true);
			unsafe = (Unsafe) singleoneInstanceField.get(null);
			mmap = getMethod(FileChannelImpl.class, "map0", int.class, long.class, long.class);
			unmmap = getMethod(FileChannelImpl.class, "unmap0", long.class, long.class);
			BYTE_ARRAY_OFFSET = unsafe.arrayBaseOffset(byte[].class);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static Method getMethod(Class<?> cls, String name, Class<?>... params) throws Exception {
		Method m = cls.getDeclaredMethod(name, params);
		m.setAccessible(true);
		return m;
	}
 
	private static long roundTo4096(long i) {
		return (i + 0xfffL) & ~0xfffL;
	}
 
	private void mapAndSetOffset() throws Exception {
		final RandomAccessFile backingFile = new RandomAccessFile(this.loc, "rw");
		backingFile.setLength(this.size);
		final FileChannel ch = backingFile.getChannel();
		this.addr = (long) mmap.invoke(ch, 1, 0L, this.size);
		ch.close();
		backingFile.close();
	}
 
	/**
	 * Constructs a new memory mapped file.
	 * @param loc the file name
	 * @param len the file length
	 * @throws Exception in case there was an error creating the memory mapped file
	 */
	protected MemoryMappedFile(final String loc, long len) throws Exception {
		this.loc = loc;
		this.size = roundTo4096(len);
		mapAndSetOffset();
	}

	protected void unmap() throws Exception {
		unmmap.invoke(null, addr, this.size);
	}
	
	private void check(long pos, int typeSize) throws IOException {
		if(pos < 0 || pos + typeSize > this.size) {
			throw new IOException("pos " + pos + " typeSize " + typeSize + " is illegal, " + this.size);
		}
	}
	
	/**
	 * Reads a byte from the specified position.
	 * @param pos the position in the memory mapped file
	 * @return the value read
	 */
	public byte getByte(long pos) throws IOException {
		check(pos, 1);
		return unsafe.getByte(pos + addr);
	}

	/**
	 * Reads a byte (volatile) from the specified position.
	 * @param pos the position in the memory mapped file
	 * @return the value read
	 */
	protected byte getByteVolatile(long pos) throws IOException {
		check(pos, 1);
		return unsafe.getByteVolatile(null, pos + addr);
	}
 
	/**
	 * Reads a short from the specified position.
	 * @param pos the position in the memory mapped file
	 * @return the value read
	 */
	public short getShort(long pos) throws IOException {
		check(pos, 4);
		return unsafe.getShort(pos + addr);
	}

	/**
	 * Reads a short (volatile) from the specified position.
	 * @param pos position in the memory mapped file
	 * @return the value read
	 */
	protected short getShortVolatile(long pos) throws IOException {
		check(pos, 4);
		return unsafe.getShortVolatile(null, pos + addr);
	}
	
	/**
	 * Reads an int from the specified position.
	 * @param pos the position in the memory mapped file
	 * @return the value read
	 */
	public int getInt(long pos) throws IOException {
		check(pos, 4);
		return unsafe.getInt(pos + addr);
	}

	/**
	 * Reads an int (volatile) from the specified position.
	 * @param pos position in the memory mapped file
	 * @return the value read
	 */
	protected int getIntVolatile(long pos) throws IOException {
		check(pos, 4);
		return unsafe.getIntVolatile(null, pos + addr);
	}

	/**
	 * Reads a long from the specified position.
	 * @param pos position in the memory mapped file
	 * @return the value read
	 */
	public long getLong(long pos) throws IOException {
		check(pos, 8);
		return unsafe.getLong(pos + addr);
	}
	
	/**
	 * Reads a long (volatile) from the specified position.
	 * @param pos position in the memory mapped file
	 * @return the value read
	 */
	protected long getLongVolatile(long pos) throws IOException {
		check(pos, 8);
		return unsafe.getLongVolatile(null, pos + addr);
	}
	
	/**
	 * Writes a byte to the specified position.
	 * @param pos the position in the memory mapped file
	 * @param val the value to write
	 */
	public void putByte(long pos, byte val) throws IOException {
		check(pos, 1);
		unsafe.putByte(pos + addr, val);
	}
	
	/**
	 * Writes a byte (volatile) to the specified position.
	 * @param pos the position in the memory mapped file
	 * @param val the value to write
	 */
	protected void putByteVolatile(long pos, byte val) throws IOException {
		check(pos, 1);
		unsafe.putByteVolatile(null, pos + addr, val);
	}

	/**
	 * Writes an short to the specified position.
	 * @param pos the position in the memory mapped file
	 * @param val the value to write
	 */
	public void putShort(long pos, short val) throws IOException {
		check(pos, 2);
		unsafe.putShort(pos + addr, val);
	}
	
	/**
	 * Writes an int (volatile) to the specified position.
	 * @param pos the position in the memory mapped file
	 * @param val the value to write
	 */
	protected void putShortVolatile(long pos, short val) throws IOException {
		check(pos, 2);
		unsafe.putShortVolatile(null, pos + addr, val);
	}
	
	/**
	 * Writes an int to the specified position.
	 * @param pos the position in the memory mapped file
	 * @param val the value to write
	 */
	public void putInt(long pos, int val) throws IOException {
		check(pos, 4);
		unsafe.putInt(pos + addr, val);
	}

	/**
	 * Writes an int (volatile) to the specified position.
	 * @param pos the position in the memory mapped file
	 * @param val the value to write
	 */
	protected void putIntVolatile(long pos, int val) throws IOException {
		check(pos, 4);
		unsafe.putIntVolatile(null, pos + addr, val);
	}

	/**
	 * Writes a long to the specified position.
	 * @param pos the position in the memory mapped file
	 * @param val the value to write
	 */
	public void putLong(long pos, long val) throws IOException {
		check(pos, 8);
		unsafe.putLong(pos + addr, val);
	}

	/**
	 * Writes a long (volatile) to the specified position.
	 * @param pos the position in the memory mapped file
	 * @param val the value to write
	 */
	protected void putLongVolatile(long pos, long val) throws IOException {
		check(pos, 8);
		unsafe.putLongVolatile(null, pos + addr, val);
	}
	
	/**
	 * Reads a buffer of data.
	 * @param pos the position in the memory mapped file
	 * @param data the input buffer
	 * @param offset the offset in the buffer of the first byte to read data into
	 * @param length the length of the data
	 */
	public void getBytes(long pos, byte[] data, int offset, int length) throws IOException {
		check(pos, 1);
		unsafe.copyMemory(null, pos + addr, data, BYTE_ARRAY_OFFSET + offset, length);
	}
 
	/**
	 * Writes a buffer of data.
	 * @param pos the position in the memory mapped file
	 * @param data the output buffer
	 * @param offset the offset in the buffer of the first byte to write
	 * @param length the length of the data
	 */
	public void setBytes(long pos, byte[] data, int offset, int length) throws IOException {
		check(pos, length);
		unsafe.copyMemory(data, BYTE_ARRAY_OFFSET + offset, null, pos + addr, length);
	}

	protected boolean compareAndSwapInt(long pos, int expected, int value) throws IOException {
		check(pos, 4);
		return unsafe.compareAndSwapInt(null, pos + addr, expected, value);
	}
		
	protected boolean compareAndSwapLong(long pos, long expected, long value) throws IOException {
		check(pos, 4);
		return unsafe.compareAndSwapLong(null, pos + addr, expected, value);
	}

	protected long getAndAddLong(long pos, long delta) throws IOException {
		check(pos, 8);
		return unsafe.getAndAddLong(null, pos + addr, delta);
	}
}