package com.docker.analytics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AnalyticsLogger {
    private static Logger analytics = LoggerFactory.getLogger("analytics");

    public static void log() {
        analytics.info("hello world " + System.currentTimeMillis());
    }
}
