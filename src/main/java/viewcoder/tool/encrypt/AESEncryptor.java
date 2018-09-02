package viewcoder.tool.encrypt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Created by Administrator on 2018/2/17.
 */
public class AESEncryptor {

    private static Logger logger = LoggerFactory.getLogger(AESEncryptor.class);

    /**
     * 加密
     * 1.构造密钥生成器
     * 2.根据ecnodeRules规则初始化密钥生成器
     * 3.产生密钥
     * 4.创建和初始化密码器
     * 5.内容加密
     * 6.返回字符串
     */
    public static String AESEncode(String encodeRules, String content) {
        try {
            //如果加密明文为null则直接返回null
            if(content==null){
                return null;
            }
            //1.构造密钥生成器，指定为AES算法,不区分大小写
            KeyGenerator keygen = KeyGenerator.getInstance("AES");
            //2.根据ecnodeRules规则初始化密钥生成器
            //生成一个256位的随机源,根据传入的字节数组
            keygen.init(256, new SecureRandom(encodeRules.getBytes()));
            //3.产生原始对称密钥
            SecretKey original_key = keygen.generateKey();
            //4.获得原始对称密钥的字节数组
            byte[] raw = original_key.getEncoded();
            //5.根据字节数组生成AES密钥
            SecretKey key = new SecretKeySpec(raw, "AES");
            //6.根据指定算法AES自成密码器
            Cipher cipher = Cipher.getInstance("AES");
            //7.初始化密码器，第一个参数为加密(Encrypt_mode)或者解密解密(Decrypt_mode)操作，第二个参数为使用的KEY
            cipher.init(Cipher.ENCRYPT_MODE, key);
            //8.获取加密内容的字节数组(这里要设置为utf-8)不然内容中如果有中文和英文混合中文就会解密为乱码
            byte[] byte_encode = content.getBytes("utf-8");
            //9.根据密码器的初始化方式--加密：将数据加密
            Base64.Encoder encoder = Base64.getEncoder();
            String AES_encode = encoder.encodeToString(byte_encode);
            //11.将字符串返回
            return AES_encode;
        } catch (Exception e) {
            AESEncryptor.logger.error("===AESEncode with error: ", e);
        }
        //如果有错就返加nulll
        return null;
    }


    /**
     * 解密
     * 解密过程：
     * 1.同加密1-4步
     * 2.将加密后的字符串反纺成byte[]数组
     * 3.将加密内容解密
     */
    public static String AESDncode(String encodeRules, String content) {
        try {
            //如果解密密文为null则直接返回null
            if(content==null){
                return null;
            }
            //1.构造密钥生成器，指定为AES算法,不区分大小写
            KeyGenerator keygen = KeyGenerator.getInstance("AES");
            //2.根据ecnodeRules规则初始化密钥生成器
            //生成一个256位的随机源,根据传入的字节数组
            keygen.init(256, new SecureRandom(encodeRules.getBytes()));
            //3.产生原始对称密钥
            SecretKey original_key = keygen.generateKey();
            //4.获得原始对称密钥的字节数组
            byte[] raw = original_key.getEncoded();
            //5.根据字节数组生成AES密钥
            SecretKey key = new SecretKeySpec(raw, "AES");
            //6.根据指定算法AES自成密码器
            Cipher cipher = Cipher.getInstance("AES");
            //7.初始化密码器，第一个参数为加密(Encrypt_mode)或者解密(Decrypt_mode)操作，第二个参数为使用的KEY
            cipher.init(Cipher.DECRYPT_MODE, key);
            //8.将加密并编码后的内容解码成字节数组
            Base64.Decoder decoder = Base64.getDecoder();
            byte[] buffer = decoder.decode(content);
            String AES_decode = new String(buffer, "utf-8");
            return AES_decode;
        } catch (Exception e) {
            AESEncryptor.logger.error("===AESDncode with error: ", e);
        }
        //如果有错就返加nulll
        return null;
    }
}
