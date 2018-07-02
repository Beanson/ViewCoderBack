package viewcoder.tool.redis;

import viewcoder.tool.config.GlobalConfig;
import viewcoder.tool.encrypt.AESEncryptor;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;


/**
 * Created by Administrator on 2018/2/17.
 */
public class RedisJava {

    private static Logger logger = Logger.getLogger(RedisJava.class);

    private final static String REDIS_HOST = "=com.viewcoder.redis.host";
    private final static String REDIS_PORT = "com.viewcoder.redis.port";
    private final static String REDIS_TIMEOUT = "com.viewcoder.redis.timeout";
    private final static String REDIS_PASS = "com.viewcoder.redis.pass";
    private final static String redisAuthEncryptedKey = "admin";
    private static Jedis jedis;

    static {
        //初始化连接redis数据库
//        jedis = new Jedis(GlobalConfig.getProperties(REDIS_HOST),
//                GlobalConfig.getIntProperties(REDIS_PORT),
//                GlobalConfig.getIntProperties(REDIS_TIMEOUT));
//        String redisAuth = AESEncryptor.AESDncode(redisAuthEncryptedKey,
//                GlobalConfig.getProperties(REDIS_PASS));
//        String redisPingReply = jedis.auth(redisAuth);
//        RedisJava.logger.debug("connect to redis: " + redisPingReply);
    }

    /**
     *
     * @return 返回Jedis对象
     */
    public static Jedis getInstance() {
       return jedis;
    }
}
