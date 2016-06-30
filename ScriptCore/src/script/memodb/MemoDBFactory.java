package script.memodb;

public class MemoDBFactory {
	private static MemoDBFactory instance = new MemoDBFactory();
	
	public static MemoDBFactory getInstance() {
		return instance;
	}
	
}
