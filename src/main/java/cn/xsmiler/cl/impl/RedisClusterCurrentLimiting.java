package cn.xsmiler.cl.impl;

import cn.xsmiler.cl.api.ICurrentLimiting;
import cn.xsmiler.cl.common.Constants;
import cn.xsmiler.cl.common.URL;
import com.google.common.io.Files;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisSlotBasedConnectionHandler;
import redis.clients.util.JedisClusterCRC16;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by xsmiler on 2017/12/20.
 */
public class RedisClusterCurrentLimiting implements ICurrentLimiting{

    /* 处理某个key在那个槽点处理类 */
    private JedisSlotBasedConnectionHandler connectionHandler;
    /* 存储Redis Lua脚本key */
    private Map<String, String> clKeys = new ConcurrentHashMap<>();
    /* 存放每个节点的JedisPool */
    private Map<String, JedisPool> jedisPools = new ConcurrentHashMap<>();

    @Override
    public void init(String address, GenericObjectPoolConfig poolConfig) throws Exception {
        URL redisURL = URL.valueOf(address);
        String backupAddress = redisURL.getBackupAddress();
        Map<String, HostAndPort> hosts = new HashMap<>();
        hosts.put(redisURL.getIp() + ":" + redisURL.getPort(), new HostAndPort(redisURL.getIp(), redisURL.getPort()));
        String[] backups = backupAddress.split(",");
        // 获取backup服务器列表
        for (String backup : backups) {
            String[] hostip = backup.split(":");
            if (hostip.length == 2) {
                hosts.put(hostip[0] + "-" + hostip[1], new HostAndPort(hostip[0], Integer.valueOf(hostip[1])));
            }
        }
        Set<HostAndPort> jedisClusterNode = new HashSet<>();
        // 遍历所有Redis服务器地址
        for (Map.Entry<String, HostAndPort> entry : hosts.entrySet()) {
            HostAndPort hostAndPort = entry.getValue();
            // 新建JedisPool
            JedisPool pool = new JedisPool(poolConfig, hostAndPort.getHost(), hostAndPort.getPort());
            jedisPools.put(entry.getKey(), pool);
            Jedis jedis = pool.getResource();
            String luaScript = Files.toString(new File("src/main/resources/cl.lua"), Charset.defaultCharset());
            String clKey = jedis.scriptLoad(luaScript);
            clKeys.put(jedis.getClient().getHost() + "-" + jedis.getClient().getPort(), clKey);
            jedis.close();
            jedisClusterNode.add(hostAndPort);
        }
        connectionHandler = new JedisSlotBasedConnectionHandler(jedisClusterNode, poolConfig,
                Constants.DEFAULT_TIMEOUT);
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

    /**
     * 根据Redis Key获取应该在那个Redis节点上
     * @param key
     * @return
     * @throws IOException
     */
    private String jedisForKey(String key) throws IOException {
        Jedis jedis = connectionHandler.getConnectionFromSlot(JedisClusterCRC16.getSlot(key));
        String jedisKey = jedis.getClient().getHost() + "-" + jedis.getClient().getPort();
        jedis.close();
        return jedisKey;
    }
}
