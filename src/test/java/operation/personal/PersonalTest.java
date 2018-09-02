package operation.personal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import viewcoder.operation.impl.common.CommonService;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2018/3/4.
 */
public class PersonalTest {

    private static Logger logger = LoggerFactory.getLogger(PersonalTest.class);

    @Test
    public void testChangeUserInfo(){
        Map<String, Object> map = new HashMap<>();
        //更新user_name信息，无portrait图片信息
        map.put("id", 1);
        map.put("user_name", "beanson1");
        map.put("phone", "183-16433415");
        map.put("role", "Developer");
        map.put("nation", "England");
        map.put("portrait", "456.png");
        CommonService.junitReqRespVerify(map,"updateUserInfo",200);

        //有portrait图片信息
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("files/ly.png").getFile());
        map.put("portrait", "789.png");
        map.put("portrait_file",file);
        CommonService.junitReqRespVerify(map,"updateUserInfo",200);
    }
}
