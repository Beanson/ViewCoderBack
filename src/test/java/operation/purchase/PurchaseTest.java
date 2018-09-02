package operation.purchase;

import com.alipay.api.internal.util.StringUtils;
import com.alipay.api.internal.util.codec.Base64;
import com.aliyun.oss.OSSClient;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import viewcoder.operation.entity.Orders;
import viewcoder.operation.entity.User;
import viewcoder.operation.impl.common.CommonService;
import viewcoder.operation.impl.purchase.AliPay;
import org.junit.Test;
import viewcoder.operation.impl.purchase.Purchase;
import viewcoder.tool.common.Common;
import viewcoder.tool.common.CommonObject;
import viewcoder.tool.common.OssOpt;
import viewcoder.tool.config.GlobalConfig;
import viewcoder.tool.msg.MsgHelper;
import viewcoder.tool.util.MybatisUtils;

import java.io.ByteArrayInputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.alipay.api.internal.util.AlipaySignature.getPublicKeyFromX509;

/**
 * Created by Administrator on 2018/3/11.
 */
public class PurchaseTest {

    private static Logger logger = LoggerFactory.getLogger(PurchaseTest.class);

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
            PurchaseTest.logger.debug(orders.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @Test
    public void testPay() throws Exception{
        PublicKey e = getPublicKeyFromX509("RSA", new ByteArrayInputStream("MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAsIsTWDSGq69oZnAliQfWZqIsU2KVNOG1GK3/4ePGgjttb0vuU5WYArGhXbmQWYfYLA3b4dW5xtcke8fzZDcBluoJzYrOJQbKKLwxsxUAMsQWA6CKu7gWlQZoLrKONlclT+IBCv2BCLNA8FMNvlPisrxyo/sgnznDTbQKsi2lFjgLS0Hq8Z1w0VZfbdHgXRWSIvWg8N7SQcYBzhapaO6jmzb3NFhwMxjSpMWP4+saMCthARwcsXWtfCdPx0OLqH7uEUQdttfJ0c6Df8xZAHdB9vlhNk7zhILOkbCUZBc5Rq9cv163IxsjdACG0dVv5pmR/KRMji8Ao7Nppip2q3RYKwIDAQAB".getBytes()));
        Signature signature = Signature.getInstance("SHA256WithRSA");
        signature.initVerify(e);
        signature.update("app_id=2018042660038244&auth_app_id=2018042660038244&body=Iphone6 16G, body&buyer_id=2088212004043356&buyer_pay_amount=0.01&charset=UTF-8&fund_bill_list=[{\"amount\":\"0.01\",\"fundChannel\":\"ALIPAYACCOUNT\"}]&gmt_create=2018-06-29 11:31:42&gmt_payment=2018-06-29 11:32:14&invoice_amount=0.01&notify_id=6dd350302a6d5a39cfe2142aacc699aipd&notify_time=2018-06-29 11:32:14&notify_type=trade_status_sync&out_trade_no=1530243042670&passback_params=merchantBizType%3d3C%26merchantBizNo%3d2016010101111&point_amount=0.00&receipt_amount=0.01&seller_id=2088131043865379&subject=Iphone6 16G&total_amount=0.01&trade_no=2018062921001004350569761553&trade_status=TRADE_SUCCESS&version=1.0".getBytes());
//        if(StringUtils.isEmpty(charset)) {
//            signature.update(content.getBytes());
//        } else {
//            signature.update(content.getBytes(charset));
//        }

        System.out.println(signature.verify(Base64.decodeBase64("RYFUrLeE0cPI7XHYPjqOx0jsbI7PXh6nbmwYN7g1OU2zBHBaCnhEw03Lj6YUVFKALM9u33m8XKOJ8bCRupeH3Xkj3WIQu0AxKVL+1CXeMjmzUUR9x817cjlaYZX0a1uHRbyNm2vdaQwkg/0sioxIBJRl+79vjgbomBo7JK0jA7DKnE4pttCiZKFPYvZd+ChQfN7ymqHEdRVtgmA0og0aewcL+Q/RDLd8CNwVVEvvCLBYvXWqu+GEXh9vwNmHNFZyfM16CRzXDoZGZdUYQaLJNLs3yEeB/46YHePrbUDM5SpP3w1DSgTW+2mja3panUDZywsBa+D4z0dTH5DGO+T4YQ==".getBytes()))); ;
    }


//    @Test
//    public void testSing() throws  Exception{
//
//        //2.执行签名
//        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(base642Byte(ecPrivateKey));
//        KeyFactory keyFactory = KeyFactory.getInstance("RSA2");
//        PrivateKey privateKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec);
//        Signature signature = Signature.getInstance("SHA256WithRSA");
//        signature.initSign(privateKey);
//        signature.update(src.getBytes());
//        byte[] result = signature.sign();
//        System.out.println("jdk ecdsa sign : " + Hex.encodeHexString(result));
//
//        PublicKey e = getPublicKeyFromX509("RSA", new ByteArrayInputStream("MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAsIsTWDSGq69oZnAliQfWZqIsU2KVNOG1GK3/4ePGgjttb0vuU5WYArGhXbmQWYfYLA3b4dW5xtcke8fzZDcBluoJzYrOJQbKKLwxsxUAMsQWA6CKu7gWlQZoLrKONlclT+IBCv2BCLNA8FMNvlPisrxyo/sgnznDTbQKsi2lFjgLS0Hq8Z1w0VZfbdHgXRWSIvWg8N7SQcYBzhapaO6jmzb3NFhwMxjSpMWP4+saMCthARwcsXWtfCdPx0OLqH7uEUQdttfJ0c6Df8xZAHdB9vlhNk7zhILOkbCUZBc5Rq9cv163IxsjdACG0dVv5pmR/KRMji8Ao7Nppip2q3RYKwIDAQAB".getBytes()));
//        Signature signature = Signature.getInstance("SHA256WithRSA");
//        signature.initVerify(e);
//        signature.update("app_id=2018042660038244&auth_app_id=2018042660038244&body=Iphone6 16G, body&buyer_id=2088212004043356&buyer_pay_amount=0.01&charset=UTF-8&fund_bill_list=[{\"amount\":\"0.01\",\"fundChannel\":\"ALIPAYACCOUNT\"}]&gmt_create=2018-06-29 11:31:42&gmt_payment=2018-06-29 11:32:14&invoice_amount=0.01&notify_id=6dd350302a6d5a39cfe2142aacc699aipd&notify_time=2018-06-29 11:32:14&notify_type=trade_status_sync&out_trade_no=1530243042670&passback_params=merchantBizType%3d3C%26merchantBizNo%3d2016010101111&point_amount=0.00&receipt_amount=0.01&seller_id=2088131043865379&subject=Iphone6 16G&total_amount=0.01&trade_no=2018062921001004350569761553&trade_status=TRADE_SUCCESS&version=1.0".getBytes());
////        if(StringUtils.isEmpty(charset)) {
////            signature.update(content.getBytes());
////        } else {
////            signature.update(content.getBytes(charset));
////        }
//
//        System.out.println(signature.verify(Base64.decodeBase64("RYFUrLeE0cPI7XHYPjqOx0jsbI7PXh6nbmwYN7g1OU2zBHBaCnhEw03Lj6YUVFKALM9u33m8XKOJ8bCRupeH3Xkj3WIQu0AxKVL+1CXeMjmzUUR9x817cjlaYZX0a1uHRbyNm2vdaQwkg/0sioxIBJRl+79vjgbomBo7JK0jA7DKnE4pttCiZKFPYvZd+ChQfN7ymqHEdRVtgmA0og0aewcL+Q/RDLd8CNwVVEvvCLBYvXWqu+GEXh9vwNmHNFZyfM16CRzXDoZGZdUYQaLJNLs3yEeB/46YHePrbUDM5SpP3w1DSgTW+2mja3panUDZywsBa+D4z0dTH5DGO+T4YQ==".getBytes()))); ;
//
//    }

    @Test
    public void testHex() throws DecoderException {
        String content = "hello world";
        byte[]a = content.getBytes();
        System.out.println(new String(a));
    }

    @Test
    public void sendMailMsgToNewPurchase(){
        User user = new User();
        user.setUser_name("beanson");
        user.setEmail("2920248385@qq.com");
        user.setPhone("18316433415");
        Orders order = new Orders();
        order.setService_id(3);
        order.setService_num(2);
        order.setExpire_date("2018-09-02 00:00:00");
        Purchase.notifyUserPurchaseSuccess(user, order);
    }

    @Test
    public void sendMsg(){
        Map<String, String> replaceData = new HashMap<String, String>();
        String templateId = Common.MSG_TEMPLEATE_PURCHASE;

        User user = new User();
        user.setUser_name("beanson");
        user.setEmail("2920248385@qq.com");
        user.setPhone("18316433415");
        Orders order = new Orders();
        order.setService_id(3);
        order.setService_num(2);
        order.setExpire_date("2018-09-02 00:00:00");

        //准备替换原文的用户数据
        replaceData.put("name", user.getUser_name());
        replaceData.put("service", CommonObject.getServiceName(order.getService_id()));
        replaceData.put("time", order.getExpire_date());
        replaceData.put("service_length", order.getService_num() + " " + CommonObject.getServiceUnit(order.getService_id()));

        //发送短信操作${name} ${service}
        MsgHelper.sendSingleMsg(templateId, replaceData, user.getPhone(), Common.MSG_SIGNNAME_LIPHIN);
    }

    @Test
    public void setACK(){
        SqlSession sqlSession = MybatisUtils.getSession();
        OSSClient ossClient = OssOpt.initOssClient();
        try {
            CommonService.setACKOpt(sqlSession, ossClient, 30,true);

        }catch (Exception e){
            logger.error("dala",e);

        }finally {
            sqlSession.close();
            OssOpt.shutDownOssClient(ossClient);
        }
    }

    @Test
    public void test(){
        Orders orders = new Orders();
        orders.setId(2);
        System.out.println(Purchase.getPayStatus(orders));
    }


}
