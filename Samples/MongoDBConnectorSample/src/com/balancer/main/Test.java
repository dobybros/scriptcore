//package com.balancer.main;
//
//import groovy.lang.Binding;
//import groovy.lang.GroovyClassLoader;
//import groovy.util.GroovyScriptEngine;
//import groovy.util.ResourceException;
//import groovy.util.ScriptException;
//
//import java.io.IOException;
//
//import org.codehaus.groovy.control.CompilerConfiguration;
//
//public class Test {
//	public static final String A = "asdf";
//	public static void main(String[] args) throws IOException,
//			ResourceException, ScriptException {
//		String[] roots = new String[] { "/Users/aplombchen/Dev/github/scriptcore/Samples/MongoDBConnectorSample/gggg/" };
//		 CompilerConfiguration compilerConfiguration = new CompilerConfiguration();
//		    compilerConfiguration.setScriptBaseClass("test");
//		    GroovyClassLoader scriptClassLoader = new GroovyClassLoader(Test.class.getClassLoader(), compilerConfiguration);
//		GroovyScriptEngine gse = new GroovyScriptEngine(roots, scriptClassLoader);
//		Binding binding = new Binding();
//		binding.setVariable("input", "world");
//		while(true) {
//			gse.run("test.groovy", binding);
//			try {
//				Thread.sleep(1000L);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}
////		System.out.println(binding.getVariable("output"));
//	}
//}