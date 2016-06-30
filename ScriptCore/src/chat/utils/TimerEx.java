package chat.utils;

import java.util.Timer;
import java.util.TimerTask;

import chat.logs.LoggerEx;

public class TimerEx {
	private static final String TAG = TimerEx.class.getSimpleName();
	private static Timer timer = new Timer();
	
	public static void schedule(TimerTask task, long delay) {
		try {
			timer.schedule(task, delay);
		} catch(Exception e) {
			LoggerEx.error(TAG, "Schedule TimerTask " + task + " failed, " + e.getMessage());
		}
	}
	public static void schedule(TimerTask task, long delay, long period) {
		try {
			timer.schedule(task, delay, period);
		} catch(Exception e) {
			LoggerEx.error(TAG, "Schedule TimerTask " + task + " failed, " + e.getMessage());
		}
	}
	
	public static void cancel() {
		timer.cancel();
	}
	
}