package chat.utils;

import chat.logs.LoggerEx;

import java.util.TimerTask;

public abstract class TimerTaskEx extends TimerTask {

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
}
