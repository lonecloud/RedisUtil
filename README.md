Redis快捷使用的工具类
=====================
该项目采用`maven`进行构建

##最简单的配置`redis.properties`<br>
1).redis在本机,无需配置,直接使用(默认端口:6379)<br>
2).redis在远程需要在您工程的目录下新建redis.properties文件:<br>
        `redis.url`:Redis的url<br>
        `redis.port`:Redis的端口<br>
##`RedisUtils`中提供了以下几个函数<br>
        用于对redis进行数据的插入
        void setString(String key,String value)
                key 键
                value 值
         
    
    