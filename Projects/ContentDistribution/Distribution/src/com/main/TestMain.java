package com.main;


public class TestMain {
	
	private static final String defaultPort = "6061";
    private static final String MAX_THREADS = "1024";
	
	public static void main(String[] args) {
		chat.main.Main.main(new String[]{"-t", MAX_THREADS, "-p", defaultPort});
	}
}

