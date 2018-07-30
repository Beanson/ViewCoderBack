package viewcoder.operation.impl.purchase;

import org.apache.commons.codec.binary.Hex;
import viewcoder.exception.purchase.PayException;
import viewcoder.tool.common.Assemble;
import viewcoder.tool.common.Common;
import viewcoder.tool.common.CommonObject;
import viewcoder.tool.common.Mapper;
import viewcoder.tool.encrypt.ECCUtil;
import viewcoder.tool.parser.form.FormData;
import viewcoder.tool.parser.text.TextData;
import viewcoder.tool.util.MybatisUtils;
import viewcoder.operation.entity.Instance;
import viewcoder.operation.entity.Orders;
import viewcoder.operation.entity.User;
import viewcoder.operation.entity.response.ResponseData;
import viewcoder.operation.entity.response.StatusCode;
import viewcoder.operation.impl.common.CommonService;
import viewcoder.operation.impl.purchase.wechat.WechatPay;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Administrator on 2018/3/11.
 */
public class Purchase {
    private static Logger logger = Logger.getLogger(Purchase.class.getName());

    /**
     * 刷新获取该实例方法的信息
     *
     * @param msg
     * @return
     */
    public static ResponseData refreshInstance(Object msg) {
        ResponseData responseData = new ResponseData(StatusCode.ERROR.getValue());
        SqlSession sqlSession = MybatisUtils.getSession();

        try {
            Map<String, Object> data = FormData.getParam(msg);
            Integer userId = Integer.parseInt(data.get(Common.USER_ID).toString());
            //根据user_id获取user表resource_remain和resource_used的数据
            User user = sqlSession.selectOne(Mapper.GET_USER_SPACE_INFO, userId);
            //根据user_id获取instance表数据
            List<Instance> instances = sqlSession.selectList(Mapper.GET_INSTANCE_BY_USER_ID, userId);
            //准备返回数据
            Map<String, Object> map = new HashMap<>();
            map.put(Common.SPACE_INFO, user);
            map.put(Common.INSTANCE_INFO, instances);
            Assemble.responseSuccessSetting(responseData, map);

        } catch (Exception e) {
            Assemble.responseErrorSetting(responseData, 500,
                    "refreshInstance error: " + e);
        } finally {
            CommonService.databaseCommitClose(sqlSession, responseData, false);
        }
        return responseData;
    }


    /**
     * 计算扩容或续期的价格
     *
     * @param msg
     * @return
     */
    public static ResponseData calculateExtendPrice(Object msg) {
        ResponseData responseData = new ResponseData(StatusCode.ERROR.getValue());

        try {
            Map<String, Object> data = FormData.getParam(msg);
            String extendUnit = (String) data.get(Common.EXTEND_UNIT);
            Integer extendSize = (Integer) data.get(Common.EXTEND_SIZE);
            //通过扩容单位找到扩容价格
            Double unitPrice = CommonObject.getExtendPrice().get(extendUnit);
            Double priceTotal = unitPrice * extendSize;
            Assemble.responseSuccessSetting(responseData, priceTotal);

        } catch (Exception e) {
            Assemble.responseErrorSetting(responseData, 500,
                    "calculateExtendPrice error: " + e);
        }
        return responseData;
    }


    /**
     * 查询该用户的所有订单
     *
     * @param msg http请求数据
     * @return
     */
    public static ResponseData getOrderList(Object msg) {

        ResponseData responseData = new ResponseData(StatusCode.ERROR.getValue());
        SqlSession sqlSession = MybatisUtils.getSession();

        try {
            //获取用户user_id
            String userId = FormData.getParam(msg, Common.USER_ID);
            List<Orders> orders = sqlSession.selectList(Mapper.GET_ORDER_LIST, Integer.parseInt(userId));
            if (orders != null) {
                Assemble.responseSuccessSetting(responseData, orders);
            } else {
                Assemble.responseErrorSetting(responseData, 401,
                        "getOrderList from db null error ");
            }

        } catch (Exception e) {
            Assemble.responseErrorSetting(responseData, 500,
                    "getOrderList error: " + e);
        } finally {
            CommonService.databaseCommitClose(sqlSession, responseData, false);
        }
        return responseData;
    }


