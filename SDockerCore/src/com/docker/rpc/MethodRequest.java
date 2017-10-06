package com.docker.rpc;

import chat.errors.ChatErrorCodes;
import chat.errors.CoreException;
import chat.logs.LoggerEx;
import chat.utils.GZipUtils;
import com.alibaba.fastjson.JSON;
import com.docker.rpc.remote.MethodMapping;
import com.docker.rpc.remote.skeleton.ServiceSkeletonAnnotationHandler;
import com.docker.rpc.remote.stub.ServiceStubManager;
import com.docker.script.BaseRuntime;
import com.docker.script.MyBaseRuntime;
import com.docker.script.ScriptManager;
import com.docker.utils.SpringContextUtil;
import org.apache.commons.io.IOUtils;

import javax.xml.ws.Service;
import java.io.*;
import java.util.List;

public class MethodRequest extends RPCRequest {
	public static final String RPCTYPE = "mthd";
    private static final String TAG = MethodRequest.class.getSimpleName();
    private byte version = 1;

    /**
	 * generated from classname and method name and parameters
	 */
	private Long crc;

	private String service;

    private Object[] args;

    private Integer argCount;

    /**
     * 只用于内存, 不错传输序列化
     */
    private String fromService;

	public MethodRequest() {
		super(RPCTYPE);
	}

//    public Object invoke() {
//        return methodMapping.invoke(args);
//    }

	@Override
	public void resurrect() throws CoreException {
		byte[] bytes = getData();
		Byte encode = getEncode();
		if(bytes != null) {
			if(encode != null) {
				switch(encode) {
				case ENCODE_JAVABINARY:
                    ByteArrayInputStream bais = null;
                    DataInputStream dis = null;
					try {
					    bais = new ByteArrayInputStream(bytes);
                        dis = new DataInputStream(bais);
                        version = dis.readByte();
                        crc = dis.readLong();
                        service = dis.readUTF();
                        if(crc == null || crc == 0 || crc == -1)
                            throw new CoreException(ChatErrorCodes.ERROR_METHODREQUEST_CRC_ILLEGAL, "CRC is illegal for MethodRequest");

                        if(service == null)
                            throw new CoreException(ChatErrorCodes.ERROR_METHODREQUEST_SERVICE_NULL, "Service is null for crc " + crc);
                        ScriptManager scriptManager = (ScriptManager) SpringContextUtil.getBean("scriptManager");
                        BaseRuntime baseRuntime = scriptManager.getBaseRuntime(service);
                        if(baseRuntime == null)
                            throw new CoreException(ChatErrorCodes.ERROR_METHODREQUEST_SERVICE_NOTFOUND, "Service " + service + " not found for crc " + crc);
                        ServiceSkeletonAnnotationHandler serviceSkeletonAnnotationHandler = (ServiceSkeletonAnnotationHandler) baseRuntime.getClassAnnotationHandler(ServiceSkeletonAnnotationHandler.class);
                        if(serviceSkeletonAnnotationHandler == null)
                            throw new CoreException(ChatErrorCodes.ERROR_METHODREQUEST_SKELETON_NULL, "Skeleton handler is not for service " + service + " on method crc " + crc);
                        MethodMapping methodMapping = serviceSkeletonAnnotationHandler.getMethodMapping(crc);
                        if(methodMapping == null)
                            throw new CoreException(ChatErrorCodes.ERROR_METHODREQUEST_METHODNOTFOUND, "Method doesn't be found by crc " + crc);

                        argCount = dis.readInt();
                        Integer length = dis.readInt();
                        byte[] argsData = new byte[length];
                        dis.readFully(argsData);

                        Class<?>[] parameterTypes = methodMapping.getParameterTypes();
						if(parameterTypes != null && parameterTypes.length > 0) {
                            if(parameterTypes.length > argCount) {
                                LoggerEx.debug(TAG, "Parameter types not equal actual is " + parameterTypes.length + " but expected " + argCount + ". Cut off");
                                Class<?>[] newParameterTypes = new Class<?>[argCount];
                                System.arraycopy(parameterTypes, 0, newParameterTypes, 0, argCount);
                                parameterTypes = newParameterTypes;
                            } else if(parameterTypes.length < argCount){
                                LoggerEx.debug(TAG, "Parameter types not equal actual is " + parameterTypes.length + " but expected " + argCount + ". Fill with Object.class");
                                Class<?>[] newParameterTypes = new Class<?>[argCount];
                                for(int i = parameterTypes.length; i < argCount; i++) {
                                    newParameterTypes[i] = Object.class;
                                }
                                parameterTypes = newParameterTypes;
                            }
							try {
                                byte[] rawData = argsData;
                                if(rawData.length > 0) {
                                    byte[] data = GZipUtils.decompress(rawData);
                                    String json = new String(data, "utf8");
                                    List<Object> array = JSON.parseArray(json, parameterTypes);
                                    if(array != null)
                                        args = array.toArray();
                                }
							} catch (IOException e) {
								e.printStackTrace();
                                LoggerEx.error(TAG, "Parse bytes failed, " + e.getMessage());
							}
						}
					} catch (Throwable e) {
						e.printStackTrace();
						throw new CoreException(ChatErrorCodes.ERROR_RPC_DECODE_FAILED, "PB parse data failed, " + e.getMessage());
					} finally {
					    IOUtils.closeQuietly(bais);
					    IOUtils.closeQuietly(dis);
                    }
                    break;
					default:
						throw new CoreException(ChatErrorCodes.ERROR_RPC_ENCODER_NOTFOUND, "Encoder type doesn't be found for resurrect");
				}
			}
		}
	}

