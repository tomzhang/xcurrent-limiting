package cn.xsmiler.cl;

import cn.xsmiler.cl.api.ICurrentLimiting;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisPoolConfig;

import java.util.LinkedHashSet;
import java.util.Set;

public class CurrentLimitingFactory {

	public static void main(String[] args) throws Exception {
		
		JedisPoolConfig poolConfig = new JedisPoolConfig();
		poolConfig.setMaxTotal(10);
		poolConfig.setMaxIdle(5);
		poolConfig.setMaxWaitMillis(10000);
		
		Set<HostAndPort> nodes = new LinkedHashSet<>();
		nodes.add(new HostAndPort("192.168.150.130", 7000));
		nodes.add(new HostAndPort("192.168.150.130", 7001));
		nodes.add(new HostAndPort("192.168.150.130", 7002));
		nodes.add(new HostAndPort("192.168.150.130", 7003));
		nodes.add(new HostAndPort("192.168.150.130", 7004));
		nodes.add(new HostAndPort("192.168.150.130", 7005));
		
		ICurrentLimiting currentLimiting = new RedisCurrentLimiting();
		currentLimiting.init(nodes, poolConfig);
		long start = System.currentTimeMillis();
		int success = 0;
		for (int i = 0; i < 10000; i++) {
			if (currentLimiting.currentLimit("name:", 60000, 50)) {
				success++;
			}
		}
		System.out.println("success: " + success + ", time:" + (System.currentTimeMillis() - start));
	}

}