    /**
     * 根据获得查询特定条件的目标订单数据
     *
     * @param msg http请求数据
     * @return
     */
    public static ResponseData getTargetOrderList(Object msg) {
        ResponseData responseData = new ResponseData(StatusCode.ERROR.getValue());
        SqlSession sqlSession = MybatisUtils.getSession();

        try {
            //获取用户user_id，service_id, pay_status, order_from_date, order_end_date等数据
            Map<String, Object> map = FormData.getParam(msg, Common.USER_ID, Common.SERVICE_ID,
                    Common.ORDER_FROM_DATE, Common.ORDER_END_DATE);

            //查询数据库相关数据
            List<Orders> orders = sqlSession.selectList(Mapper.GET_TARGET_ORDER_LIST, map);
            if (orders != null) {
                Assemble.responseSuccessSetting(responseData, orders);
            } else {
                Assemble.responseErrorSetting(responseData, 401,
                        "getOrderList from db null error ");
            }
        } catch (Exception e) {
            Assemble.responseErrorSetting(responseData, 500,
                    "getOrderList error: " + e);
        } finally {
            CommonService.databaseCommitClose(sqlSession, responseData, false);
        }
        return responseData;
    }


    /**
     * 插入新的orders数据
     *
     * @param msg
     * @return
     */
    public static ResponseData insertNewOrderItem(Object msg) {
        ResponseData responseData = new ResponseData(StatusCode.ERROR.getValue());
        try {
            //从http请求中获取要插入的orders实体类数据
            Orders orders = (Orders) FormData.getParam(msg, Orders.class);
            //进入alipay或WeChatpay页面进行支付
            doPayment(responseData, orders);

        } catch (Exception e) {
            Assemble.responseErrorSetting(responseData, 500,
                    "insertNewOrderItem error: " + e);
        }
        return responseData;
    }


    /**
     * 根据不同支付方式进入不同支付页面
     */
    public static void doPayment(ResponseData responseData, Orders orders) throws Exception {

        //设置支付日期等
        orders.setOrder_date(CommonService.getDateTime());
        //插入订单条目到数据库操作
        insertNewPaidOrderItem(orders);

        //先插入数据库支付，后调用payment方法，这样可以获取新插入的id值作为附加数据添加上
        //根据不同支付方式进行不同逻辑处理
        switch (orders.getPay_way()) {
            case 1: {
                //进行支付宝方式支付，并返回支付宝官方的支付页面HTML，以text/html方式返回
                String aliPayHtml = AliPay.invokePayment(orders);
                responseData.setMark(1);
                Assemble.responseSuccessSetting(responseData, aliPayHtml);
                break;
            }
            case 2: {
                //进行微信方式支付，返回一个支付的URL，返回前端通过生成二维码扫码进行支付操作
                String wechatPayUrlCode = WechatPay.invokePayment(orders);
                responseData.setMark(2);
                Assemble.responseSuccessSetting(responseData, wechatPayUrlCode);
                break;
            }
            case 3: {
                //进行积分兑换交易操作
                //设置pay_status为1，表示已支付
                orders.setPay_status(1);
                responseData.setMark(2);
                updateTotalPointsAfterExchange(orders, responseData);
                break;
            }
            default: {
                //返回错误提示
                Assemble.responseErrorSetting(responseData, 402,
                        "doPayment with unknown payWay: " + orders.getPay_way());
                break;
            }
        }
    }


    /**
     * 支付宝或微信支付后插入新订单到数据库
     *
     * @param orders 订单详情
     */
    public static void insertNewPaidOrderItem(Orders orders) {
        SqlSession sqlSession = MybatisUtils.getSession();
        try {
            //更新orders数据库条目，并返回影响条数
            int num = sqlSession.insert(Mapper.INSERT_NEW_ORDER_ITEM, orders);
            Purchase.logger.debug("Purchase update database num is:" + num);

        } catch (Exception e) {
            Purchase.logger.error("insertNewOrderItem error: ", e);

        } finally {
            CommonService.databaseCommitClose(sqlSession, new ResponseData(200), true);
        }
    }


