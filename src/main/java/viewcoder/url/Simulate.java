package viewcoder.url;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.apache.log4j.Logger;
import org.openqa.selenium.*;
import viewcoder.operation.entity.ProjectProgress;
import viewcoder.tool.pool.WebDriverPool;

import java.net.URL;


/**
 * Created by Administrator on 2018/5/8.
 * 创建URL项目的操作核心类
 */
public class Simulate {
    private static Logger logger = Logger.getLogger(Simulate.class);

    public static void main(String[] args) throws Exception {
        createProject("http://www.hao365.org.cn/",
                new ProjectProgress(), 1300, 700);
    }

    /**
     * 通过网址提取部分组件元素，返回该组件元素集合
     *
     * @param webUrl          目标网站网站
     * @param projectProgress 项目进度记录
     * @param totalWidth      网站整体宽度
     * @param totalHeight     网站整体高度
     * @return
     * @throws Exception
     */
    public static String createProject(String webUrl, ProjectProgress projectProgress, int totalWidth, int totalHeight)
            throws Exception {

        WebDriver driver = null;
        String projectData = null;
        try {
            //从pool中获取PhantomJS driver对象
            driver = getWebDriver();
            if (driver == null) {
                return null;
            }
            projectProgress.setProgress(20);

            //打开网页
            connectToUrl(driver, webUrl, totalWidth, totalHeight);
            projectProgress.setProgress(80);

            //加载提取网站元素的js文件
            JavascriptExecutor js = (JavascriptExecutor) driver;
            URL simulateURL = Resources.getResource("js/pure_simulate/simulate2.js");
            String simulateScript = Resources.toString(simulateURL, Charsets.UTF_8);
            projectData = (String) js.executeScript(simulateScript);
            //清空driver缓存
            driver.manage().deleteAllCookies();
            //使用完driver后返回pool
            WebDriverPool.getPool().returnObject(driver);
            Simulate.logger.debug("Get project data: " + projectData);

        } catch (Exception e) {
            //操作过程发生错误，销毁该driver对象并返回null
            Simulate.logger.error("createProject with error: ", e);
            WebDriverPool.getPool().invalidateObject(driver);
            return null;
        }

        return projectData;
    }


    /**
     * 从pool中获取webDriver方法，最多连续尝试获取3次，3次无果后返回null
     *
     * @return 返回driver实例对象
     */
    private static WebDriver getWebDriver() {
        WebDriver driver = null;
        int getDriverTimes = 0;

        //从pool中获取webDriver实例
        while (getDriverTimes < 3) {
            try {
                driver = WebDriverPool.getPool().borrowObject();
                break;

            } catch (Exception e) {
                getDriverTimes++;
                Simulate.logger.error("Get web driver from pool error times: " + getDriverTimes, e);
            }
        }
        return driver;
    }


    /**
     * 通过webDriver尝试连接该网站，超过20秒未完成加载则停止网页loading。
     *
     * @param webUrl 目标网站的url
     */
    private static void connectToUrl(WebDriver driver, String webUrl, int totalWidth, int totalHeight) {
        try {
            Dimension dimension = new Dimension(totalWidth, totalHeight);
            //设置屏幕宽高
            driver.manage().window().setSize(dimension);
            //打开指定网址
            driver.get(webUrl);

        } catch (Exception e) {
            //打开网页错误或超时，结束加载网页, 继续后续js操作
            Simulate.logger.warn("Simulate connectToUrl error:", e);
        }
    }
}
