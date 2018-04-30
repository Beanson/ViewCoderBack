package viewcoder.tool.config;

import viewcoder.tool.common.Common;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.Properties;

/**
 * Created by Administrator on 2017/5/22.
 */
public class GlobalConfig {

    private static Logger logger = Logger.getLogger(GlobalConfig.class);

    private final static String CONFIG_PATH = "/config/config.properties";
    private final static String PAY_PATH = "/config/pay_prod.properties";
    private static Properties props = new Properties();

    //加载properties文件信息
    static {
        try {
            InputStream inputStream = GlobalConfig.class.getResourceAsStream(CONFIG_PATH);
            InputStream inputStream2 = GlobalConfig.class.getResourceAsStream(PAY_PATH);
            props.load(new InputStreamReader(inputStream, "UTF-8"));
            props.load(new InputStreamReader(inputStream2, "UTF-8"));
        } catch (Exception e) {
            GlobalConfig.logger.debug("===init properties error: ", e);
        }
    }

    /**
     * 读取配置文件String value
     *
     * @param key properties的key
     * @return
     */
    public static String getProperties(String key) {
        return props.getProperty(key);
    }

    /**
     * 读取配置文件int value
     *
     * @param key properties的key
     * @return
     */
    public static Integer getIntProperties(String key) {
        return Integer.parseInt(props.getProperty(key));
    }

    /**
     * 读取配置文件boolean value
     *
     * @param key properties的key
     * @return
     */
    public static Boolean getBooleanProperties(String key) {
        return Boolean.parseBoolean(props.getProperty(key));
    }

    /**
     * 获取文件在文件系统的路径
     *
     * @param key properties的key
     * @return
     */
    public static String getSysFileUrl(String key) {
        return props.getProperty(Common.FILE_SYS_BASE_URL_KEY) + props.getProperty(key)+"/";
    }

    /**
     * 获取文件在文件系统的路径
     *
     * @param key properties的key
     * @return
     */
    public static String getOssFileUrl(String key) {
        return props.getProperty(Common.FILE_OSS_BASE_URL_KEY) + props.getProperty(key)+"/";
    }

    /**
     * 获取文件在HTTP请求的路径
     *
     * @param key properties的key
     * @return
     */
    public static String getHttpFileUrl(String key) {
        return props.getProperty(Common.FILE_HTTP_BASE_URL_KEY) + props.getProperty(key)+"/";
    }

}
