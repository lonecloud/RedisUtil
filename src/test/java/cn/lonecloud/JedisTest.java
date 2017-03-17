package cn.lonecloud;

import cn.lonecloud.model.Role;
import cn.lonecloud.model.User;
import cn.lonecloud.util.RedisUtils;
import org.junit.Test;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by lonecloud on 17/3/15.
 */
public class JedisTest {
    @Test
    public void getJedis(){
//        Jedis jedis = RedisUtils.getJedis();
        Jedis jedis=new Jedis("127.0.0.1",6379);
        System.out.println(jedis.ping());

    }
    @Test
    public void set(){
        RedisUtils.setString("1","2");
    }
    @Test
    public void setList(){
        List<String> list=new ArrayList<>();
        list.add("1");
        list.add("2");
        list.add("3");
        list.add("4");
        list.add("5");
        RedisUtils.setList("list",list,false);
        for (String s : RedisUtils.getList("list", 0, list.size())) {
            System.out.println("list"+s);
        }
    }
    @Test
    public void testObj() throws IOException {
        User user=new User();
        user.setDate(new Date());
        user.setId("dsdsdsds");
        user.setName("小明");
        Role role=new Role();
        role.setId("dsds");
        role.setName("dsdss");
        user.getRoles().add(role);
        RedisUtils.setObject("obj1",user);
    }
    @Test
    public void getObj() throws IOException {
        User user1 = RedisUtils.getObject("obj1", User.class);
        System.out.println(user1.toString());
    }
}
