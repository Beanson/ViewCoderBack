package viewcoder.url.fork;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebElement;

import java.util.*;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Administrator on 2018/5/11.
 */
@Deprecated
public class EleForkTask  extends RecursiveTask<List<Map<String, Object>>> {

    private static Logger logger = Logger.getLogger(EleForkTask.class);
    AtomicInteger progress = new AtomicInteger(0);
    static final int THRESHOLD = 10;
    List<WebElement> elements;
    int start, end;

    EleForkTask(int start, int end, List<WebElement> elements) {
        this.start = start;
        this.end = end;
        this.elements = elements;
    }

    @Override
    protected List<Map<String, Object>> compute() {
        if (end - start <= THRESHOLD) {
            List<Map<String, Object>> list = new ArrayList<>();
            for (WebElement element :
                    elements.subList(start, end)) {
                try {
                    switch (element.getTagName()) {
                        case "div": {
                            SimulateFork.renderDiv(element, list);
                            break;
                        }
                        case "img": {
                            SimulateFork.renderImg(element, list);
                            break;
                        }
                        case "span": {
                            SimulateFork.extractTextElement(list, element);
                            break;
                        }
                        default: {
                            break;
                        }
                    }
                } catch (Exception e) {
                    EleForkTask.logger.warn("SimulateFork backgroundAndImg error:", e);
                }
            }
            progress.addAndGet(THRESHOLD);
            return list;
        }

        //如果任务太大, 则一分为二:
        int middle = (end + start) / 2;
        System.out.println(String.format("split %d~%d ==> %d~%d, %d~%d", start, end, start, middle, middle, end));
        EleForkTask subtask1 = new EleForkTask(start, middle, elements);
        EleForkTask subtask2 = new EleForkTask(middle, end, elements);
        invokeAll(subtask1, subtask2);
        List<Map<String, Object>> subresult1 = subtask1.join();
        List<Map<String, Object>> subresult2 = subtask2.join();

        List<Map<String, Object>> list = new ArrayList<>();
        list.addAll(subresult1);
        list.addAll(subresult2);
        return list;
    }


    /**
     * 发送最新的解析进度信息到前台
     */
    private void sendProgressNotify(){

    }
}
