package operation.test;

import org.apache.commons.io.IOUtils;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import viewcoder.tool.common.Common;
import viewcoder.tool.config.GlobalConfig;
import viewcoder.tool.util.HttpUtil;
import org.junit.Test;
import viewcoder.tool.util.MybatisUtils;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;


/**
 * Created by Administrator on 2018/2/19.
 */
public class TestUtilTest {

    private static Logger logger = LoggerFactory.getLogger(TestUtilTest.class);

    @Test
    public void testEntityTest(){
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("log4j.properties").getFile());
        Map<String, Object> map=new HashMap<>();
        map.put("unknown",26);
        map.put("user_name","beanson");
        map.put("portrait_file",file);
        String result= HttpUtil.httpClientUploadFile("http://127.0.0.1:8080/testWechatPay",map);
        logger.debug("get result: "+result);
    }

    @Test
    public void testEntityPureText(){
        HttpUtil.httpClientPureText("http://127.0.0.1:8080/testWechatPay","hello world");
    }


    public static String encryptBASE64(byte[] data) {
        // BASE64Encoder encoder = new BASE64Encoder();
        // String encode = encoder.encode(data);
        // 从JKD 9开始rt.jar包已废除，从JDK 1.8开始使用java.util.Base64.Encoder
        Base64.Encoder encoder = Base64.getEncoder();
        String encode = encoder.encodeToString(data);
        return encode;
    }
    /**
     * BASE64Decoder 解密
     *
     * @param data
     *            要解密的字符串
     * @return 解密后的byte[]
     * @throws Exception
     */
    public static byte[] decryptBASE64(String data) throws Exception {
        // BASE64Decoder decoder = new BASE64Decoder();
        // byte[] buffer = decoder.decodeBuffer(data);
        // 从JKD 9开始rt.jar包已废除，从JDK 1.8开始使用java.util.Base64.Decoder
        Base64.Decoder decoder = Base64.getDecoder();
        byte[] buffer = decoder.decode(data);
        return buffer;
    }


    @Test
    public void tryConfig() throws Exception{

    }
}
