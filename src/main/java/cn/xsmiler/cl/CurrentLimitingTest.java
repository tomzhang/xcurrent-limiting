package cn.xsmiler.cl;

import cn.xsmiler.cl.api.ICurrentLimiting;
import cn.xsmiler.cl.impl.RedisClusterCurrentLimiting;
import redis.clients.jedis.JedisPoolConfig;

public class CurrentLimitingTest {

    public static void main(String[] args) throws Exception {

        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(10);
        poolConfig.setMaxIdle(5);
        poolConfig.setMaxWaitMillis(10000);

        // 单机版Redis限流
//        ICurrentLimiting currentLimiting = new RedisCurrentLimiting();
//        currentLimiting.init("redis://192.168.150.133:7000", poolConfig);

        // Redis Cluster版限流
        ICurrentLimiting currentLimiting = new RedisClusterCurrentLimiting();
        currentLimiting.init("redis://192.168.150.133:7000?backup=192.168.150.133:7001,192.168.150.133:7002,192.168.150.133:7003,192.168.150.133:7004,192.168.150.133:7005", poolConfig);
        long startTime = System.currentTimeMillis();
        int success = 0;
        for (int i = 0; i < 1000; i++) {
            if (currentLimiting.currentLimit("name", 60000, 100)) {
                success++;
            }
        }
        System.out.println("success times:" + success);
        System.out.println("spend time:" + (System.currentTimeMillis() - startTime));
    }
}
