package viewcoder.url.test;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.apache.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
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
        createProject("https://baike.baidu.com/item/%E4%B8%AD%E5%9B%BD%E5%A5%BD%E9%A1%B9%E7%9B%AE/15888364?fr=aladdin",
                new ProjectProgress(),1300, 700);
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
        WebDriver driver = browserInit(simulateTime, projectProgress, webUrl, totalWidth, totalHeight); //初始化web driver

        //如果driver启动失败则返回null
        if (driver == null) {
            return;
        }

        //开始页面元素渲染，嵌入执行JavaScript脚本
        JavascriptExecutor js = (JavascriptExecutor) driver;
        //加载jQuery文件
        URL jqueryUrl = Resources.getResource("js/jquery.min.js");
        String jqueryText = Resources.toString(jqueryUrl, Charsets.UTF_8);
        js.executeScript(jqueryText);
        //加载提取网站元素的js文件
        new Thread().sleep(5000);

        URL simulateURL = Resources.getResource("js/simulateTest.js");
        String simulateScript = Resources.toString(simulateURL, Charsets.UTF_8);
        String projectData = (String) js.executeScript(simulateScript);
        simulateTime.setTotalEndTime(CommonService.getDateTime());

        System.out.println("Total time: " + simulateTime.getTotalTimeLength() + " ,Open browser time: " + simulateTime.getBrowserTimeLength() +
                " ,Open url time:" + simulateTime.getGetUrlTimeLength() + " and open url try times:" + simulateTime.getGetUrlTimes());
        driver.close();//关闭释放资源
        driver.quit();

        System.out.println(projectData);
    }

    /**
     * 调用打开浏览器并返回一个driver对象
     *
     * @param simulateTime 监测记录时间的对象
     * @param webUrl       目标网站的网址
     * @return
     */
    public static WebDriver browserInit(SimulateTime simulateTime, ProjectProgress projectProgress, String webUrl,
                                        int totalWidth, int totalHeight) {
        //初始化浏览器driver
        simulateTime.setBrowserBeginTime(CommonService.getDateTime());
        System.setProperty("webdriver.chrome.driver", "src/main/resources/driver/chromedriver.exe");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-extensions");
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("window-size=" + totalWidth + "x" + totalHeight);
        WebDriver driver = new ChromeDriver(options);
        simulateTime.setBrowserEndTime(CommonService.getDateTime());
        projectProgress.setProgress(40); //设置到达40%进度

        simulateTime.setGetUrlBeginTime(CommonService.getDateTime());
        driver.manage().timeouts().pageLoadTimeout(35, TimeUnit.SECONDS);
        driver.manage().timeouts().setScriptTimeout(30, TimeUnit.SECONDS);

        //通过webDriver尝试连接该网站，连续两次超过30秒则返回错误信息。
        try {
            connectToUrl(driver, projectProgress, webUrl, simulateTime);

        } catch (Exception e) {
            //尝试连接两次，如果两次超时30秒则返回错误消息
            if (simulateTime.getGetUrlTimes() >= 2) {
                //超过两次30秒请求失败，返回错误消息
                SimulateMock.logger.warn("Simulate wait exceed 30 seconds twice error", e);
                driver = null;
            } else {
                connectToUrl(driver, projectProgress, webUrl, simulateTime);
            }
        }
        return driver;
    }

    /**
     * 用headless浏览器连接网站的操作
     *
     * @param driver       浏览器的driver程序
     * @param webUrl       目标网站网址
     * @param simulateTime 记录操作时间对象
     */
    public static void connectToUrl(WebDriver driver, ProjectProgress projectProgress, String webUrl, SimulateTime simulateTime) {
        simulateTime.setGetUrlTimes(simulateTime.getGetUrlTimes() + 1);
        driver.get(webUrl);
        driver.manage().window().maximize();
        simulateTime.setGetUrlEndTime(CommonService.getDateTime());
        projectProgress.setProgress(80);//成功打开并加载完成网站，到达80%进度
    }
}
