package script.memodb;

public class MemoTableFactory {
	private static MemoTableFactory instance = new MemoTableFactory();
	
	public static MemoTableFactory getInstance() {
		return instance;
	}
	
}
