package script.groovy.runtime;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import groovy.lang.GroovySystem;
import groovy.lang.MetaClassRegistry;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.tools.GroovyClass;

import script.ScriptRuntime;
import script.groovy.object.GroovyObjectEx;
import chat.errors.ChatErrorCodes;
import chat.errors.CoreException;
import chat.logs.LoggerEx;

public class GroovyRuntime extends ScriptRuntime{
	private static final String TAG = GroovyRuntime.class.getSimpleName();
	private MyGroovyClassLoader classLoader;
	private ConcurrentHashMap<Thread, MyGroovyClassLoader> threadClassLoaderMap = new ConcurrentHashMap<>();
	private AtomicLong latestVersion = new AtomicLong(0);
	private ClassLoader parentClassLoader;
	private List<ClassAnnotationHandler> annotationHandlers;
	private static GroovyRuntime instance;
	private Class<?> groovyObjectExProxyClass;

	public static GroovyRuntime getInstance() {
		return instance;
	}
/*
	public static void main(String[] args) throws Exception {
		String path = "/Users/aplombchen/Dev/github/scriptcore/ScriptCore/test/";
		
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
				}).start();
				
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
        
//		Runtime runtime = Runtime.getRuntime();
//		long time;
//		for (int i = 0; i < 10000; i++) {
//			time = System.currentTimeMillis();
//			groovyRuntime.redeploy();
//			time = System.currentTimeMillis() - time;
//			// runtime.gc();
//			System.out.println("total " + (runtime.totalMemory() / 1024)
//					+ "K - free " + (runtime.freeMemory() / 1024) + "K = "
//					+ ((runtime.totalMemory() - runtime.freeMemory()) / 1024)
//					+ "K takes " + time);
//		}
	}
*/
	public MyGroovyClassLoader registerClassLoaderOnThread() {
		MyGroovyClassLoader loader = classLoader;
		if (loader == null)
			return null;
//		MyGroovyClassLoader old = threadClassLoaderMap.putIfAbsent(
//				Thread.currentThread(), loader);
//		if (old == null)
//			return loader;
//		else
//			return old;
		return loader;
	}

	public void unregisterClassLoaderOnThread() {
		Thread thread = Thread.currentThread();
//		MyGroovyClassLoader removed = threadClassLoaderMap.remove(thread);
	}

	public class ClassHolder {
		private Class<?> parsedClass;
		private GroovyObject cachedObject;

		public Class<?> getParsedClass() {
			return parsedClass;
		}

		public GroovyObject getCachedObject() {
			return cachedObject;
		}

		public void setCachedObject(GroovyObject cachedObject) {
			this.cachedObject = cachedObject;
		}
	}

	/*
	public class MyGroovyClassLoader extends GroovyClassLoader {
		private long version;
		private HashMap<String, ClassHolder> classCache;
		private HashSet<String> pendingGroovyClasses;
		
		public MyGroovyClassLoader(ClassLoader parentClassLoader,
				CompilerConfiguration cc) {
			super(parentClassLoader, cc);
//			final GroovyResourceLoader defaultResourceLoader = super.getResourceLoader();
//			LoggerEx.info(TAG, "defaultResourceLoader " + defaultResourceLoader);
//			super.setResourceLoader(new GroovyResourceLoader() {
//				@Override
//				public URL loadGroovySource(String path) throws MalformedURLException {
//					LoggerEx.info(TAG, "load groovy source " + path);
//					if(defaultResourceLoader != null) {
//						return defaultResourceLoader.loadGroovySource(path);
//					}
//					return null;
//				}
//			});
			classCache = new HashMap<>();
		}

		public Class<?> parseGroovyClass(String key, File classFile)
				throws CoreException {
			try {
				return parseClass(classFile);
			} catch (CompilationFailedException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		@Override
		public Class recompile(URL source, String name, Class oldClass) {
			LoggerEx.info(TAG, "Recompile " + source + " name " + name + " oldClass " + oldClass);
			try {
				return super.recompile(source, name, oldClass);
			} catch (CompilationFailedException | IOException e) {
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected boolean isSourceNewer(URL source, Class cls) {
			LoggerEx.info(TAG, "isSourceNewer " + source + " cls " + cls);
			return true;
		}

		public ClassHolder getClass(String classPath) {
			return classCache.get(classPath);
		}

		public long getVersion() {
			return version;
		}

		public String toString() {
			return MyGroovyClassLoader.class.getSimpleName() + "#" + version;
		}
	}
	*/
	
