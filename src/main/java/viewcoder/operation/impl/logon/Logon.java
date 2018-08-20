package viewcoder.operation.impl.logon;

import sun.rmi.runtime.Log;
import viewcoder.operation.entity.Project;
import viewcoder.operation.entity.WeChatInfo;
import viewcoder.operation.impl.project.ProjectList;
import viewcoder.tool.cache.GlobalCache;
import viewcoder.tool.common.Assemble;
import viewcoder.tool.common.Common;
import viewcoder.tool.common.CommonObject;
import viewcoder.tool.common.Mapper;
import viewcoder.tool.msg.MsgHelper;
import viewcoder.tool.parser.form.FormData;
import viewcoder.tool.util.MybatisUtils;
import viewcoder.operation.entity.User;
import viewcoder.operation.entity.response.ResponseData;
import viewcoder.operation.impl.common.CommonService;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by Administrator on 2018/2/2.
 */
public class Logon {

    private static Logger logger = Logger.getLogger(Logon.class);

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
            //检查该邮件地址是否已被注册过
            User userDB = sqlSession.selectOne(Mapper.REGISTER_ACCOUNT_CHECK, user);
            //进行新用户注册逻辑并对responseData进行相应赋值
            Logon.signUpLogic(responseData, userDB, user, sqlSession);

        } catch (Exception e) {
            Logon.logger.debug("Sign up catch exception: ", e);
            Assemble.responseErrorSetting(responseData, 500,
                    "Server error");

        } finally {
            CommonService.databaseCommitClose(sqlSession, responseData, true);

            //给刚注册成功的用户提供example页面case
            if (responseData.getStatus_code() == 200 && user != null && user.getId() > 0) {
                Project project = new Project();
                project.setUser_id(user.getId());
                project.setRef_id(Common.EXAMPLE_REF_ID);
                project.setOpt_type(Common.STORE_TYPE);
                project.setProject_name(Common.EXAMPLE_CASE_1);
                project.setNew_parent(0);
                project.setOpt(1);//1代表从project面板中创建，3是重新生成页面，会重构当前页面
                ProjectList.copyProject(project);
            }
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
                Logon.logger.warn("Phone signed before");
                Assemble.responseErrorSetting(responseData, 401,
                        "Phone signed before");

            } else if (Objects.equals(userDB.getEmail(), user.getEmail())) {
                //Email之前已注册过
                Logon.logger.warn("Email signed before");
                Assemble.responseErrorSetting(responseData, 402,
                        "Email signed before");

            } else {
                //不明觉厉的case
                Logon.logger.warn("Unknown case：" + userDB.toString());
                Assemble.responseErrorSetting(responseData, 403,
                        "Unknown case：" + userDB.toString());
            }

        } else {
            //验证手机验证码操作
            if (Objects.equals(user.getVerifyCode(), GlobalCache.getRegisterVerifyCache().get(user.getPhone()))) {
                //插入数据库进行注册操作
                user.setPortrait("default_portrait.png");//设置默认portrait
                int num = sqlSession.insert(Mapper.REGISTER_NEW_ACCOUNT, user);
                Logon.logger.debug("注册添加数：" + num + " ；用户id为：" + user.getId());

                if (num > 0) {
                    //如果添加记录后影响记录数大于0，则添加成功
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
                    Logon.logger.error("Insert info to db error");
                    Assemble.responseErrorSetting(responseData, 404,
                            "Insert info to db error");
                }
            } else {
                Logon.logger.warn("verify code incorrect");
                Assemble.responseErrorSetting(responseData, 405,
                        "verify code incorrect");
            }
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
                generatePhoneVerifyCode(phone);
                //返回成功数据
                Assemble.responseSuccessSetting(responseData, null);
            }

        } catch (Exception e) {
            Logon.logger.error("getRegisterVerifyCode error: ", e);
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
        String message = "";

        try {
            WeChatInfo weChatInfo = (WeChatInfo) FormData.getParam(msg, WeChatInfo.class);
            int num = sqlSession.update(Mapper.UPDATE_WECHAT_INFO_TO_USER, weChatInfo);
            if (num > 0) {
                Assemble.responseSuccessSetting(responseData, null);
            } else {
                message = "Db Insert Error";
                Logon.logger.warn(message);
                Assemble.responseErrorSetting(responseData, 401, message);
            }

        } catch (Exception e) {
            message = "System error";
            Logon.logger.error(message, e);
            Assemble.responseErrorSetting(responseData, 500, message);

        } finally {
            CommonService.databaseCommitClose(sqlSession, responseData, true);
        }
        return responseData;
    }


    /**
     * *******************************************************************************************************************
     * 用户登录方法
     *
     * @param msg
     * @return
     */
    public static ResponseData ViewCoderLogin(Object msg) {
        ResponseData responseData = new ResponseData();
        SqlSession sqlSession = MybatisUtils.getSession();

        try {
            User user = (User) FormData.getParam(msg, User.class);
            //检查是否已存在有该用户
            User userDB = sqlSession.selectOne(Mapper.LOGON_VALIDATION, user);
            //进行已注册过的用户登录逻辑，并对responseData相应赋值
            signInLogic(responseData, userDB, user, sqlSession);

        } catch (Exception e) {
            Logon.logger.debug("Sign in catch exception: ", e);
            Assemble.responseErrorSetting(responseData, 500,
                    "Server error");

        } finally {
            CommonService.databaseCommitClose(sqlSession, responseData, false);
        }
        return responseData;
    }

    /**
     * 进行已注册过的用户登录逻辑，并对responseData相应赋值
     *
     * @param responseData 返回数据包装
     * @param userDB       从数据库查询的用户信息
     * @param user         从http数据流接收的请求用户信息
     * @param sqlSession   sql句柄
     */
    private static void signInLogic(ResponseData responseData, User userDB, User user, SqlSession sqlSession) {
        if (userDB != null) {
            //登录成功则返回用户的profile信息
            userDB.setPassword(null);//不会传密码

            //查看该userId是否已经存在登录信息，若是则发消息回去告知要短信验证，否则走登录成功流程
            if (CommonService.checkNotNull(CommonObject.getLoginVerify().get(user.getId()))) {
                //若已经系统存在该user登录信息，则可能多用户同时登录状态，此时发送验证码进行验证
                generatePhoneVerifyCode(user.getPhone());
                Assemble.responseErrorSetting(responseData, 301, "Multi Login");

            } else {
                //赋予该用户session_id
                userDB.setSession_id(CommonService.getTimeStamp());
                //返回数据到前端
                Assemble.responseSuccessSetting(responseData, userDB);
                //插入userId : sessionId 的map数据到object中
                CommonObject.getLoginVerify().put(userDB.getId(), userDB.getSession_id());
            }

        } else {
            //登录不成功，数据库无该user信息，查看是否该账号存在
            List<User> users = sqlSession.selectList(Mapper.SIGN_ACCOUNT_CHECK, user);

            if (users.size() > 0) {
                //账号存在但是密码错误
                Logon.logger.error("Password error");
                Assemble.responseErrorSetting(responseData, 401,
                        "Correct account, wrong password");
            } else {
                //账号尚未注册
                Logon.logger.error("Account not exist");
                Assemble.responseErrorSetting(responseData, 402,
                        "Account not exist");
            }
        }
    }


    /**
     * 验证登录验证码是否一致
     *
     * @param msg
     * @return
     */
    public static ResponseData signInVerifyCodeCheck(Object msg) {
        ResponseData responseData = new ResponseData();
        SqlSession sqlSession = MybatisUtils.getSession();
        String message = "";

        try {
            User user = (User) FormData.getParam(msg, User.class);
            //检查是否已存在有该用户
            User userDB = sqlSession.selectOne(Mapper.LOGON_VALIDATION, user);
            if (userDB != null) {
                String targetVerifyCode = GlobalCache.getRegisterVerifyCache().get(userDB.getPhone());
                //比较系统验证码和用户填写的验证码是否一致，是则返回成功，否则返回false
                if (CommonService.checkNotNull(targetVerifyCode) && Objects.equals(user.getVerifyCode(), targetVerifyCode)) {
                    //验证成功，更新该用户对应的sessionId
                    userDB.setSession_id(CommonService.getTimeStamp());
                    CommonObject.getLoginVerify().put(userDB.getId(), userDB.getSession_id());
                    Assemble.responseSuccessSetting(responseData, userDB);

                } else {
                    message = "verify code error";
                    Logon.logger.error(message);
                    Assemble.responseErrorSetting(responseData, 405, message, userDB.getPhone());
                }
            } else {
                message = "no such account";
                Logon.logger.error(message);
                Assemble.responseErrorSetting(responseData, 406, message);
            }

        } catch (Exception e) {
            message = "system error";
            Logon.logger.debug(message, e);
            Assemble.responseErrorSetting(responseData, 500, message);

        } finally {
            CommonService.databaseCommitClose(sqlSession, responseData, false);
        }
        return responseData;
    }


    /**
     * 生成验证码并发送到手机
     *
     * @param phone 手机号
     */
    private static void generatePhoneVerifyCode(String phone) {
        //获取6为数验证码
        String sixDigits = CommonService.generateSixDigits();
        //发送验证码到用户手机
        Map<String, String> map = new HashMap<>();
        map.put(Common.CODE, sixDigits);
        //发送短信验证码
        MsgHelper.sendSingleMsg(Common.MESG_REGISTER_VERIFY_CODE, map, phone, Common.MSG_SIGNNAME_LIPHIN);
        //验证码存储到cache中
        GlobalCache.getRegisterVerifyCache().put(phone, sixDigits);
    }


    /**
     * 用户直接扫描微信二维码登录
     *
     * @param msg
     * @return
     */
    public static ResponseData weChatUserLogin(Object msg) {
        ResponseData responseData = new ResponseData();
        SqlSession sqlSession = MybatisUtils.getSession();
        String message = "";

        try {
            //获取用户open_id
            String openId = FormData.getParam(msg, Common.OPEN_ID);
            //数据库读取到该open_id对应的user记录
            User user = sqlSession.selectOne(Mapper.GET_USER_BY_OPEN_ID, openId);

            if (CommonService.checkNotNull(user)) {
                //更新sessionid内存数据
                String timestamp = CommonService.getTimeStamp();
                CommonObject.getLoginVerify().put(user.getId(), timestamp);
                user.setSession_id(timestamp);
                user.setPassword(null);

                Assemble.responseSuccessSetting(responseData, user);

            } else {
                message = "Not such openId in user db";
                Logon.logger.error(message);
                Assemble.responseErrorSetting(responseData, 401, message);
            }

        } catch (Exception e) {
            message = "System error";
            Logon.logger.error(message, e);
            Assemble.responseErrorSetting(responseData, 500, message);

        } finally {
            CommonService.databaseCommitClose(sqlSession, responseData, false);
        }
        return responseData;
    }


}
