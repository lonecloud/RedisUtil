package cn.lonecloud.util;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * <p>用于便捷使用Redis的工具类</p>
 * Created by lonecloud on 17/3/15.
 *
 * @author lonecloud
 */
public class RedisUtils {

    protected static final Logger log = LoggerFactory.getLogger(RedisUtils.class);
    /**
     * 链接
     */
    private static Jedis jedis;
    /**
     * 默认127.0.0.1
     */
    private static String url = "127.0.0.1";
    /**
     * 默认6379端口
     */
    private static int port = 6379;
    /**
     * redis认证密码
     */
    private static String password = "123456";
    /**
     * 是否为使用redisPool
     */
    private static boolean isPool = false;
    /**
     * 设置默认的超时时间10s
     */
    private static int timeout = 1000;
    /**
     * redis池
     */
    private static JedisPool jedisPool;

    private static GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();

    static {
        //初始化redis
        initRedis();
    }

    /**
     * 获取Jedis 实例
     *
     * @return
     */
    public static Jedis getJedis() {
        if (isPool){
            if (jedisPool==null){
                jedisPool=new JedisPool(poolConfig,url,port,timeout,password);
            }
            return jedisPool.getResource();
        }else {
            if (jedis == null) {
                jedis = new Jedis(url, port);
            }
            return jedis;
        }
    }

    /**
     * 将String设置到redis中
     *
     * @param key   键
     * @param value 值
     */
    public static void setString(String key, String value) {
        getJedis().set(key, value);
    }

    /**
     * 通过键获取String
     *
     * @param key 键
     * @return
     */
    public static String getString(String key) {
        return getJedis().get(key);
    }

    /**
     * 将List数组存进redis中
     *
     * @param key    键
     * @param list   String的List对象
     * @param isHead 是否每次插入从头部进行插入
     */
    public static void setList(String key, List<String> list, boolean isHead) {
        String[] arr = new String[list.size()];
        list.toArray(arr);
        //判断是不是从头部开始插入
        if (isHead) {
            getJedis().lpush(key, arr);
        } else {
            getJedis().rpush(key, arr);
        }
        if (log.isDebugEnabled()) {
            log.debug("redis insert a list of " + key + ":" + list);
        }
    }

    /**
     * 获取redis中的数组
     *
     * @param key   键
     * @param start 开始
     * @param end   结束
     * @return
     */
    public static List<String> getList(String key, long start, long end) {
        return getJedis().lrange(key, start, end);
    }

    /**
     * redis存储对象
     *
     * @param key 键
     * @param obj 对象
     * @throws IOException
     */
    public static void setObject(String key, Object obj) throws IOException {
        if (obj != null) {
            byte[] bytes = ObjectUtils.ObjectToBytes(obj);
            getJedis().set(key.getBytes(), bytes);
        } else {
            log.error("没有找到类");
            throw new NullPointerException("没有找到该类");
        }
    }

    /**
     * redis获取对象
     *
     * @param key   键
     * @param clazz 获取的对象类型
     * @param <T>
     * @return
     * @throws IOException
     */
    public static <T extends Serializable> T getObject(String key, Class<T> clazz) throws IOException {
        byte[] bytes = getJedis().get(key.getBytes());
        T obj = ObjectUtils.ObjectFromBytes(bytes, clazz);
        return obj;
    }

    /**
     * 批量删除key
     * @param keys
     * @return
     */
    public static int mDel(Set<String> keys){
        Pipeline pipelined = getJedis().pipelined();
        for (String key : keys) {
            pipelined.del(key);
        }
        List<Object> objects = pipelined.syncAndReturnAll();
        return objects.size();
    }

    /**
     * 初始化redis参数操作
     */
    private static void initRedis() {
        Properties properties = new Properties();
        try {
            //设置
            InputStream inputStream = RedisUtils.class.getResourceAsStream("/redis.properties");
            if (inputStream == null) {
                jedis = new Jedis();
                log.debug("开始执行默认的配置,使用单例redis");
                System.err.println("cant find redis.properties file,load default set ---->127.0.0.1->6379");
            } else {
                properties.load(inputStream);
                //判断是否使用pool 如果不使用则是单例的redis
                String proisPool = properties.getProperty("redis.isPool");
                String proUrl = properties.getProperty("redis.url");
                String proProt = properties.getProperty("redis.port");
                String proPass = properties.getProperty("redis.password");
                //设置全局的配置
                if (proUrl != null && proProt != null && proPass!=null) {
                    url = proUrl;
                    port = Integer.valueOf(proProt);
                    password=proPass;
                }
                if (proisPool != null && "true".equals(proisPool.trim())) {
                    //设置全局判断变量为true
                    isPool=true;
                    log.debug("开始执行pool的创建工作");
                    jedisPool = new JedisPool(poolConfig, url, port, timeout, password);
                    /**
                     * 设置默认的redis
                     */
                    jedis = jedisPool.getResource();
                } else {
                    log.debug("开始执行单例redis");
                    if (proUrl != null && proProt != null) {
                        url = proUrl;
                        port = Integer.valueOf(proProt);
                    }
                    jedis = new Jedis(url, port);
                }
            }
        } catch (IOException e) {
            System.err.println("cant find redis.properties file,load default set ---->127.0.0.1->6379");
            e.printStackTrace();
        }
    }

    /**
     * 设置该类不能实例化
     */
    private RedisUtils() {
    }
}
