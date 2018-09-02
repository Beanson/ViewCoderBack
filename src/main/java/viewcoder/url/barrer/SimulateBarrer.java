package viewcoder.url.barrer;

import com.alibaba.fastjson.JSON;
import org.openqa.selenium.By;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import viewcoder.operation.entity.ProjectProgress;
import viewcoder.operation.impl.common.CommonService;
import viewcoder.psd.entity.PsdInfo;
import viewcoder.url.SimulateTime;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;


/**
 * Created by Administrator on 2018/5/8.
 */
@Deprecated
public class SimulateBarrer {
    private static Logger logger = LoggerFactory.getLogger(SimulateBarrer.class);

//    public static void main(String[] args) throws Exception{
//        //createProject("https://baidu.com", new ProjectProgress(), 1300, 700);
//        CreateProject.createSimulateOpt("https://baidu.com", new ProjectProgress(), 1300,
//                700,1,"url1");
//    }

    public static String createProject(String webUrl, ProjectProgress projectProgress, int totalWidth, int totalHeight) throws Exception {
        //基础数据准备
        HashMap<String, HashMap<String, Object>> map = new HashMap<>();
        map.put("Common_Image", new HashMap<String, Object>());
        map.put("Common_Text", new HashMap<String, Object>());
        map.put("Common_Background", new HashMap<String, Object>());
        SimulateTime simulateTime = new SimulateTime(); //统计时间对象
        simulateTime.setTotalBeginTime(CommonService.getDateTime());
        WebDriver driver = browserInit(simulateTime, webUrl, totalWidth, totalHeight); //初始化web driver

        //selenium获取页面上div/span/img三种element的统计处理
        List<WebElement> divElements = driver.findElements(By.tagName("div"));
        List<WebElement> spanElements = driver.findElements(By.tagName("span"));
        List<WebElement> imgElements = driver.findElements(By.tagName("img"));
        int divId = 1; //生成的element需要赋值id值，从1开始
        int spanId = divElements.size() * 2 + 1; //*2是img或background之后的text预留
        int imgId = divElements.size() * 2 + spanElements.size() + 1;
        int maxId = divId + spanId + imgId;

        try {
            //分别对div/span/img的element连主线程共三个线程运行
            CyclicBarrier barrer = new CyclicBarrier(3);
            Thread spanRender = spanRender(barrer, spanElements, simulateTime, map, spanId);//span 元素解析thread
            Thread imgRender = imgRender(barrer, imgElements, simulateTime, map, imgId);//img 元素解析的thread
            spanRender.start();
            imgRender.start();

            divRender(divElements, divId, map, simulateTime, projectProgress);//div元素解析，在main thread中
            barrer.await();  //main thread等待另外两个线程完成继续

        } catch (Exception e) {
            SimulateBarrer.logger.warn("SimulateBarrer multi barrier thread error", e);
            throw e;
        }
        simulateTime.setTotalEndTime(CommonService.getDateTime()); //日志记录结束
        logAnalyze(webUrl, simulateTime, divElements, spanElements, imgElements); //打印日志分析

        driver.close();//关闭释放资源
        driver.quit();

        //返回渲染后的数据，以Json方式返回
        return wrapSimulateData(maxId, totalWidth, totalHeight, map);
    }


    private static String wrapSimulateData(int maxId, int totalWidth, int totalHeight, HashMap<String, HashMap<String, Object>> map) {
        HashMap<String, Object> overall = new HashMap<>();
        overall.put("width", totalWidth);
        overall.put("height", totalHeight);
        overall.put("is_mobile", false); //标识是否是mobile网页，默认是PC网页
        overall.put("scale", false);
        overall.put("bg-color", "rgba(250,0,0,0.04)");
        overall.put("max_id", maxId);
        overall.put("max_rate", 3);
        PsdInfo psdInfo = new PsdInfo();
        psdInfo.setOverall(overall);
        psdInfo.setAll_tools(map);
        return JSON.toJSONString(psdInfo);
    }


    /**
     * 调用打开浏览器并返回一个driver对象
     *
     * @param simulateTime
     * @param webUrl
     * @return
     */
    public static WebDriver browserInit(SimulateTime simulateTime, String webUrl, int totalWidth, int totalHeight) {
        simulateTime.setBrowserBeginTime(CommonService.getDateTime());
        System.setProperty("webdriver.chrome.driver", "src/main/resources/driver/chromedriver.exe");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("headless");
        options.addArguments("window-size=" + totalWidth + "x" + totalHeight);
        options.addArguments("--proxy-server='direct://'");
        options.addArguments("--proxy-bypass-list=*");
        WebDriver driver = new ChromeDriver(options);

        try {
            driver.manage().timeouts().pageLoadTimeout(20, TimeUnit.SECONDS);
            driver.navigate().to(webUrl);
            driver.manage().window().maximize();

        } catch (Exception e) {
            //超时也接着继续进行
            SimulateBarrer.logger.warn("SimulateBarrer wait exceed 20 seconds error", e);

        } finally {
            simulateTime.setBrowserEndTime(CommonService.getDateTime());
        }
        return driver;
    }


