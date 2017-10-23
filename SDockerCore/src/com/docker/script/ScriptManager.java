package com.docker.script;

import chat.errors.CoreException;
import chat.logs.LoggerEx;
import chat.utils.TimerEx;
import chat.utils.TimerTaskEx;
import com.docker.data.Service;
import com.docker.errors.CoreErrorCodes;
import com.docker.server.OnlineServer;
import com.docker.storage.adapters.DockerStatusService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import script.file.FileAdapter;
import script.file.FileAdapter.FileEntity;
import script.file.FileAdapter.PathEx;

import javax.annotation.Resource;
import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ScriptManager {
	private static final String TAG = ScriptManager.class.getSimpleName();

	@Resource
	private FileAdapter fileAdapter;

	private DockerStatusService dockerStatusService;

	private IRuntimeNullHandler runtimeNullHandler;

	private String remotePath;
	private String localPath;
	private ConcurrentHashMap<String, BaseRuntime> scriptRuntimeMap = new ConcurrentHashMap<>();

	private Class<?> baseRuntimeClass;

	public void init() {
//		dslFileAdapter.getFilesInDirectory(arg0)
		TimerEx.schedule(new TimerTaskEx() {
			@Override
			public void execute() {
				reload();
			}
		}, 5000, TimeUnit.SECONDS.toMillis(10));//30
	}
	
	public BaseRuntime getBaseRuntime(String service) {
		BaseRuntime runtime = scriptRuntimeMap.get(service);
//		2017-05-08 为了通用版加入当为null的时候的逻辑处理
		if (runtime == null && runtimeNullHandler != null) {
			runtime = runtimeNullHandler.getRuntime(service);
		}
		return runtime;
	}

	public Set<Map.Entry<String, BaseRuntime>> getBaseRunTimes() {
		return scriptRuntimeMap.entrySet();
	}

	private String getServiceName(String service) {
		Integer version = null;
		String versionSeperator = "_v";
		int lastIndex = service.lastIndexOf(versionSeperator);
		if(lastIndex > 0) {
			String curVerStr = service.substring(lastIndex + versionSeperator.length());
			if(curVerStr != null) {
				try {
					version = Integer.parseInt(curVerStr);
				} catch (Exception e) {}
			}
			if(version != null) {
				String serviceName = service.substring(0, lastIndex);
				return serviceName;
			}
		}
		return service;
	}

	private Integer getServiceVersion(String service) {
		Integer version = null;
		String versionSeperator = "_v";
		int lastIndex = service.lastIndexOf(versionSeperator);
		if(lastIndex > 0) {
			String curVerStr = service.substring(lastIndex + versionSeperator.length());
			if(curVerStr != null) {
				try {
					version = Integer.parseInt(curVerStr);
				} catch (Exception e) {}
			}
		}
		if(version == null)
			version = 1;
		return version;
	}

	private void reload() {
		try {
			Collection<FileEntity> files = fileAdapter.getFilesInDirectory(new PathEx(remotePath), new String[]{"zip"}, true);
			OnlineServer server = OnlineServer.getInstance();
			if(server == null) {
				LoggerEx.error(TAG, "Online server is null while reload scripts");
				return;
			}
			String serverType = server.getServerType();
			if(serverType == null) {
				LoggerEx.error(TAG, "ServerType is null while reload scripts");
				return;
			}
			if(files != null) {
			    Set<String> remoteServices = new HashSet<>();
				String serverTypePath = "/" + serverType + "/";
				for(FileEntity file : files) {
					try {
						// for example, /gateway/SS/groovy.zip
						String abPath = file.getAbsolutePath();
						int index = abPath.indexOf(serverTypePath);
						boolean createRuntime = false;
						if(index > -1) {
							String thePath = abPath.substring(index + serverTypePath.length(), abPath.length()); //SS/groovy.zip
							String[] strs = thePath.split("/");
							if(strs.length == 2) {
								String service = strs[0];
								String zipFile = strs[1];
								String language = null;
								String localScriptPath = null;

                                remoteServices.add(service);
								BaseRuntime runtime = scriptRuntimeMap.get(service);
								boolean needRedeploy = false;
								if(runtime != null && (runtime.getVersion() == null || runtime.getVersion() < file.getLastModificationTime())) {
									needRedeploy = true;
								}
								if(runtime == null) {
									createRuntime = true;
									needRedeploy = true;
									language = zipFile.substring(0, zipFile.length() - ".zip".length()).toLowerCase();
									switch(language) {
									case "groovy":
										if(baseRuntimeClass != null) {
											runtime = (BaseRuntime) baseRuntimeClass.newInstance();
										} else {
											runtime = new MyBaseRuntime();
										}
//										switch(serverType) {
//											case "gateway":
//												runtime = new GatewayGroovyRuntime();
//												break;
//											case "login":
//												runtime = new LoginGroovyRuntime();
//												break;
//											case "presence":
//												runtime = new PresenceGroovyRuntime();
//												break;
//										}
										break;
									}
									if(runtime != null) {
										localScriptPath = localPath + serverTypePath + service + "/" + language;
										runtime.setPath(localScriptPath + "/");
									}
								}
								
								if(runtime != null && needRedeploy) {
									File localZipFile = new File(localPath + serverTypePath + thePath);
									FileUtils.deleteQuietly(localZipFile);
									fileAdapter.readFile(new PathEx(file.getAbsolutePath()), FileUtils.openOutputStream(localZipFile));

									String n = localZipFile.getName();
									n = n.substring(0, n.length() - ".zip".length());
									localScriptPath = localPath + serverTypePath + service + "/" + n;
									FileUtils.deleteDirectory(new File(localScriptPath));
									unzip(localZipFile, localScriptPath);

									if(createRuntime) {
										String propertiesPath = localScriptPath + "/config.properties";
										Properties properties = null;
										File propertiesFile = new File(propertiesPath);
										if(propertiesFile.exists() && propertiesFile.isFile()) {
											properties = new Properties();
											properties.load(FileUtils.openInputStream(propertiesFile));
										}

										String minVersionStr = properties != null ? properties.getProperty("service.minversion") : null;
										Integer minVersion = null;
										if(minVersionStr != null) {
											try {
												minVersion = Integer.parseInt(minVersionStr);
											} catch (Exception e) {}
										}
										if(minVersion == null) {
											minVersion = 0;
										}
										Integer version = getServiceVersion(service);
										String serviceName = getServiceName(service);

										runtime.setServiceName(serviceName);
										runtime.setServiceVersion(version);
										runtime.prepare(service, properties, localScriptPath);

										Service theService = new Service();
										theService.setService(serviceName);
										theService.setMinVersion(minVersion);
										theService.setVersion(version);

										if(dockerStatusService != null)
											dockerStatusService.addService(OnlineServer.getInstance().getServer(), theService);

										scriptRuntimeMap.put(service, runtime);
									}

									runtime.setVersion(file.getLastModificationTime());
									runtime.redeploy();
								}
							}
						}
					} catch (Throwable t) {
						t.printStackTrace();
						LoggerEx.error(TAG, "Reload script zip file " + file.getAbsolutePath() + " failed, " + t.getMessage());
					}
				}

                Collection<String> keys = scriptRuntimeMap.keySet();
                for(String key : keys) {
				    if(!remoteServices.contains(key)) {
                        BaseRuntime runtime = scriptRuntimeMap.remove(key);
                        if(runtime != null) {
                            LoggerEx.info(TAG, "Service " + key + " is going to be removed, because it is not found in remote.");
                            try {
                                runtime.close();
                            } catch(Throwable t) {
                                t.printStackTrace();
                            } finally {
                                try {
                                	if(dockerStatusService != null) {
										String serviceName = getServiceName(key);
										Integer version = getServiceVersion(key);
										dockerStatusService.deleteService(OnlineServer.getInstance().getServer(), serviceName, version);
									}
                                } catch (CoreException e) {
                                    e.printStackTrace();
                                    LoggerEx.error(TAG, "Delete service " + key + " from docker " + OnlineServer.getInstance().getServer() + " failed, " + e.getMessage());
                                }
                            }
                        }
                    }
                }
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void unzip(File file, String dir) throws CoreException {
		try {
			FileUtils.forceMkdir(new File(dir));
			
			ZipFile zipFile = new ZipFile(file);
			try {
			  Enumeration<? extends ZipEntry> entries = zipFile.entries();
			  while (entries.hasMoreElements()) {
			    ZipEntry entry = entries.nextElement();
			    File entryDestination = new File(dir,  entry.getName());
			    if (entry.isDirectory()) {
			        entryDestination.mkdirs();
			    } else {
			        entryDestination.getParentFile().mkdirs();
			        InputStream in = zipFile.getInputStream(entry);
			        OutputStream out = new FileOutputStream(entryDestination);
			        IOUtils.copy(in, out);
			        IOUtils.closeQuietly(in);
			        out.close();
			    }
			  }
			} finally {
			  zipFile.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new CoreException(CoreErrorCodes.ERROR_SCRIPT_UNZIP_FAILED, "Groovy zip " + file.getAbsolutePath() + " unzip failed, " + e.getMessage());
		}
	}

	public String getRemotePath() {
		return remotePath;
	}

	public void setRemotePath(String remotePath) {
		this.remotePath = remotePath;
	}

	public String getLocalPath() {
		return localPath;
	}

	public void setLocalPath(String localPath) {
		this.localPath = localPath;
	}

	public IRuntimeNullHandler getRuntimeNullHandler() {
		return runtimeNullHandler;
	}

	public void setRuntimeNullHandler(IRuntimeNullHandler runtimeNullHandler) {
		this.runtimeNullHandler = runtimeNullHandler;
	}

	public DockerStatusService getDockerStatusService() {
		return dockerStatusService;
	}

	public void setDockerStatusService(DockerStatusService dockerStatusService) {
		this.dockerStatusService = dockerStatusService;
	}

	public Class<?> getBaseRuntimeClass() {
		return baseRuntimeClass;
	}

	public void setBaseRuntimeClass(Class<?> baseRuntimeClass) {
		this.baseRuntimeClass = baseRuntimeClass;
	}
}
