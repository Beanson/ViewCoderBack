package viewcoder.operation.impl.purchase;

import com.sun.org.apache.xpath.internal.operations.Or;
import org.apache.ibatis.session.SqlSession;
import viewcoder.operation.impl.common.CommonService;
import viewcoder.tool.common.Common;
import viewcoder.tool.common.Mapper;
import viewcoder.tool.config.GlobalConfig;
import viewcoder.tool.encode.Encode;
import viewcoder.tool.parser.form.FormData;
import viewcoder.operation.entity.Orders;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import org.apache.log4j.Logger;
import viewcoder.tool.util.MybatisUtils;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.bouncycastle.asn1.ua.DSTU4145NamedCurves.params;


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
        alipayRequest.setReturnUrl(GlobalConfig.getProperties(Common.PAY_ALI_RETURN_URL));//设置付款成功后同步跳转地址
        alipayRequest.setNotifyUrl(GlobalConfig.getProperties(Common.PAY_ALI_NOTIFY_URL));//设置异步回传地址
        String formHtml = "";

        try {
            //准备订单数据
            JSONObject json = new JSONObject();
            json.put(Common.PAY_ALI_KEY_TRADE_NO, CommonService.getTimeStamp());
            json.put(Common.PAY_ALI_KEY_PRODUCT_CODE, GlobalConfig.getProperties(Common.PAY_ALI_PRODUCT_CODE));
            json.put(Common.PAY_ALI_KEY_TOTAL_AMOUNT, orders.getPrice());
            json.put(Common.PAY_ALI_KEY_SUBJECT, orders.getSubject());//商品描述标题
            //notify_url中回传接收数据
            Map<String, Object> map = new HashMap<>(2);
            map.put(Common.ID, orders.getId());
            map.put(Common.SERVICE_ID, orders.getService_id());
            map.put(Common.SERVICE_NUM, orders.getService_num());
            json.put(Common.PAY_ALI_KEY_PASSBACK_PARAMS, URLEncoder.encode(JSON.toJSONString(map), Common.UTF8));
            //设置支付信息参数
            alipayRequest.setBizContent(json.toString());
            //调用SDK生成表单
            formHtml = alipayClient.pageExecute(alipayRequest).getBody();

        } catch (Exception e) {
            AliPay.logger.error("Alipay exception: ", e);
        }
        return formHtml;
    }


    /**
     * 支付宝支付完成后，后台发送响应请求到notify_url中
     */
    public static String aliPayNotify(Object msg) {

        //初始化返回支付宝的response
        String notifyResponse = Common.FAIL;
        try {
            //获取支付宝POST过来反馈信息
            Map<String, String> params = new HashMap<String, String>();
            Map<String, Object> requestParams = FormData.getParam(msg);
            for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext(); ) {
                String name = (String) iter.next();
                String valueStr = String.valueOf(requestParams.get(name));
                AliPay.logger.debug("name:" + name + " , value:" + valueStr);
                params.put(name, valueStr);
            }

            //调用签名验证函数
            boolean signVerified = AlipaySignature.rsaCheckV1(params, GlobalConfig.getProperties(Common.PAY_ALI_PUBLIC_KEY),
                    GlobalConfig.getProperties(Common.PAY_ALI_CHARSET), GlobalConfig.getProperties(Common.PAY_ALI_SIGN_TYPE));

            if (signVerified) {
                notifyResponse = Common.SUCCESS;
                AliPay.logger.debug("aliPayNotify signVerified success");
                //签名成功，进行业务逻辑处理
                verifyOrderLogic(params);

            } else {
                AliPay.logger.debug("aliPayNotify signVerified failure");
            }

        } catch (Exception e) {
            AliPay.logger.error("aliPayNotify occurs error", e);
        }
        return notifyResponse;
    }


    /**
     * 数字签名认证成功后，进行业务逻辑处理操作
     *
     * @param params
     * @throws Exception
     */
    private static void verifyOrderLogic(Map<String, String> params) throws Exception {
        // 验签成功后，按照支付结果异步通知中的描述，对支付结果中的业务内容进行二次校验，
        // 校验成功后在response中返回success并继续商户自身业务处理，校验失败返回failure
        //商户订单号，查询交易订单详情记录，相当于查看订单详情
        String out_trade_no = new String(params.get("out_trade_no").getBytes("ISO-8859-1"), "UTF-8");
        //支付宝交易号，查询交易流水记录，客户可以自己登陆根据交易号查找
        String trade_no = new String(params.get("trade_no").getBytes("ISO-8859-1"), "UTF-8");
        //交易状态
        String trade_status = new String(params.get("trade_status").getBytes("ISO-8859-1"), "UTF-8");

        if (trade_status.equals("TRADE_FINISHED")) {
            //判断该笔订单是否在商户网站中已经做过处理
            //如果没有做过处理，根据订单号（out_trade_no）在商户网站的订单系统中查到该笔订单的详细，并执行商户的业务程序
            //如果有做过处理，不执行商户的业务程序
            //注意：退款日期超过可退款期限后（如三个月可退款），支付宝系统发送该交易状态通知
            AliPay.logger.debug("verifyOrderLogic Trade finish notify");

        } else if (trade_status.equals("TRADE_SUCCESS")) {
            //判断该笔订单是否在商户网站中已经做过处理
            //如果没有做过处理，根据订单号（out_trade_no）在商户网站的订单系统中查到该笔订单的详细，并执行商户的业务程序
            //如果有做过处理，不执行商户的业务程序
            //注意：付款完成后，支付宝系统发送该交易状态通知

            //判断该订单号是否已在数据库中，若是则不进行操作，否则进行插入操作并下载数字证书文件
            //从交易记录解析出该订单信息
            Orders orders = JSON.parseObject(URLDecoder.decode(
                    params.get(Common.PAY_ALI_KEY_PASSBACK_PARAMS), Common.UTF8), Orders.class);
            //设置订单的购买时间和到期时间
            Purchase.updateOrderTime(orders);
            //设置外部订单号和支付宝交易号
            orders.setOut_trade_no(out_trade_no);
            orders.setTrade_no(trade_no);
            Purchase.updateOrderStatus(orders);
        }
    }
}















