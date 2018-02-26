package com.docker.storage.redis;

import chat.errors.CoreException;
import chat.logs.LoggerEx;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.docker.errors.CoreErrorCodes;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author wangrongxuan
 * @version 1.1
 * @Description: 读写redis。 要求： 1、配置文件名字为redis.properties，放在根目录下;
 *               2、redis.properties中必须包含redis.pool字段，指定redis地址。
 *               3、redis.pool格式为host
 *               :port。如果有多个pool，用逗号分隔，如host1:port1,host2:port2
 * @create time 2016-3-8 下午3:28:32
 */
public class RedisHandler {

	private final String TAG = RedisHandler.class.getSimpleName();
	private ShardedJedisPool pool = null;

	private String hosts;

	public RedisHandler(String hosts) {
		this.hosts = hosts;
	}

	public void connect() {
		disconnect();

		LoggerEx.info(TAG, "JedisPool initializing...");
		JedisPoolConfig config = new JedisPoolConfig();
		config.setMaxTotal(100);// 最大连接数
		config.setMaxIdle(20);// 最大空闲实例数
		config.setMaxWaitMillis(30000L);// 最长等待时间
		config.setTestOnBorrow(true);// 在borrow一个jedis实例时，是否进行有效性检查。为true，则得到的jedis实例均是可用的

		String[] strArray = hosts.split(",");// redis.properties中必须包含redis.pool字段，指定redis地址。如果有多个，用逗号分隔。
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
		LoggerEx.info(TAG, "JedisPool connected, " + hosts);
	}

