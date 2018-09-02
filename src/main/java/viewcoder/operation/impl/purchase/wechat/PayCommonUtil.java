package viewcoder.operation.impl.purchase.wechat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Administrator on 2018/3/17.
 */
public class PayCommonUtil {

    private static Logger logger = LoggerFactory.getLogger(PayCommonUtil.class.getName());

    /**
     * 是否签名正确,规则是:按参数名称a-z排序,遇到空值的参数不参加签名。
     *
     * @return boolean
     */
    public static boolean isTenpaySign(String characterEncoding, Map<String, String> packageParams, String API_KEY)
            throws Exception {
        //算出摘要
        String mySign = createSign(characterEncoding, packageParams, API_KEY);
        String wechatPaySign = ((String) packageParams.get("sign"));
        System.out.println("mySign: " + mySign + " , originSign:" + wechatPaySign);
        return wechatPaySign.equals(mySign);

    }

    /**
     * @param characterEncoding 编码格式
     * @return
     * @author
     * @date 2016-4-22
     * @Description：sign签名
     */
    public static String createSign(String characterEncoding, Map<String, String> packageParams, String API_KEY) throws Exception {

        Set<String> keySet = packageParams.keySet();
        String[] keyArray = keySet.toArray(new String[keySet.size()]);
        Arrays.sort(keyArray);
        StringBuilder sb = new StringBuilder();
        for (String k : keyArray) {
            if (k.equals("sign")) {
                continue;
            }
            if (packageParams.get(k).trim().length() > 0) // 参数值为空，则不参与签名
                sb.append(k).append("=").append(packageParams.get(k).trim()).append("&");
        }
        sb.append("key=").append(API_KEY);
        return MD5WechatPayUtil.MD5Encode(sb.toString(), characterEncoding).toUpperCase();
    }


    /**
     * @param parameters 请求参数
     * @return
     * @author
     * @date 2016-4-22
     * @Description：将请求参数转换为xml格式的string
     */
    public static String getRequestXml(SortedMap<Object, Object> parameters) {
        StringBuffer sb = new StringBuffer();
        sb.append("<xml>");
        Set es = parameters.entrySet();
        Iterator it = es.iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            String k = (String) entry.getKey();
            Object v = entry.getValue();
            if ("attach".equalsIgnoreCase(k) || "body".equalsIgnoreCase(k) || "sign".equalsIgnoreCase(k)) {
                sb.append("<" + k + ">" + "<![CDATA[" + v + "]]></" + k + ">");
            } else {
                sb.append("<" + k + ">" + v + "</" + k + ">");
            }
        }
        sb.append("</xml>");
        return sb.toString();
    }

    /**
     * 取出一个指定长度大小的随机正整数.
     *
     * @param length int 设定所取出随机数的长度。length小于11
     * @return int 返回生成的随机数。
     */
    public static int buildRandom(int length) {
        int num = 1;
        double random = Math.random();
        if (random < 0.1) {
            random = random + 0.1;
        }
        for (int i = 0; i < length; i++) {
            num = num * 10;
        }
        return (int) ((random * num));
    }

    /**
     * 获取当前时间 yyyyMMddHHmmss
     *
     * @return String
     */
    public static String getCurrTime() {
        Date now = new Date();
        SimpleDateFormat outFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String s = outFormat.format(now);
        return s;
    }

    /**
     * 微信通知后台支付状态后，后台需返回response数据，
     * 此方法为后台打包返回数据
     *
     * @return
     */
    public static String packNotifyResXml(boolean status) {
        String resXml = "";
        if (status) {
            resXml = "<xml>" + "<return_code><![CDATA[SUCCESS]]></return_code>"
                    + "<return_msg><![CDATA[OK]]></return_msg>" + "</xml> ";
        } else {
            resXml = "<xml>" + "<return_code><![CDATA[FAIL]]></return_code>"
                    + "<return_msg><![CDATA[签名失败]]></return_msg>" + "</xml> ";
        }
        return resXml;
    }

}