	public class MyGroovyClassLoader extends GroovyClassLoader {
		private long version;
		private HashMap<String, ClassHolder> myClassCache;
		private HashSet<String> pendingGroovyClasses;
		private HashSet<String> parsingGroovyClasses;

		public MyGroovyClassLoader(ClassLoader parentClassLoader,
				CompilerConfiguration cc) {
			super(parentClassLoader, cc);
//			final GroovyResourceLoader defaultResourceLoader = super.getResourceLoader();
//			LoggerEx.info(TAG, "defaultResourceLoader " + defaultResourceLoader);
//			super.setResourceLoader(new GroovyResourceLoader() {
//				@Override
//				public URL loadGroovySource(String path) throws MalformedURLException {
//					LoggerEx.info(TAG, "load groovy source " + path);
//					if(defaultResourceLoader != null) {
//						return defaultResourceLoader.loadGroovySource(path);
//					}
//					return null;
//				}
//			});
			myClassCache = new HashMap<>();
		}

		public Class<?> parseGroovyClass(String key, File classFile)
				throws CoreException {
			ClassHolder holder =  myClassCache.get(key);
			if(holder != null && holder.getParsedClass() != null) {
				LoggerEx.info(TAG, "Load groovy class " + key
						+ " from cache");
				return holder.getParsedClass();
			}
			try {
				Class<?> parsedClass = parseClass(classFile);
				if (parsedClass != null) {
					holder = new ClassHolder();
					holder.parsedClass = parsedClass;
					myClassCache.put(key, holder);
				}
				LoggerEx.info(TAG, "Parse groovy class " + key
						+ " successfully");
				return parsedClass;
			} catch (Throwable e) {
				e.printStackTrace();
				throw new CoreException(
						ChatErrorCodes.ERROR_GROOVY_PARSECLASS_FAILED,
						"Parse class " + classFile + " failed, "
								+ e.getMessage());
			}
			
			/*ClassHolder holder =  classCache.get(key);
			if(holder != null && holder.getParsedClass() != null) {
				LoggerEx.info(TAG, "Load groovy class " + key
						+ " from cache");
				return holder.getParsedClass();
			}
			try {
				
				CompilationUnit compileUnit = new CompilationUnit();
				String name = key.replace("/", ".");
				int pos = name.lastIndexOf(".");
				if(pos != -1) {
					name = name.substring(0, pos);
				} 
			    compileUnit.addSource(name, FileUtils.openInputStream(classFile));
			    compileUnit.compile(Phases.CLASS_GENERATION);
			    compileUnit.setClassLoader(this);

			    GroovyClass target = null;
			    for (Object compileClass : compileUnit.getClasses()) {
			        GroovyClass groovyClass = (GroovyClass) compileClass;
//			        try {
//			        	this.defineClass(groovyClass.getName(), groovyClass.getBytes());
//					} catch (LinkageError e) {
////						e.printStackTrace();
//					}
			        if(groovyClass.getName().equals(name)) {
			            target = groovyClass;
			        }
			    }

			    if(target == null) 
			        throw new IllegalStateException("Could not find proper class");

			    Class<?> parsedClass = this.loadClass(target.getName());
				
				if (parsedClass != null) {
					holder = new ClassHolder();
					holder.parsedClass = parsedClass;
					classCache.put(key, holder);
				}
				LoggerEx.info(TAG, "Parse groovy class " + key
						+ " successfully");
				return parsedClass;
			} catch (Throwable e) {
				e.printStackTrace();
				throw new CoreException(
						ChatErrorCodes.ERROR_GROOVY_PARSECLASS_FAILED,
						"Parse class " + classFile + " failed, "
								+ e.getMessage());
			}*/
		}
		
