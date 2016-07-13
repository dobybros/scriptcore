package script.groovy.runtime;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Map;

import script.groovy.annotation.TimerTask;
import script.groovy.object.GroovyObjectEx;
import script.groovy.runtime.GroovyRuntime.MyGroovyClassLoader;
import chat.logs.LoggerEx;
import chat.utils.ConcurrentHashSet;
import chat.utils.TimerEx;
import chat.utils.TimerTaskEx;

public class GroovyTimerTaskHandler implements ClassAnnotationHandler {

	private static final String TAG = GroovyTimerTaskHandler.class.getSimpleName();
	private ConcurrentHashSet<MyTimerTask> timerTasks;
	
	@Override
	public Class<? extends Annotation> handleAnnotationClass(GroovyRuntime groovyRuntime) {
		return TimerTask.class;
	}

	@Override
	public void handleAnnotatedClasses(Map<String, Class<?>> annotatedClassMap,
			MyGroovyClassLoader classLoader) {
		if(annotatedClassMap != null) {
			ConcurrentHashSet<MyTimerTask> newTimerTasks = new ConcurrentHashSet<>();
			Collection<Class<?>> values = annotatedClassMap.values();
			for(Class<?> groovyClass : values) {
				TimerTask timerTask = groovyClass.getAnnotation(TimerTask.class);
				if(timerTask != null) {
					long period = timerTask.period();
					if(period > 10) {
						GroovyObjectEx<?> groovyObj = GroovyRuntime.getInstance().create(groovyClass);
						MyTimerTask task = new MyTimerTask(period, groovyObj);
						newTimerTasks.add(task);
					} else {
						LoggerEx.warn(TAG, "Groovy TimerTask " + groovyClass + " was ignored because of small period " + (period / 1000));
					}
				}
			}
			
			if(timerTasks != null) {
				for(TimerTaskEx timerTask : timerTasks) {
					timerTask.cancel();
				}
			}
			if(newTimerTasks != null) {
				timerTasks = newTimerTasks;
				int count = 0;
				for(MyTimerTask timerTask : timerTasks) {
					long delay = (++count) * 1000L;
					LoggerEx.info(TAG, "Redeploy scheduled Groovy timer task " + timerTask.groovyObj.getGroovyPath() + " delay " + (delay / 1000) + "s to execute. Period " + (timerTask.period / 1000));
					TimerEx.schedule(timerTask, delay, timerTask.getPeriod());
				}
			}
		}
	}

	public class MyTimerTask extends TimerTaskEx {
		private long period;
		private GroovyObjectEx<?> groovyObj;
		
		public MyTimerTask(long period, GroovyObjectEx<?> groovyObj) {
			this.period = period;
			this.groovyObj = groovyObj;
		}
		@Override
		public void execute() {
//			LoggerEx.info(TAG, "Scheduled Groovy timer task " + groovyObj.getGroovyPath() + " to execute. Period " + (period / 1000) + "s");
			try {
				groovyObj.invokeRootMethod("main");
			} catch (Throwable t) {
				t.printStackTrace();
				LoggerEx.error(TAG, "Execute execute main for " + groovyObj + " failed, " + t.getMessage());
			}
		}
		public long getPeriod() {
			return period;
		}
	}
}
