package script.memodb;

import java.util.List;


public abstract class MemoTable {
	private String name;

	public static interface MemoCursor {
		public boolean hasNext();
		public MemoData next();
	}
	
	public static class MemoIndexer {
		private List<String> fields;
		public static final int ORDER_ASC = 1;
		public static final int ORDER_DESC = 2;
		
		private int order = ORDER_ASC;

		public List<String> getFields() {
			return fields;
		}

		public void setFields(List<String> fields) {
			this.fields = fields;
		}

		public int getOrder() {
			return order;
		}

		public void setOrder(int order) {
			this.order = order;
		}
		
		public Querys.Builder createBuilder() {
			return new Querys.Builder(this);
		}
	}
	
	public static class Querys {
		public static final int OPERATOR_EQUAL = 1;
		public static final int OPERATOR_NOTEQUAL = 2;
		public static final int OPERATOR_GREATERTHAN = 3;
		public static final int OPERATOR_LESSTHAN = 4;
		
		private MemoIndexer indexer;
//		private HashMap<String, >
		
		private static class Builder {
			private Querys query;
			private Builder(MemoIndexer indexer) {
				query.indexer = indexer;
			}
			private Builder() {
				query = new Querys();
			}
			public Builder field(String field) {
				return this;
			}
			public Builder equal(Object value) {
				return this;
			}
			public Builder notEqual(Object value) {
				return this;
			}
			public Builder greaterThan(Object value) {
				return this;
			}
			public Builder lessThan(Object value) {
				return this;
			}
			
			public Querys build() {
				return new Querys();
			}
		}
		
	}
	
	public abstract void addMemoData(MemoData data);
	public abstract MemoCursor find(MemoIndexer byIndexer);
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
