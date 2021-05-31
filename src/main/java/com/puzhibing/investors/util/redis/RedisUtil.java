package com.puzhibing.investors.util.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Redis工具类
 */
@Component
public class RedisUtil {

    @Autowired
    private JedisPool jedisPool;


    /**
     * 向redis中存储字符串没有过期时间
     * @param key
     * @param value
     */
    public void setStrValue(String key, String value){
        if(StringUtils.hasLength(key)){
            Jedis resource = jedisPool.getResource();
            String set = resource.set(key, value);
            closeJedis(resource);
        }
    }


    /**
     * 以分钟为单位设置存储值（设置过期时间）
     * @param key
     * @param value
     * @param time 秒
     */
    public void setStrValue(String key, String value, int time){
        if(StringUtils.hasLength(key)){
            Jedis resource = jedisPool.getResource();
            String setex = resource.setex(key, time, value);
            closeJedis(resource);
        }
    }


    /**
     * 从redis中获取值
     * @param key
     * @return
     */
    public String getValue(String key){
        if(StringUtils.hasLength(key)){
            Jedis resource = jedisPool.getResource();
            String data = resource.get(key);
            closeJedis(resource);
            return data;
        }
        return null;
    }


    /**
     * 批量获取
     * @param kes
     * @return
     */
    public List<Object> getValues(List<String> kes){
        if(null != kes){
            Jedis resource = jedisPool.getResource();
            Pipeline pipelined = resource.pipelined();
            for(String key : kes){
                pipelined.get(key);
            }
            List<Object> list = pipelined.syncAndReturnAll();

            closeJedis(resource);
            pipelined.clear();
            try {
                pipelined.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            List<Object> data = new ArrayList<>();
            for(Object o : list){
                if(null != o){
                    data.add(o);
                }
            }
            return data;
        }
        return null;
    }


    /**
     * 删除key
     * @param key
     */
    public void remove(String key){
        if(StringUtils.hasLength(key)){
            Jedis resource = jedisPool.getResource();
            Long del = resource.del(key);
            closeJedis(resource);
        }
    }


    /**
     * 删除资源
     * @param jedis
     */
    public void closeJedis(Jedis jedis){
        if(null != jedis){
            jedis.close();
        }
    }

}
