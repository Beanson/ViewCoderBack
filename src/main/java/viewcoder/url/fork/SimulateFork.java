package viewcoder.url.fork;

import org.openqa.selenium.By;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import viewcoder.operation.impl.common.CommonService;
import viewcoder.url.SimulateTime;

import java.util.*;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.TimeUnit;


/**
 * Created by Administrator on 2018/5/8.
 */
@Deprecated
public class SimulateFork  {
    private static Logger logger = LoggerFactory.getLogger(SimulateFork.class);
    private static int cores = Runtime.getRuntime().availableProcessors(); //获取CPU的核数

    public static void main(String[] args) {
        // "http://baidu.com"
        // "http://www.ccb.com/cn/home/indexv3.html"
        // "https://baike.baidu.com/item/%E4%B8%AD%E5%9B%BD%E5%A5%BD%E9%A1%B9%E7%9B%AE/15888364?fr=aladdin"
        createProject("https://baike.baidu.com/item/%E4%B8%AD%E5%9B%BD%E5%A5%BD%E9%A1%B9%E7%9B%AE/15888364?fr=aladdin");
    }

    /**
     * 通过url生成web的fork方式
     *
     * @param webUrl　需要模仿的网站URL
     */
    public static void createProject(String webUrl) {
        //基础数据准备
        HashMap<String, HashMap<String, Object>> map = new HashMap<>();
        map.put("Common_Text", new HashMap<String, Object>());
        map.put("Common_Image", new HashMap<String, Object>());
        map.put("Common_Background", new HashMap<String, Object>());
        SimulateTime simulateTime = new SimulateTime(); //统计时间对象
        simulateTime.setTotalBeginTime(CommonService.getDateTime());

        WebDriver driver = browserInit(simulateTime, webUrl); //初始化web driver

        //selenium获取页面上div/span/img三种element的统计处理
        List<WebElement> allElements = new ArrayList<>();
        allElements.addAll(driver.findElements(By.xpath("//div | //img | //span")));

        try {
            //Fork,Join处理，遍历source网站的element，并提取出需要的element和信息放进list中
            List<Map<String, Object>> list = eleRender(allElements);
            //把数据进行整理
            int id = 0;
            for (Map<String, Object> ele :
                    list) {
                if (CommonService.checkNotNull(ele.get("type"))) {
                    ele.put("layer_id", id++);
                    ele.put("name", ele.get("type") + "_" + id);
                    map.get(ele.get("type")).put(String.valueOf(id), ele);
                }
            }

        } catch (Exception e) {
            SimulateFork.logger.warn("SimulateFork multi barrier thread error", e);
        }

        simulateTime.setTotalEndTime(CommonService.getDateTime());
        //打印日志
        printLog(simulateTime, webUrl, allElements, map);
        driver.close();
        driver.quit();
    }


    /**
     * 调用打开浏览器并返回一个driver对象
     *
     * @param simulateTime　记录时间的对象
     * @param webUrl　需要模仿的网站URL
     * @return
     */
    public static WebDriver browserInit(SimulateTime simulateTime, String webUrl) {
        simulateTime.setBrowserBeginTime(CommonService.getDateTime());
        System.setProperty("webdriver.chrome.driver", "src/main/resources/driver/chromedriver.exe");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("headless");
        options.addArguments("window-size=1366x768");
        options.addArguments("--proxy-server='direct://'");
        options.addArguments("--proxy-bypass-list=*");
        WebDriver driver = new ChromeDriver(options);

        try {
            driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
            driver.navigate().to(webUrl);
            driver.manage().window().maximize();

        } catch (Exception e) {
            //超时也接着继续进行
            SimulateFork.logger.warn("SimulateFork wait exceed 10 seconds error", e);

        } finally {
            simulateTime.setBrowserEndTime(CommonService.getDateTime());
        }
        return driver;
    }