		@Override
		public Class loadClass(String name, boolean lookupScriptFiles,
				boolean preferClassOverScript) throws ClassNotFoundException,
				CompilationFailedException {
			// TODO Auto-generated method stub
//			System.out.println("name = " + name + " lookup = " + lookupScriptFiles + " prefer = " + preferClassOverScript);
			Class<?> loadedClass = null;
			if(pendingGroovyClasses.contains(name)) {
//				pendingGroovyClasses.remove(name);
				String key = name.replace(".", "/") + ".groovy";
				if(!parsingGroovyClasses.contains(name)) {
					parsingGroovyClasses.add(name);
					try {
						loadedClass = parseGroovyClass(key, new File(path + key));
						if(loadedClass != null)
							return loadedClass;
					} catch (CoreException e) {
						e.printStackTrace();
						LoggerEx.error(TAG, "parse groovy class failed while load class, " + e.getMessage());
					}
				} else {
					ClassHolder holder =  myClassCache.get(key);
					if(holder != null && holder.getParsedClass() != null) {
						LoggerEx.info(TAG, "Load groovy class " + key
								+ " from cache to avoid loop parse");
						return holder.getParsedClass();
					}
				}
			}
			
			boolean bool = parsingGroovyClasses.contains(name);
			if(bool) {
				LoggerEx.warn(TAG, "Loop parse class " + name + ", may cause this class available. PLEASE BE CAUTION! " + name);
			}
			loadedClass = super.loadClass(name, lookupScriptFiles,
					preferClassOverScript);
			return loadedClass;
			/*
			int indx = name.lastIndexOf('.');
			String substr = name;
			if (indx != -1) {
				substr = name.substring(indx + 1);
			}
			String groovyFileName = substr + ".groovy"	;
			String path = "C:\\" + groovyFileName;

			try {
				return parseClass(new File(path).toString(), groovyFileName);
			} catch (CompilationFailedException exception) {
				throw exception;
			}*/
		}
		
		public ClassHolder getClass(String classPath) {
			return myClassCache.get(classPath);
		}

		public long getVersion() {
			return version;
		}

		public String toString() {
			return MyGroovyClassLoader.class.getSimpleName() + "#" + version;
		}
	}

	public GroovyRuntime() {
//		instance = this;
	}

	public synchronized void init() throws CoreException {
		instance = this;
		redeploy();
	}

	public boolean addClassAnnotationHandler(ClassAnnotationHandler handler) {
		if (annotationHandlers == null)
			annotationHandlers = new ArrayList<ClassAnnotationHandler>();
		if (handler != null && !annotationHandlers.contains(handler))
			return annotationHandlers.add(handler);
		return false;
	}

	public boolean removeClassAnnotationHandler(ClassAnnotationHandler handler) {
		if (annotationHandlers == null)
			annotationHandlers = new ArrayList<ClassAnnotationHandler>();
		if (handler != null)
			return annotationHandlers.remove(handler);
		return false;
	}

	private MyGroovyClassLoader getNewClassLoader() {
		CompilerConfiguration cc = new CompilerConfiguration();
		// cc.setMinimumRecompilationInterval(0);
		// cc.setRecompileGroovySource(true);
		cc.setSourceEncoding("utf8");
		// cc.setTargetDirectory("/home/momo/Aplomb/workspaces/workspace (server)/Group/");
		cc.setClasspath(path);

		// cc.setDebug(true);
		cc.setRecompileGroovySource(false);
		cc.setMinimumRecompilationInterval(Integer.MAX_VALUE);
		cc.setVerbose(false);
		cc.setDebug(false); 
//		try {
//			cc.setOutput(new PrintWriter(new File("/Users/aplomb/Dev/taineng/test/log.txt")));
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		}

