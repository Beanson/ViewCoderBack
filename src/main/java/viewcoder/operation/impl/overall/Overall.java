package viewcoder.operation.impl.overall;

import com.aliyun.oss.OSSClient;
import viewcoder.operation.entity.Feedback;
import viewcoder.tool.common.Assemble;
import viewcoder.tool.common.Common;
import viewcoder.tool.common.Mapper;
import viewcoder.tool.common.OssOpt;
import viewcoder.tool.config.GlobalConfig;
import viewcoder.tool.parser.form.FormData;
import viewcoder.tool.util.MybatisUtils;
import viewcoder.operation.entity.User;
import viewcoder.operation.entity.response.ResponseData;
import viewcoder.operation.impl.common.CommonService;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;

/**
 * Created by Administrator on 2018/2/28.
 */
public class Overall {

    private static Logger logger = Logger.getLogger(Overall.class);


    public static ResponseData getUserInfoById(Object msg) {
        ResponseData responseData = new ResponseData();
        SqlSession sqlSession = MybatisUtils.getSession();

        try {
            String userId = FormData.getParam(msg, Common.USER_ID);
            //根据userId从数据库中查找对应的user信息
            User user = sqlSession.selectOne(Mapper.GET_USER_DATA, Integer.parseInt(userId));
            if (user != null && user.getId() > 0) {
                user.setPassword(null);
                Assemble.responseSuccessSetting(responseData, user);
            } else {
                Assemble.responseErrorSetting(responseData, 401,
                        "Get user info from database null error");
            }

        } catch (Exception e) {
            Overall.logger.debug("Get User info with exception: ", e);
            Assemble.responseErrorSetting(responseData, 500,
                    "Server error");

        } finally {
            CommonService.databaseCommitClose(sqlSession, responseData, false);
        }
        return responseData;
    }



    /**
     * 接收客户的建议和反馈消息
     * 1、数据库中添加新记录
     * 2、oss中存储feedback的信息
     * @param msg
     * @return
     */
    public static ResponseData sendSuggestion(Object msg) {
        ResponseData responseData = new ResponseData();
        SqlSession sqlSession = MybatisUtils.getSession();
        OSSClient ossClient = OssOpt.initOssClient();

        try {
            Feedback feedback = (Feedback) FormData.getParam(msg, Feedback.class);
            //设置message存储的时间戳，实际text文本存储在oss中
            feedback.setMessage(CommonService.getTimeStamp());
            //新建feedback记录插入数据库中
            int num = sqlSession.insert(Mapper.INSERT_NEW_FEEDBACK, feedback);
            //如果数据库插入成功则把反馈text插入oss中
            if (num > 0) {
                //生成新的oss文件
                String feedbackFileName = GlobalConfig.getOssFileUrl(Common.FEEDBACK) + feedback.getMessage() + Common.TEXT_FILE_SUFFIX;
                OssOpt.uploadFileToOss(feedbackFileName, feedback.getText().getBytes(), ossClient);
                //返回数据插入成功的消息体
                Assemble.responseSuccessSetting(responseData, null);
            } else {
                //插入新记录到数据库中失败
                Assemble.responseErrorSetting(responseData, 401,
                        "insert new feedback to database null error");
            }

        } catch (Exception e) {
            Overall.logger.debug("insert new feedback with error: ", e);
            Assemble.responseErrorSetting(responseData, 500,
                    "Server error");

        } finally {
            CommonService.databaseCommitClose(sqlSession, responseData, true);
        }
        return responseData;
    }
}
