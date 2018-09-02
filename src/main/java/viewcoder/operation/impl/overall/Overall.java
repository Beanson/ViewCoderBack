package viewcoder.operation.impl.overall;

import com.aliyun.oss.OSSClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import viewcoder.ViewCoderServer;
import viewcoder.operation.entity.Feedback;
import viewcoder.operation.entity.response.StatusCode;
import viewcoder.tool.common.*;
import viewcoder.tool.config.GlobalConfig;
import viewcoder.tool.parser.form.FormData;
import viewcoder.tool.util.MybatisUtils;
import viewcoder.operation.entity.User;
import viewcoder.operation.entity.response.ResponseData;
import viewcoder.operation.impl.common.CommonService;
import org.apache.ibatis.session.SqlSession;

import java.util.Map;
import java.util.Objects;

/**
 * Created by Administrator on 2018/2/28.
 */
public class Overall {

    private static Logger logger = LoggerFactory.getLogger(Overall.class);


    /**
     * overall中刷新页面时重新获取用户信息
     *
     * @param msg
     * @return
     */
    public static ResponseData getUserInfoByIdAndSessionId(Object msg) {
        ResponseData responseData = new ResponseData(StatusCode.ERROR.getValue());
        SqlSession sqlSession = MybatisUtils.getSession();
        String message = "";

        try {
            Map<String, Object> map = FormData.getParam(msg);
            String userId = String.valueOf(map.get(Common.USER_ID));
            String sessionId = String.valueOf(map.get(Common.SESSION_ID));

            if (CommonService.checkNotNull(userId) && CommonService.checkNotNull(sessionId)) {
                String targetSessionId = CommonObject.getLoginVerify().get(Integer.parseInt(userId));
                if (Objects.equals(sessionId, targetSessionId)){

                    //根据userId从数据库中查找对应的user信息
                    User user = sqlSession.selectOne(Mapper.GET_USER_DATA, Integer.parseInt(userId));
                    if (user != null && user.getId() > 0) {
                        user.setPassword(null);
                        user.setSession_id(targetSessionId);
                        Assemble.responseSuccessSetting(responseData, user);

                    } else {
                        message = "Get user info from database null error";
                        Overall.logger.warn(message);
                        Assemble.responseErrorSetting(responseData, 401, message);
                    }
                }else {
                    message = "reLogin suspect";
                    Overall.logger.warn(message);
                    Assemble.responseErrorSetting(responseData, 402, message);
                }

            } else {
                message = "user or session null";
                Overall.logger.warn(message);
                Assemble.responseErrorSetting(responseData, 403, message);
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
     *
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
            OssOpt.shutDownOssClient(ossClient);
        }
        return responseData;
    }


    /**
     * 用户退出登录，sessionId记录移除
     *
     * @param msg
     * @return
     */
    public static ResponseData logoutUserAccount(Object msg) {
        ResponseData responseData = new ResponseData(StatusCode.ERROR.getValue());
        String message = "";

        try {
            Map<String, Object> map = FormData.getParam(msg);
            int userId = Integer.parseInt(String.valueOf(map.get(Common.USER_ID)));
            String sessionId = String.valueOf(map.get(Common.SESSION_ID));

            //保证传递的数据正确性
            if (CommonService.checkNotNull(sessionId) && userId > 0) {

                String targetSessionId = CommonObject.getLoginVerify().get(userId);
                //验证若传递的sessionId和后台记录的sessionId一致则后台取消该sessionId的记录条目
                if (Objects.equals(sessionId, targetSessionId)) {
                    CommonObject.getLoginVerify().remove(userId);
                    Assemble.responseSuccessSetting(responseData, null);

                } else {
                    message = "sessionId not match. request sessionId: " + sessionId + " , target sessionId: " + targetSessionId;
                    Assemble.responseErrorSetting(responseData, 401, message);
                    Overall.logger.warn(message);
                }

            } else {
                message = "request data error. sessionId: " + sessionId + " , userId: " + userId;
                Assemble.responseErrorSetting(responseData, 402, message);
                Overall.logger.warn(message);
            }

        } catch (Exception e) {
            message = "system error";
            Assemble.responseErrorSetting(responseData, 500, message);
            Overall.logger.error(message, e);
        }
        return responseData;
    }
}