    /**
     * 提取文本信息
     *
     * @param map
     * @param spanId
     * @param element
     */
    public static void extractTextElement(HashMap<String, HashMap<String, Object>> map, int spanId, WebElement element) {
        //String text = getTextNode(element);
        String text = element.getText();
        if (CommonService.checkNotNull(text)) {
            HashMap<String, Object> layer = new HashMap<>();
            generalWidget(layer, element, spanId, 10); //基础位置大小数据配置
            commonText(layer, element, spanId, text); //对text进行样式设置
            map.get("Common_Text").put(String.valueOf(spanId), layer); //把text组件层级设置进map中
        }
    }

    /**
     * 解析div元素的方法
     *
     * @param divElements
     * @param divId
     * @param map
     */
    private static void divRender(List<WebElement> divElements, int divId, HashMap<String,
            HashMap<String, Object>> map, SimulateTime simulateTime, ProjectProgress projectProgress) {

        simulateTime.setDivBeginTime(CommonService.getDateTime());
        int divProgress = 0, divAllTask = divElements.size() + 5; //设定在render中无法达到100%的完成比例，在程序外侧完成其他操作后才显示100%
        //图片资源处理
        for (WebElement element :
                divElements) {
            try {
                String bgImage = element.getCssValue("background-image");
                String bgColor = element.getCssValue("background-color");
                if (!Objects.equals(bgImage, "none")) {
                    //背景图片资源处理******
                    HashMap<String, Object> layer = new HashMap<>();
                    generalWidget(layer, element, divId++, 1);//基础位置大小数据配置
                    commonImage(layer, element, bgImage, divId, true); //对图片资源层级进行customize的设置
                    map.get("Common_Image").put(String.valueOf(divId), layer);//把图片资源的层级设置进map中

                } else if (!Objects.equals(bgColor, "rgba(0, 0, 0, 0)") && !Objects.equals(bgColor, "rgb(0, 0, 0)")
                        && !Objects.equals(bgColor, "transparent")) {
                    //纯背景资源处理
                    HashMap<String, Object> layer = new HashMap<>();
                    generalWidget(layer, element, divId++, 1); //基础位置大小数据配置

                    //对背景资源层级进行customize的设置
                    layer.put("type", "Common_Background");
                    layer.put("name", "bg_" + divId);
                    generalBackground(layer, element);//设置基础background
                    map.get("Common_Background").put(String.valueOf(divId), layer);//把background组件层级设置进map中
                }
                //获取div中的文本消息
                extractTextElement(map, divId++, element);

            } catch (Exception e) {
                SimulateBarrer.logger.warn("SimulateBarrer backgroundAndImg error:", e);

            } finally {
                //添加到全局对象中，记录解析进度，返回前端查看进度
                projectProgress.setProgress((int) Math.floor((float)++divProgress / divAllTask * 100));
            }
        }
        simulateTime.setDivEndTime(CommonService.getDateTime());
    }

    /**
     * 解析span元素的thread
     *
     * @param barrer       线程barrer参数
     * @param spanElements span类型的element数组
     * @param simulateTime 统计模拟操作的时间对象
     * @return
     */
    private static Thread spanRender(final CyclicBarrier barrer, List<WebElement> spanElements, SimulateTime simulateTime,
                                     HashMap<String, HashMap<String, Object>> map, int spanId) {
        return new Thread(new Runnable() {
            public void run() {
                try {
                    try {
                        simulateTime.setSpanBeginTime(CommonService.getDateTime());
                        int id = spanId;
                        for (WebElement element :
                                spanElements) {
                            try {
                                extractTextElement(map, id++, element);
                            } catch (Exception e) {
                                SimulateBarrer.logger.warn("SimulateBarrer span element with error: " + e.getMessage());
                            }
                        }
                        simulateTime.setSpanEndTime(CommonService.getDateTime());
                    } catch (Exception e) {
                        SimulateBarrer.logger.warn("SimulateBarrer spanThread with error: " + e.getMessage());
                    }
                    barrer.await();

                } catch (Exception e) {
                    simulateTime.setSpanEndTime(CommonService.getDateTime());
                    SimulateBarrer.logger.warn("SimulateBarrer spanThread outer with error: " + e.getMessage());
                    barrer.reset();
                }
            }
        });
    }

