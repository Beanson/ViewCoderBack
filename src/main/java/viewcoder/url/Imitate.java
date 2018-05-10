package viewcoder.url;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import viewcoder.operation.impl.common.CommonService;
import viewcoder.tool.common.Common;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


/**
 * Created by Administrator on 2018/5/8.
 */
public class Imitate {
    private static Logger logger = Logger.getLogger(Imitate.class);


    public static void main(String[] args) {
        HashMap<String, HashMap<String, Object>> map = new HashMap<>();
        map.put("Common_Image", new HashMap<String, Object>());
        map.put("Common_Text", new HashMap<String, Object>());
        map.put("Common_Background", new HashMap<String, Object>());
        String time1, time2, time3;
        String divBeginTime, divEndTime, spanBeginTime, spanEndTime, imgBeginTime, imgEndTime;
        time1 = CommonService.getDateTime();
        System.setProperty("webdriver.chrome.driver", "src/main/resources/driver/chromedriver.exe");

        WebDriver driver = new ChromeDriver();
        try {
            driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
            //Launch website
            //driver.navigate().to("http://www.ccb.com/cn/home/indexv3.html");
            driver.navigate().to("https://baike.baidu.com/item/%E4%B8%AD%E5%9B%BD%E5%A5%BD%E9%A1%B9%E7%9B%AE/15888364?fr=aladdin");
            //driver.navigate().to("http://baidu.com");

        } catch (Exception e) {
            logger.warn("Imitate wait exceed 10 seconds error", e);
        }

        //driver.navigate().to("http://127.0.0.1:3030/src/learning/testUrlToHtml.html");
        //Maximize the browser
        driver.manage().window().maximize();
        //List<WebElement> elements = driver.findElements(By.xpath("//div | //span"));
        List<WebElement> divElements = driver.findElements(By.tagName("div"));
        List<WebElement> spanElements = driver.findElements(By.tagName("span"));
        List<WebElement> imgElements = driver.findElements(By.tagName("img"));

        time2 = CommonService.getDateTime();
        int id = 1; //循环递归查看每个layer的id值

        divBeginTime = CommonService.getDateTime();
        //图片资源处理******
        for (WebElement element :
                divElements) {
            try {

//                String bgImage = element.getCssValue("background-image");
//                String bgColor = element.getCssValue("background-color");
//                if (!Objects.equals(bgImage, "none")) {
//                    //背景图片资源处理******
//                    HashMap<String, Object> layer = new HashMap<>();
//                    generalWidget(layer, element, id++);//基础位置大小数据配置
//                    commonImage(layer, element, bgImage, id); //对图片资源层级进行customize的设置
//                    map.get("Common_Image").put(String.valueOf(id), layer);//把图片资源的层级设置进map中
//
//
//                } else if (!Objects.equals(bgColor, "rgba(0, 0, 0, 0)") && !Objects.equals(bgColor, "rgb(0, 0, 0)")
//                        && !Objects.equals(bgColor, "transparent")) {
//                    //纯背景资源处理******
//                    HashMap<String, Object> layer = new HashMap<>();
//                    generalWidget(layer, element, id++); //基础位置大小数据配置
//
//                    //对背景资源层级进行customize的设置
//                    layer.put("type", "Common_Background");
//                    layer.put("name", "bg_" + id);
//                    generalBackground(layer, element);//设置基础background
//                    map.get("Common_Background").put(String.valueOf(id), layer);//把background组件层级设置进map中
//                }

                String text = element.getText();
                if (CommonService.checkNotNull(text)) {
                    HashMap<String, Object> layer = new HashMap<>();
                    generalWidget(layer, element, id++); //基础位置大小数据配置
                    commonText(layer, element, id, text); //对text进行样式设置
                    map.get("Common_Text").put(String.valueOf(id), layer); //把text组件层级设置进map中
                }

            } catch (Exception e) {
                logger.warn("get text exception", e);
            }
        }
        divEndTime = CommonService.getDateTime();

        spanBeginTime = CommonService.getDateTime();
        //纯文本资源处理******
        for (WebElement element :
                spanElements) {
            try {
                String text = element.getText();
                if (CommonService.checkNotNull(text)) {
                    HashMap<String, Object> layer = new HashMap<>();
                    generalWidget(layer, element, id++); //基础位置大小数据配置
                    commonText(layer, element, id, text); //对text进行样式设置
                    map.get("Common_Text").put(String.valueOf(id), layer); //把text组件层级设置进map中
                }
            } catch (Exception e) {
                logger.warn("span deal with exception", e);
            }
        }
        spanEndTime = CommonService.getDateTime();

        imgBeginTime = CommonService.getDateTime();
        //img类型资源处理******
        for (WebElement element :
                imgElements) {
            try {
                String bgImage = element.getAttribute("src");
                HashMap<String, Object> layer = new HashMap<>();
                generalWidget(layer, element, id++);//基础位置大小数据配置
                commonImage(layer, element, bgImage, id); //对图片资源层级进行customize的设置
                map.get("Common_Image").put(String.valueOf(id), layer);//把图片资源的层级设置进map中
            } catch (Exception e) {
                logger.warn("img deal with exception", e);
            }
        }
        imgEndTime = CommonService.getDateTime();


        time3 = CommonService.getDateTime();
        logger.debug("div size: " + divElements.size()+" ,begin time:"+divBeginTime+" ,end time:"+divEndTime);
        logger.debug("span size: " + spanElements.size()+" ,begin time:"+spanBeginTime+" ,end time:"+spanEndTime);
        logger.debug("img size: " + imgElements.size()+" ,begin time:"+imgBeginTime+" ,end time:"+imgEndTime);
        logger.debug("Time is: " + time1 + " ," + time2 + " ," + time3);
        logger.debug("Map is: " + map);
        //logger.debug("whole html is: " + driver.getPageSource());

        driver.close();
        driver.quit();
    }


    public static void generalWidget(HashMap<String, Object> layer, WebElement element, int id) {
        Point classname = element.getLocation();
        layer.put("layer_id", id);
        layer.put("layer_rate", id);
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
//        layer.put("border-color", element.getCssValue("border-color"));
//        layer.put("border-top-width", element.getCssValue("border-top-width").replace("px",""));
//        layer.put("border-right-width", element.getCssValue("border-right-width").replace("px",""));
//        layer.put("border-bottom-width", element.getCssValue("border-bottom-width").replace("px",""));
//        layer.put("border-left-width", element.getCssValue("border-left-width").replace("px",""));
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
     * @param id      id值
     */
    public static void commonImage(HashMap<String, Object> layer, WebElement element, String imageSrc, int id) {
        //图片资源处理*******
        layer.put("type", "Common_Image");
        layer.put("name", "img_" + id);
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
     * @param id      id值
     * @param text    文本消息
     */
    public static void commonText(HashMap<String, Object> layer, WebElement element, int id, String text) {
        //文本资源处理********
        layer.put("type", "Common_Background");
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

}
