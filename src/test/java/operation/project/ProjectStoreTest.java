package operation.project;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static viewcoder.operation.impl.common.CommonService.junitReqRespVerify;

/**
 * Created by Administrator on 2018/4/9.
 */
public class ProjectStoreTest {

    private static Logger logger = LoggerFactory.getLogger(ProjectStoreTest.class);

    /**
     * 更新project的开放程度的测试
     */
    @Test
    public void updateProjectOpennessTest() {
        Map<String, Object> map = new HashMap<>();
        //成功返回user_id为1的所有用户的数据
//        map.put("user_id", 1);
//        map.put("project_id", 177);
//        map.put("is_public", 1);
//        map.put("industry_code", 'I');
//        map.put("industry_sub_code", 65);

        map.put("is_public", 0);
        map.put("user_id", 1);
        map.put("project_id", 177);

        junitReqRespVerify(map,"updateProjectOpenness",200);
    }
}
