package chat.logs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import chat.utils.ChatUtils;

public class LoggerEx {
	private static Logger logger = LoggerFactory.getLogger("");
	private LoggerEx() {}
	
	public static String getClassTag(Class<?> clazz) {
		return clazz.getSimpleName();
	}

	public static void debug(String tag, String msg) {
	    logger.info(getLogMsg(tag, msg));
	}

//    public static void debug(String msg) {
//	    logger.info(msg);
//	}

	public static void info(String tag, String msg) {
	    logger.info(getLogMsg(tag, msg));
	}
	
//	public static void info(String msg) {
//	    logger.info(msg);
//	}
	
	public static void warn(String tag, String msg) {
	    logger.warn(getLogMsg(tag, msg));
	}
	
//	public static void warn(String msg) {
//	    logger.warn(msg);
//	}
	
	public static void error(String tag, String msg) {
	    logger.error(getLogMsg(tag, msg));
	}
	
//	public static void error(String msg) {
//	    logger.error(msg);
//	}

	public static void fatal(String tag, String msg) {
	    logger.error("FATAL: " + getLogMsg(tag, msg));
	}
	
	private static String getLogMsg(String tag, String msg) {
        return new StringBuilder("[").append(ChatUtils.dateString()).append("|").append(tag).append("] ").append(msg).toString();
    }
	
}
