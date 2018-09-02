package viewcoder.tool.encrypt;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.security.Signature;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;

import com.alibaba.fastjson.JSON;
import io.netty.handler.codec.http.multipart.FileUpload;
import org.apache.commons.codec.binary.Hex;
import org.apache.ibatis.session.SqlSession;
import org.bouncycastle.jce.interfaces.ECPrivateKey;
import org.bouncycastle.jce.interfaces.ECPublicKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import viewcoder.operation.entity.Orders;
import viewcoder.operation.entity.Verify;
import viewcoder.operation.entity.response.ResponseData;
import viewcoder.operation.entity.response.StatusCode;
import viewcoder.operation.impl.common.CommonService;
import viewcoder.tool.common.Assemble;
import viewcoder.tool.common.Common;
import viewcoder.tool.common.Mapper;
import viewcoder.tool.config.GlobalConfig;
import viewcoder.tool.parser.form.FormData;
import viewcoder.tool.util.MybatisUtils;

/**
 * Created by Administrator on 2018/6/28.
 * ECC 椭圆曲线加解密和签名解签
 */
public class ECCUtil {
    private static Logger logger = LoggerFactory.getLogger(ECCUtil.class.getName());

    static {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

    //字节数组转Base64编码
    public static String byte2Base64(byte[] bytes) {
        //not included since jdk8
        //BASE64Encoder encoder = new BASE64Encoder();
        //return encoder.encode(bytes);
        Base64.Encoder encoder = Base64.getEncoder();
        return encoder.encodeToString(bytes);
    }

    //Base64编码转字节数组
    public static byte[] base642Byte(String base64Key) throws IOException {
        //not included since jdk8
        //BASE64Decoder decoder = new BASE64Decoder();
        //return decoder.decodeBuffer(base64Key);
        Base64.Decoder decoder = Base64.getDecoder();
        return decoder.decode(base64Key);
    }

    //生成秘钥对
    public static KeyPair getKeyPair() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC", "BC");
        keyPairGenerator.initialize(256, new SecureRandom());
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        return keyPair;
    }

    //获取公钥(Base64编码)
    public static String getPublicKey(KeyPair keyPair) {
        ECPublicKey publicKey = (ECPublicKey) keyPair.getPublic();
        byte[] bytes = publicKey.getEncoded();
        return byte2Base64(bytes);
    }

    //获取私钥(Base64编码)
    public static String getPrivateKey(KeyPair keyPair) {
        ECPrivateKey privateKey = (ECPrivateKey) keyPair.getPrivate();
        byte[] bytes = privateKey.getEncoded();
        return byte2Base64(bytes);
    }

    //将Base64编码后的公钥转换成PublicKey对象
    public static ECPublicKey string2PublicKey(String pubStr) throws Exception {
        byte[] keyBytes = base642Byte(pubStr);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("EC", "BC");
        ECPublicKey publicKey = (ECPublicKey) keyFactory.generatePublic(keySpec);
        return publicKey;
    }

    //将Base64编码后的私钥转换成PrivateKey对象
    public static ECPrivateKey string2PrivateKey(String priStr) throws Exception {
        byte[] keyBytes = base642Byte(priStr);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("EC", "BC");
        ECPrivateKey privateKey = (ECPrivateKey) keyFactory.generatePrivate(keySpec);
        return privateKey;
    }

