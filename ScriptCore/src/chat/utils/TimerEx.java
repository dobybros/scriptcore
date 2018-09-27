package chat.utils;

import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import chat.logs.LoggerEx;

public class TimerEx {
	private static final String TAG = TimerEx.class.getSimpleName();
	private static ScheduledExecutorService scheduledExecutorService;
	static {
		scheduledExecutorService = Executors.newScheduledThreadPool(3);
	}
	
	public static void schedule(TimerTask task, long delay) {
		try {
            ScheduledFuture future = scheduledExecutorService.schedule(task, delay, TimeUnit.MILLISECONDS);
            if(task instanceof TimerTaskEx) {
                ((TimerTaskEx)task).setFuture(future);
            } else {
                LoggerEx.warn(TAG, "There still a TimerTask " + task + " delay " + delay + " not using TimerTaskEx, the cancel method will not be available");
            }
		} catch(Throwable e) {
		    e.printStackTrace();
			LoggerEx.error(TAG, "Schedule TimerTask " + task + " failed, " + e.getMessage());
		}
	}
	public static void schedule(TimerTask task, long delay, long period) {
		try {
            ScheduledFuture future = scheduledExecutorService.scheduleAtFixedRate(task, delay, period, TimeUnit.MILLISECONDS);
            if(task instanceof TimerTaskEx) {
                ((TimerTaskEx)task).setFuture(future);
            } else {
                LoggerEx.warn(TAG, "There still a  periodic TimerTask " + task + " delay " + delay + " not using TimerTaskEx, the cancel method will not be available");
            }
		} catch(Throwable e) {
            e.printStackTrace();
			LoggerEx.error(TAG, "Schedule Period TimerTask " + task + " failed, " + e.getMessage());
		}
	}
	
	public static void cancel() {
		LoggerEx.warn(TAG, "Why you want to cancel a Timer? Please email to aplomb@aculearn.com.cn");
	}


	public static void main(String args[]) {
		System.out.println("start");
		TimerEx.schedule(new TimerTaskEx() {
			@Override
			public void execute() {
				System.out.println("done");
			}
		}, -1234);
		TimerEx.schedule(new TimerTaskEx() {
			@Override
			public void execute() {
				System.out.println("done1");
			}
		}, 2000);
		TimerEx.schedule(new TimerTaskEx() {
			@Override
			public void execute() {
				System.out.println("123");
			}
		}, 2000, 2000);
		TimerEx.schedule(new TimerTaskEx() {
			@Override
			public void execute() {
				System.out.println("negetive");
			}
		}, -123);
	}

}