    /**
     * 提取文本信息，仅在当前div或span的文本
     *
     * @param list    装载span元素数据的列表
     * @param element div或span元素的组件
     */
    public static void extractTextElement(List<Map<String, Object>> list, WebElement element) {
        //String text = element.getText();
        String text = getTextNode(element);
        if (CommonService.checkNotNull(text)) {
            HashMap<String, Object> layer = new HashMap<>();
            generalWidget(layer, element, 10); //基础位置大小数据配置
            commonText(layer, element, text); //对text进行样式设置
            list.add(layer);
        }
    }

    /**
     * 渲染source页面上的element
     *
     * @param elements     source页面的div元素组件
     * @return
     */
    private static List<Map<String, Object>> eleRender(List<WebElement> elements) {
        ForkJoinPool fjp = new ForkJoinPool(cores); // 最大并发数
        ForkJoinTask<List<Map<String, Object>>> task = new EleForkTask(0, elements.size(), elements);
        return fjp.invoke(task);
    }


    public static void generalWidget(HashMap<String, Object> layer, WebElement element, int layerId) {
        Point classname = element.getLocation();
        //layer.put("layer_id", id);
        layer.put("layer_rate", layerId);
        layer.put("left", classname.getX());
        layer.put("top", classname.getY());
        layer.put("width", element.getSize().getWidth());
        layer.put("height", element.getSize().getHeight());
    }


    /**
     * 图片和背景都有进行如下方法的调用
     *
     * @param layer   图层信息
     * @param element HTML的Dom
     */
    public static void generalBackground(HashMap<String, Object> layer, WebElement element) {
        layer.put("bg-color", element.getCssValue("background-color"));
        layer.put("border-color", "rgba(0,0,0,0)");
        layer.put("border-top-width", "0");
        layer.put("border-left-width", "0");
        layer.put("border-right-width", "0");
        layer.put("border-bottom-width", "0");
        layer.put("border-top-left-radius", "0");
        layer.put("border-top-right-radius", "0");
        layer.put("border-bottom-left-radius", "0");
        layer.put("border-bottom-right-radius", "0");

//        layer.put("border-color", element.getCssValue("border-color"));
//        layer.put("border-left-width", element.getCssValue("border-left-width").replace("px",""));
//        layer.put("border-top-width", element.getCssValue("border-top-width").replace("px",""));
//        layer.put("border-right-width", element.getCssValue("border-right-width").replace("px",""));
//        layer.put("border-bottom-width", element.getCssValue("border-bottom-width").replace("px",""));
//        layer.put("border-top-left-radius", element.getCssValue("border-top-left-radius").replace("px",""));
//        layer.put("border-top-right-radius", element.getCssValue("border-top-right-radius").replace("px",""));
//        layer.put("border-bottom-left-radius", element.getCssValue("border-bottom-left-radius").replace("px",""));
//        layer.put("border-bottom-right-radius", element.getCssValue("border-bottom-right-radius").replace("px",""));
    }


    /**
     * Common_Image组件
     *
     * @param layer   组件层级
     * @param element HTML元素
     */
    public static void commonImage(HashMap<String, Object> layer, WebElement element, String imageSrc) {
        //图片资源处理*******
        layer.put("type", "Common_Image");
        //layer.put("name", "img_" + id);
        layer.put("image_reposition", false);
        layer.put("src", imageSrc.substring(5, imageSrc.length() - 2)); //src 形式如下 url("http://..."),需要提取url("")引号中间的内容
        layer.put("bg-position-left", 0);
        layer.put("bg-position-top", 0);
        layer.put("bg-repeat", element.getCssValue("background-repeat"));
        layer.put("bg-size", element.getCssValue("background-size"));
        generalBackground(layer, element); //设置基础background
    }


