package cn.xsmiler.cl.impl;

import cn.xsmiler.cl.api.ICurrentLimiting;
import cn.xsmiler.cl.common.URL;
import com.google.common.io.Files;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * Created by xsmiler on 2017/12/20.
 */
public class RedisCurrentLimiting implements ICurrentLimiting {

    private JedisPool jedisPool;
    private String clKey;

    @Override
    public void init(String address, GenericObjectPoolConfig poolConfig) throws Exception {
        URL redisURL = URL.valueOf(address);

        jedisPool = new JedisPool(poolConfig, redisURL.getHost(), redisURL.getPort());

        Jedis jedis = jedisPool.getResource();
        String luaScript = Files.toString(new File("src/main/resources/cl.lua"), Charset.defaultCharset());
        clKey = jedis.scriptLoad(luaScript);
        jedis.close();
    }

    @Override
    public boolean currentLimit(String key, long dimension, long times) throws Exception {
        key = key + System.currentTimeMillis()/dimension;
        Jedis jedis = jedisPool.getResource();
        boolean flg = (long)jedis.evalsha(clKey, Arrays.asList(key), Arrays.asList(String.valueOf(times))) == 1;
        jedis.close();
        return flg;
    }
}
