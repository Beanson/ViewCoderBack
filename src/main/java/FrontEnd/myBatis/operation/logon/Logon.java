package FrontEnd.myBatis.operation.logon;

import FrontEnd.helper.common.Assemble;
import FrontEnd.helper.common.Mapper;
import FrontEnd.helper.parser.form.FormData;
import FrontEnd.myBatis.MybatisUtils;
import FrontEnd.myBatis.entity.User;
import FrontEnd.myBatis.entity.response.ResponseData;
import FrontEnd.myBatis.operation.common.CommonService;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * Created by Administrator on 2018/2/2.
 */
public class Logon {

    private static Logger logger = Logger.getLogger(Logon.class);

    /**
     * *********************************************************************************
     * 注册新用户方法
     * @param msg
     * @return
     */
    public static ResponseData ViewCoderRegister(Object msg) {

        ResponseData responseData=new ResponseData();
        SqlSession sqlSession = MybatisUtils.getSession();

        try{
            User user = (User) FormData.getParam(msg, User.class);

            //检查该邮件地址是否已被注册过
            User userDB = sqlSession.selectOne(Mapper.REGISTER_ACCOUNT_CHECK, user);
            //进行新用户注册逻辑并对responseData进行相应赋值
            Logon.signUpLogic(responseData,userDB,user,sqlSession);

        }catch (Exception e){
            Logon.logger.debug("Sign up catch exception: ",e);
            Assemble.responseErrorSetting(responseData,500,
                    "Server error");

        }finally {
            CommonService.databaseCommitClose(sqlSession,responseData,true);
        }
        return responseData;
    }

    /**
     * 进行新用户注册逻辑并对responseData进行相应赋值
     * @param responseData 包装返回数据
     * @param userDB 查询数据库中user结果信息
     * @param user http请求发送过来的user查询信息
     * @param sqlSession sql句柄
     */
    private static void signUpLogic(ResponseData responseData, User userDB, User user,SqlSession sqlSession){

        if (userDB != null) {
            //Email之前已注册过
            Assemble.responseErrorSetting(responseData,401,
                    "Email signed before");

        } else {
            //如果Email之前尚未注册过则进行注册操作
            user.setPortrait("default_portrait.png");//设置默认portrait
            int num = sqlSession.insert(Mapper.REGISTER_NEW_ACCOUNT, user);
            Logon.logger.debug("注册添加数：" + num + " ；用户id为：" + user.getId());

            if (num > 0) {
                //如果添加记录后影响记录数大于0，则添加成功
                //返回数据时不传递密码
                user.setPassword(null);
                Assemble.responseSuccessSetting(responseData,user);

            } else {
                //添加记录数目等于0，则添加失败
                Logon.logger.error("Insert info to db error");
                Assemble.responseErrorSetting(responseData,402,
                        "Insert info to db error");
            }
        }
    }


    /**
     * ******************************************************************************************
     * 老用户登录方法
     * @param msg
     * @return
     */
    public static ResponseData ViewCoderLogin(Object msg) {
        ResponseData responseData=new ResponseData();
        SqlSession sqlSession = MybatisUtils.getSession();

        try{
            User user = (User) FormData.getParam(msg, User.class);
            //检查是否已存在有该用户
            User userDB = sqlSession.selectOne(Mapper.LOGON_VALIDATION, user);
            //进行已注册过的用户登录逻辑，并对responseData相应赋值
            Logon.signInLogic(responseData,userDB,user,sqlSession);

        }catch (Exception e){
            Logon.logger.debug("Sign in catch exception: ",e);
            Assemble.responseErrorSetting(responseData,500,
                    "Server error");

        }finally {
            CommonService.databaseCommitClose(sqlSession,responseData,false);
        }
        return responseData;
    }

    /**
     * 进行已注册过的用户登录逻辑，并对responseData相应赋值
     * @param responseData 返回数据包装
     * @param userDB 从数据库查询的用户信息
     * @param user 从http数据流接收的请求用户信息
     * @param sqlSession sql句柄
     */
    private static void signInLogic(ResponseData responseData, User userDB, User user,SqlSession sqlSession){
        if (userDB != null) {
            //登录成功则返回用户的profile信息
            userDB.setPassword(null);//不会传密码
            Assemble.responseSuccessSetting(responseData,userDB);

        } else {
            //登录不成功，数据库无该user信息，查看是否该账号存在
            List<User> users = sqlSession.selectList(Mapper.REGISTER_ACCOUNT_CHECK, user);

            if (users.size() > 0) {
                //账号存在但是密码错误
                Logon.logger.error("Password error");
                Assemble.responseErrorSetting(responseData,401,
                        "Correct account, wrong password");
            } else {
                //账号尚未注册
                Logon.logger.error("Account not exist");
                Assemble.responseErrorSetting(responseData,402,
                        "Account not exist");
            }
        }
    }
}
