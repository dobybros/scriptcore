package com.docker.rpc;

import chat.errors.ChatErrorCodes;
import chat.errors.CoreException;
import chat.logs.LoggerEx;
import chat.utils.GZipUtils;
import com.alibaba.fastjson.JSON;
import com.docker.rpc.remote.MethodMapping;
import com.docker.rpc.remote.stub.ServiceStubManager;
import org.apache.commons.io.IOUtils;

import java.io.*;

public class MethodResponse extends RPCResponse {
	private static final String TAG = MethodResponse.class.getSimpleName();
	private byte version = 1;
	private Long crc;
	private Object returnObject;
	private CoreException exception;

	public static final String FIELD_RETURN = "return";
	public static final String FIELD_ERROR = "error";

	public MethodResponse() {
		super(MethodRequest.RPCTYPE);
	}

	public MethodResponse(Object returnObj, CoreException exception) {
		super(MethodRequest.RPCTYPE);
		this.returnObject = returnObj;
		this.exception = exception;
	}

	public String toString() {
		StringBuilder builder = new StringBuilder(MethodResponse.class.getSimpleName());
//		builder.append(": ").append(server);
		return builder.toString();
	}

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
						if(crc == null || crc == 0 || crc == -1)
							throw new CoreException(ChatErrorCodes.ERROR_METHODREQUEST_CRC_ILLEGAL, "CRC is illegal for MethodRequest");


						MethodMapping methodMapping = ServiceStubManager.getInstance().getMethodMapping(crc);
						if(methodMapping == null)
							throw new CoreException(ChatErrorCodes.ERROR_METHODREQUEST_METHODNOTFOUND, "Method doesn't be found by crc " + crc);

						int returnLength = dis.readInt();
						byte[] returnBytes = new byte[returnLength];
						dis.readFully(returnBytes);
						try {
							if(returnBytes.length > 0) {
								byte[] data = GZipUtils.decompress(returnBytes);
								String json = new String(data, "utf8");
								returnObject = JSON.parseObject(json, methodMapping.getReturnClass());
							}
						} catch (IOException e) {
							e.printStackTrace();
							LoggerEx.error(TAG, "Parse return bytes failed, " + e.getMessage());
						}

						int execeptionLength = dis.readInt();
						byte[] exceptionBytes = new byte[execeptionLength];
						dis.readFully(exceptionBytes);
						try {
							if(exceptionBytes.length > 0) {
								byte[] data = GZipUtils.decompress(exceptionBytes);
								String json = new String(data, "utf8");
								exception = JSON.parseObject(json, CoreException.class);
							}
						} catch (IOException e) {
							e.printStackTrace();
							LoggerEx.error(TAG, "Parse exception bytes failed, " + e.getMessage());
						}
					} catch (Throwable e) {
						e.printStackTrace();
						throw new CoreException(ChatErrorCodes.ERROR_RPC_ENCODE_FAILED, "PB parse data failed, " + e.getMessage());
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

				byte[] returnBytes = null;
				if(returnObject != null) {
					String returnStr = JSON.toJSONString(returnObject);
					try {
						returnBytes = GZipUtils.compress(returnStr.getBytes("utf8"));
					} catch (IOException e) {
						e.printStackTrace();
						LoggerEx.error(TAG, "Generate return " + returnStr + " to bytes failed, " + e.getMessage());
					}
				}
				if(returnBytes != null) {
					dis.writeInt(returnBytes.length);
					dis.write(returnBytes);
				} else {
					dis.writeInt(0);
				}

				byte[] exceptionBytes = null;
				if(exception != null) {
					String errorStr = JSON.toJSONString(exception);
					try {
						exceptionBytes = GZipUtils.compress(errorStr.getBytes("utf8"));
					} catch (IOException e) {
						e.printStackTrace();
						LoggerEx.error(TAG, "Generate error " + errorStr + " to bytes failed, " + e.getMessage());
					}
				}
				if(exceptionBytes != null) {
					dis.writeInt(exceptionBytes.length);
					dis.write(exceptionBytes);
				} else {
					dis.writeInt(0);
				}

				byte[] bytes = baos.toByteArray();
				setData(bytes);
				setEncode(ENCODE_JAVABINARY);
				setType(MethodRequest.RPCTYPE);
			} catch (Throwable t) {
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

	public Object getReturnObject() {
		return returnObject;
	}

	public void setReturnObject(Object returnObject) {
		this.returnObject = returnObject;
	}

	public CoreException getException() {
		return exception;
	}

	public void setException(CoreException exception) {
		this.exception = exception;
	}

	public byte getVersion() {
		return version;
	}

	public void setVersion(byte version) {
		this.version = version;
	}
}
