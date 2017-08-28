package com.main;


public class SDockerTestMain {

    /**
     * 日志记录器
     */

    private static final String defaultPort = "10055";
//    private static final String defaultPort = "10002";
    private static final String MAX_THREADS = "1024";
    private static final String WAR_PATH = "./SDocker/war";

    public static void main(String[] args) {
//		docker.main.Main.main(new String[]{"-t", MAX_THREADS, "-p", defaultPort});
        chat.main.Main.main(new String[]{"-t", MAX_THREADS, "-p", defaultPort, "-w", WAR_PATH});
    }
}

