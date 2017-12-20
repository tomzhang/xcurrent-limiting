package cn.xsmiler.cl.api;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

/**
 * 分布式限流接口
 * @author xsmiler
 * @date 2017/10/12
 */
public interface ICurrentLimiting {

	/**
	 * 初始化
	 * @param address redis地址，如：redis://192.168.150.130:6379 或者 redis://192.168.130:7000?/backup=192.168.150.130:7001,192.168.150.130:7002,192.168.150.130:7003,192.168.150.130:7004,192.168.150.130:7005
	 * @param poolConfig redis pool配置
	 * @throws Exception
	 */
	void init(String address, final GenericObjectPoolConfig poolConfig) throws Exception;


	/**
	 * 限流
	 * @param key 限流key
	 * @param dimension 时间维度（这里单位为毫秒，如一分钟为60000）
	 * @param times 限流次数
	 * @return
	 * @throws Exception
	 */
	boolean currentLimit(String key, long dimension, long times) throws Exception;
}
