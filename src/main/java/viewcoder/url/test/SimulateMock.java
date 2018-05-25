package viewcoder.url.test;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.apache.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import viewcoder.operation.entity.ProjectProgress;
import viewcoder.operation.impl.common.CommonService;
import viewcoder.url.SimulateTime;

import java.net.URL;
import java.util.concurrent.TimeUnit;


/**
 * Created by Administrator on 2018/5/8.
 */
public class SimulateMock {
    private static Logger logger = Logger.getLogger(SimulateMock.class);

    public static void main(String[] args) throws Exception {
        createProject("http://www.hao365.org.cn/",
                new ProjectProgress(), 1300, 700);
//        createProject("https://stackoverflow.com/questions/43734797/page-load-strategy-for-chrome-driver-updated-till-selenium-v3-12-0",
//                new ProjectProgress(), 1300, 700);
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
    public static void createProject(String webUrl, ProjectProgress projectProgress, int totalWidth, int totalHeight) throws Exception {
        //基础数据准备
        SimulateTime simulateTime = new SimulateTime(); //统计时间对象
        simulateTime.setTotalBeginTime(CommonService.getDateTime());
        browserInit(simulateTime, projectProgress, webUrl, totalWidth, totalHeight); //初始化web driver
        //如果driver启动失败则返回null


    }

    /**
     * 调用打开浏览器并返回一个driver对象
     *
     * @param simulateTime 监测记录时间的对象
     * @param webUrl       目标网站的网址
     * @return
     */
    public static void browserInit(SimulateTime simulateTime, ProjectProgress projectProgress, String webUrl,
                                   int totalWidth, int totalHeight) throws InterruptedException {
        //初始化浏览器driver
        simulateTime.setBrowserBeginTime(CommonService.getDateTime());
        System.setProperty("webdriver.chrome.driver", "src/main/resources/driver/chromedriver.exe");
//        ChromeOptions options = new ChromeOptions();
//        DesiredCapabilities capabilities = DesiredCapabilities.chrome();
//        capabilities.setCapability(ChromeOptions.CAPABILITY, options);
//        capabilities.setCapability("pageLoadStrategy", "none");
//        WebDriver driver = new ChromeDriver(capabilities);
        WebDriver driver = new ChromeDriver();
        driver.manage().deleteAllCookies();
        driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
        try {
            driver.get(webUrl);
        } catch (Exception e) {
            JavascriptExecutor js2 = (JavascriptExecutor) driver;
            js2.executeScript("return document.readyState");
            logger.error("load failure", e);
        }

        try {
            System.out.println("come to 1");
            //开始页面元素渲染，嵌入执行JavaScript脚本
            JavascriptExecutor js = (JavascriptExecutor) driver;
            //加载jQuery文件
            URL jqueryUrl = Resources.getResource("js/simulate_jquery/jquery.min.js");
            String jqueryText = Resources.toString(jqueryUrl, Charsets.UTF_8);
            js.executeScript(jqueryText);
            System.out.println("come to 2");
            //加载提取网站元素的js文件
            URL simulateURL = Resources.getResource("js/simulate_jquery/simulate.js");
            String simulateScript = Resources.toString(simulateURL, Charsets.UTF_8);
            String projectData = (String) js.executeScript(simulateScript);
            System.out.println("come to 3");
            logger.debug("get project data:" + projectData);

        } catch (Exception e) {
            logger.error("javascript failure", e);
        }
        driver.close();
        driver.quit();
        driver = null;
    }

}
