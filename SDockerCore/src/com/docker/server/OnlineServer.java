package com.docker.server;

import chat.errors.ChatErrorCodes;
import chat.errors.CoreException;
import chat.logs.LoggerEx;
import chat.main.ServerStart;
import chat.utils.ChatUtils;
import chat.utils.IPHolder;
import com.docker.data.DockerStatus;
import com.docker.errors.CoreErrorCodes;
import com.docker.storage.adapters.DockerStatusService;
import com.docker.storage.adapters.LansService;
import com.docker.storage.adapters.ServersService;
import com.docker.tasks.Task;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Properties;


public class OnlineServer {
    private static final String TAG = OnlineServer.class.getSimpleName();
    private String server;

    private List<Task> tasks;

    private String internalKey;

    private String serverType;

    @Resource
    private IPHolder ipHolder;

    private DockerStatusService dockerStatusService;

    private static OnlineServer instance;

    private DockerStatus dockerStatus;

    private OnlineServerStartHandler startHandler;

    private String sslRpcPort;
    private String rpcPort;
    private Integer status;

    private String lanId;

    /** rpc ssl certificate */
    private String rpcSslClientTrustJksPath;
    private String rpcSslServerJksPath;
    private String rpcSslJksPwd;

    public static interface OnlineServerStartHandler {
        public void serverWillStart(OnlineServer onlineServer) throws CoreException;

        public void serverWillShutdown(OnlineServer onlineServer);
    }

    protected OnlineServer() {
        instance = this;
    }

    public static OnlineServer getInstance() {
        return instance;
    }

    public void prepare() {
    }

    protected DockerStatus generateDockerStatus(Integer port) {
        DockerStatus dockerStatus = new DockerStatus();
        dockerStatus.setServer(server);
        dockerStatus.setServerType(serverType);
        dockerStatus.setIp(ipHolder.getIp());
        if (rpcPort != null) {
            try {
                dockerStatus.setRpcPort(Integer.parseInt(rpcPort));
            } catch (Throwable t) {
            }
        }
        if (sslRpcPort != null) {
            try {
                dockerStatus.setSslRpcPort(Integer.parseInt(sslRpcPort));
            } catch (Throwable t) {
            }
        }
        dockerStatus.setHttpPort(port);
        dockerStatus.setLanId(lanId);
        dockerStatus.setHealth(0);
        if(status == null)
            status = DockerStatus.STATUS_OK;
        dockerStatus.setStatus(status);
        return dockerStatus;
    }