	public void disconnect() {
		if(pool != null && pool.isClosed()) {
			pool.close();
			LoggerEx.info(TAG, "JedisPool closed, " + hosts);
		}
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
	public Long hsetnx(String key, String field, String value)
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
	public Long hset(String key, String field, String value)
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
	 * @param fields
	 * @return
	 * @throws CoreException
	 */
	public Long hdel(String key, String... fields) throws CoreException {
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
	public String hget(String key, String field) throws CoreException {
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
	public Long hsetObject(String key, String field, Object obj) throws CoreException {
		String jsonStr = JSON.toJSONString(obj);
		return hset(key, field, jsonStr);
	}

	public Long hsetnxObject(String key, String field, Object obj) throws CoreException {
		String jsonStr = JSON.toJSONString(obj);
		return hsetnx(key, field, jsonStr);
	}

	public <T> T hgetObject(String key, String field, Class<T> clazz) throws CoreException {
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

	public JSONObject hgetJsonObject(String key, String field) throws CoreException {
		String value = hget(key, field);
		if(value != null) {
			try {
				return JSON.parseObject(value);
			} catch(Throwable t) {
				LoggerEx.warn(TAG, "Value " + value + " is not  json format, return null for key " + key + " field " + field + ", error " + t.getMessage());
				return null;
			}
		}
		return null;
	}

	/**
	 * 获取哈希表中字段的数量
	 * O(1)
	 */
	public Long hlen(String key) throws CoreException {
		ShardedJedis jedis = null;
		try {
			jedis = pool.getResource();
			return jedis.hlen(key);
		} catch (Throwable e) {
			e.printStackTrace();
			// LoggerEx.error(TAG, "redis保存异常 " + e.getMessage());
			throw new CoreException(CoreErrorCodes.ERROR_REDIS, "hlen " + key
					+ " failed, " + e.getMessage());
		} finally {
			if (jedis != null)
				jedis.close();
		}
	}

	public String setObject(String prefix, String key, Object obj, String nxxx, String expx, long time) throws CoreException {
		return setObject(prefix + "_" + key, obj, nxxx, expx, time);
	}

	public String setObject(String key, Object obj, String nxxx, String expx, long time) throws CoreException {
		ShardedJedis jedis = null;
		try {
			jedis = pool.getResource();
			String jsonStr = JSON.toJSONString(obj);
			return jedis.set(key, jsonStr, nxxx, expx, time);
		} catch (Throwable e) {
			e.printStackTrace();
			// LoggerEx.error(TAG, "redis保存异常 " + e.getMessage());
			throw new CoreException(CoreErrorCodes.ERROR_REDIS, "setObject " + key
					+ " " + obj + " " + nxxx + " " + expx + " " + time + " failed, " + e.getMessage());
		} finally {
			if (jedis != null)
				jedis.close();
		}
	}

	public <T> T getObject(String prefix, String key, Class<T> clazz) throws CoreException {
		return getObject(prefix + "_" + key, clazz);
	}

	public <T> T getObject(String key, Class<T> clazz) throws CoreException {
		ShardedJedis jedis = null;
		String json = null;
		try {
			jedis = pool.getResource();
			json = jedis.get(key);
		} catch (Throwable e) {
			e.printStackTrace();
			// LoggerEx.error(TAG, "redis保存异常 " + e.getMessage());
			throw new CoreException(CoreErrorCodes.ERROR_REDIS, "get " + key
					+ " " + clazz + " failed, " + e.getMessage());
		} finally {
			if (jedis != null)
				jedis.close();
		}

		if(json != null) {
			try {
				return JSON.parseObject(json, clazz);
			} catch(Throwable t) {
				LoggerEx.warn(TAG, "Value " + json + " is not  json format, return null for key " + key + " class " + clazz + ", error " + t.getMessage());
				return null;
			}
		}
		return null;
	}

	public Long expire(String prefix, String key, long expire) throws CoreException {
		return expire(prefix + "_" + key, expire);
	}

	public Long expire(String key, long expire) throws CoreException {
		ShardedJedis jedis = null;
		try {
			jedis = pool.getResource();
			return jedis.pexpire(key, expire);
		} catch (Throwable e) {
			e.printStackTrace();
			// LoggerEx.error(TAG, "redis保存异常 " + e.getMessage());
			throw new CoreException(CoreErrorCodes.ERROR_REDIS, "expire " + key
					+ " " + expire + " failed, " + e.getMessage());
		} finally {
			if (jedis != null)
				jedis.close();
		}
	}

	/**
	 *
	 * 将一个对象插入到列表头部
	 *
	 * @param key
	 * @param obj
	 * @return
	 * @throws CoreException
	 */
	public Long lpushObject(String key, Object obj) throws CoreException {
		ShardedJedis jedis = null;
		try {
			jedis = pool.getResource();
			String jsonStr = JSON.toJSONString(obj);
			return jedis.lpush(key, jsonStr);
		} catch (Throwable e) {
			e.printStackTrace();
			// LoggerEx.error(TAG, "redis保存异常 " + e.getMessage());
			throw new CoreException(CoreErrorCodes.ERROR_REDIS, "lpushObject " + key
					+ " " + JSON.toJSONString(obj) + " failed, " + e.getMessage());
		} finally {
			if (jedis != null)
				jedis.close();
		}
	}

	/**
	 *
	 * 将一个字符串插入到列表头部
	 *
	 * @param key
	 * @param value
	 * @return
	 * @throws CoreException
	 */
	public Long lpush(String key, String value) throws CoreException {
		ShardedJedis jedis = null;
		try {
			jedis = pool.getResource();
			return jedis.lpush(key, value);
		} catch (Throwable e) {
			e.printStackTrace();
			// LoggerEx.error(TAG, "redis保存异常 " + e.getMessage());
			throw new CoreException(CoreErrorCodes.ERROR_REDIS, "lpush " + key
					+ " " + value + " failed, " + e.getMessage());
		} finally {
			if (jedis != null)
				jedis.close();
		}
	}

	/**
	 *
	 * 将一个对象从列表头部取出
	 *
	 * @param key
	 * @param clazz 获取类的类型
	 * @return
	 * @throws CoreException
	 */
	public <T> T lpopObject(String key, Class<T> clazz) throws CoreException {
		ShardedJedis jedis = null;
		try {
			jedis = pool.getResource();
			String value = jedis.lpop(key);
			return JSON.parseObject(value, clazz);
		} catch (Throwable e) {
			e.printStackTrace();
			// LoggerEx.error(TAG, "redis保存异常 " + e.getMessage());
			throw new CoreException(CoreErrorCodes.ERROR_REDIS, "lpopObject " + key
					+ " " + key + " failed, " + e.getMessage());
		} finally {
			if (jedis != null)
				jedis.close();
		}
	}

	/**
	 *
	 * 将一个字符串从列表头部取出
	 *
	 * @param key
	 * @return
	 * @throws CoreException
	 */
	public String lpop(String key) throws CoreException {
		ShardedJedis jedis = null;
		try {
			jedis = pool.getResource();
			return jedis.lpop(key);
		} catch (Throwable e) {
			e.printStackTrace();
			// LoggerEx.error(TAG, "redis保存异常 " + e.getMessage());
			throw new CoreException(CoreErrorCodes.ERROR_REDIS, "lpop " + key
					+ " " + key + " failed, " + e.getMessage());
		} finally {
			if (jedis != null)
				jedis.close();
		}
	}

	/**
	 *
	 * 将一个对象从列表尾部取出
	 *
	 * @param key
	 * @param obj
	 * @return
	 * @throws CoreException
	 */
	public Long rpushObject(String key, Object obj) throws CoreException {
		ShardedJedis jedis = null;
		try {
			jedis = pool.getResource();
			String jsonStr = JSON.toJSONString(obj);
			return jedis.rpush(key, jsonStr);
		} catch (Throwable e) {
			e.printStackTrace();
			// LoggerEx.error(TAG, "redis保存异常 " + e.getMessage());
			throw new CoreException(CoreErrorCodes.ERROR_REDIS, "rpushObject " + key
					+ " " + " failed, " + e.getMessage());
		} finally {
			if (jedis != null)
				jedis.close();
		}
	}

	/**
	 *
	 * 将一个字符串从列表尾部取出
	 *
	 * @param key
	 * @return
	 * @throws CoreException
	 */
	public Long rpush(String key, String value) throws CoreException {
		ShardedJedis jedis = null;
		try {
			jedis = pool.getResource();
			return jedis.rpush(key, value);
		} catch (Throwable e) {
			e.printStackTrace();
			// LoggerEx.error(TAG, "redis保存异常 " + e.getMessage());
			throw new CoreException(CoreErrorCodes.ERROR_REDIS, "rpush " + key
					+ " " + " failed, " + e.getMessage());
		} finally {
			if (jedis != null)
				jedis.close();
		}
	}

	/**
	 *
	 * 将一个对象从列表尾部取出
	 *
	 * @param key
	 * @param clazz
	 * @return
	 * @throws CoreException
	 */
	public  <T> T rpopObject(String key, Class<T> clazz) throws CoreException {
		ShardedJedis jedis = null;
		try {
			jedis = pool.getResource();
			String value = jedis.rpop(key);
			return JSON.parseObject(value, clazz);
		} catch (Throwable e) {
			e.printStackTrace();
			// LoggerEx.error(TAG, "redis保存异常 " + e.getMessage());
			throw new CoreException(CoreErrorCodes.ERROR_REDIS, "rpopObject " + key
					+ " " + " failed, " + e.getMessage());
		} finally {
			if (jedis != null)
				jedis.close();
		}
	}

	/**
	 *
	 * 将一个字符串从列表尾部取出
	 *
	 * @param key
	 * @return
	 * @throws CoreException
	 */
	public String rpop(String key) throws CoreException {
		ShardedJedis jedis = null;
		try {
			jedis = pool.getResource();
			return jedis.rpop(key);
		} catch (Throwable e) {
			e.printStackTrace();
			// LoggerEx.error(TAG, "redis保存异常 " + e.getMessage());
			throw new CoreException(CoreErrorCodes.ERROR_REDIS, "rpop " + key
					+ " " + " failed, " + e.getMessage());
		} finally {
			if (jedis != null)
				jedis.close();
		}
	}

	/**
	 * 获取list长度
	 * @param key
	 * @return
	 * @throws CoreException
	 */
	public Long llen(String key) throws CoreException {
		ShardedJedis jedis = null;
		try {
			jedis = pool.getResource();
			return jedis.llen(key);
		} catch (Throwable e) {
			e.printStackTrace();
			// LoggerEx.error(TAG, "redis保存异常 " + e.getMessage());
			throw new CoreException(CoreErrorCodes.ERROR_REDIS, "llen " + key
					+ " " + " failed, " + e.getMessage());
		} finally {
			if (jedis != null)
				jedis.close();
		}
	}

	/**
	 * 获取list中string对象
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 * @throws CoreException
	 */
	public List<String> lrange(String key, long start, long end) throws CoreException {
		ShardedJedis jedis = null;
		try {
			jedis = pool.getResource();
			return jedis.lrange(key, start, end);
		} catch (Throwable e) {
			e.printStackTrace();
			// LoggerEx.error(TAG, "redis保存异常 " + e.getMessage());
			throw new CoreException(CoreErrorCodes.ERROR_REDIS, "lrange " + key
					+ " " + start + " " + end + " failed, " + e.getMessage());
		} finally {
			if (jedis != null)
				jedis.close();
		}
	}

	/**
	 * 获取list中object对象
	 * @param key
	 * @param start
	 * @param end
	 * @param clazz
	 * @param <T>
	 * @return
	 * @throws CoreException
	 */
	public <T> List<T> lrange(String key, long start, long end, Class<T> clazz) throws CoreException {
		ShardedJedis jedis = null;
		List<String> redisResult;
		try {
			jedis = pool.getResource();
			redisResult = jedis.lrange(key, start, end);
		} catch (Throwable e) {
			e.printStackTrace();
			// LoggerEx.error(TAG, "redis保存异常 " + e.getMessage());
			throw new CoreException(CoreErrorCodes.ERROR_REDIS, "lrange object " + key
					+ " " + start + " " + end + " failed, " + e.getMessage());
		} finally {
			if (jedis != null)
				jedis.close();
		}

		if(redisResult != null) {
			try {
				List<T> T = new ArrayList<T>();
				for (String json : redisResult) {
					T.add(JSON.parseObject(json, clazz));
				}
				return T;
			} catch(Throwable t) {
				LoggerEx.warn(TAG, "Value " + redisResult + " is not  json format, return null for key " + key + " class " + clazz + ", error " + t.getMessage());
				return null;
			}
		}
		return null;
	}


	public String ltrim(String key, long start, long end) throws CoreException {
		ShardedJedis jedis = null;
		try {
			jedis = pool.getResource();
			return jedis.ltrim(key, start, end);
		} catch (Throwable e) {
			e.printStackTrace();
			// LoggerEx.error(TAG, "redis保存异常 " + e.getMessage());
			throw new CoreException(CoreErrorCodes.ERROR_REDIS, "ltrim " + key
					+ " " + start + " " + end + " failed, " + e.getMessage());
		} finally {
			if (jedis != null)
				jedis.close();
		}
	}

}
