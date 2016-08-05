package script.memodb.data;

/**
 * 
 * 
 * @author aplombchen
 *
 * @param <T>
 */
public class Index<T> extends MData{
	
	public static final byte VALUETYPE_INT = 1;
	public static final byte VALUETYPE_DOUBLE = 2;
	public static final byte VALUETYPE_FLOAT = 3;
	public static final byte VALUETYPE_STRING = 4;
	public static final byte VALUETYPE_LONG = 5;
	public static final byte VALUETYPE_SHORT = 6;
	public static final byte VALUETYPE_BYTE = 7;
	
	/**
	 * Type of index value. 
	 */
	public byte valueType;
	/**
	 * Id of data this index stand for.
	 */
	public String id;
	/**
	 * Index value
	 */
	public T value;
	@Override
	protected int length() {
		// TODO Auto-generated method stub
		return 0;
	}
}
