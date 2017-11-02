package cn.xsmiler.cl;

import java.util.LinkedHashSet;
import java.util.Set;

import cn.xsmiler.cl.api.ICurrentLimiting;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisPoolConfig;

public class CurrentLimitingFactory {

	public static void main(String[] args) throws Exception {
		
		JedisPoolConfig poolConfig = new JedisPoolConfig();
		poolConfig.setMaxTotal(10);
		poolConfig.setMaxIdle(1);
		poolConfig.setMaxWaitMillis(10000);
		
		Set<HostAndPort> nodes = new LinkedHashSet<>();
		nodes.add(new HostAndPort("192.168.150.129", 7000));
		nodes.add(new HostAndPort("192.168.150.129", 7001));
		nodes.add(new HostAndPort("192.168.150.129", 7002));
		nodes.add(new HostAndPort("192.168.150.129", 7003));
		nodes.add(new HostAndPort("192.168.150.129", 7004));
		nodes.add(new HostAndPort("192.168.150.129", 7005));
		
		ICurrentLimiting currentLimiting = new RedisCurrentLimiting();
		currentLimiting.init(nodes, poolConfig);
		long start = System.currentTimeMillis();
		int success = 0;
		for (int i = 0; i < 100000; i++) {
			if (currentLimiting.currentLimit("name:", 1000, 3000)) {
				success++;
			}
		}
		System.out.println("success: " + success + ", time:" + (System.currentTimeMillis() - start));
	}

}
