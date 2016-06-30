package script.memodb;

public abstract class MemoDB {
	private String name;

	public abstract MemoTable getMemoTable(String name);

	public abstract void deleteMemoTable(String name);

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
