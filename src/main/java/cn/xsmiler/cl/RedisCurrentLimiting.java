package cn.xsmiler.cl;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import com.google.common.io.Files;

import cn.xsmiler.cl.api.ICurrentLimiting;
import cn.xsmiler.cl.common.Constants;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisSlotBasedConnectionHandler;
import redis.clients.util.JedisClusterCRC16;

public class RedisCurrentLimiting implements ICurrentLimiting {

	private JedisSlotBasedConnectionHandler connectionHandler;
	private Map<String, String> clKeys = new ConcurrentHashMap<>();
	private Map<String, JedisPool> jedisPools = new ConcurrentHashMap<>();
	
	@Override
	public void init(Set<HostAndPort> jedisClusterNode, GenericObjectPoolConfig poolConfig) throws Exception {
		
		for (HostAndPort hostAndPort : jedisClusterNode) {
			JedisPool pool = new JedisPool(poolConfig, hostAndPort.getHost(), hostAndPort.getPort());
			jedisPools.put(hostAndPort.getHost() + "-" + hostAndPort.getPort(), pool);
			Jedis jedis = pool.getResource();
			String luaScript = Files.toString(new File("src/main/resources/cl.lua"), Charset.defaultCharset());
			String clKey = jedis.scriptLoad(luaScript);
			clKeys.put(jedis.getClient().getHost() + "-" + jedis.getClient().getPort(), clKey);
		}
		connectionHandler = new JedisSlotBasedConnectionHandler(jedisClusterNode, poolConfig,
				Constants.DEFAULT_TIMEOUT);
	}
	
	private String jedisForKey(String key) throws IOException {
		Jedis jedis = connectionHandler.getConnectionFromSlot(JedisClusterCRC16.getSlot(key));
		String jedisKey = jedis.getClient().getHost() + "-" + jedis.getClient().getPort();
		jedis.close();
		return jedisKey;
	}

	@Override
	public boolean currentLimit(String key, long dimension, long times) throws Exception {

		key = key + System.currentTimeMillis()/dimension;
		String addr = jedisForKey(key);
		String clKey = clKeys.get(addr);
		Jedis jedis = jedisPools.get(addr).getResource();
		boolean flg = (long)jedis.evalsha(clKey, Arrays.asList(key), Arrays.asList(String.valueOf(times))) == 1;
		jedis.close();
		return flg;
	}

}
