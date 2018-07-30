package viewcoder.operation.impl.purchase.wechat;

import viewcoder.operation.impl.common.CommonService;
import viewcoder.tool.common.Common;
import viewcoder.tool.config.GlobalConfig;
import viewcoder.tool.parser.text.TextData;
import viewcoder.operation.entity.Orders;
import viewcoder.operation.impl.purchase.Purchase;
import com.alibaba.fastjson.JSON;
import org.apache.log4j.Logger;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;


/**
 * Created by Administrator on 2018/3/15.
 */
public class WechatPay {

    private static Logger logger = Logger.getLogger(WechatPay.class.getName());
    private static final String TRADE_TYPE = "NATIVE";

    public static String invokePayment(Orders orders) {
        String urlCode = "";
        try {
            urlCode = weChatPay(orders);

        } catch (Exception e) {
            WechatPay.logger.error("WechatPay Process with error: ", e);
        }
        return urlCode;
    }

    /**
     * 进行WeChatPay操作
     *
     * @param orders 订单详情
     * @return 返回微信回调支付URL，需把该URL生成二维码
     * @throws Exception
     */
    public static String weChatPay(Orders orders) throws Exception {
        // 生成获取nonce_str
        String currTime = PayCommonUtil.getCurrTime();
        String strTime = currTime.substring(8, currTime.length());
        String strRandom = PayCommonUtil.buildRandom(4) + "";
        String nonce_str = strTime + strRandom;

        Map<String, String> packageParams = new HashMap<String, String>();
        //获取config配置的参数***************************************************
        //微信支付分配的公众账号ID（企业号corpid即为此appId)
        packageParams.put(Common.PAY_WECHAT_KEY_APPID, GlobalConfig.getProperties(Common.PAY_WECHAT_APPID));
        //微信支付分配的商户号
        packageParams.put(Common.PAY_WECHAT_KEY_MCH_ID, GlobalConfig.getProperties(Common.PAY_WECHAT_MCH_ID));
        //符合ISO 4217标准的三位字母代码，默认人民币：CNY
        packageParams.put(Common.PAY_WECHAT_KEY_FEE_TYPE, GlobalConfig.getProperties(Common.PAY_WECHAT_FEE_TYPE));
        //异步接收微信支付结果通知的回调地址，通知url必须为外网可访问的url，不能携带参数。
        packageParams.put(Common.PAY_WECHAT_KEY_NOTIFY_URL, GlobalConfig.getProperties(Common.PAY_WECHAT_NOTIFY_URL));
        //APP和网页支付提交用户端ip，Native支付填调用微信支付API的机器IP
        packageParams.put(Common.PAY_WECHAT_KEY_CREATE_IP, GlobalConfig.getProperties(Common.PAY_WECHAT_CREATE_IP));
        //商户系统内部订单号，要求32个字符内，只能是数字、大小写字母_-|*@ ，且在同一个商户号下唯一
        packageParams.put(Common.PAY_WECHAT_KEY_OUT_TRADE_NO, CommonService.getTimeStamp());
        //随机字符串，长度要求在32位以内。
        packageParams.put(Common.PAY_WECHAT_KEY_NONCE_STR, nonce_str);
        packageParams.put(Common.PAY_WECHAT_PRODUCT_ID, nonce_str);
        //商品简单描述
        packageParams.put(Common.PAY_WECHAT_KEY_BODY, orders.getSubject());
        //订单总金额，单位为分
        int price = (int) (Float.parseFloat(orders.getPrice()) * 100);
        packageParams.put(Common.PAY_WECHAT_KEY_TOTAL_FEE, String.valueOf(price));
        //JSAPI 公众号支付, NATIVE 扫码支付, APP APP支付
        packageParams.put(Common.PAY_WECHAT_KEY_TRADE_TYPE, TRADE_TYPE);
        //附加数据，在查询API和支付通知中原样返回，该字段主要用于商户携带订单的自定义数据，该数据记录哪条订单数据新插入了，在notification时update即可
        Map<String, Object> map = new HashMap<>(2);
        map.put(Common.ID, orders.getId());
        map.put(Common.SERVICE_ID, orders.getService_id());
        map.put(Common.SERVICE_NUM, orders.getService_num());
        packageParams.put(Common.PAY_WECHAT_KEY_ATTACH, URLEncoder.encode(JSON.toJSONString(map), Common.UTF8));

        //通过签名算法计算得出的签名值
        String sign = PayCommonUtil.createSign(Common.UTF8, packageParams, GlobalConfig.getProperties(Common.PAY_WECHAT_API_KEY));
        packageParams.put(Common.PAY_WECHAT_KEY_SIGN, sign);

        //发送数据并获取返回URLCode
        String requestXML = XMLWechatPayUtil.mapToXml(packageParams);
        WechatPay.logger.debug("Request XML Data: " + requestXML);
        String resXml = HttpWechatPayUtil.postData(GlobalConfig.getProperties(Common.PAY_WECHAT_UNIFIED_URL), requestXML);
        WechatPay.logger.debug("Response XML Data: " + resXml);

        //获取支付二维码URL
        Map map2 = XMLWechatPayUtil.xmlToMap(resXml);
        String urlCode = (String) map2.get(Common.PAY_WECHAT_RES_CODE_URL);
        WechatPay.logger.debug("Get urlCode from wechatPay request: \n" + urlCode);

        return urlCode;
    }


    /**
     * weChatPay的回调方法通知后台交易状态
     *
     * @param msg
     * @return
     * @throws Exception
     */
    public static String weChatPayNotify(Object msg) throws Exception {

        //获取WeChatPay的notify数据
        String text = TextData.getText(msg);
        Map<String, String> packageParams = XMLWechatPayUtil.xmlToMap(text);
        //打印查看微信回传的信息
        for (Map.Entry<String, String> entry : packageParams.entrySet()) {
            logger.info("key= " + entry.getKey() + " and value= " + entry.getValue());
        }

        String resXml = "";
        //判断签名是否正确
        if (PayCommonUtil.isTenpaySign(Common.UTF8, packageParams, GlobalConfig.getProperties(Common.PAY_WECHAT_API_KEY))) {
            //通知微信.异步确认成功.必写.不然会一直通知后台.八次之后就认为交易失败了.
            resXml = PayCommonUtil.packNotifyResXml(true);

            //判断支付状态是否成功支付
            if (Common.PAY_WECHAT_NOTIFY_RESULT_CODE_SUCCESS.equals(packageParams.get(Common.PAY_WECHAT_NOTIFY_RESULT_CODE))) {
                WechatPay.logger.debug("Get WechatPay Notify Success");

                //获取回传的附加数据，并更新数据库
                Orders orders = JSON.parseObject(URLDecoder.decode(
                        packageParams.get(Common.PAY_WECHAT_KEY_ATTACH), Common.UTF8), Orders.class);
                //设置订单的购买时间和到期时间
                Purchase.updateOrderTime(orders);
                //设置外部订单号和支付宝交易号
                orders.setOut_trade_no(packageParams.get(Common.PAY_WECHAT_KEY_OUT_TRADE_NO));
                orders.setTrade_no(packageParams.get(Common.PAY_WECHAT_TRANSACTION_ID));
                Purchase.updateOrderStatus(orders);

            } else {
                WechatPay.logger.warn("WeChatPay failure");
            }

        } else {
            //通知微信.异步确认，签名失败
            resXml = PayCommonUtil.packNotifyResXml(false);
            WechatPay.logger.info("Notify Wechat Sign-Name failure");

        }
        return resXml;
    }
}