	@Override
	public void persistent() throws CoreException {
		Byte encode = getEncode();
		if(encode == null)
			throw new CoreException(ChatErrorCodes.ERROR_RPC_ENCODER_NULL, "Encoder is null for persistent");
		switch(encode) {
		case ENCODE_JAVABINARY:
            ByteArrayOutputStream baos = null;
            DataOutputStream dis = null;
		    try {
                baos = new ByteArrayOutputStream();
                dis = new DataOutputStream(baos);
                dis.writeByte(version);
                dis.writeLong(crc);
                dis.writeUTF(service);

                ScriptManager scriptManager = (ScriptManager) SpringContextUtil.getBean("scriptManager");
                MyBaseRuntime baseRuntime = (MyBaseRuntime) scriptManager.getBaseRuntime(fromService);
                if(baseRuntime == null)
                    throw new CoreException(ChatErrorCodes.ERROR_METHODREQUEST_SERVICE_NOTFOUND, "Service " + service + " not found for crc " + crc);

                MethodMapping methodMapping = baseRuntime.getServiceStubManager().getMethodMapping(crc);
                if(methodMapping == null)
                    throw new CoreException(ChatErrorCodes.ERROR_METHODREQUEST_METHODNOTFOUND, "Method doesn't be found by crc " + crc);
                Class<?>[] parameterTypes = methodMapping.getParameterTypes();
                if(parameterTypes != null) {
                    argCount = parameterTypes.length;
                } else {
                    argCount = 0;
                }
                dis.writeInt(argCount);
                if(args != null) {
                    String json = JSON.toJSONString(args);
                    try {
                        byte[] data = GZipUtils.compress(json.getBytes("utf8"));
                        dis.writeInt(data.length);
                        dis.write(data);
                    } catch (IOException e) {
                        e.printStackTrace();
                        LoggerEx.error(TAG, "Generate " + json + " to bytes failed, " + e.getMessage());
                    }
                }

                byte[] bytes = baos.toByteArray();
                setData(bytes);
                setEncode(ENCODE_JAVABINARY);
                setType(RPCTYPE);
            } catch(Throwable t) {
		        t.printStackTrace();
                throw new CoreException(ChatErrorCodes.ERROR_RPC_ENCODE_FAILED, "PB parse data failed, " + t.getMessage());
            } finally {
                IOUtils.closeQuietly(baos);
                IOUtils.closeQuietly(dis);
            }
            break;
			default:
				throw new CoreException(ChatErrorCodes.ERROR_RPC_ENCODER_NOTFOUND, "Encoder type doesn't be found for persistent");
		}
	}

    public Long getCrc() {
        return crc;
    }

    public void setCrc(Long crc) {
        this.crc = crc;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    public Integer getArgCount() {
        return argCount;
    }

    public void setArgCount(Integer argCount) {
        this.argCount = argCount;
    }

    public byte getVersion() {
        return version;
    }

    public void setVersion(byte version) {
        this.version = version;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getFromService() {
        return fromService;
    }

    public void setFromService(String fromService) {
        this.fromService = fromService;
    }
}
