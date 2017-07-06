package com.balancer.main;


public class TestMain {
	
	/**
	 * 日志记录器
	 */
	
	private static final String defaultPort = "6061";
    private static final String MAX_THREADS = "1024";
	private static final String WAR_PATH = "./Samples/MongoDBConnectorSample/war";
	
	public static void main(String[] args) {
		chat.main.Main.main(new String[]{"-t", MAX_THREADS, "-p", defaultPort, "-w", WAR_PATH});
	}
}

