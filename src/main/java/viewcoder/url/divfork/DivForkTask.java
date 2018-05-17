package viewcoder.url.divfork;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebElement;
import viewcoder.tool.cache.GlobalCache;
import viewcoder.tool.common.Common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Administrator on 2018/5/11.
 */
@Deprecated
public class DivForkTask extends RecursiveTask<List<Map<String, Object>>> {

    private static Logger logger = Logger.getLogger(DivForkTask.class);
    private AtomicInteger atomicInteger = new AtomicInteger(0);
    private List<WebElement> elements;
    private String projectMark;
    private int threshold, start, end;

    public DivForkTask(int start, int end, List<WebElement> elements, int threshold, String projectMark) {
        this.start = start;
        this.end = end;
        this.elements = elements;
        this.threshold = threshold;
        this.projectMark = projectMark;
    }

    @Override
    protected List<Map<String, Object>> compute() {
        if (end - start <= threshold) {
            List<Map<String, Object>> list = new ArrayList<>();
            for (WebElement element :
                    elements.subList(start, end)) {
                try {
                    DivSimulate.renderDiv(element, list);
                } catch (Exception e) {
                    DivForkTask.logger.warn("SimulateFork backgroundAndImg error:", e);
                }
            }
            //计算cache并装到
            int progress = atomicInteger.addAndGet(threshold);
            //GlobalCache.getSimulateCache().put(projectMark,(int)(progress/ elements.size()));
            return list;
        }

        //如果任务太大, 则一分为二:
        int middle = (end + start) / 2;
        System.out.println(String.format("split %d~%d ==> %d~%d, %d~%d", start, end, start, middle, middle, end));
        DivForkTask subtask1 = new DivForkTask(start, middle, elements, threshold, projectMark);
        DivForkTask subtask2 = new DivForkTask(middle, end, elements, threshold, projectMark);
        invokeAll(subtask1, subtask2);
        List<Map<String, Object>> subresult1 = subtask1.join();
        List<Map<String, Object>> subresult2 = subtask2.join();

        List<Map<String, Object>> list = new ArrayList<>();
        list.addAll(subresult1);
        list.addAll(subresult2);
        return list;
    }
}
