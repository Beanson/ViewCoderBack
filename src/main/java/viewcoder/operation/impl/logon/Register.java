package viewcoder.operation.impl.logon;

import com.aliyun.oss.OSSClient;
import org.apache.ibatis.session.SqlSession;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import viewcoder.operation.entity.Project;
import viewcoder.operation.entity.User;
import viewcoder.operation.entity.WeChatInfo;
import viewcoder.operation.entity.response.ResponseData;
import viewcoder.operation.impl.common.CommonService;
import viewcoder.operation.impl.project.ProjectList;
import viewcoder.operation.impl.purchase.Purchase;
import viewcoder.tool.cache.GlobalCache;
import viewcoder.tool.common.*;
import viewcoder.tool.config.GlobalConfig;
import viewcoder.tool.parser.form.FormData;
import viewcoder.tool.util.MybatisUtils;

import java.io.InputStream;
import java.net.URL;
import java.util.Objects;

/**
 * Created by Administrator on 2018/2/2.
 */
public class Register {

    private static Logger logger = LoggerFactory.getLogger(Register.class);

    /**
     * *********************************************************************************
     * 注册新用户方法
     *
     * @param msg
     * @return
     */
    public static ResponseData ViewCoderRegister(Object msg) {

        ResponseData responseData = new ResponseData();
        SqlSession sqlSession = MybatisUtils.getSession();
        User user = null;

        try {
            user = (User) FormData.getParam(msg, User.class);
            //检查该邮件和电话是否已被注册过
            User userDB = sqlSession.selectOne(Mapper.REGISTER_ACCOUNT_CHECK, user);
            //进行新用户注册逻辑并对responseData进行相应赋值
            Register.signUpLogic(responseData, userDB, user, sqlSession);

        } catch (Exception e) {
            Register.logger.debug("Sign up catch exception: ", e);
            Assemble.responseErrorSetting(responseData, 500,
                    "Server error");

        } finally {
            CommonService.databaseCommitClose(sqlSession, responseData, true);
            //注册成功后操作，免费试用三天的使用套餐+提供一个example页面，放在commit后操作防止死锁
            afterRegisterSuccessLogic(responseData, user);
        }
        return responseData;
    }

    /**
     * 进行新用户注册逻辑并对responseData进行相应赋值
     *
     * @param responseData 包装返回数据
     * @param userDB       查询数据库中user结果信息
     * @param user         http请求发送过来的user查询信息
     * @param sqlSession   sql句柄
     */
    private static void signUpLogic(ResponseData responseData, User userDB, User user, SqlSession sqlSession) {

        if (userDB != null) {
            //查看数据库相关数据并和原数据作比对
            if (Objects.equals(userDB.getPhone(), user.getPhone())) {
                //Phone之前已注册过
                Register.logger.warn("Phone signed before");
                Assemble.responseErrorSetting(responseData, 401,
                        "Phone signed before");

            } else if (Objects.equals(userDB.getEmail(), user.getEmail())) {
                //Email之前已注册过
                Register.logger.warn("Email signed before");
                Assemble.responseErrorSetting(responseData, 402,
                        "Email signed before");

            } else {
                //不明觉厉的case
                Register.logger.warn("Unknown case：" + userDB.toString());
                Assemble.responseErrorSetting(responseData, 403,
                        "Unknown case：" + userDB.toString());
            }

        } else {
            //验证手机验证码操作
            if (Objects.equals(user.getVerifyCode(), GlobalCache.getRegisterVerifyCache().get(user.getPhone()))) {
                //插入数据库进行注册操作
                user.setTimestamp(CommonService.getTimeStamp());
                //默认用户名称为电话，后续步骤微信扫码会update该名字，若不扫码则使用手机为用户名
                user.setUser_name(user.getPhone());
                //插入用户默认portrait，万一用户不扫码绑定则也有默认portrait
                user.setPortrait(Common.DEFAULT_PORTRAIT);
                //新注册用户有20M的可用空间
                user.setResource_total(Common.SERVICE_TRY_RESOURCE);
                //注册并把用户id set进userId中
                int num = sqlSession.insert(Mapper.REGISTER_NEW_ACCOUNT, user);
                Register.logger.debug("注册添加数：" + num + " ；用户id为：" + user.getId());

                //如果添加记录后影响记录数大于0，则添加成功
                if (num > 0) {
                    //返回数据时不传递密码
                    user.setPassword(null);
                    //赋予该用户session_id
                    user.setSession_id(CommonService.getTimeStamp());
                    //正确返回操作
                    Assemble.responseSuccessSetting(responseData, user);
                    //插入userId : sessionId 的map数据到object中
                    CommonObject.getLoginVerify().put(user.getId(), user.getSession_id());

                } else {
                    //添加记录数目等于0，则添加失败
                    Register.logger.error("Insert info to db error");
                    Assemble.responseErrorSetting(responseData, 404,
                            "Insert info to db error");
                }
            } else {
                Register.logger.warn("verify code incorrect");
                Assemble.responseErrorSetting(responseData, 405,
                        "verify code incorrect");
            }
        }
    }


