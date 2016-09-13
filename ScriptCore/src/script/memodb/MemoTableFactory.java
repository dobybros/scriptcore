package script.memodb;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import script.memodb.data.MemoTableImpl;

public class MemoTableFactory {
	private static MemoTableFactory instance = new MemoTableFactory();
	private Map<String, MemoTable> tableMap = new ConcurrentHashMap<>();
	private String basePath;
	public static MemoTableFactory getInstance() {
		return instance;
	}
	
	public MemoTable getMemoTable(String name) {
		MemoTable table = tableMap.get(name);
		if(table != null) {
			return table;
		}
		table = new MemoTableImpl();
		table.setName(name);
		table.setBasePath(basePath);
		MemoTable t = tableMap.putIfAbsent(name, table);
		if(t != null) {
			table = t;
		} else {
			table.open();
		}
		return table;
	}

	public String getBasePath() {
		return basePath;
	}

	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}
}