    //公钥加密
    public static byte[] publicEncrypt(byte[] content, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("ECIES", "BC");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] bytes = cipher.doFinal(content);
        return bytes;
    }

    //公钥机密
    public static byte[] publicDecrypt(byte[] content, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("ECIES", "BC");
        cipher.init(Cipher.DECRYPT_MODE, publicKey);
        byte[] bytes = cipher.doFinal(content);
        return bytes;
    }


    //私钥加密
    public static byte[] privateSign(byte[] content, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance("ECIES", "BC");
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);
        byte[] bytes = cipher.doFinal(content);
        return bytes;
    }

    //私钥解密
    public static byte[] privateDecrypt(byte[] content, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance("ECIES", "BC");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] bytes = cipher.doFinal(content);
        return bytes;
    }


    /**
     * 下载支付凭证信息，用平台私钥对文件内容进行签名，可以用公钥进行验证
     *
     * @param content
     * @return
     */
    public static Object getCertStr(String content) {

        Object certStr = null;
        try {
            String privateKeyStr = GlobalConfig.getProperties(Common.SIGN_PRIVATE_KEY);

            //2.执行签名
            PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(base642Byte(privateKeyStr));
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            PrivateKey privateKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec);
            Signature signature = Signature.getInstance("SHA256withECDSA");
            signature.initSign(privateKey);
            signature.update(content.getBytes());
            byte[] result = signature.sign();

            //签名结果数据包装
            HashMap<String, Object> map = new HashMap<>(2);
            map.put("sign", Hex.encodeHexString(result));
            map.put("content", content);
            certStr = map;
            //certStr = JSON.toJSONString(map);

        } catch (Exception e) {
            logger.error("getCertStr with error", e);
        }
        return certStr;
    }


    /**
     * 验证交易记录信息是否是平台颁发的
     *
     * @param msg
     * @return
     */
    public static ResponseData verifyCert(Object msg) {
        ResponseData responseData = new ResponseData(StatusCode.ERROR.getValue());
        SqlSession sqlSession = MybatisUtils.getSession();

        try {
            String publicKeyStr = GlobalConfig.getProperties(Common.SIGN_PUBLIC_KEY);

            //从前端获取数据
            Map<String, Object> data = FormData.getParam(msg);
            Integer userId = Integer.parseInt(String.valueOf(data.get(Common.USER_ID)));
            String signStr = String.valueOf(data.get(Common.SIGN_STR));

            //数据解析
            Verify verify = JSON.parseObject(signStr, Verify.class);

            //3.验证签名
            X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(base642Byte(publicKeyStr));
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            PublicKey publicKey = keyFactory.generatePublic(x509EncodedKeySpec);
            Signature signature = Signature.getInstance("SHA256withECDSA");
            signature.initVerify(publicKey);
            signature.update(verify.getContent().getBytes());
            boolean bool = signature.verify(Hex.decodeHex(verify.getSign().toCharArray()));
            System.out.println("Digital verify result:" + bool);

            //如果成功则返回详细订单信息，否则返回错误消息
            if(bool){
                //查询数据库获取该订单条目数据
                Map<String, String> map = new HashMap<>(2);
                String decodedContent = new String(Hex.decodeHex(verify.getContent().toCharArray()), "UTF-8");
                map.put(Common.OUT_TRADE_NO, decodedContent.substring(0, 13));
                map.put(Common.TRADE_NO, decodedContent.substring(13, 41));
                Orders orders = sqlSession.selectOne(Mapper.GET_ORDER_BY_TRADE_NO, map);

                //包装返回数据
                Map<String, Object> response = new HashMap<>(5);
                response.put(Common.OUT_TRADE_NO, orders.getOut_trade_no());
                response.put(Common.SERVICE_ID, orders.getService_id());
                response.put(Common.SERVICE_NUM, orders.getService_num());
                response.put(Common.PAY_WAY, orders.getPay_way());
                response.put(Common.PRICE, orders.getPrice());
                Assemble.responseSuccessSetting(responseData, response);

            }else{
                Assemble.responseErrorSetting(responseData, 401,"signature verify failure");
            }

        } catch (Exception e) {
            logger.error("verifyCert with error", e);
            Assemble.responseErrorSetting(responseData, 500,
                    "verifyCert error: " + e);
        }
        return responseData;
    }


    public static void main(String[] args) throws Exception {
//        KeyPair keyPair = ECCUtil.getKeyPair();
//        String publicKeyStr = ECCUtil.getPublicKey(keyPair);
//        String privateKeyStr = ECCUtil.getPrivateKey(keyPair);
//        System.out.println("ECC公钥Base64编码:" + publicKeyStr);
//        System.out.println("ECC私钥Base64编码:" + privateKeyStr);
//
//        ECPublicKey publicKey = string2PublicKey(publicKeyStr);
//        ECPrivateKey privateKey = string2PrivateKey(privateKeyStr);
//
//        byte[] publicEncrypt = publicEncrypt("welcome_to_view_coder".getBytes(), publicKey);
//        byte[] privateDecrypt = privateDecrypt(publicEncrypt, privateKey);
//
//        System.out.println("---------------------------");
//        System.out.println(new String(publicEncrypt));
//        System.out.println(new String(privateDecrypt));

        testSign();
        //verifyCert(new Object());
    }


    /**
     * 测试数字签名操作
     *
     * @throws Exception
     */
    public static void testSign() throws Exception {
        String src = "to encrypt content";

        String ecPublicKey = GlobalConfig.getProperties(Common.SIGN_PUBLIC_KEY);
        String ecPrivateKey = GlobalConfig.getProperties(Common.SIGN_PRIVATE_KEY);

        //2.执行签名
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(base642Byte(ecPrivateKey));
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        PrivateKey privateKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec);
        Signature signature = Signature.getInstance("SHA256withECDSA");
        signature.initSign(privateKey);
        signature.update(src.getBytes());
        byte[] result = signature.sign();
        String encodeStr = Hex.encodeHexString(result);
        System.out.println("jdk ecdsa sign : " + encodeStr);

        //3.验证签名
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(base642Byte(ecPublicKey));
        keyFactory = KeyFactory.getInstance("EC");
        PublicKey publicKey = keyFactory.generatePublic(x509EncodedKeySpec);
        signature = Signature.getInstance("SHA256withECDSA");
        signature.initVerify(publicKey);
        signature.update(src.getBytes());
        //boolean bool = signature.verify(result);
        boolean bool = signature.verify(Hex.decodeHex(encodeStr.toCharArray()));
        System.out.println("jdk ecdsa verify : " + bool);
    }

}
