package chat.main;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

import org.eclipse.jetty.deploy.DeploymentManager;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.eclipse.jetty.webapp.WebAppContext;
import org.xml.sax.SAXException;

import chat.logs.LoggerEx;

public final class ServerStart {
	private static final String TAG = "ServerStart";
	private static final String contextPath = "/";
	private static final String warPath = "/war";
	private static final String jetty_home = ".";
	// private static final String logs_dir = "/logs";
	private Server server;
	private int serverPort;
	private int maxThreadPoolSize;
	private int maxInnerThreadPoolSize;
	private String customWarPath;
	
	private Map<String, String> asyncServletMap;
	
	private static ServerStart instance;
	private boolean isStarted = false;
	
	private QueuedThreadPool threadPool;
	private static ContextHandler createStaticResourceHandler() {
		ResourceHandler resourceHandler = new ResourceHandler();
		resourceHandler.setDirectoriesListed(false);
		resourceHandler.setResourceBase("war/WEB-INF/static");
		ContextHandler context = new ContextHandler();
		context.setContextPath("/res");
		context.setHandler(resourceHandler);
		return context;
	}
	
	public ServerStart(int serverPort, int maxThreadPoolSize, int maxInnerThreadPoolSize, String customWarPath) {
		this.serverPort = serverPort;
		this.maxThreadPoolSize = maxThreadPoolSize;
		this.maxInnerThreadPoolSize = maxInnerThreadPoolSize;
		this.customWarPath = customWarPath;
		instance = this;
		if(maxInnerThreadPoolSize > 0) {
			threadPool = new QueuedThreadPool(maxInnerThreadPoolSize);
			try {
				threadPool.start();
			} catch (Exception e) {
				e.printStackTrace();
				LoggerEx.error(TAG, "InnerThreadPool start failed, " + e.getMessage());
			}
		}
	}
	
	public static ServerStart getInstance() {
		return instance;
	}
	public static void main(String[] args) {
		File f = new File("./tmp");
		System.out.println(f.getAbsolutePath());
	}
	/**
	 * @param args
	 */
	
	public synchronized void start() {
		if(isStarted)
			return;
		isStarted = true;
		System.setProperty("jetty.home", jetty_home);
		try {
			WebAppContext webapp = new WebAppContext();
			webapp.setContextPath(contextPath);
			if(customWarPath != null)
				webapp.setWar(customWarPath);
			else
				webapp.setWar(jetty_home + warPath);
			webapp.setParentLoaderPriority(true);
			webapp.setTempDirectory(new File("./tmp"));
			
			ContextHandler resourceContext = createStaticResourceHandler();
			
//			System.out.println("warPath--" + webapp.getWar());
			DeploymentManager dm  = new DeploymentManager();
			ContextHandlerCollection contexts = new ContextHandlerCollection();
			dm.setContexts(contexts);
			
//			ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
//			context.setContextPath("/");
			
//			ServletHandler servletHandler = new ServletHandler();
//			if(asyncServletMap != null) {
//				for(Entry<String, String> entry : asyncServletMap.entrySet()) {
//					Class<? extends Servlet> clazz = null;
//					try {
//						clazz = (Class<? extends Servlet>) Class.forName(entry.getValue());
//					} catch(Throwable t) {
//						t.printStackTrace();
//					}
//					if(clazz == null) {
//						AcuLogger.error(TAG, "Handle asyncServletMap failed for " + entry.getKey() + ": " + entry.getValue());
//						System.exit(-1);
//					}
//					ServletHolder holder = new ServletHolder(entry.getKey(), clazz);
//					holder.setAsyncSupported(true);
//					servletHandler.addServlet(holder);
//				}
//			}
			
			QueuedThreadPool threadPool = new QueuedThreadPool(maxThreadPoolSize);
            server = new Server(threadPool);
            ServerConnector connector = new ServerConnector(server);
            connector.setPort(serverPort);
            server.addConnector(connector);
			server.addBean(dm);
			contexts.setHandlers(new Handler[]{resourceContext, webapp});
			server.setHandler(contexts);
			server.setStopAtShutdown(true);
			server.setAttribute("org.eclipse.jetty.server.Request.maxFormContentSize", -1);
			
			server.start();
			LoggerEx.info(TAG, "handler--" + server.getHandler());
			LoggerEx.info(TAG, "connector--"+java.util.Arrays.toString(server.getConnectors()));
			LoggerEx.info(TAG, "threadPool--"+server.getThreadPool()+", minithread-number:"+server.getThreadPool().getThreads());
			LoggerEx.info(TAG, "Server started , current Time: " + new Date());
			
			server.join();
		} catch (SAXException e) {
			System.out.println(e.getMessage());
			System.exit(-1);
		} catch (IOException e) {
			System.out.println(e.getMessage());
			System.exit(-1);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.exit(-1);
		}		
	}
	
	public String threadPoolStatus() {
		ThreadPool threadPool = server.getThreadPool();
		 StringBuffer buffer = new StringBuffer("Web ThreadPool");
		 buffer.append(" state: " + server.getState());
		 buffer.append(" threadPool: " + threadPool);
		 return buffer.toString();
	}
	
	public String innerThreadPoolStatus() {
		if(threadPool != null) {
			StringBuffer buffer = new StringBuffer("Inner ThreadPool");
			buffer.append(" state: " + server.getState());
			buffer.append(" threadPool: " + threadPool);
			return buffer.toString();
		}
		return "No innerThreadPool";
	}
	
	public int getServerPort() {
		return serverPort;
	}

	public Map<String, String> getAsyncServletMap() {
		return asyncServletMap;
	}

	public void setAsyncServletMap(Map<String, String> asyncServletMap) {
		this.asyncServletMap = asyncServletMap;
	}

	public QueuedThreadPool getThreadPool() {
		return threadPool != null ? threadPool : (QueuedThreadPool)server.getThreadPool();
	}

	public int getMaxInnerThreadPoolSize() {
		return maxInnerThreadPoolSize;
	}

	public void setMaxInnerThreadPoolSize(int maxInnerThreadPoolSize) {
		this.maxInnerThreadPoolSize = maxInnerThreadPoolSize;
	}
}
