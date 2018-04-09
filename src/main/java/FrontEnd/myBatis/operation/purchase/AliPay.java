package FrontEnd.myBatis.operation.purchase;

import FrontEnd.exceptions.purchase.PayException;
import FrontEnd.helper.common.Common;
import FrontEnd.helper.common.Mapper;
import FrontEnd.helper.config.GlobalConfig;
import FrontEnd.helper.parser.form.FormData;
import FrontEnd.myBatis.MybatisUtils;
import FrontEnd.myBatis.entity.Orders;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by Administrator on 2018/3/15.
 */
public class AliPay {

    private static Logger logger = Logger.getLogger(AliPay.class.getName());

    /**
     * 支付宝网上支付接口调用
     *
     * @param orders 订单信息，注入回传参数中
     * @return
     * @throws AlipayApiException
     */
    public static String invokePayment(Orders orders) throws AlipayApiException {
        //实例化客户端
        AlipayClient alipayClient = new DefaultAlipayClient(GlobalConfig.getProperties(Common.PAY_ALI_GW_URL),
                GlobalConfig.getProperties(Common.PAY_ALI_APPID), GlobalConfig.getProperties(Common.PAY_ALI_PRIVATE_KEY),
                GlobalConfig.getProperties(Common.PAY_ALI_FORMAT), GlobalConfig.getProperties(Common.PAY_ALI_CHARSET),
                GlobalConfig.getProperties(Common.PAY_ALI_PUBLIC_KEY), GlobalConfig.getProperties(Common.PAY_ALI_SIGN_TYPE));

        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();//创建API对应的request
        alipayRequest.setReturnUrl(GlobalConfig.getProperties(Common.PAY_ALI_RETURN_URL));
        alipayRequest.setNotifyUrl(GlobalConfig.getProperties(Common.PAY_ALI_NOTIFY_URL));//在公共参数中设置回跳和通知地址
        String formHtml = "";

        try {
            //准备订单数据
            JSONObject json = new JSONObject();
            json.put(Common.PAY_ALI_KEY_TRADE_NO, orders.getOrder_id());
            json.put(Common.PAY_ALI_KEY_PRODUCT_CODE, GlobalConfig.getProperties(Common.PAY_ALI_PRODUCT_CODE));
            json.put(Common.PAY_ALI_KEY_TOTAL_AMOUNT, orders.getPrice());
            json.put(Common.PAY_ALI_KEY_SUBJECT, orders.getSubject()); //商品描述标题
            json.put(Common.PAY_ALI_KEY_PASSBACK_PARAMS,
                    URLEncoder.encode(JSON.toJSONString(orders), Common.UTF8)); //notify_url中回传接收数据
            alipayRequest.setBizContent(json.toString());
//            alipayRequest.setBizContent("{\"out_trade_no\":\""+ 121233454657656345d +"\","
//                    + "\"total_amount\":\""+ 12 +"\","
//                    + "\"subject\":\""+ "hello world" +"\","
//                    + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");
            formHtml = alipayClient.pageExecute(alipayRequest).getBody(); //调用SDK生成表单

        } catch (Exception e) {
            AliPay.logger.error("Alipay exception: ", e);
        }
        return formHtml;
    }


    /**
     * 支付宝支付完成后，后台发送响应请求到notify_url中
     */
    public static void aliPayNotify(Object msg) {

        try {
            //从http中获取回传参数值
            Map<String, Object> map = FormData.getParam(msg);
            //遍历装载所有从aliPay回调发送的数据
            Map<String, String> paramsMap = new HashMap<>();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                paramsMap.put(entry.getKey(), entry.getValue().toString());
            }
            //调用SDK验证签名
            boolean signVerified = AlipaySignature.rsaCheckV1(paramsMap, GlobalConfig.getProperties(Common.PAY_ALI_PUBLIC_KEY),
                    GlobalConfig.getProperties(Common.PAY_ALI_CHARSET), GlobalConfig.getProperties(Common.PAY_ALI_SIGN_TYPE));

            if (signVerified) {
                // 验签成功后，按照支付结果异步通知中的描述，对支付结果中的业务内容进行二次校验，
                // 校验成功后在response中返回success并继续商户自身业务处理，校验失败返回failure
                Orders orders = JSON.parseObject(
                        URLDecoder.decode(paramsMap.get(Common.PAY_ALI_KEY_PASSBACK_PARAMS), Common.UTF8), Orders.class);
                //插入新订单信息到数据库操作
                Purchase.insertNewOrderItem(orders);

            } else {
                AliPay.logger.debug("aliPayNotify signVerified failure");
            }

        } catch (Exception e) {
            AliPay.logger.error("aliPayNotify occurs error", e);
        }
    }
}















