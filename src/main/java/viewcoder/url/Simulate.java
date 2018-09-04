package viewcoder.url;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.apache.commons.text.StrSubstitutor;
import org.openqa.selenium.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import viewcoder.operation.entity.ProjectProgress;
import viewcoder.tool.common.Common;
import viewcoder.tool.pool.WebDriverPool;

import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


/**
 * Created by Administrator on 2018/5/8.
 * 创建URL项目的操作核心类
 */
public class Simulate {
    private static Logger logger = LoggerFactory.getLogger(Simulate.class);
    private static final String TEST_URL = "http://www.baidu.com";
    private static final int TOTAL_HEIGHT = 700;
    private static final int MID_LOAD_TIME = 10000;

    public static void main(String[] args) throws Exception {
        createProject(Simulate.TEST_URL, new ProjectProgress(), 400, "mo");
    }

    /**
     * 通过网址提取部分组件元素，返回该组件元素集合
     *
     * @param webUrl          目标网站网站
     * @param projectProgress 项目进度记录
     * @param totalWidth      网站整体宽度
     * @param version         网站版本，手机版还是电脑版
     * @return
     * @throws Exception
     */
    public static String createProject(String webUrl, ProjectProgress projectProgress, int totalWidth, String version)
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
            connectToUrl(driver, webUrl, totalWidth);
            projectProgress.setProgress(80);

            //加载提取网站元素的js文件
            JavascriptExecutor js = (JavascriptExecutor) driver;

            URL jqueryUrl = Resources.getResource("js/simulate_jquery/jquery.min.js");
            String jqueryText = Resources.toString(jqueryUrl, Charsets.UTF_8);
            js.executeScript(jqueryText);
            int contentHeight = ((Number) js.executeScript("return $(document).height()")).intValue();
            Simulate.logger.debug("get page height: " + contentHeight);
            int contentWidth = ((Number) js.executeScript("return $(document).width()")).intValue();
            Simulate.logger.debug("get page width: " + contentWidth);

            //慢加载操作，后期前端设置允许用户调节每次滚动的等待时间，此步骤还可获取页面高度
            for (int i = Simulate.TOTAL_HEIGHT; i < contentHeight; i += Simulate.TOTAL_HEIGHT) {
                //两个参数分别文档向右移动和向下移动位置
                js.executeScript("window.scrollBy(0, " + i + ")");
                Thread.sleep(500);
            }

            //解析页面元素JavaScript
            URL simulateURL = Resources.getResource("js/pure_simulate/simulate2.js");
            String simulateScript = Resources.toString(simulateURL, Charsets.UTF_8);
            //替换数据准备
            String isMobile = Objects.equals(version, Common.MOBILE_V) ? Common.TRUE_RESULT : Common.FALSE_RESULT;
            Map<String, String> replaceData = new HashMap<>(1);
            replaceData.put("is_mobile", isMobile);
            String executeScript = StrSubstitutor.replace(simulateScript, replaceData);
            //执行解析页面元素的JavaScript
            projectData = (String) js.executeScript(executeScript);

            //清空driver部分本地存储
            driver.manage().deleteAllCookies();
            js.executeScript("localStorage.clear();");

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
    private static void connectToUrl(WebDriver driver, String webUrl, int totalWidth) {
        try {
            Dimension dimension = new Dimension(totalWidth, Simulate.TOTAL_HEIGHT);
            //设置屏幕宽高
            driver.manage().window().setSize(dimension);

            //打开指定网址，且loading时间不少于7秒
//            long beginLoad = new Date().getTime();
//            driver.get(webUrl);
//            long endLoad = new Date().getTime();
//            long compare = endLoad - beginLoad;
//            if (compare < MID_LOAD_TIME) {
//                long sleepTime = MID_LOAD_TIME - compare;
//                Simulate.logger.debug("force sleep time: " + sleepTime);
//                Thread.sleep(sleepTime);
//
//            }else {
//                Simulate.logger.debug("compare: "+ compare + " minLoadTime: "+ MID_LOAD_TIME);
//            }
            driver.get(webUrl);

            Simulate.logger.debug("create url project: " + dimension.getWidth());

        } catch (Exception e) {
            //打开网页错误或超时，结束加载网页, 继续后续js操作
            Simulate.logger.warn("Simulate connectToUrl error:", e);
        }
    }
}
