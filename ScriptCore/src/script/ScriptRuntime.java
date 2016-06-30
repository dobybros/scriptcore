package script;

import chat.errors.CoreException;

public abstract class ScriptRuntime {
	protected String path;
	
	public abstract void init() throws CoreException;
	
	public abstract void redeploy() throws CoreException;
	
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
}