    /**
     * 解析img元素的thread
     *
     * @param barrer
     * @param imgElements
     * @param simulateTime
     * @param map
     * @param imgId
     * @return
     */
    private static Thread imgRender(final CyclicBarrier barrer, List<WebElement> imgElements, SimulateTime simulateTime,
                                    HashMap<String, HashMap<String, Object>> map, int imgId) {
        return new Thread(new Runnable() {
            public void run() {
                try {
                    try {
                        simulateTime.setImgBeginTime(CommonService.getDateTime());
                        int id = imgId;
                        //img类型资源处理******
                        for (WebElement element :
                                imgElements) {
                            try {
                                String bgImage = element.getAttribute("src");
                                HashMap<String, Object> layer = new HashMap<>();
                                generalWidget(layer, element, id++, 1);//基础位置大小数据配置
                                commonImage(layer, element, bgImage, id, false); //对图片资源层级进行customize的设置
                                map.get("Common_Image").put(String.valueOf(id), layer);//把图片资源的层级设置进map中
                            } catch (Exception e) {
                                logger.warn("SimulateBarrer img element with exception", e);
                            }
                        }
                        simulateTime.setImgEndTime(CommonService.getDateTime());
                    } catch (Exception e) {
                        SimulateBarrer.logger.warn("imgThread with error: " + e.getMessage());
                    }
                    barrer.await();

                } catch (Exception e) {
                    simulateTime.setImgEndTime(CommonService.getDateTime());
                    SimulateBarrer.logger.warn("imgThread outer with error: " + e.getMessage());
                    barrer.reset();
                }
            }
        });
    }


    public static void generalWidget(HashMap<String, Object> layer, WebElement element, int id, int layerId) {
        Point classname = element.getLocation();
        layer.put("layer_id", id);
        layer.put("layer_rate", layerId);
        layer.put("left", classname.getX());
        layer.put("top", classname.getY());
        layer.put("width", element.getSize().getWidth());
        layer.put("height", element.getSize().getHeight());
        layer.put("show", true);
        layer.put("opacity", 1.0);
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
    }

    /**
     * Common_Image组件
     *
     * @param layer   组件层级
     * @param element HTML元素
     * @param id      id值
     */
    public static void commonImage(HashMap<String, Object> layer, WebElement element, String imageSrc, int id, boolean toSplice) {
        //图片资源处理*******
        layer.put("type", "Common_Image");
        layer.put("name", "img_" + id);
        layer.put("image_reposition", false);
        String src = "";
        if (toSplice) {
            src = imageSrc.substring(5, imageSrc.length() - 2); //对于div元素。src 形式如下 url("http://..."),需要提取url("")引号中间的内容
        } else {
            src = imageSrc; //对于img元素，直接可获取URL信息，无需转换
        }
        layer.put("src", src);
        layer.put("bg-position-left", 0);
        layer.put("bg-position-top", 0);
        //如果获取不是undefined，则进行赋值，否则默认赋值为no-repeat
        if (!Objects.equals(element.getCssValue("background-repeat"), "undefined")) {
            layer.put("bg-repeat", element.getCssValue("background-repeat"));
        } else {
            layer.put("bg-repeat", "no-repeat");
        }
        layer.put("bg-size", 101);
        generalBackground(layer, element); //设置基础background
    }

    /**
     * Common_Text组件
     *
     * @param layer   组件层级
     * @param element HTML元素
     * @param id      id值
     * @param text    文本消息
     */
    public static void commonText(HashMap<String, Object> layer, WebElement element, int id, String text) {
        //文本资源处理********
        layer.put("type", "Common_Text");
        layer.put("name", "text_" + id);
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


    /**
     * simulate 通过URL生成网页的log的分析
     *
     * @param webUrl       传递过来的WebUrl
     * @param simulateTime 记录日志的对象
     * @param divElements  源网页的div元素
     * @param spanElements 源网页的span元素
     * @param imgElements  源网页的img元素
     */
    private static void logAnalyze(String webUrl, SimulateTime simulateTime, List<WebElement> divElements,
                                   List<WebElement> spanElements, List<WebElement> imgElements) {

        SimulateBarrer.logger.debug("simulate browser url: " + webUrl);

        SimulateBarrer.logger.debug("total begin time: " + simulateTime.getTotalBeginTime() +
                " ,end time: " + simulateTime.getTotalEndTime() + " ,use:" + simulateTime.getTotalTimeLength());

        SimulateBarrer.logger.debug("browser begin time: " + simulateTime.getBrowserBeginTime() +
                " ,end time: " + simulateTime.getBrowserEndTime() + " ,use:" + simulateTime.getBrowserTimeLength());

        SimulateBarrer.logger.debug("div size: " + divElements.size() + " ,begin time:" + simulateTime.getDivBeginTime() +
                " ,end time:" + simulateTime.getDivEndTime() + " ,use:" + simulateTime.getDivTimeLength());

        SimulateBarrer.logger.debug("span size: " + spanElements.size() + " ,begin time:" + simulateTime.getSpanBeginTime() +
                " ,end time:" + simulateTime.getSpanEndTime() + " ,use:" + simulateTime.getSpanTimeLength());

        SimulateBarrer.logger.debug("img size: " + imgElements.size() + " ,begin time:" + simulateTime.getImgBeginTime() +
                " ,end time:" + simulateTime.getImgEndTime() + " ,use:" + simulateTime.getImgTimeLength());
    }
}
