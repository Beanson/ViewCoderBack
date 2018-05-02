package viewcoder.tool.common;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2018/4/16.
 * 一些对象数据的初始化
 */
public class CommonObject {

    //积分兑换套餐服务
    private static final Map<Integer, Integer> serviceToPoints = new HashMap<Integer, Integer>();
    private static final Map<String, Double> extendPrice = new HashMap<String, Double>();
    private static final Map<String, String> xmlMap = new HashMap<>();
    private static final Map<Integer, String> serviceName = new HashMap<>();

    static {
        //初始化积分兑换套餐的数据
        serviceToPoints.put(1, 3);
        serviceToPoints.put(2, 30);

        //续期的价格计算
        extendPrice.put("day", 2.8);
        extendPrice.put("month", 38.3);
        extendPrice.put("day", 388.0);
        //扩容价格计算
        extendPrice.put("M", 1.0);
        extendPrice.put("G", 10.0);

        //套餐名称
        serviceName.put(1,"日套餐");
        serviceName.put(2,"月套餐");
        serviceName.put(3,"年套餐");

        //xml的map
        xmlMap.put("msg", "config/msg.xml");
        xmlMap.put("general", "xml/general.xml");
    }

    public static Map<Integer, Integer> getServiceToPoints() {
        return serviceToPoints;
    }

    public static Map<String, Double> getExtendPrice() {
        return extendPrice;
    }

    public static Map<String, String> getXmlMap() {
        return xmlMap;
    }

    public static String getServiceName(int serviceId) {
        return serviceName.get(serviceId);
    }
}
