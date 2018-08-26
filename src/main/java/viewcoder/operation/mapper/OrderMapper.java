package viewcoder.operation.mapper;

import viewcoder.operation.entity.Orders;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/2/18.
 */

public interface OrderMapper {

    //************************* 以下是orders表的我的订单面板数据操作 ******************************************

    /**************************以下是select操作**********************************/
    //根据用户user_id获取该用户所有订单order的数据
    @Select("select * from orders where user_id=#{userId}")
    List<Orders> getOrderList(int userId);

    //根据用户user_id获取该用户所有订单order的数据
    @SelectProvider(type = SqlProvider.class, method = "targetOrdersSqlProvider")
    List<Orders> getTargetOrderList(Map<String, Object> map);

    //根据外部订单号out_trade_no获取数据库记录
    @Select("select count(*) from orders where out_trade_no=#{outTradeNo}")
    int getOrderNumByTradeNo(String outTradeNo);

    //根据外部订单号out_trade_no获取数据库记录
    @Select("select * from orders where out_trade_no=#{out_trade_no} and trade_no=#{trade_no}")
    Orders getOrderByTradeNo(@Param("out_trade_no") String out_trade_no, @Param("trade_no") String trade_no);

    //根据company_credit查询对应的order信息
    @Select("select * from orders where company_credit=#{companyCredit}")
    List<Orders> getCompanyDiscountOrder(String companyCredit);


    /**************************以下是insert操作**********************************/
    //插入新的order数据
    @Insert("insert into orders(user_id,service_id,service_num,order_date,pay_way,price)" +
            " values(#{user_id},#{service_id},#{service_num},#{order_date},#{pay_way},#{price})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insertNewOrderItem(Orders orders);

    //新注册用户三天免费试用order的数据插入
    @Insert("insert into orders(out_trade_no, user_id, service_id, service_num, order_date, pay_date, expire_date, " +
            "expire_days, pay_status, pay_way, price) values(#{out_trade_no}, #{user_id}, #{service_id}, #{service_num}, " +
            "#{order_date}, #{pay_date}, #{expire_date}, #{expire_days}, #{pay_status}, #{pay_way}, #{price})")
    int newRegisterTryOrder(Orders orders);


    /**************************以下是delete操作**********************************/
    //更新order支付状态的数据
    @Update("delete from orders where id=#{id}")
    int deleteOrderItem(int id);


    /**************************以下是update操作**********************************/
    //更新order支付状态的数据
    @Update("update orders set out_trade_no=#{out_trade_no}, trade_no=#{trade_no}, pay_date=#{pay_date}, " +
            "expire_date=#{expire_date}, expire_days=#{expire_days}, pay_status=1 where id=#{id}")
    int updateOrderPayment(Orders orders);


    //************************* 以下是orders表的我的套餐面板数据操作 ******************************************

    /**************************以下是select操作**********************************/
    @Select("select out_trade_no, service_id, service_num, pay_date, expire_date, expire_days from " +
            "orders where user_id=#{user_id}")
    List<Orders> getInstanceByUserId(int userId);

    //选择即将过期和已经过期的订单实例
    @Select("select * from orders where expire_days in (0, 1, 3, 7)")
    public List<Orders> getToExpireInstance(int user_id);


    /**************************以下是update操作**********************************/
    //设置过期时间减少一天，每天跑batch job测试该部分代码
    @Update("update orders set expire_days=(expire_days-1)")
    public int updateInstanceExpireDays(int instanceId);
}






