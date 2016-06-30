package chat.utils;

import java.util.TimerTask;

public abstract class TimerTaskEx extends TimerTask {

	@Override
	public final void run() {
		try {
			execute();
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public abstract void execute();
}