    /**
     * Common_Text组件
     *
     * @param layer   组件层级
     * @param element HTML元素
     * @param text    文本消息
     */
    public static void commonText(HashMap<String, Object> layer, WebElement element, String text) {
        //文本资源处理********
        layer.put("type", "Common_Text");
        //layer.put("name", "text_" + id);
        layer.put("text-editable", false); //为true时可以通过对文字内容进行编辑处理
        layer.put("text", text);
        layer.put("temp_text", text);
        layer.put("font-size", element.getCssValue("font-size").replace("px", ""));
        layer.put("font-weight", element.getCssValue("font-weight"));
        layer.put("line-height", element.getCssValue("line-height"));
        layer.put("text-align", element.getCssValue("text-align"));
        layer.put("font-family", element.getCssValue("font-family"));
        layer.put("font-style", element.getCssValue("font-style"));
        layer.put("font-color", element.getCssValue("color"));
        layer.put("text-decoration", element.getCssValue("text-decoration"));
    }


    /**
     * 获取该div中的文字的内容等
     *
     * @param e 标签组件
     * @return
     */
    public static String getTextNode(WebElement e) {
        String text = e.getText();
        List<WebElement> children = e.findElements(By.xpath("./*"));
        for (WebElement child : children) {
            if (child.getText() != null && !child.getText().equals("")) {
                text = text.replaceFirst(child.getText(), "").trim();
            }
        }
        return text.trim();
    }


    public static void renderDiv(WebElement element, List<Map<String, Object>> list) {
        String bgImage = element.getCssValue("background-image");
        String bgColor = element.getCssValue("background-color");
        if (!Objects.equals(bgImage, "none")) {
            //背景图片资源处理******
            HashMap<String, Object> layer = new HashMap<>();
            SimulateFork.generalWidget(layer, element, 1);//基础位置大小数据配置
            SimulateFork.commonImage(layer, element, bgImage); //对图片资源层级进行customize的设置
            list.add(layer);

        } else if (!Objects.equals(bgColor, "rgba(0, 0, 0, 0)") && !Objects.equals(bgColor, "rgb(0, 0, 0)")
                && !Objects.equals(bgColor, "transparent")) {
            //纯背景资源处理
            HashMap<String, Object> layer = new HashMap<>();
            SimulateFork.generalWidget(layer, element, 1); //基础位置大小数据配置

            //对背景资源层级进行customize的设置
            layer.put("type", "Common_Background");
            SimulateFork.generalBackground(layer, element);//设置基础background
            list.add(layer);
        }
        //获取div中的文本消息
        SimulateFork.extractTextElement(list, element);
    }


    public static void renderImg(WebElement element, List<Map<String, Object>> list) {
        String bgImage = element.getAttribute("src");
        HashMap<String, Object> layer = new HashMap<>();
        SimulateFork.generalWidget(layer, element, 1);//基础位置大小数据配置
        SimulateFork.commonImage(layer, element, bgImage); //对图片资源层级进行customize的设置
        list.add(layer);
    }


    /**
     * 打印运行日志的信息
     *
     * @param simulateTime 时间纪录对象
     * @param webUrl       制定的url
     * @param webUrl       制定的url
     * @return
     */
    public static void printLog(SimulateTime simulateTime, String webUrl, List<WebElement> allElements,
                                HashMap<String, HashMap<String, Object>> map) {

        SimulateFork.logger.debug("simulate browser url: " + webUrl);

        SimulateFork.logger.debug("total begin time: " + simulateTime.getTotalBeginTime() +
                " ,end time: " + simulateTime.getTotalEndTime() + " ,use:" + simulateTime.getTotalTimeLength());

        SimulateFork.logger.debug("browser begin time: " + simulateTime.getBrowserBeginTime() +
                " ,end time: " + simulateTime.getBrowserEndTime() + " ,use:" + simulateTime.getBrowserTimeLength());

        SimulateFork.logger.debug("total element number: " + allElements.size() + " ,Image Num:" + map.get("Common_Image").size() +
                " ,Background Num:" + map.get("Common_Background").size() + " ,Span Num:" + map.get("Common_Text").size());

    }
}
