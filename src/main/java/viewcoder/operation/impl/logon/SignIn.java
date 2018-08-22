package viewcoder.operation.impl.logon;

import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import viewcoder.operation.entity.User;
import viewcoder.operation.entity.response.ResponseData;
import viewcoder.operation.impl.common.CommonService;
import viewcoder.tool.cache.GlobalCache;
import viewcoder.tool.common.Assemble;
import viewcoder.tool.common.Common;
import viewcoder.tool.common.CommonObject;
import viewcoder.tool.common.Mapper;
import viewcoder.tool.parser.form.FormData;
import viewcoder.tool.util.MybatisUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by Administrator on 2018/8/21.
 */
public class SignIn {

    private static Logger logger = Logger.getLogger(SignIn.class);

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
            SignIn.logger.debug("Sign in catch exception: ", e);
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
            if (CommonService.checkNotNull(CommonObject.getLoginVerify().get(userDB.getId()))) {
                //若已经系统存在该user登录信息，则可能多用户同时登录状态，此时发送验证码进行验证
                Logon.generatePhoneVerifyCode(userDB.getPhone());
                Assemble.responseErrorSetting(responseData, 301, "Multi Login", userDB.getPhone());

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
                SignIn.logger.error("Password error");
                Assemble.responseErrorSetting(responseData, 401,
                        "Correct account, wrong password");
            } else {
                //账号尚未注册
                SignIn.logger.error("Account not exist");
                Assemble.responseErrorSetting(responseData, 402,
                        "Account not exist");
            }
        }
    }


    /**
     * 获取手机验证码操作
     *
     * @param msg 前端传递过来的数据
     * @return
     */
    public static ResponseData getSignVerifyCode(Object msg) {
        ResponseData responseData = new ResponseData();
        SqlSession sqlSession = MybatisUtils.getSession();
        String message = "";

        try {
            //获取从前端传递过来的account
            Map<String, Object> map = FormData.getParam(msg);

            //查看数据库中该account对应的user信息
            User user = sqlSession.selectOne(Mapper.LOGON_VALIDATION, map);
            if (CommonService.checkNotNull(user)) {
                //生成验证码并发送到手机
                Logon.generatePhoneVerifyCode(user.getPhone());
                //返回成功数据
                Assemble.responseSuccessSetting(responseData, user.getPhone());

            } else {
                //告知用户该账号尚未注册
                message = "account or password not right";
                SignIn.logger.debug(message);
                Assemble.responseErrorSetting(responseData, 401, message);
            }

        } catch (Exception e) {
            message = "system error";
            SignIn.logger.error(message, e);
            Assemble.responseErrorSetting(responseData, 500, message);

        } finally {
            CommonService.databaseCommitClose(sqlSession, responseData, false);
        }
        return responseData;
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
            if (CommonService.checkNotNull(userDB)) {

                String targetVerifyCode = GlobalCache.getRegisterVerifyCache().get(userDB.getPhone());
                //比较系统验证码和用户填写的验证码是否一致，是则返回成功，否则返回false
                if (CommonService.checkNotNull(targetVerifyCode) && Objects.equals(user.getVerifyCode(), targetVerifyCode)) {

                    //验证成功，更新该用户对应的sessionId
                    userDB.setSession_id(CommonService.getTimeStamp());
                    CommonObject.getLoginVerify().put(userDB.getId(), userDB.getSession_id());
                    Assemble.responseSuccessSetting(responseData, userDB);

                } else {
                    message = "verify code error";
                    SignIn.logger.error(message);
                    Assemble.responseErrorSetting(responseData, 405, message, userDB.getPhone());
                }
            } else {
                message = "no such account";
                SignIn.logger.error(message);
                Assemble.responseErrorSetting(responseData, 406, message);
            }

        } catch (Exception e) {
            message = "system error";
            SignIn.logger.debug(message, e);
            Assemble.responseErrorSetting(responseData, 500, message);

        } finally {
            CommonService.databaseCommitClose(sqlSession, responseData, false);
        }
        return responseData;
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
            String openId = FormData.getParam(msg, Common.OPENID);
            //数据库读取到该open_id对应的user记录
            User user = sqlSession.selectOne(Mapper.GET_USER_BY_OPEN_ID, openId);

            if (CommonService.checkNotNull(user)) {

                //更新sessionid内存数据
                user.setSession_id(CommonService.getTimeStamp());
                CommonObject.getLoginVerify().put(user.getId(), user.getSession_id());

                //设置user脱敏信息，返回user相关数据
                user.setPassword(null);
                Assemble.responseSuccessSetting(responseData, user);

            } else {
                message = "Not such openId in user db";
                SignIn.logger.error(message);
                Assemble.responseErrorSetting(responseData, 401, message);
            }

        } catch (Exception e) {
            message = "System error";
            SignIn.logger.error(message, e);
            Assemble.responseErrorSetting(responseData, 500, message);

        } finally {
            CommonService.databaseCommitClose(sqlSession, responseData, false);
        }
        return responseData;
    }
}
