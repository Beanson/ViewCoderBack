package viewcoder.tool.pool;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import viewcoder.tool.common.Common;
import viewcoder.tool.config.GlobalConfig;

import java.util.concurrent.TimeUnit;


/**
 * Created by Administrator on 2018/5/22.
 * WebDriver对象的pool，可快速获取driver实例并且重复使用资源，提高资源使用效率
 */
public class WebDriverPool extends BasePooledObjectFactory<WebDriver> {

    private static Logger logger = LoggerFactory.getLogger(WebDriverPool.class);
    private static GenericObjectPool<WebDriver> pool;
    private static String ACCEPT_SSL_CERTS = "acceptSslCerts";

    static {
        int minIdle = GlobalConfig.getIntProperties(Common.DRIVER_MIN_IDLE);
        GenericObjectPoolConfig conf = new GenericObjectPoolConfig();
        conf.setMaxTotal(GlobalConfig.getIntProperties(Common.DRIVER_MAX_ACTIVE));
        conf.setMinIdle(minIdle);
        //最大等待初始化实例时间
        conf.setMaxWaitMillis(GlobalConfig.getIntProperties(Common.DRIVER_MAX_WAIT));
        conf.setNumTestsPerEvictionRun(GlobalConfig.getIntProperties(Common.DRIVER_CHECK_TIME));
        //“空闲链接”监测线程执行时间间隔
        conf.setTimeBetweenEvictionRunsMillis(GlobalConfig.getIntProperties(Common.DRIVER_CHECK_NUM));
        pool = new GenericObjectPool<WebDriver>(new WebDriverPool(), conf);
        //根据min idle配置数目初始化pool实例个数
        initIdleDriver(minIdle);
    }

    /**
     * 初始化最小idle数的driver实例
     *
     * @param minIdle 最小idle数的driver
     */
    private static void initIdleDriver(int minIdle) {
        //初始化minIdle个driver对象
        for (int i = 0; i < minIdle; i++) {
            try {
                pool.addObject();

            } catch (Exception e) {
                WebDriverPool.logger.error("WebDriverPool init idle object error: " + i, e);
            }
        }
        WebDriverPool.logger.debug("idle: " + WebDriverPool.getPool().getNumIdle() +
                " num active:" + WebDriverPool.getPool().getNumActive() +
                " waiter:" + WebDriverPool.getPool().getNumWaiters());
    }

    /**
     * 初始化phantomjs driver的配置信息
     *
     * @return
     */
    private DesiredCapabilities initOptions() {
        //设置必要参数
        DesiredCapabilities dcaps = new DesiredCapabilities();
        //ssl证书支持
        dcaps.setCapability(ACCEPT_SSL_CERTS, true);
        //js支持
        dcaps.setJavascriptEnabled(true);
        //驱动支持
        dcaps.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY,
                GlobalConfig.getProperties(Common.DRIVER_LOCATION));
        return dcaps;
    }

    /**
     * 定义pool实例元素的创建
     *
     * @return
     * @throws Exception
     */
    @Override
    public WebDriver create() throws Exception {
        DesiredCapabilities dcaps = initOptions();
        WebDriver driver = new PhantomJSDriver(dcaps);
        driver.manage().timeouts().pageLoadTimeout(
                GlobalConfig.getIntProperties(Common.PAGELOAD_TIMEOUT), TimeUnit.SECONDS);
        driver.manage().deleteAllCookies();
        return driver;
    }

    @Override
    public PooledObject<WebDriver> wrap(WebDriver driver) {
        return new DefaultPooledObject<WebDriver>(driver);
    }

    @Override
    public void destroyObject(PooledObject<WebDriver> p) throws Exception {
        super.destroyObject(p);
        WebDriver driver = p.getObject();
        if (driver != null) {
            try {
                driver.close();
                driver.quit();

            } catch (Exception e) {
                WebDriverPool.logger.error("destroy webdriver error: " + e);

            } finally {
                driver = null;
            }
        }
    }

    /**
     * 对外开放pool对象静态获取
     *
     * @return
     */
    public static GenericObjectPool<WebDriver> getPool() {
        return pool;
    }

//    public static void main(String[] args) throws Exception {
//        logger.debug("idle: " + WebDriverPool.getPool().getNumIdle() +
//                " num total:" + WebDriverPool.getPool().getNumActive() + " waiter:" + WebDriverPool.getPool().getNumWaiters());
//    }
}
