package operation.test;

import FrontEnd.helper.util.HttpUtil;
import FrontEnd.myBatis.operation.common.CommonService;
import operation.logon.LogonTest;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2018/2/19.
 */
public class TestUtilTest {

    private static Logger logger = Logger.getLogger(TestUtilTest.class);

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

    @Test
    public void testOrderList(){
        System.out.println(CommonService.getTimeStamp());
    }
}
