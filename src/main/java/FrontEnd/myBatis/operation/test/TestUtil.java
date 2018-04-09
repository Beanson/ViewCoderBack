package FrontEnd.myBatis.operation.test;

import FrontEnd.helper.common.Assemble;
import FrontEnd.helper.parser.form.FormData;
import FrontEnd.myBatis.MybatisUtils;
import FrontEnd.myBatis.entity.User;
import FrontEnd.myBatis.entity.response.ResponseData;
import FrontEnd.myBatis.operation.common.CommonService;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;

/**
 * Created by Administrator on 2018/2/19.
 */
public class TestUtil {

    private static Logger logger = Logger.getLogger(TestUtil.class);

    public static ResponseData testEntity(Object msg) {
        ResponseData responseData = new ResponseData();
        SqlSession sqlSession = MybatisUtils.getSession();

        try {
            //测试用hashmap接收数据
//            Map<String, Object> data= FormData.getParam(msg, "id","user_name","portrait_file");
//            for (Map.Entry<String, Object> entry : data.entrySet()) {
//                System.out.println("key= " + entry.getKey() + " and value= " + entry.getValue());
//            }
            //测试用对象数据接收数据
            User user = (User) FormData.getParam(msg, User.class);
            TestUtil.logger.debug("get user data: " + user);

            Assemble.responseSuccessSetting(responseData, null);

        } catch (Exception e) {
            Assemble.responseErrorSetting(responseData, 500, "Sys Error");
            TestUtil.logger.error("testEntity error: ", e);

        } finally {
            //对数据库进行后续提交和关闭操作等
            CommonService.databaseCommitClose(sqlSession, responseData, false);
        }
        return responseData;
    }
}
