package com.main;


public class LoginTestMain {

    /**
     * 日志记录器
     */

    private static final String defaultPort = "10051";
    private static final String MAX_THREADS = "1024";
    private static final String WAR_PATH = "./workspace-chat/Login/war";

    public static void main(String[] args) {
//		chat.main.Main.main(new String[]{"-t", MAX_THREADS, "-p", defaultPort});
        chat.main.Main.main(new String[]{"-t", MAX_THREADS, "-p", defaultPort, "-w", WAR_PATH});
    }
}

