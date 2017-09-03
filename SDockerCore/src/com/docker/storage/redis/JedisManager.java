package com.docker.storage.redis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import com.alibaba.fastjson.JSON;
import org.springframework.core.io.ClassPathResource;

import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;
import chat.errors.CoreException;
import chat.logs.LoggerEx;

import com.docker.errors.CoreErrorCodes;

/**
 * @author wangrongxuan
 * @version 1.1
 * @Description: 读写redis。 要求： 1、配置文件名字为redis.properties，放在根目录下;
 *               2、redis.properties中必须包含redis.pool字段，指定redis地址。
 *               3、redis.pool格式为host
 *               :port。如果有多个pool，用逗号分隔，如host1:port1,host2:port2
 * @create time 2016-3-8 下午3:28:32
 */
public class JedisManager {

	private static final String TAG = JedisManager.class.getSimpleName();
	private static ShardedJedisPool pool = null;

	static {
		LoggerEx.info(TAG, "JedisPool初始化开始");
		JedisPoolConfig config = new JedisPoolConfig();
		config.setMaxTotal(100);// 最大连接数
		config.setMaxIdle(20);// 最大空闲实例数
		config.setMaxWaitMillis(30000L);// 最长等待时间
		config.setTestOnBorrow(true);// 在borrow一个jedis实例时，是否进行有效性检查。为true，则得到的jedis实例均是可用的

		ClassPathResource resource = new ClassPathResource("redis.properties");
		Properties pro = new Properties();
		try {
			pro.load(resource.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
			LoggerEx.error(TAG,
					"Prepare redis.properties is failed, " + e.getMessage());
		}
		String[] strArray = pro.getProperty("redis.pool").split(",");// redis.properties中必须包含redis.pool字段，指定redis地址。如果有多个，用逗号分隔。
		List<JedisShardInfo> shardJedis = new ArrayList<JedisShardInfo>();
		for (int i = 0; i < strArray.length; i++) {
			if (strArray[i].indexOf(":") > 0) {
				String host = strArray[i].trim().substring(0,
						strArray[i].indexOf(":"));
				int port = Integer.parseInt(strArray[i].substring(strArray[i]
						.indexOf(":") + 1));
				shardJedis.add(new JedisShardInfo(host, port));
			} else {
				shardJedis.add(new JedisShardInfo(strArray[i]));
			}
		}
		pool = new ShardedJedisPool(config, shardJedis);
		LoggerEx.info(TAG, "JedisPool初始化完成，连接到" + pro.getProperty("redis.pool"));
	}

	private JedisManager() {
	}

	/**
	 * 将哈希表 key 中的域 field 的值设置为 value ，当且仅当域 field 不存在。

		若域 field 已经存在，该操作无效。
		
		如果 key 不存在，一个新哈希表被创建并执行 HSETNX 命令。
		
		时间复杂度：
		O(1)
		返回值：
		设置成功，返回 1 。
		如果给定域已经存在且没有操作被执行，返回 0 。
	 * 
	 * @param key
	 * @param field
	 * @param value
	 * @return
	 * @throws CoreException
	 */
	public static Long hsetnx(String key, String field, String value)
			throws CoreException {
		ShardedJedis jedis = null;
		try {
			jedis = pool.getResource();
			return jedis.hsetnx(key, field, value);
		} catch (Throwable e) {
			e.printStackTrace();
			// LoggerEx.error(TAG, "redis保存异常 " + e.getMessage());
			throw new CoreException(CoreErrorCodes.ERROR_REDIS, "hsetnx " + key
					+ " " + field + " " + value + " failed, " + e.getMessage());
		} finally {
			if (jedis != null)
				jedis.close();
		}
	}

	/**
	 * 将哈希表 key 中的域 field 的值设为 value 。
		
		如果 key 不存在，一个新的哈希表被创建并进行 HSET 操作。
		
		如果域 field 已经存在于哈希表中，旧值将被覆盖。
		
		时间复杂度：
		O(1)
		返回值：
		如果 field 是哈希表中的一个新建域，并且值设置成功，返回 1 。
		如果哈希表中域 field 已经存在且旧值已被新值覆盖，返回 0 。
	 * 
	 * @param key
	 * @param field
	 * @param value
	 * @return
	 * @throws CoreException
	 */
	public static Long hset(String key, String field, String value)
			throws CoreException {
		ShardedJedis jedis = null;
		try {
			jedis = pool.getResource();
			return jedis.hset(key, field, value);
		} catch (Throwable e) {
			e.printStackTrace();
			// LoggerEx.error(TAG, "redis保存异常 " + e.getMessage());
			throw new CoreException(CoreErrorCodes.ERROR_REDIS, "hset " + key
					+ " " + field + " " + value + " failed, " + e.getMessage());
		} finally {
			if (jedis != null)
				jedis.close();
		}
	}

	/**
	 * 
	 * 删除哈希表 key 中的一个或多个指定域，不存在的域将被忽略。
		时间复杂度:
		O(N)， N 为要删除的域的数量。
		返回值:
		被成功移除的域的数量，不包括被忽略的域。
	 * 
	 * @param key
	 * @param field
	 * @return
	 * @throws CoreException
	 */
	public static Long hdel(String key, String... fields) throws CoreException {
		ShardedJedis jedis = null;
		try {
			jedis = pool.getResource();
			return jedis.hdel(key, fields);
		} catch (Throwable e) {
			e.printStackTrace();
			// LoggerEx.error(TAG, "redis保存异常 " + e.getMessage());
			throw new CoreException(CoreErrorCodes.ERROR_REDIS, "hdel " + key
					+ " " + Arrays.toString(fields) + " failed, " + e.getMessage());
		} finally {
			if (jedis != null)
				jedis.close();
		}
	}

	/**
	 * 返回哈希表 key 中给定域 field 的值。

		时间复杂度：
		O(1)
		返回值：
		给定域的值。
		当给定域不存在或是给定 key 不存在时，返回 nil 。
	 * 
	 * @param key
	 * @param field
	 * @return
	 * @throws CoreException
	 */
	public static String hget(String key, String field) throws CoreException {
		ShardedJedis jedis = null;
		try {
			jedis = pool.getResource();
			return jedis.hget(key, field);
		} catch (Throwable e) {
			e.printStackTrace();
			// LoggerEx.error(TAG, "redis保存异常 " + e.getMessage());
			throw new CoreException(CoreErrorCodes.ERROR_REDIS, "hget " + key
					+ " " + field + " failed, " + e.getMessage());
		} finally {
			if (jedis != null)
				jedis.close();
		}
	}
	public static Long hsetObject(String key, String field, Object obj) throws CoreException {
		String jsonStr = JSON.toJSONString(obj);
		return hset(key, field, jsonStr);
	}

	public static Long hsetnxObject(String key, String field, Object obj) throws CoreException {
		String jsonStr = JSON.toJSONString(obj);
		return hsetnx(key, field, jsonStr);
	}

	public static <T> T hgetObject(String key, String field, Class<T> clazz) throws CoreException {
		String value = hget(key, field);
		if(value != null) {
			try {
				return JSON.parseObject(value, clazz);
			} catch(Throwable t) {
				LoggerEx.warn(TAG, "Value " + value + " is not  json format, return null for key " + key + " field " + field + ", error " + t.getMessage());
				return null;
			}
		}
		return null;
	}
}