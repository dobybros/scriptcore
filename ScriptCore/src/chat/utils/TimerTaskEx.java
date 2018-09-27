package chat.utils;

import chat.logs.LoggerEx;

import java.util.TimerTask;
import java.util.concurrent.Future;

public abstract class TimerTaskEx extends TimerTask {
	private Future future;

	public void setFuture(Future future) {
		this.future = future;
	}
	@Override
	public final void run() {
		try {
			execute();
		} catch (Throwable t) {
			t.printStackTrace();
			LoggerEx.error("TimerTaskEx", "execute failed, " + t.getMessage());
		}
	}

	public abstract void execute();

	@Override
	public boolean cancel() {
		if(future != null) {
			return future.cancel(false);
		}
		return false;
	}
}
