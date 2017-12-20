package cn.xsmiler.cl.impl;

import cn.xsmiler.cl.api.ICurrentLimiting;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

/**
 * Created by xsmiler on 2017/12/20.
 */
public class RedisClusterCurrentLimiting implements ICurrentLimiting{

    @Override
    public void init(String address, GenericObjectPoolConfig poolConfig) throws Exception {

    }

    @Override
    public boolean currentLimit(String key, long dimension, long times) throws Exception {
        return false;
    }
}
