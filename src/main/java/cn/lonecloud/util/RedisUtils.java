package cn.lonecloud.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Properties;

/**
 * <p>用于便捷使用Redis的工具类</p>
 * Created by lonecloud on 17/3/15.
 * @author lonecloud
 */
public class RedisUtils {

    protected static final Logger log= LoggerFactory.getLogger(RedisUtils.class);
    /**
     * 链接
     */
    private static Jedis jedis;
    /**
     * 默认127.0.0.1
     */
    private static String url="127.0.0.1";
    /**
     * 默认6379端口
     */
    private static int port=6379;

    static {
        Properties properties=new Properties();
        try {
            //设置
            properties.load(RedisUtils.class.getResourceAsStream("/redis.properties"));
            String proUrl = properties.getProperty("redis.url");
            String proProt = properties.getProperty("redis.port");
            if (proUrl!=null&&proProt!=null){
                url=proUrl;
                port=Integer.valueOf(proProt);
            }
            jedis=new Jedis(url,port);
        } catch (IOException e) {
            System.err.println("cant find redis.properties file,load default set ---->127.0.0.1->6379");
            e.printStackTrace();
        }
    }

    /**
     * 获取Jedis 实例
     * @return
     */
    public static Jedis getJedis(){

        if (jedis==null){
            jedis=new Jedis(url,port);
        }
        return jedis;
    }

    /**
     * 将数组设置到redis中
     * @param key
     * @param value
     */
    public static void setString(String key,String value){
       jedis.set(key,value);
    }

    /**
     * 将List数组存进redis中
     * @param key
     * @param list
     */
    public static void setList(String key, List<String> list){
        String[] arr=new String[list.size()];
        list.toArray(arr);
        jedis.lpush(key,arr);
        if (log.isDebugEnabled()){
            log.debug("redis insert a list of "+key+":"+list);
        }
    }

    /**
     * 获取redis中的数组
     * @param key
     * @param start
     * @param end
     * @return
     */
    public static List<String> getList(String key,long start,long end){
        return jedis.lrange(key,start,end);
    }

    /**
     * redis存储对象
     * @param key 键
     * @param obj 对象
     * @throws IOException
     */
    public static void setObject(String key, Object obj) throws IOException {
        if (obj!=null){
            byte[] bytes = ObjectUtils.ObjectToBytes(obj);
            jedis.set(key.getBytes(),bytes);
        }else{
            log.error("没有找到类");
            throw new NullPointerException("没有找到该类");
        }
    }

    /**
     * redis获取对象
     * @param key 键
     * @param clazz 获取的对象类型
     * @param <T>
     * @return
     * @throws IOException
     */
    public static <T extends Serializable> T getObject(String key,Class<T> clazz) throws IOException {
        byte[] bytes = jedis.get(key.getBytes());
        T obj = ObjectUtils.ObjectFromBytes(bytes, clazz);
        return obj;
    }
}
