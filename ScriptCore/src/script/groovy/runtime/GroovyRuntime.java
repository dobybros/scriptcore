package script.groovy.runtime;

import chat.utils.TimerEx;
import chat.utils.TimerTaskEx;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import groovy.lang.GroovySystem;
import groovy.lang.MetaClassRegistry;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.groovy.control.CompilerConfiguration;

import script.ScriptRuntime;
import script.groovy.object.GroovyObjectEx;
import chat.errors.ChatErrorCodes;
import chat.errors.CoreException;
import chat.logs.LoggerEx;

public class GroovyRuntime extends ScriptRuntime{
    private static final String TAG = GroovyRuntime.class.getSimpleName();
    private MyGroovyClassLoader classLoader;
    private HashMap<String, ClassHolder> cachedClasses;
    private ConcurrentHashMap<Thread, MyGroovyClassLoader> threadClassLoaderMap = new ConcurrentHashMap<>();
    private AtomicLong latestVersion = new AtomicLong(0);
    private ClassLoader parentClassLoader;
    private ArrayList<ClassAnnotationHandler> annotationHandlers = new ArrayList<>();
    private ConcurrentHashMap<Object, ClassAnnotationHandler> annotationHandlerMap = new ConcurrentHashMap<>();
    //	private static GroovyRuntime instance;
    private Class<?> groovyObjectExProxyClass;
    private GroovyBeanFactory beanFactory;
    private List<FieldInjectionListener> fieldInjectionListeners;
    private List<String> libPaths;
    private URLClassLoader libClassLoader;

    public GroovyRuntime addLibPath(String libPath) {
        if(StringUtils.isBlank(libPath))
            return this;
        if(libPaths == null) {
            libPaths = new ArrayList<>();
        }
        if(!libPaths.contains(libPath)) {
            libPaths.add(libPath);
        }
        return this;
    }

    public boolean removeLibPath(String libPath) {
        if(StringUtils.isBlank(libPath) || libPaths == null)
            return false;
        return libPaths.remove(libPath);
    }

    public GroovyRuntime addFieldInjectionListener(FieldInjectionListener listener) {
        if(fieldInjectionListeners == null) {
            fieldInjectionListeners = new ArrayList<>();
        }
        if(!fieldInjectionListeners.contains(listener))
            fieldInjectionListeners.add(listener);
        return this;
    }

    public void removeFieldInjectionListener(FieldInjectionListener listener) {
        if(fieldInjectionListeners != null)
            fieldInjectionListeners.remove(listener);
    }

    public List<FieldInjectionListener> getFieldInjectionListeners() {
        return fieldInjectionListeners;
    }

    public static GroovyRuntime getCurrentGroovyRuntime(ClassLoader currentClassLoader) {
        if(currentClassLoader == null)
            return null;
        ClassLoader classLoader = currentClassLoader.getParent();
        if(classLoader != null && classLoader instanceof GroovyRuntime.MyGroovyClassLoader) {
            return ((GroovyRuntime.MyGroovyClassLoader) classLoader).getGroovyRuntime();
        }
        return null;
    }

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

    public class MyGroovyClassLoader extends GroovyClassLoader {
        private long version;

        public MyGroovyClassLoader(ClassLoader parentClassLoader,
                                   CompilerConfiguration cc) {
            super(parentClassLoader, cc);
        }

        public GroovyRuntime getGroovyRuntime() {
            return GroovyRuntime.this;
        }

        public long getVersion() {
            return version;
        }

        public String toString() {
            return MyGroovyClassLoader.class.getSimpleName() + "#" + version;
        }

        public ClassHolder getClass(String classStr) {
            if(classStr.endsWith(".groovy")) {
                classStr = classStr.substring(0, classStr.length() - 7).replace("/", ".");
            }
            return cachedClasses.get(classStr);
        }
    }

    public GroovyRuntime() {
//		instance = this;
    }

    public synchronized void init() throws CoreException {
//		instance = this;
        redeploy();
    }

    public boolean addClassAnnotationHandler(ClassAnnotationHandler handler) {
        if (handler != null && !annotationHandlers.contains(handler)) {
            boolean bool = annotationHandlers.add(handler);
            annotationHandlerMap.put(handler.getKey(), handler);
            handler.setGroovyRuntime(this);
            return bool;
        }

        return false;
    }

    public boolean removeClassAnnotationHandler(ClassAnnotationHandler handler) {
        if (handler != null) {
            boolean bool = annotationHandlers.remove(handler);
            annotationHandlerMap.remove(handler.getKey(), handler);
            return bool;
        }
        return false;
    }

