package cn.xsmiler.cl.api;

import java.io.IOException;
import java.util.Set;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;

/**
 * 分布式限流接口
 * @author xsmiler
 * @date 2017/10/12
 */
public interface ICurrentLimiting {
	
	void init(Set<HostAndPort> jedisClusterNode, final GenericObjectPoolConfig poolConfig);
	
	Jedis jedisForKey(String key) throws IOException;

	boolean currentLimit(String key, Jedis jedis, long dimension, long times);
}
