package test;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;

import chat.errors.CoreException;
import script.groovy.runtime.GroovyBeanFactory;
import script.groovy.runtime.GroovyObjectFactory;
import script.groovy.runtime.GroovyRuntime;

public class Main {

	public static void main(String[] args) throws CoreException {
		String path = "/Users/aplombchen/Dev/taineng/Server/workspace-ggts/MongoDBConnector/groovy/";
		
		Collection<File> files = FileUtils.listFiles(new File(path),
				FileFilterUtils.suffixFileFilter(".groovy"),
				FileFilterUtils.directoryFileFilter());
		
//		for(File file : files) {
//			String key = file.getAbsolutePath().substring(path.length());
//			int pos = key.lastIndexOf(".");
//			if(pos >= 0) {
//				key = key.substring(0, pos);
//			}
//			key = key.replace("/", ".");
//		}

		GroovyRuntime groovyRuntime = new GroovyRuntime();
		groovyRuntime.setPath(path);

		GroovyBeanFactory beanFactory = new GroovyBeanFactory();
		beanFactory.setGroovyRuntime(groovyRuntime);
		groovyRuntime.addClassAnnotationHandler(beanFactory);
		
		groovyRuntime.init();
		
		GroovyObjectFactory factory = new GroovyObjectFactory();
		factory.setGroovyRuntime(groovyRuntime);
		
//		boolean bool = true;
//		while(bool) {
//			groovyRuntime.redeploy();
//			GroovyObjectEx<Callable> c1 = factory.getObject(a.B.class);
////		String hello = c1.getObject().hello();
//			c1.getObject().call();
//			System.gc();
//			System.out.println("used " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 + "K");
//		}
		
		while(true) {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));  
			String str = null;  
			try {  
				str = br.readLine();  
				if(str != null && str.equals("a")) {
					groovyRuntime.redeploy();
				}
				
				/*
				new Thread(new Runnable() {
					@Override
					public void run() {
						GroovyObjectEx<Callable> c1 = factory.getObject(a.B.class);
//						String hello = c1.getObject().hello();
						try {
							c1.getObject().call();
						} catch (Exception e) {
							e.printStackTrace();
						}
						System.gc();
						System.out.println("used " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 + "K");
						
					}
				}).start();*/
				
//				GroovyObjectEx<Callable> c1 = factory.getObject(c.C.class);
//				c1.getObject().call();
//				
//				GroovyObjectEx<Callable> d1 = factory.getObject(c.D.class);
//				d1.getObject().call();
//				c.C c = new c.C();
//				c.hello();
			} catch (Throwable e) {  
				e.printStackTrace();  
			}  
		}
        
	}

}
