package connectors.mongodb.codec;

public abstract class DataObject {
	private String id;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
