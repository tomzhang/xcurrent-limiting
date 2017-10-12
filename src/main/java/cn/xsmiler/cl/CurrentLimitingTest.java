package cn.xsmiler.cl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;

import redis.clients.jedis.Jedis;

public class CurrentLimitingTest {

	private Jedis jedis;
	private String shaKey;
	
	public CurrentLimitingTest() throws Exception {
		
		jedis = new Jedis("192.168.211.129", 7001);
		BufferedReader bf = new BufferedReader(new FileReader(new File("src/main/resources/cl.lua")));
		String content = "";
		StringBuilder sb = new StringBuilder();
		while(content != null){
			content = bf.readLine();
			if(content == null){
				break;
			}
			sb.append(content.trim());
		}
		bf.close();
		String luaScript = sb.toString();
		shaKey = jedis.scriptLoad(luaScript);
	}
	
	public boolean currentLimit(String key) {
		
		return (long)jedis.evalsha(shaKey, Arrays.asList(key), Arrays.asList("100")) == 1;
	}
	
	
	public static void main(String[] args) throws Exception {
		
		CurrentLimitingTest currentLimiting = new CurrentLimitingTest();
		int success = 0;
		for (int i = 0; i < 1000; i++) {
			if (currentLimiting.currentLimit("name")) {
				success++;
			}
		}
		System.out.println(success);
	}
}
