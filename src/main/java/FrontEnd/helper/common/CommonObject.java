package FrontEnd.helper.common;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2018/4/16.
 * 一些对象数据的初始化
 */
public class CommonObject {

    public static final Map<Integer,Integer> serviceToPoints=new HashMap<Integer,Integer>();

    static {
        serviceToPoints.put(1,3);
        serviceToPoints.put(2,30);
    }
}
