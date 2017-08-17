package connectors.mongodb;

import chat.errors.CoreException;
import chat.logs.LoggerEx;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;


public class MongoClientHelper {
	private static final String TAG = MongoClientHelper.class.getSimpleName();

	private String hosts;//"mongodb://localhost:27017,localhost:27018,localhost:27019"
	
//	private static MongoClientHelper instance;
	
	private static int[] lock = new int[0];
	
	private MongoClient mongoClient;
	private Integer connectionsPerHost;
	private Integer threadsAllowedToBlockForConnectionMultiplier;
	private Integer maxWaitTime;
	private Integer connectTimeout;
	private Integer socketTimeout;
	private Boolean socketKeepAlive;
	
	public MongoClientHelper() {
//		instance = this;
	}
	
//	public static MongoClientHelper getInstance() {
//		return instance;
//	}
	
	public void connect() throws CoreException {
		connect(null);
	}
	
	public MongoClient connect(String toHosts) throws CoreException {
		synchronized (lock) {
			if(toHosts == null)
				toHosts = hosts;
			if(mongoClient == null || hosts == null || !hosts.equals(toHosts)) {
				LoggerEx.info(TAG, "Connecting hosts " + toHosts + " from old hosts " + hosts);
				try {
					MongoClientOptions.Builder optionsBuilder = MongoClientOptions.builder();
					if(connectionsPerHost != null)
						optionsBuilder.connectionsPerHost(connectionsPerHost);
					if(threadsAllowedToBlockForConnectionMultiplier != null)
						optionsBuilder.threadsAllowedToBlockForConnectionMultiplier(threadsAllowedToBlockForConnectionMultiplier);
					if(maxWaitTime != null)
						optionsBuilder.maxWaitTime(maxWaitTime);
					if(connectTimeout != null)
						optionsBuilder.connectTimeout(connectTimeout);
					if(socketTimeout != null)
						optionsBuilder.socketTimeout(socketTimeout);
					if(socketKeepAlive != null)
						optionsBuilder.socketKeepAlive(socketKeepAlive);
//					CodecRegistry registry = CodecRegistries.fromRegistries(MongoClient.getDefaultCodecRegistry(), CodecRegistries.fromCodecs(new CleanDocumentCodec()));
//					optionsBuilder.codecRegistry(registry);

					if(mongoClient != null) {
						mongoClient.close();
						LoggerEx.info(TAG, "Connected hosts " + toHosts + " closing old hosts client " + hosts + " now.");
					}

					MongoClientURI connectionString = new MongoClientURI(toHosts, optionsBuilder);
					mongoClient = new MongoClient(connectionString);
					
					hosts = toHosts;
					LoggerEx.info(TAG, "Connected hosts " + toHosts);
				} catch (Throwable t) {
					t.printStackTrace();
					LoggerEx.fatal(TAG, "Build mongo uri for hosts " + hosts + " failed, " + t.getMessage());
				}
			}
		}
		return mongoClient;
	}
	
	public void disconnect() {
		synchronized (lock) {
			if(hosts != null) {
				if(mongoClient != null) {
					mongoClient.close();
				}
				hosts = null;
				mongoClient = null;
			}
		}
	}

	public MongoDatabase getMongoDatabase(String databaseName) {
		if(mongoClient != null) {
			return mongoClient.getDatabase(databaseName);
		}
		return null;
	}
	
	public String getHosts() {
		return hosts;
	}

	public MongoClient getMongoClient() {
		return mongoClient;
	}

	public Integer getConnectionsPerHost() {
		return connectionsPerHost;
	}

	public void setConnectionsPerHost(Integer connectionsPerHost) {
		this.connectionsPerHost = connectionsPerHost;
	}

	public Integer getThreadsAllowedToBlockForConnectionMultiplier() {
		return threadsAllowedToBlockForConnectionMultiplier;
	}

	public void setThreadsAllowedToBlockForConnectionMultiplier(
			Integer threadsAllowedToBlockForConnectionMultiplier) {
		this.threadsAllowedToBlockForConnectionMultiplier = threadsAllowedToBlockForConnectionMultiplier;
	}

	public Integer getMaxWaitTime() {
		return maxWaitTime;
	}

	public void setMaxWaitTime(Integer maxWaitTime) {
		this.maxWaitTime = maxWaitTime;
	}

	public Integer getConnectTimeout() {
		return connectTimeout;
	}

	public void setConnectTimeout(Integer connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	public Integer getSocketTimeout() {
		return socketTimeout;
	}

	public void setSocketTimeout(Integer socketTimeout) {
		this.socketTimeout = socketTimeout;
	}

	public Boolean getSocketKeepAlive() {
		return socketKeepAlive;
	}

	public void setSocketKeepAlive(Boolean socketKeepAlive) {
		this.socketKeepAlive = socketKeepAlive;
	}

	public void setHosts(String hosts) {
		this.hosts = hosts;
	}

}