    private MyGroovyClassLoader getNewClassLoader(URLClassLoader newLibClassLoader) {
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

        ClassLoader newParentClassLoader = parentClassLoader;
        if (newParentClassLoader == null)
            newParentClassLoader = GroovyRuntime.class.getClassLoader();
        if(newLibClassLoader != null)
            newParentClassLoader = newLibClassLoader;
        return new MyGroovyClassLoader(newParentClassLoader, cc);
    }

    public void beforeDeploy() {

    }

    private void closeLibClassloader(URLClassLoader oldLibClassLoader) {
        if(libClassLoader != null) {
//            try {
//                Class clazz = java.net.URLClassLoader.class;
//                Field ucp = clazz.getDeclaredField("ucp");
//                ucp.setAccessible(true);
//                Object sunMiscURLClassPath = ucp.get(oldLibClassLoader);
//                Field loaders = sunMiscURLClassPath.getClass().getDeclaredField("loaders");
//                loaders.setAccessible(true);
//                Object collection = loaders.get(sunMiscURLClassPath);
//                for (Object sunMiscURLClassPathJarLoader : ((Collection) collection).toArray()) {
//                    try {
//                        Field loader = sunMiscURLClassPathJarLoader.getClass().getDeclaredField("jar");
//                        loader.setAccessible(true);
//                        Object jarFile = loader.get(sunMiscURLClassPathJarLoader);
//                        if(jarFile instanceof JarFile) {
//                            JarFile theJarFile = (JarFile) jarFile;
//                            theJarFile.close();
//                            LoggerEx.info(TAG, "Jar file " + theJarFile.getName() + " has been closed");
//                        }
//                    } catch (Throwable t) {
//                        LoggerEx.warn(TAG, "Close jar file failed, " + t.getMessage());
//                        // if we got this far, this is probably not a JAR loader so skip it
//                    }
//                }
//            } catch (Throwable t) {
//                LoggerEx.warn(TAG, "Close all jar files failed, " + t.getMessage());
//            }

            try {
                oldLibClassLoader.close();
                LoggerEx.info(TAG, "oldLibClassLoader " + oldLibClassLoader + " has been closed.");
            } catch (IOException e) {
                e.printStackTrace();
                LoggerEx.error(TAG, "oldLibClassLoader close failed, " + e.getMessage());
            }
        }
    }
    public synchronized void redeploy() throws CoreException {
        try {
            beforeDeploy();
        } catch(Throwable t) {
            LoggerEx.warn(TAG, "beforeDeploy failed, " + t.getMessage());
        }

        //load libs
//        closeLibClassloader();
        ClassLoader newParentClassLoader = parentClassLoader;
        URLClassLoader newLibClassLoader = null, oldLibClassLoader = libClassLoader;
        File libsPath = new File(path + "/libs");
        if(libsPath.exists() && libsPath.isDirectory()) {
            List<URL> urls = new ArrayList<>();
            Collection<File> jars = FileUtils.listFiles(libsPath,
                    FileFilterUtils.suffixFileFilter(".jar"),
                    FileFilterUtils.directoryFileFilter());
            for(File jar : jars) {
                String path = "jar:file://" + jar.getAbsolutePath() + "!/";
                try {
                    urls.add(new URL(path));
                    LoggerEx.info(TAG, "Loaded jar " + jar.getAbsolutePath());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    LoggerEx.warn(TAG, "MalformedURL " + path + " while load jars, error " + e.getMessage());
                }
            }
            if(!urls.isEmpty()) {
                URL[] theUrls = new URL[urls.size()];
                urls.toArray(theUrls);
                if(newParentClassLoader == null)
                    newParentClassLoader = GroovyRuntime.class.getClassLoader();
                newLibClassLoader = new URLClassLoader(theUrls, newParentClassLoader);
            }
        }

        MyGroovyClassLoader newClassLoader = null;
        MyGroovyClassLoader oldClassLoader = classLoader;
        boolean deploySuccessfully = false;
        ByteArrayOutputStream baos = null;
        List<File> compileFirstFiles = new ArrayList<>();
        final Map<ClassAnnotationHandler, Map<String, Class<?>>> handlerMap = new LinkedHashMap<ClassAnnotationHandler, Map<String, Class<?>>>();
        try {
            File importPath = new File(path + "/config/imports.groovy");
            StringBuilder importBuilder = null;
            if(importPath.isFile() && importPath.exists()) {
                LoggerEx.info(TAG, "Start imports " + FilenameUtils.separatorsToUnix(importPath.getAbsolutePath()));
                String content = FileUtils.readFileToString(importPath, "utf8");
                if(!content.endsWith("//THE END\r\n")) {
                    final CommandLine cmdLine = CommandLine.parse("groovy " + FilenameUtils.separatorsToUnix(importPath.getAbsolutePath()));
                    ExecuteWatchdog watchdog = new ExecuteWatchdog(TimeUnit.MINUTES.toMillis(15));//设置超时时间
                    DefaultExecutor executor = new DefaultExecutor();
                    baos = new ByteArrayOutputStream();
                    executor.setStreamHandler(new PumpStreamHandler(baos, baos));
                    executor.setWatchdog(watchdog);
                    executor.setExitValue(0);//由于ping被到时间终止，所以其默认退出值已经不是0，而是1，所以要设置它
                    int exitValue = executor.execute(cmdLine);
                    final String result = baos.toString().trim();
                    LoggerEx.info(TAG, "import log " + result);
                    LoggerEx.info(TAG, "Imported " + FilenameUtils.separatorsToUnix(importPath.getAbsolutePath()));

                    importBuilder = new StringBuilder(content);
                    importBuilder.append("\r\n");
                } else {
                    LoggerEx.info(TAG, "Already added imports for " + FilenameUtils.separatorsToUnix(importPath.getAbsolutePath()));
                }
            } else {
                String[] strs = new String[] {
                        "package config",
                        "\r\n",
                };
                String content = StringUtils.join(strs, "\r\n");
                FileUtils.writeStringToFile(importPath, content, "utf8");
                importBuilder = new StringBuilder(content);
                importBuilder.append("\r\n");
                LoggerEx.info(TAG, "Generates imports " + FilenameUtils.separatorsToUnix(importPath.getAbsolutePath()));
            }
            compileFirstFiles.add(importPath);
//            StringBuilder importBuilder = new StringBuilder(FileUtils.readFileToString(importPath, "utf8"));

            newClassLoader = getNewClassLoader(newLibClassLoader);
            Collection<File> files = FileUtils.listFiles(new File(path),
                    FileFilterUtils.suffixFileFilter(".groovy"),
                    FileFilterUtils.directoryFileFilter());

            if(importBuilder != null) {
                for(File file : files) {
                    String absolutePath = FilenameUtils.separatorsToUnix(file.getAbsolutePath());
                    int pathPos = absolutePath.indexOf(path);
                    if(pathPos < 0 || absolutePath.endsWith("config/imports.groovy")) {
                        LoggerEx.warn(TAG, "Find path " + path + " in file " + absolutePath + " failed, " + pathPos + ". Ignore...");
                        continue;
                    }
                    String key = absolutePath.substring(pathPos + path.length());

                    if(libPaths != null) {
                        boolean ignore = false;
                        for(String libPath : libPaths) {
                            if(key.startsWith(libPath)) {
                                ignore = true;
//                            LoggerEx.info(TAG, "Ignore lib classes " + key + " while parsing. hit lib " + libPath);
                                break;
                            }
                        }
                        if(ignore)
                            continue;
                    }

                    int pos = key.lastIndexOf(".");
                    if(pos >= 0) {
                        key = key.substring(0, pos);
                    }
                    key = key.replace("/", ".");

                    importBuilder.append("import ").append(key).append("\r\n");
                }
                importBuilder.append("//THE END\r\n");

                FileUtils.writeStringToFile(importPath, importBuilder.toString(), "utf8");
            }

//			newClassLoader
//					.parseClass(new File(
//							"/home/momo/Aplomb/workspaces/ggtsworkspaces/Admin/groovy/services/IBaihuaService.groovy"));
//            for(File file : files) {
//                String absolutePath = FilenameUtils.separatorsToUnix(file.getAbsolutePath());
//                int pathPos = absolutePath.indexOf(path);
//                if(pathPos < 0) {
//                    LoggerEx.warn(TAG, "Find path " + path + " in file " + absolutePath + " failed, " + pathPos + ". Ignore...");
//                    continue;
//                }
//                String key = absolutePath.substring(pathPos + path.length());
//                int pos = key.lastIndexOf(".");
//                if(pos >= 0) {
//                    key = key.substring(0, pos);
//                }
//                key = key.replace("/", ".");
//            }
            for(File file : compileFirstFiles) {
                newClassLoader.parseClass(file);
//				parseFile(file, handlerMap, newClassLoader);
            }
//            for (File file : files) {
//                if(!file.getAbsolutePath().contains("/core/")) {
//                    newClassLoader.parseClass(file);
////                    parseFile(file, handlerMap, newClassLoader);
//                }
//            }

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

            Class[] loadedClasses = newClassLoader.getLoadedClasses();
            cachedClasses = new HashMap<>();
            if(loadedClasses != null) {
                for(Class clazz : loadedClasses) {
                    ClassHolder classHolder = new ClassHolder();
                    classHolder.parsedClass = clazz;
                    LoggerEx.info(TAG, "Loaded class " + clazz.getName());
                    cachedClasses.put(clazz.getName(), classHolder);
                    if (annotationHandlers != null) {
                        Collection<ClassAnnotationHandler> handlers = annotationHandlers;
                        for (ClassAnnotationHandler handler : handlers) {
//						ClassAnnotationHandler handler = annotationHandlers.get(i);
//						handler.setGroovyRuntime(this);
                            Class<? extends Annotation> annotationClass = handler
                                    .handleAnnotationClass(this);
                            if (annotationClass != null) {
                                Annotation annotation = clazz
                                        .getAnnotation(annotationClass);
                                if (annotation != null) {
                                    Map<String, Class<?>> classes = handlerMap
                                            .get(handler);
                                    if (classes == null) {
                                        classes = new HashMap<>();
                                        handlerMap.put(handler, classes);
                                    }

                                    //XXX the key original is groovy path, not absolute.
                                    classes.put(clazz.getName(), clazz);
                                }
                            }
                        }
                    }
                }
            }

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
            IOUtils.closeQuietly(baos);
            if (deploySuccessfully) {
                if (oldClassLoader != null) {
                    TimerEx.schedule(new TimerTaskEx() {
                        @Override
                        public void execute() {
                            LoggerEx.info(TAG, "Old class loader " + oldClassLoader + " is releasing");
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
                            } catch (Throwable e) {
                                e.printStackTrace();
                                LoggerEx.error(TAG, oldClassLoader + " close failed, "
                                        + e.getMessage());
                            }

                            closeLibClassloader(oldLibClassLoader);
                        }
                    }, TimeUnit.SECONDS.toMillis(60)); //release old class loader after 60 seconds.
                    LoggerEx.info(TAG, "Old class loader " + oldClassLoader + " will be released after 60 seconds");
                }
                long version = latestVersion.incrementAndGet();
                newClassLoader.version = version;
                classLoader = newClassLoader;
                libClassLoader = newLibClassLoader;

                if (handlerMap != null && !handlerMap.isEmpty()) {
                    Thread handlerThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Collection<ClassAnnotationHandler> handlers = annotationHandlers;
                            for(ClassAnnotationHandler annotationHandler : handlers) {
                                if(annotationHandler.getGroovyRuntime() == null)
                                    annotationHandler.setGroovyRuntime(GroovyRuntime.this);
                                if(annotationHandler instanceof GroovyBeanFactory) {
                                    beanFactory = (GroovyBeanFactory) annotationHandler;
                                }
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
            GroovyBeanFactory factory = beanFactory;
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

    @Override
    public void close() {
        Collection<ClassAnnotationHandler> handlers = annotationHandlers;
        for(ClassAnnotationHandler annotationHandler : handlers) {
            try {
                annotationHandler.handlerShutdown();
            } catch (Throwable t) {
                t.printStackTrace();
                LoggerEx.fatal(TAG,
                        "Handle annotated classes shutdown failed, class loader "
                                + classLoader
                                + " the handler " + annotationHandler + " error " + t.getMessage());
            }
        }
        if (classLoader != null) {
            try {
                MetaClassRegistry metaReg = GroovySystem
                        .getMetaClassRegistry();
                Class<?>[] classes = classLoader.getLoadedClasses();
                for (Class<?> c : classes) {
                    LoggerEx.info(TAG, classLoader
                            + " remove meta class " + c);
                    metaReg.removeMetaClass(c);
                }

                classLoader.clearCache();
                classLoader.close();
                LoggerEx.info(TAG, "oldClassLoader " + classLoader
                        + " is closed");
            } catch (Exception e) {
                e.printStackTrace();
                LoggerEx.error(TAG, classLoader + " close failed, "
                        + e.getMessage());
            }
        }
        if(libClassLoader != null) {
            closeLibClassloader(libClassLoader);
        }
        annotationHandlerMap.clear();
        annotationHandlers.clear();
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

    public Collection<ClassAnnotationHandler> getAnnotationHandlers() {
        return annotationHandlers;
    }

    public void setAnnotationHandlers(
            List<ClassAnnotationHandler> annotationHandlers) {
        if (annotationHandlers != null) {
            for(ClassAnnotationHandler handler : annotationHandlers) {
                handler.setGroovyRuntime(this);
                this.annotationHandlers.add(handler);
                this.annotationHandlerMap.put(handler.getKey(), handler);
            }
        }
    }

    public ClassAnnotationHandler getClassAnnotationHandler(Object key) {
        return this.annotationHandlerMap.get(key);
    }

    public Class<?> getClass(String classStr) {
        if(StringUtils.isBlank(classStr))
            return null;

        ClassHolder holder = cachedClasses.get(classStr);
        if(holder != null) {
            return holder.parsedClass;
        }
        return null;
    }

    public GroovyBeanFactory getBeanFactory() {
        return beanFactory;
    }

    public void setBeanFactory(GroovyBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }
}