    /**
     * 设置订单服务的下单支付时间和过期时间
     *
     * @param orders 订单服务
     * @throws Exception
     */
    public static void updateOrderTime(Orders orders) throws Exception {
        int serviceId = orders.getService_id();
        SimpleDateFormat sdf = new SimpleDateFormat(Common.TIME_FORMAT_1);
        Date date = new Date();
        Calendar expireDate = Calendar.getInstance();

        //根据不同的订单类型设置不同的过期时间区间
        switch (serviceId) {
            case 0: {
                expireDate.add(Calendar.DATE, orders.getService_num());
                break;
            }
            case 1: {
                expireDate.add(Calendar.MONTH, orders.getService_num());
                break;
            }
            case 2: {
                expireDate.add(Calendar.YEAR, orders.getService_num());
                break;
            }
            default: {
                throw new PayException("updateOrderTime unknown serviceId: " + serviceId);
            }
        }

        //设置过期日期为晚上十二点，也就是过期日期第二天的凌晨0点
        expireDate.add(Calendar.DATE, 1);
        expireDate.set(Calendar.HOUR_OF_DAY, 0);
        expireDate.set(Calendar.MINUTE, 0);
        expireDate.set(Calendar.SECOND, 0);

        //设置订单order的支付和过期时间
        orders.setPay_date(sdf.format(date));
        orders.setExpire_date(sdf.format(expireDate.getTime()));
    }


    /**
     * 更新订单交易情况，针对支付宝和微信支付方式
     *
     * @param orders 更新的订单信息
     */
    public static void updateOrderStatus(Orders orders) {
        SqlSession sqlSession = MybatisUtils.getSession();
        try {
            sqlSession.update(Mapper.UPDATE_ORDER_PAYMENT, orders);

        } catch (Exception e) {
            Purchase.logger.error("updateOrderStatus error: ", e);

        } finally {
            //跟新数据库并关闭连接
            sqlSession.commit();
            sqlSession.close();
        }
    }


    /**
     * 获取支付交易信息
     *
     * @param msg
     * @return
     */
    public static ResponseData getPayInfo(Object msg) {
        ResponseData responseData = new ResponseData(StatusCode.ERROR.getValue());
        SqlSession sqlSession = MybatisUtils.getSession();

        try {
            Map<String, Object> data = FormData.getParam(msg);
            int userId = Integer.parseInt(String.valueOf(data.get(Common.USER_ID)));
            String outTradeNo = String.valueOf(data.get(Common.OUT_TRADE_NO));
            String tradeNo = String.valueOf(data.get(Common.TRADE_NO));

            //根据outTradeNo查询是否有该记录
            if (CommonService.checkNotNull(outTradeNo) && CommonService.checkNotNull(tradeNo)) {
                Orders orders = sqlSession.selectOne(Mapper.GET_ORDER_BY_TRADE_NO, data);
                if (CommonService.checkNotNull(orders)) {
                    //生成交易信息content
                    String content = generateTradeContent(orders);
                    //获取支付交易凭证
                    Object certStr = ECCUtil.getCertStr(content);
                    //数据内容装载返回
                    Map<String, Object> map = new HashMap<>(2);
                    map.put("cert", certStr);
                    map.put("order", orders);
                    Assemble.responseSuccessSetting(responseData, map);

                } else {
                    //返回数据库中无该交易订单数据
                    Purchase.logger.debug("getPayInfo--> No such trade number:" + outTradeNo);
                    Assemble.responseErrorSetting(responseData, 401, "No such trade number");
                }
            } else {
                //返回交易订单号为空错误
                Purchase.logger.debug("getPayInfo--> trade number empty:" + outTradeNo);
                Assemble.responseErrorSetting(responseData, 402, "trade number empty");
            }

        } catch (Exception e) {
            Assemble.responseErrorSetting(responseData, 500,
                    "getPayInfo error: " + e);
        } finally {
            CommonService.databaseCommitClose(sqlSession, responseData, false);
        }
        return responseData;
    }


