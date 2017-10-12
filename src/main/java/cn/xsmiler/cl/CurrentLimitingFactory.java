package cn.xsmiler.cl;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import cn.xsmiler.cl.api.ICurrentLimiting;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;

public class CurrentLimitingFactory {

	public static void main(String[] args) throws IOException {
		
		JedisPoolConfig poolConfig = new JedisPoolConfig();
		poolConfig.setMaxTotal(10);
//		poolConfig.setMaxIdle(1);
		poolConfig.setMaxWaitMillis(10000);
		
		Set<HostAndPort> nodes = new LinkedHashSet<>();
		nodes.add(new HostAndPort("192.168.211.129", 7000));
		nodes.add(new HostAndPort("192.168.211.129", 7001));
		nodes.add(new HostAndPort("192.168.211.129", 7002));
		nodes.add(new HostAndPort("192.168.211.129", 7003));
		nodes.add(new HostAndPort("192.168.211.129", 7004));
		nodes.add(new HostAndPort("192.168.211.129", 7005));
		
		ICurrentLimiting currentLimiting = new RedisCurrentLimiting();
		currentLimiting.init(nodes, poolConfig);

		int success = 0;
		for (int i = 0; i < 10000; i++) {
			Jedis jedis = currentLimiting.jedisForKey("name" + i);
			if (currentLimiting.currentLimit("name" + i, jedis, 0, 100)) {
				success++;
			}
			jedis.close();
		}
		System.out.println(success);
	}

}
