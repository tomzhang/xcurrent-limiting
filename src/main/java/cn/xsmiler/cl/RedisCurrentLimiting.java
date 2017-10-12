package cn.xsmiler.cl;

import java.util.Set;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import cn.xsmiler.cl.api.ICurrentLimiting;
import cn.xsmiler.cl.common.Constants;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisSlotBasedConnectionHandler;
import redis.clients.util.JedisClusterCRC16;

public class RedisCurrentLimiting implements ICurrentLimiting {

	private JedisSlotBasedConnectionHandler connectionHandler;
	
	@Override
	public void init(Set<HostAndPort> jedisClusterNode, GenericObjectPoolConfig poolConfig) {
		
		connectionHandler = new JedisSlotBasedConnectionHandler(jedisClusterNode, poolConfig,
				Constants.DEFAULT_TIMEOUT);
	}
	
	@Override
	public Jedis jedisForKey(String key) {
		
		return connectionHandler.getConnectionFromSlot(JedisClusterCRC16.getSlot(key));
	}

}
