package script.memodb.data;

public class Index extends Blob{
	public static final byte VALUETYPE_INT = 1;
	public static final byte VALUETYPE_DOUBLE = 2;
	public static final byte VALUETYPE_FLOAT = 3;
	public static final byte VALUETYPE_STRING = 4;
	public static final byte VALUETYPE_LONG = 5;
	public static final byte VALUETYPE_SHORT = 6;
	public static final byte VALUETYPE_BYTE = 7;
	
	public byte valueType;
	public String id;
	public Object value;
}
