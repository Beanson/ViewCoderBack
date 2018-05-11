package viewcoder.url.fork;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebElement;
import viewcoder.url.Simulate;

import java.util.*;
import java.util.concurrent.RecursiveTask;

/**
 * Created by Administrator on 2018/5/11.
 */
public class DivForkTask extends RecursiveTask<List<Map<String, Object>>> {

    private static Logger logger = Logger.getLogger(DivForkTask.class);
    static final int THRESHOLD = 20;
    List<WebElement> divElements;
    int start;
    int end;

    DivForkTask(int start, int end, List<WebElement> divElements) {
        this.start = start;
        this.end = end;
        this.divElements=divElements;
    }

    @Override
    protected List<Map<String, Object>> compute() {
        if (end - start <= THRESHOLD) {
            List<Map<String, Object>> list = new ArrayList<>();
            for (WebElement element :
                    divElements.subList(start, end)) {
                try {
                    String bgImage = element.getCssValue("background-image");
                    String bgColor = element.getCssValue("background-color");
                    if (!Objects.equals(bgImage, "none")) {
                        //背景图片资源处理******
                        HashMap<String, Object> layer = new HashMap<>();
                        SimulateFork.generalWidget(layer, element, start++, 1);//基础位置大小数据配置
                        SimulateFork.commonImage(layer, element, bgImage, start); //对图片资源层级进行customize的设置
                        Map<String, Object> map= new HashMap<>();
                        map.put(String.valueOf(start), layer);
                        list.add(map);

                    } else if (!Objects.equals(bgColor, "rgba(0, 0, 0, 0)") && !Objects.equals(bgColor, "rgb(0, 0, 0)")
                            && !Objects.equals(bgColor, "transparent")) {
                        //纯背景资源处理
                        HashMap<String, Object> layer = new HashMap<>();
                        SimulateFork.generalWidget(layer, element, start++, 1); //基础位置大小数据配置

                        //对背景资源层级进行customize的设置
                        layer.put("type", "Common_Background");
                        layer.put("name", "bg_" + start);
                        SimulateFork.generalBackground(layer, element);//设置基础background
                        Map<String, Object> map= new HashMap<>();
                        map.put(String.valueOf(start), layer);
                        list.add(map);
                    }
                    //获取div中的文本消息
                    SimulateFork.extractTextElement(list, start++, element);

                } catch (Exception e) {
                    DivForkTask.logger.warn("Simulate backgroundAndImg error:", e);
                }
            }
            return list;
        }

        // 任务太大,一分为二:
        int middle = (end + start) / 2;
        System.out.println(String.format("split %d~%d ==> %d~%d, %d~%d", start, end, start, middle, middle, end));
        DivForkTask subtask1 = new DivForkTask(start, middle,divElements);
        DivForkTask subtask2 = new DivForkTask(middle, end,divElements);
        invokeAll(subtask1, subtask2);
        List<Map<String, Object>> subresult1 = subtask1.join();
        List<Map<String, Object>> subresult2 = subtask2.join();

        List<Map<String, Object>> list = new ArrayList<>();
        list.addAll(subresult1);
        list.addAll(subresult2);
        return list;
    }


}
