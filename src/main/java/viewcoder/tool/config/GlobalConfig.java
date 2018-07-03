package viewcoder.tool.config;

import org.apache.commons.io.IOUtils;
import viewcoder.tool.common.Common;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;

/**
 * Created by Administrator on 2017/5/22.
 * config配置，即把所有配置读取到properties中
 */
public class GlobalConfig {

    private static Logger logger = Logger.getLogger(GlobalConfig.class);

    private final static String GENERAL_CONFIG = "/config/general.properties";
    private final static String SLASH = "/";
    private static Properties props = new Properties();
    private static int cores = Runtime.getRuntime().availableProcessors();

    //加载properties文件信息
    static {
        InputStream inputStream = null;
        try {
            /*加载主配置文件，获取目标environment*/
            inputStream = GlobalConfig.class.getResourceAsStream(GENERAL_CONFIG);
            props.load(new InputStreamReader(inputStream, Common.UTF8));
            String targetEnvironment = GlobalConfig.getProperties(Common.TARGET_ENVIRONMENT);

            /*读取具体环境下所有config文件*/
            String targetConfigUrl = "config/" + targetEnvironment + SLASH;
            List<String> files = IOUtils.readLines(GlobalConfig.class.getClassLoader()
                    .getResourceAsStream(targetConfigUrl), StandardCharsets.UTF_8);

            /*加载所有该配置环境下的文件*/
            for (String file : files) {
                inputStream = GlobalConfig.class.getResourceAsStream(SLASH + targetConfigUrl + file);
                props.load(new InputStreamReader(inputStream, Common.UTF8));
            }

        } catch (Exception e) {
            GlobalConfig.logger.error("===init properties error: ", e);

        } finally {
            /*关闭输入流*/
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (Exception e) {
                GlobalConfig.logger.error("close inputStream occurs error: ", e);
            }
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
        return props.getProperty(Common.FILE_SYS_BASE_URL_KEY) + props.getProperty(key) + "/";
    }

    /**
     * 获取文件在文件系统的路径
     *
     * @param key properties的key
     * @return
     */
    public static String getOssFileUrl(String key) {
        return props.getProperty(Common.FILE_OSS_BASE_URL_KEY) + props.getProperty(key) + "/";
    }

    /**
     * 获取文件在HTTP请求的路径
     *
     * @param key properties的key
     * @return
     */
    public static String getHttpFileUrl(String key) {
        return props.getProperty(Common.FILE_HTTP_BASE_URL_KEY) + props.getProperty(key) + "/";
    }

    /**
     * 获取所有环境properties的引用
     * @return
     */
    public static Properties getPropertiesRef(){
        return props;
    }

    public static int getCores() {
        return cores;
    }
}