    public void start() {
        try {
            ClassPathResource resource = new ClassPathResource("lan.properties");
            Properties pro = new Properties();
            try {
                pro.load(resource.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
                LoggerEx.error(TAG, "Prepare lan.properties is failed, " + e.getMessage());
            }
            lanId = pro.getProperty("lan.id");
            if (StringUtils.isBlank(lanId)) {
                throw new CoreException(CoreErrorCodes.ERROR_LANID_ILLEGAL, "LanId is illegal, " + lanId);
            }

            String serverPort = System.getProperty("jetty.port");
            if (serverPort == null || StringUtils.isBlank(serverPort)) {
                throw new CoreException(ChatErrorCodes.ERROR_CORE_SERVERPORT_ILLEGAL, "Server port is null");
            }
            Integer port = Integer.parseInt(serverPort);

            if (server == null)
                server = ChatUtils.generateFixedRandomString();
            prepare();
            if (dockerStatusService != null) {
                dockerStatus = generateDockerStatus(port);
                dockerStatusService.addDockerStatus(dockerStatus);
            }

            QueuedThreadPool threadPool = ServerStart.getInstance().getThreadPool();
            if (tasks != null) {
                for (Task task : tasks) {
                    task.setOnlineServer(this);
                    task.init();
                    LoggerEx.info(TAG, "Task " + task + " initialized!");
                    int numOfThreads = task.getNumOfThreads();
                    for (int i = 0; i < numOfThreads; i++) {
                        threadPool.execute(task);
                    }
                }
            }

            //Will call below only when server enter OK status from standby status.
//			if(startHandler != null) {
//				startHandler.serverWillStart(this);
//			}
        } catch (Throwable e) {
            e.printStackTrace();
            LoggerEx.error(TAG, "Start online server " + server + " failed, " + e.getMessage());
            if (dockerStatusService != null) {
                try {
                    dockerStatusService.deleteDockerStatus(server);
                    LoggerEx.info(TAG, "Deleted OnlineServer " + server + " because of error " + e.getMessage());
                } catch (CoreException e1) {
                    e.printStackTrace();
                    LoggerEx.info(TAG, "Remove online server " + server + " failed, " + e1.getMessage());
                }
            }
            OnlineServer.shutdownNow();
            System.exit(0);
        }
    }

    public static void shutdownNow() {
        OnlineServer onlineServer = OnlineServer.getInstance();
        if(onlineServer != null)
            onlineServer.shutdown();
    }

    public void shutdown() {
        LoggerEx.info(TAG, "OnlineServer " + server + " is shutting down");
        if (startHandler != null) {
            try {
                startHandler.serverWillShutdown(this);
            } catch (Exception e) {
                e.printStackTrace();
                LoggerEx.fatal(TAG, "StartHandler " + startHandler + " shutdown failed, " + e.getMessage());
            }
        }
        if (dockerStatusService != null) {
            try {
                dockerStatusService.deleteDockerStatus(server);
                LoggerEx.info(TAG, "Deleted OnlineServer " + server);
            } catch (CoreException e) {
                e.printStackTrace();
                LoggerEx.fatal(TAG, "Remove online server " + server + " failed, " + e.getMessage());
            }
        }
        if (tasks != null) {
            for (Task task : tasks) {
                try {
                    LoggerEx.info(TAG, "Task " + task + " is shutting down");
                    task.shutdown();
                    LoggerEx.info(TAG, "Task " + task + " has been shutdown");
                } catch (Exception e) {
                    e.printStackTrace();
                    LoggerEx.fatal(TAG, "Task shutdown failed, " + e.getMessage());
                }
            }
        }
    }

    public String getServer() {
        return server;
    }

    public void setServer(String serverName) {
        this.server = serverName;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    public OnlineServerStartHandler getStartHandler() {
        return startHandler;
    }

    public void setStartHandler(OnlineServerStartHandler startHandler) {
        this.startHandler = startHandler;
    }

    public String getServerType() {
        return serverType;
    }

    public void setServerType(String serverType) {
        this.serverType = serverType;
    }

    public DockerStatus getDockerStatus() {
        return dockerStatus;
    }

    public void setDockerStatus(DockerStatus dockerStatus) {
        this.dockerStatus = dockerStatus;
    }

    public String getRpcPort() {
        return rpcPort;
    }

    public void setRpcPort(String rpcPort) {
        this.rpcPort = rpcPort;
    }

    public String getLanId() {
        return lanId;
    }

    public void setLanId(String lanId) {
        this.lanId = lanId;
    }

    public String getInternalKey() {
        return internalKey;
    }

    public void setInternalKey(String internalKey) {
        this.internalKey = internalKey;
    }

    public String getSslRpcPort() {
        return sslRpcPort;
    }

    public void setSslRpcPort(String sslRpcPort) {
        this.sslRpcPort = sslRpcPort;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public DockerStatusService getDockerStatusService() {
        return dockerStatusService;
    }

    public void setDockerStatusService(DockerStatusService dockerStatusService) {
        this.dockerStatusService = dockerStatusService;
    }

    public String getRpcSslClientTrustJksPath() {
        return rpcSslClientTrustJksPath;
    }

    public void setRpcSslClientTrustJksPath(String rpcSslClientTrustJksPath) {
        this.rpcSslClientTrustJksPath = rpcSslClientTrustJksPath;
    }

    public String getRpcSslServerJksPath() {
        return rpcSslServerJksPath;
    }

    public void setRpcSslServerJksPath(String rpcSslServerJksPath) {
        this.rpcSslServerJksPath = rpcSslServerJksPath;
    }

    public String getRpcSslJksPwd() {
        return rpcSslJksPwd;
    }

    public void setRpcSslJksPwd(String rpcSslJksPwd) {
        this.rpcSslJksPwd = rpcSslJksPwd;
    }
}