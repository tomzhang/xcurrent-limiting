package cn.xsmiler.cl;

import com.google.common.io.Files;
import redis.clients.jedis.Jedis;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Arrays;

public class CurrentLimitingTest {

	private Jedis jedis;
	private String shaKey;
	
	public CurrentLimitingTest() throws Exception {
		
		jedis = new Jedis("192.168.150.130", 7001);
		
		String luaScript = Files.toString(new File("src/main/resources/cl.lua"), Charset.defaultCharset());
		shaKey = jedis.scriptLoad(luaScript);
	}
	
	public boolean currentLimit(String key) {
		
		return (long)jedis.evalsha(shaKey, Arrays.asList(key), Arrays.asList("1")) == 1;
	}
	
	
	public static void main(String[] args) throws Exception {
		
		CurrentLimitingTest currentLimiting = new CurrentLimitingTest();
		int success = 0;
		for (int i = 0; i < 10; i++) {
			if (currentLimiting.currentLimit("name")) {
				success++;
			}
		}
		System.out.println(success);
	}
}
