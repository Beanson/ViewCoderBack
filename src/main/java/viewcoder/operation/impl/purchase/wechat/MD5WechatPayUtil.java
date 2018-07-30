package viewcoder.operation.impl.purchase.wechat;

import java.security.MessageDigest;

/**
 * Created by Administrator on 2018/3/18.
 */
public class MD5WechatPayUtil {

    public static String MD5Encode(String origin, String charsetname) throws Exception{
        java.security.MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] array = md.digest(origin.getBytes(charsetname));
        StringBuilder sb = new StringBuilder();
        for (byte item : array) {
            sb.append(Integer.toHexString((item & 0xFF) | 0x100).substring(1, 3));
        }
        return sb.toString().toUpperCase();
    }
}
