package viewcoder.operation.impl.common;

import viewcoder.operation.entity.Project;
import viewcoder.operation.entity.User;
import viewcoder.tool.common.Common;
import viewcoder.tool.common.Mapper;
import viewcoder.tool.common.OssOpt;
import viewcoder.tool.config.GlobalConfig;
import viewcoder.tool.test.TestResponseOpt;
import viewcoder.tool.util.HttpUtil;
import viewcoder.operation.entity.UserUploadFile;
import viewcoder.operation.entity.response.ResponseData;
import viewcoder.operation.entity.response.StatusCode;
import com.alibaba.fastjson.JSON;
import com.aliyun.oss.OSSClient;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.junit.Assert;
import viewcoder.tool.util.MybatisUtils;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Administrator on 2018/2/20.
 */
public class CommonService {

    private static Logger logger = Logger.getLogger(CommonService.class);

    /**
     * 获取唯一序列号字符串
     *
     * @return
     */
    public static String getUnionId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 返回时间戳公用方法
     *
     * @return
     */
    public static String getTimeStamp() {
        return String.valueOf(System.currentTimeMillis());
    }

    /**
     * 返回时间点公用方法
     *
     * @return
     */
    public static String getDateTime() {
        Date day = new Date();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return df.format(day);
    }

    /**
     * 打印描述时间方法，用于获取代码运行时间
     *
     * @param depict
     */
    public static void calculateTime(String depict) {
        CommonService.logger.debug("=====================" + "\n" + depict + ": " + getDateTime() + "\n");
    }

    /**
     * 进入http调用后打印request的方法名
     *
     * @param functionName http调用的方法名
     */
    public static void printHttpInvokeFunction(String functionName) {
        CommonService.logger.debug("Come into http request function: " + functionName);
    }


    /**
     * 对数据库进行后续提交和关闭操作
     *
     * @param sqlSession   sql数据库操作句柄
     * @param responseData api调用返回的数据
     * @param toCommit     是否做了需要数据库提交的操作
     */
    public static void databaseCommitClose(SqlSession sqlSession, ResponseData responseData, boolean toCommit) {
        if (sqlSession != null) {
            //传入参数定义是否需要提交操作，纯粹数据库查询则不需提交操作
            if (toCommit) {
                //如果整个流程准确无误地实现则对数据库操作进行提交，否则不提交
                if (responseData != null && responseData.getStatus_code() == StatusCode.OK.getValue()) {
                    sqlSession.commit();
                }
            }
            sqlSession.close(); //数据库操作完毕，关闭连接，释放资源
        }
    }


    /**
     * 查看该对象是否为空，返回不为空的Boolean值
     *
     * @param object
     * @return
     */
    public static boolean checkNotNull(Object object) {
        boolean status = false;
        if (object != null && object != "undefined" && object != "null") {
            if (object instanceof String) {
                if (!((String) object).isEmpty()) {
                    //CommonService.logger.debug("checkNotNull: " + object + " come to String check, result is true");
                    status = true;
                }
            } else {
                //CommonService.logger.debug("checkNotNull: " + object + " come to Object check, result is true");
                status = true;
            }
        }
        return status;
    }


    /**
     * 进行http的request和response数据的断言
     *
     * @param map         发送请求数据
     * @param functionUri 发送请求基于baseUrl的方法请求
     * @param errorCode   验证errorCode
     */
    public static void junitReqRespVerify(Map<String, Object> map, String functionUri, int errorCode) {
        String result = HttpUtil.httpClientUploadFile(Common.BASE_HTTP_URL + functionUri, map);
        ResponseData responseData = JSON.parseObject(result, ResponseData.class);
        Assert.assertNotNull(responseData);
        if (responseData.getStatus_code() == 400) {
            Assert.assertEquals(responseData.getException_code(), errorCode);
        }
        CommonService.logger.debug(functionUri + " get result: " + result);
    }


    /**
     * 进行http的request和response数据的断言
     * 带有一个返回callback函数
     *
     * @param map         发送请求数据
     * @param functionUri 发送请求基于baseUrl的方法请求
     * @param errorCode   验证errorCode
     */
    public static void junitReqRespVerify(Map<String, Object> map, String functionUri, int errorCode,
                                          TestResponseOpt testResponseOpt) {
        String result = HttpUtil.httpClientUploadFile(Common.BASE_HTTP_URL + functionUri, map);
        ResponseData responseData = JSON.parseObject(result, ResponseData.class);
        Assert.assertNotNull(responseData);
        if (responseData.getStatus_code() == 400) {
            Assert.assertEquals(responseData.getException_code(), errorCode);
        }
        testResponseOpt.doResponseOpt(responseData);
        CommonService.logger.debug(functionUri + " get result: " + result);
    }


    /**
     * 生成6位数验证码
     *
     * @return
     */
    public static String generateSixDigits() {
        Random random = new Random();
        String result = "";
        for (int i = 0; i < 6; i++) {
            result += random.nextInt(10);
        }
        return result;
    }


    /**
     * 设置用户的single_export和upload_file资源信息的ACK操作
     *
     * @param sqlSession sql的session操作
     * @param ossClient  oss句柄
     * @param userId     用户id
     * @param ACK        访问权限ACK
     */
    public static void setACKOpt(SqlSession sqlSession, OSSClient ossClient, int userId, boolean ACK) {
        //对single_export资源文件设置ACK禁用操作
        List<Project> projects = sqlSession.selectList(Mapper.GET_PROJECT_VERSION_DATA, userId);
        if (CommonService.checkNotNull(projects)) {
            String projectPrefix = GlobalConfig.getOssFileUrl(Common.SINGLE_EXPORT);
            for (Project project : projects) {
                String pathPc = projectPrefix + project.getPc_version() + Common.PROJECT_FILE_SUFFIX;
                String pathMo = projectPrefix + project.getMo_version() + Common.PROJECT_FILE_SUFFIX;
                OssOpt.updateAclConfig(ossClient, pathPc, ACK);
                OssOpt.updateAclConfig(ossClient, pathMo, ACK);
            }
        }
        //对upload_files资源文件设置ACK禁用操作
        List<UserUploadFile> files = sqlSession.selectList(Mapper.GET_RESOURCE_NAME_DATA, userId);
        if (CommonService.checkNotNull(files)) {
            String filePrefix = GlobalConfig.getOssFileUrl(Common.UPLOAD_FILES);
            for (UserUploadFile file : files) {
                String pathFile = filePrefix + file.getTime_stamp() + Common.DOT_SUFFIX + file.getSuffix();
                OssOpt.updateAclConfig(ossClient, pathFile, ACK);
            }
        }

        //更新数据的ACK数据
        int targetACK = ACK ? 1 : 0;
        User user = new User(userId, targetACK);
        sqlSession.update(Mapper.UPDATE_USER_ACK, user);
    }

}

















