package cn.xsmiler.cl.api;

import java.util.Set;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import redis.clients.jedis.HostAndPort;

/**
 * 分布式限流接口
 * @author xsmiler
 * @date 2017/10/12
 */
public interface ICurrentLimiting {
	
	void init(Set<HostAndPort> jedisClusterNode, final GenericObjectPoolConfig poolConfig) throws Exception;
	

	boolean currentLimit(String key, long dimension, long times) throws Exception;
}
