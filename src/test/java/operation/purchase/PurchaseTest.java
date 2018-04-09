package operation.purchase;

import FrontEnd.helper.common.Mapper;
import FrontEnd.myBatis.MybatisUtils;
import FrontEnd.myBatis.entity.Orders;
import FrontEnd.myBatis.operation.common.CommonService;
import FrontEnd.myBatis.operation.purchase.AliPay;
import com.sun.org.apache.xpath.internal.operations.Or;
import operation.project.ProjectTest;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.File;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/3/11.
 */
public class PurchaseTest {

    private static Logger logger = Logger.getLogger(PurchaseTest.class);

    @Test
    public void testGetOrderList(){
        Map<String, Object> map = new HashMap<>();
        //获取所有该用户的order数据
        map.put("user_id", 1);
        CommonService.junitReqRespVerify(map,"getOrderList",200);
    }

    /**
     * 根据条件搜索指定范围的list时，service_id, pay_status 如果传递all，则不进行作为筛选条件，全部返回
     */
    @Test
    public void testGetTargetOrderList(){
        Map<String, Object> map = new HashMap<>();
        //获取所有该用户指定范围的order数据
        map.put("user_id", 1);
        map.put("service_id", 1);
        map.put("order_from_date", "2005-11-28 12:31:28");
        map.put("order_end_date", "2005-11-28 12:31:28");
        CommonService.junitReqRespVerify(map,"getTargetOrderList",200);
    }

    @Test
    public void testAddNewOrderItem(){
        Map<String, Object> map = new HashMap<>();
        //插入新的order数据
        map.put("user_id", 1);
        map.put("service_id", 1);
        map.put("service_num", 1);
        map.put("subject", "套餐日销版 * 3");
        map.put("price", 2.8);
        CommonService.junitReqRespVerify(map,"insertNewOrderItem",200);
    }

    @Test
    public void testUpdateOrderTime(){
        Orders orders=new Orders();
        orders.setService_id(3);
        orders.setService_num(10);
        try {
            AliPay.invokePayment(orders);
            PurchaseTest.logger.debug(orders);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