		if (parentClassLoader == null)
			parentClassLoader = GroovyRuntime.class.getClassLoader();
		return new MyGroovyClassLoader(parentClassLoader, cc);
	}

	public synchronized void redeploy() throws CoreException {
		MyGroovyClassLoader newClassLoader = null;
		MyGroovyClassLoader oldClassLoader = classLoader;
		boolean deploySuccessfully = false;
		final Map<ClassAnnotationHandler, Map<String, Class<?>>> handlerMap = new LinkedHashMap<ClassAnnotationHandler, Map<String, Class<?>>>();
		try {
			newClassLoader = getNewClassLoader();
			Collection<File> files = FileUtils.listFiles(new File(path),
					FileFilterUtils.suffixFileFilter(".groovy"),
					FileFilterUtils.directoryFileFilter());

//			newClassLoader
//					.parseClass(new File(
//							"/home/momo/Aplomb/workspaces/ggtsworkspaces/Admin/groovy/services/IBaihuaService.groovy"));
			newClassLoader.pendingGroovyClasses = new HashSet<String>();
			newClassLoader.parsingGroovyClasses = new HashSet<String>();
			for(File file : files) {
				String absolutePath = file.getAbsolutePath();
				int pathPos = absolutePath.indexOf(path);
				if(pathPos < 0) {
					LoggerEx.warn(TAG, "Find path " + path + " in file " + absolutePath + " failed, " + pathPos + ". Ignore...");
					continue;
				}
				String key = file.getAbsolutePath().substring(pathPos + path.length());
				int pos = key.lastIndexOf(".");
				if(pos >= 0) {
					key = key.substring(0, pos);
				}
				key = key.replace("/", ".");
				newClassLoader.pendingGroovyClasses.add(key);
			}
			
			for (File file : files) {
				String absolutePath = file.getAbsolutePath();
				String key = file.getAbsolutePath().substring(absolutePath.indexOf(path) + path.length());
				// Class<?> groovyClass = groovyServlet.getGroovyClass();
				Class<?> groovyClass = newClassLoader.parseGroovyClass(key,
						file);

				if (annotationHandlers != null) {
					for (int i = 0; i < annotationHandlers.size(); i++) {
						ClassAnnotationHandler handler = annotationHandlers.get(i);
//						handler.setGroovyRuntime(this);
						Class<? extends Annotation> annotationClass = handler
								.handleAnnotationClass(this);
						if (annotationClass != null) {
							Annotation annotation = groovyClass
									.getAnnotation(annotationClass);
							if (annotation != null) {
								Map<String, Class<?>> classes = handlerMap
										.get(handler);
								if (classes == null) {
									classes = new HashMap<>();
									handlerMap.put(handler, classes);
								}
								classes.put(key, groovyClass);
							}
						} else {
							handlerMap.put(handler, new HashMap<String, Class<?>>());
						}
					}
				}
			}
			String[] strs = new String[] {
					"package script.groovy.runtime;",
					"import script.groovy.object.GroovyObjectEx",
					"class GroovyObjectExProxy implements GroovyInterceptable{",
						"private GroovyObjectEx<?> groovyObject;",
						"public GroovyObjectExProxy(GroovyObjectEx<?> groovyObject) {",
							"this.groovyObject = groovyObject;",
						"}",
						"def invokeMethod(String name, args) {",
							"Class<?> groovyClass = this.groovyObject.getGroovyClass();",
							"def calledMethod = groovyClass.metaClass.getMetaMethod(name, args);",
							"def returnObj = calledMethod?.invoke(this.groovyObject.getObject(), args);",
							"return returnObj;",
					    "}",
					"}"
			};
			String proxyClassStr = StringUtils.join(strs, "\r\n"); 
			groovyObjectExProxyClass = newClassLoader.parseClass(proxyClassStr, 
					"/script/groovy/runtime/GroovyObjectExProxy.groovy");
			deploySuccessfully = true;
		} catch (Throwable t) {
			t.printStackTrace();
			LoggerEx.fatal(TAG,
					"Redeploy occur unknown error, " + t.getMessage()
							+ " redeploy aborted!!!");
			if (t instanceof CoreException)
				throw (CoreException) t;
			else
				throw new CoreException(ChatErrorCodes.ERROR_GROOVY_UNKNOWN,
						"Groovy unknown error " + t.getMessage());
		} finally {
			if (deploySuccessfully) {
				if (oldClassLoader != null) {
					try {
						MetaClassRegistry metaReg = GroovySystem
								.getMetaClassRegistry();
						Class<?>[] classes = oldClassLoader.getLoadedClasses();
						for (Class<?> c : classes) {
							LoggerEx.info(TAG, classLoader
									+ " remove meta class " + c);
							metaReg.removeMetaClass(c);
						}

						oldClassLoader.clearCache();
						oldClassLoader.close();
						LoggerEx.info(TAG, "oldClassLoader " + oldClassLoader
								+ " is closed");
					} catch (Exception e) {
						e.printStackTrace();
						LoggerEx.error(TAG, oldClassLoader + " close failed, "
								+ e.getMessage());
					}
				}
				long version = latestVersion.incrementAndGet();
				newClassLoader.version = version;
				classLoader = newClassLoader;

				if (handlerMap != null && !handlerMap.isEmpty()) {
					Thread handlerThread = new Thread(new Runnable() {
						@Override
						public void run() {
							for(ClassAnnotationHandler annotationHandler : annotationHandlers) {
								Map<String, Class<?>> values = handlerMap.get(annotationHandler);
								if (values != null) {
									try {
										annotationHandler.handleAnnotatedClasses(values,
												classLoader);
									} catch (Throwable t) {
										t.printStackTrace();
										LoggerEx.fatal(TAG,
												"Handle annotated classes failed, "
														+ values + " class loader "
														+ classLoader
														+ " the handler " + annotationHandler
														+ " is ignored!");
									}
								}
							}
						}
					});
					handlerThread.start();
					try {
						handlerThread.join();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				LoggerEx.info(TAG, "Reload groovy scripts, current version is "
						+ version);
			} else {
				if (newClassLoader != null) {
					try {
						newClassLoader.clearCache();
						newClassLoader.close();
						LoggerEx.info(TAG, "newClassLoader " + newClassLoader
								+ " is closed");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	public static String path(Class<?> c) {
		return c.getName().replace(".", "/") + ".groovy";
	}

	public <T> GroovyObjectEx<T> create(Class<?> c) {
		return create(path(c), null);
	}

	public <T> GroovyObjectEx<T> create(String groovyPath) {
		return create(groovyPath, null);
	}

	public <T> GroovyObjectEx<T> create(String groovyPath,
			Class<? extends GroovyObjectEx<T>> groovyObjectClass) {
		GroovyObjectEx<T> goe = null;
		if (groovyObjectClass != null) {
			try {
				Constructor<? extends GroovyObjectEx<T>> constructor = groovyObjectClass
						.getConstructor(String.class);
				goe = constructor.newInstance(groovyPath);
			} catch (Throwable e) {
				e.printStackTrace();
				LoggerEx.error(TAG, "Initialize customized groovyObjectClass "
						+ groovyObjectClass + " failed, " + e.getMessage());
				return null;
			}
		} else {
			goe = new GroovyObjectEx<T>(groovyPath);
		}
		goe.setGroovyRuntime(this);
		return goe;
	}
	
	public <T> Object newObject(Class<?> c) {
		return newObject(path(c), null);
	}
	
	public <T> Object newObject(String groovyPath) {
		return newObject(groovyPath, null);
	}
	
	public <T> Object newObject(String groovyPath,
			Class<? extends GroovyObjectEx<T>> groovyObjectClass) {
		GroovyObjectEx<T> goe = null;
		if (groovyObjectClass != null) {
			try {
				Constructor<? extends GroovyObjectEx<T>> constructor = groovyObjectClass
						.getConstructor(String.class);
				goe = constructor.newInstance(groovyPath);
			} catch (Throwable e) {
				e.printStackTrace();
				LoggerEx.error(TAG, "Initialize customized groovyObjectClass "
						+ groovyObjectClass + " failed, " + e.getMessage());
				return null;
			}
		} else {
			goe = new GroovyObjectEx<T>(groovyPath);
		}
		goe.setGroovyRuntime(this);
		
		Object obj = null;
		try {
			Constructor<?> constructor = groovyObjectExProxyClass.getConstructor(GroovyObjectEx.class);
			obj = constructor.newInstance(goe);
		} catch (Throwable  e) {
			e.printStackTrace();
			LoggerEx.error(TAG, "New proxy instance "
					+ groovyObjectClass + " failed, " + e.getMessage());
		}
		return obj;
	}

	public Object getProxyObject(GroovyObjectEx<?> groovyObject) {
		Object obj = null;
		try {
			GroovyBeanFactory factory = GroovyBeanFactory.getInstance();
			Class<?> proxyClass = factory.getProxyClass(groovyObject.getGroovyClass().getName());
			if(proxyClass != null) {
				Constructor<?> constructor = proxyClass.getConstructor(GroovyObjectEx.class);
				obj = constructor.newInstance(groovyObject);
			}
		} catch (Throwable  e) {
			e.printStackTrace();
			LoggerEx.error(TAG, "New proxy instance(getProxyObject) "
					+ groovyObject.getGroovyPath() + " failed, " + e.getMessage());
		}
		return obj;
	}
	
	public AtomicLong getLatestVersion() {
		return latestVersion;
	}

	public MyGroovyClassLoader getClassLoader() {
		return classLoader;
	}

	public ClassLoader getParentClassLoader() {
		return parentClassLoader;
	}

	public void setParentClassLoader(ClassLoader parentClassLoader) {
		this.parentClassLoader = parentClassLoader;
	}

	public List<ClassAnnotationHandler> getAnnotationHandlers() {
		return annotationHandlers;
	}

	public void setAnnotationHandlers(
			List<ClassAnnotationHandler> annotationHandlers) {
		if (this.annotationHandlers != null) {
			this.annotationHandlers.addAll(annotationHandlers);
		} else {
			this.annotationHandlers = annotationHandlers;
		}
	}
	
	public Class<?> getClass(String classStr) {
		if(StringUtils.isBlank(classStr))
			return null;
		classStr = classStr.replace(".", "/") + ".groovy";
		if(classLoader != null) {
			ClassHolder holder = classLoader.getClass(classStr);
			if(holder != null) {
				return holder.getParsedClass();
			}
		}
		return null;
	}
}