    /**
     * 生成交易信息原文
     *
     * @param orders
     * @return
     */
    public static String generateTradeContent(Orders orders) {
        StringBuilder builder = new StringBuilder();
        builder.append(orders.getOut_trade_no()); //13 bits
        builder.append(orders.getTrade_no()); //28 bits
        builder.append(orders.getId());
        builder.append(orders.getPay_date());
        builder.append(orders.getPay_way());
        builder.append(orders.getPrice());
        builder.append(orders.getService_id());
        builder.append(orders.getService_num());
        return Hex.encodeHexString(builder.toString().getBytes());
    }


    /**
     * 删除orders的条目数据
     *
     * @param msg
     * @return
     */
    public static ResponseData deleteOrderItem(Object msg) {
        ResponseData responseData = new ResponseData(StatusCode.ERROR.getValue());
        SqlSession sqlSession = MybatisUtils.getSession();

        try {
            //从http请求中获取要跟新的orders实体类数据
            String id = FormData.getParam(msg, Common.ID);
            //删除order表中某条数据并返回删除影响条目数量
            int num = sqlSession.update(Mapper.DELETE_ORDER_ITEM, Integer.parseInt(id));
            if (num > 0) {
                Assemble.responseSuccessSetting(responseData, null);
            } else {
                Assemble.responseErrorSetting(responseData, 401,
                        "deleteOrderItem update error, num is:" + num);
            }
        } catch (Exception e) {
            Assemble.responseErrorSetting(responseData, 500,
                    "deleteOrderItem error: " + e);
        } finally {
            CommonService.databaseCommitClose(sqlSession, responseData, true);
        }
        return responseData;
    }


    /**
     * 获取用户最新的total_points数据
     *
     * @param msg
     * @return
     */
    public static ResponseData getTotalPoints(Object msg) {
        ResponseData responseData = new ResponseData(StatusCode.ERROR.getValue());
        SqlSession sqlSession = MybatisUtils.getSession();

        try {
            //从http请求中获取total_points数据
            String id = FormData.getParam(msg, Common.USER_ID);
            //删除order表中某条数据并返回删除影响条目数量
            int totalPoints = sqlSession.selectOne(Mapper.GET_TOTAL_POINTS, Integer.parseInt(id));
            Assemble.responseSuccessSetting(responseData, totalPoints);

        } catch (Exception e) {
            Assemble.responseErrorSetting(responseData, 500,
                    "getTotalPoints error: " + e);
        } finally {
            CommonService.databaseCommitClose(sqlSession, responseData, true);
        }
        return responseData;
    }


    /**
     * 积分兑换后，更新user表中的total_points值
     *
     * @param orders       订单信息
     * @param responseData 返回包装数据
     */
    public static void updateTotalPointsAfterExchange(Orders orders, ResponseData responseData) {
        SqlSession sqlSession = MybatisUtils.getSession();
        try {
            //获取该user的值
            User user = sqlSession.selectOne(Mapper.GET_USER_DATA, orders.getUser_id());
            //根据service_id的值不同，设置该user对应的total_points的数目
            int newTotalPoints = user.getTotal_points() - CommonObject.getServiceToPoints().get(orders.getService_id());
            //进一步验证，只有总积分不小于0才可继续进行
            if (newTotalPoints >= 0) {
                user.setTotal_points(newTotalPoints);
                //更新兑换后的total_points的值
                int num = sqlSession.update(Mapper.UPDATE_USER_TOTAL_POINTS, user);
                //如果更新成功则返回正确
                if (num > 0) {
                    Assemble.responseSuccessSetting(responseData, newTotalPoints);
                } else {
                    Assemble.responseErrorSetting(responseData, 401,
                            "updateTotalPointsAfterExchange update error, num is:" + num);
                }
            } else {
                Assemble.responseErrorSetting(responseData, 402,
                        "updateTotalPointsAfterExchange newTotalPoints error, newTotalPoints is:" + newTotalPoints);
            }
        } catch (Exception e) {
            Assemble.responseErrorSetting(responseData, 500,
                    "updateTotalPointsAfterExchange error: " + e);
        } finally {
            CommonService.databaseCommitClose(sqlSession, responseData, true);
        }
    }
}

















