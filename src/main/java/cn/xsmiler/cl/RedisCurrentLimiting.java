package cn.xsmiler.cl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import cn.xsmiler.cl.api.ICurrentLimiting;
import cn.xsmiler.cl.common.Constants;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisSlotBasedConnectionHandler;
import redis.clients.util.JedisClusterCRC16;

public class RedisCurrentLimiting implements ICurrentLimiting {

	private JedisSlotBasedConnectionHandler connectionHandler;
	private Map<String, String> clKeys = new ConcurrentHashMap<>();
	
	@Override
	public void init(Set<HostAndPort> jedisClusterNode, GenericObjectPoolConfig poolConfig) {
		
		connectionHandler = new JedisSlotBasedConnectionHandler(jedisClusterNode, poolConfig,
				Constants.DEFAULT_TIMEOUT);
	}
	
	@Override
	public Jedis jedisForKey(String key) throws IOException {
		Jedis jedis = connectionHandler.getConnectionFromSlot(JedisClusterCRC16.getSlot(key));
		if (!clKeys.containsKey(jedis.getClient().getHost() + jedis.getClient().getPort())) {
			BufferedReader bf = new BufferedReader(new FileReader(new File("src/main/resources/cl.lua")));
			String content = "";
			StringBuilder sb = new StringBuilder();
			while(content != null){
				content = bf.readLine();
				if(content == null){
					break;
				}
				sb.append(content.trim());
			}
			bf.close();
			String luaScript = sb.toString();
			String clKey = jedis.scriptLoad(luaScript);
			clKeys.put(jedis.getClient().getHost() + jedis.getClient().getPort(), clKey);
		}
		return jedis;
	}

	@Override
	public boolean currentLimit(String key, Jedis jedis, long dimension, long times) {


		String clKey = clKeys.get(jedis.getClient().getHost() + jedis.getClient().getPort());
		System.out.println(jedis.toString() + "-" + key + "-" + times);
		List<String> keys = new ArrayList<>();
		keys.add(key);
		List<String> args = new ArrayList<>();
		args.add(String.valueOf(times));
		return (long)jedis.evalsha(clKey, keys, args) == 1;
	}

}