    /**
     * 注册成功后相应后续逻辑操作
     * 1、添加 example 实例
     * 2、插入订单表和更新用户个人数据
     *
     * @param responseData 返回数据
     * @param user 用户数据
     */
    private static void afterRegisterSuccessLogic(ResponseData responseData, User user) {

        if (responseData.getStatus_code() == 200 && user != null && user.getId() > 0) {
            //给刚注册成功的用户提供example页面case
            Project project = new Project();
            project.setUser_id(user.getId());
            project.setRef_id(Common.EXAMPLE_REF_ID);
            project.setOpt_type(Common.STORE_TYPE);
            project.setProject_name(Common.EXAMPLE_CASE_1);
            project.setNew_parent(0);
            project.setOpt(1);//1代表从project面板中创建，3是重新生成页面，会重构当前页面
            ProjectList.copyProject(project);

            //插入orders表和更新user表相关数据
            Purchase.newRegisterTryService(user.getId());
        }
    }


    /**
     * 获取手机验证码操作
     *
     * @param msg 前端传递过来的数据
     * @return
     */
    public static ResponseData getRegisterVerifyCode(Object msg) {
        ResponseData responseData = new ResponseData();
        SqlSession sqlSession = MybatisUtils.getSession();
        try {
            //获取从前端传递过来的phone
            String phone = FormData.getParam(msg, Common.PHONE);
            //查看数据库中该phone是否已经注册过了
            int num = sqlSession.selectOne(Mapper.GET_PHONE_ACCOUNT, phone);
            if (num > 0) {
                //告知用户该手机已被注册
                Assemble.responseErrorSetting(responseData, 401, "phone has registered");

            } else {
                //生成验证码并发送到手机
                Logon.generatePhoneVerifyCode(phone);
                //返回成功数据
                Assemble.responseSuccessSetting(responseData, null);
            }

        } catch (Exception e) {
            Register.logger.error("getRegisterVerifyCode error: ", e);
            Assemble.responseErrorSetting(responseData, 500,
                    "getRegisterVerifyCode error");
        } finally {
            CommonService.databaseCommitClose(sqlSession, responseData, false);
        }
        return responseData;
    }


    /**
     * 注册成功后扫码绑定二维码操作
     *
     * @param msg
     */
    public static ResponseData updateWeChatInfoToUser(Object msg) {
        ResponseData responseData = new ResponseData();
        SqlSession sqlSession = MybatisUtils.getSession();
        OSSClient ossClient = OssOpt.initOssClient();
        String message = "";

        try {
            WeChatInfo weChatInfo = (WeChatInfo) FormData.getParam(msg, WeChatInfo.class);
            if (CommonService.checkNotNull(weChatInfo)) {
                //保存微信头像到oss
                if (CommonService.checkNotNull(weChatInfo.getHeadimgurl())) {
                    //OSS保存头像为PNG图片
                    URL url = new URL(weChatInfo.getHeadimgurl());
                    InputStream is = url.openStream();
                    String portrait = CommonService.getTimeStamp() + Common.IMG_PNG;
                    OssOpt.uploadFileToOss((GlobalConfig.getOssFileUrl(Common.PORTRAIT_IMG) + portrait), is, ossClient);
                    //更新微信信息，即将插入数据库
                    weChatInfo.setHeadimgurl(portrait);

                } else {
                    weChatInfo.setHeadimgurl(Common.DEFAULT_PORTRAIT);
                }

                int num = sqlSession.update(Mapper.UPDATE_WECHAT_INFO_TO_USER, weChatInfo);
                if (num > 0) {
                    User user = sqlSession.selectOne(Mapper.GET_USER_DATA, weChatInfo.getUser_id());
                    user.setSession_id(CommonService.getTimeStamp());
                    CommonObject.getLoginVerify().put(user.getId(), user.getSession_id());
                    //给user信息脱敏，返回user数据
                    user.setPassword(null);
                    Assemble.responseSuccessSetting(responseData, user);

                } else {
                    message = "Db Insert Error";
                    Register.logger.warn(message);
                    Assemble.responseErrorSetting(responseData, 401, message);
                }

            } else {
                message = "WeChat info null";
                Register.logger.warn(message);
                Assemble.responseErrorSetting(responseData, 402, message);
            }

        } catch (Exception e) {
            message = "System error";
            Register.logger.error(message, e);
            Assemble.responseErrorSetting(responseData, 500, message);

        } finally {
            CommonService.databaseCommitClose(sqlSession, responseData, true);
            OssOpt.shutDownOssClient(ossClient);
        }
        return responseData;
    }

}
