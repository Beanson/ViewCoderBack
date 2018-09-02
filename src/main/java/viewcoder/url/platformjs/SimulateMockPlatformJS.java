package viewcoder.url.platformjs;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import viewcoder.operation.entity.ProjectProgress;

import java.net.URL;
import java.util.concurrent.TimeUnit;


/**
 * Created by Administrator on 2018/5/8.
 */
public class SimulateMockPlatformJS {
    private static Logger logger = LoggerFactory.getLogger(SimulateMockPlatformJS.class);

    public static void main(String[] args) throws Exception {
        //  http://www.hao365.org.cn/
        //  http://www.baidu.com
        //  https://baike.baidu.com/item/%E4%B8%AD%E5%9B%BD%E5%A5%BD%E9%A1%B9%E7%9B%AE/15888364?fr=aladdin
        createProject("https://baike.baidu.com/item/%E4%B8%AD%E5%9B%BD%E5%A5%BD%E9%A1%B9%E7%9B%AE/15888364?fr=aladdin",
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
    public static void createProject(String webUrl, ProjectProgress projectProgress, int totalWidth, int totalHeight) throws Exception {

        //初始化driver
        //设置必要参数
        DesiredCapabilities dcaps = new DesiredCapabilities();
        //ssl证书支持
        dcaps.setCapability("acceptSslCerts", true);
        dcaps.setJavascriptEnabled(true);
        //驱动支持（第二参数表明的是你的phantomjs引擎所在的路径，使用whereis phantomjs可以查看）
        dcaps.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY,
                "E:\\platformjs\\phantomjs-2.1.1-windows\\bin\\phantomjs.exe");
        PhantomJSDriver driver = new PhantomJSDriver(dcaps);
        driver.manage().timeouts().pageLoadTimeout(5, TimeUnit.SECONDS);
        //连接网页
        try {
            driver.get(webUrl);
        } catch (Exception e) {
//            JavascriptExecutor js2 = (JavascriptExecutor) driver;
//            js2.executeScript("window.stop ? window.stop() : document.execCommand('Stop');");
            e.printStackTrace();
        }

        try {
            //开始页面元素渲染，嵌入执行JavaScript脚本
            JavascriptExecutor js = (JavascriptExecutor) driver;
            //加载提取网站元素的js文件
            URL simulateURL = Resources.getResource("js/test_simulate/simulate3.js");
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
