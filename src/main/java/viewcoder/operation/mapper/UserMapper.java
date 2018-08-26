package viewcoder.operation.mapper;

/**
 * Created by Administrator on 2017/2/8.
 */

import viewcoder.operation.entity.User;
import org.apache.ibatis.annotations.*;
import viewcoder.operation.entity.WeChatInfo;

import java.util.List;

//@CacheNamespace(flushInterval = 3000)
public interface UserMapper {

    /********************以下是选择user操作***********************/
    //根据user_id获得该user的全部信息
    @Select("select * from user where id=#{user_id}")
    public User getUserData(int user_id);

    /** 登录 *************************************************************/
    //登录验证
    @Select("select * from user where (email=#{account} or phone=#{account}) and password=#{password}")
    public List<User> loginValidation(User user);

    //登录验证
    @Select("select * from user where email=#{account} or phone=#{account}")
    public List<User> signAccountCheck(User user);

    /** 注册 *************************************************************/
    //手机号查重
    @Select("select count(*) from user where phone=#{phone}")
    public int getPhoneAccount(String phone);

    //注册验证
    @Select("select email, phone from user where email=#{email} or phone=#{phone}")
    public User registerAccountCheck(User user);

    //获取用户原portrait数据信息
    @Select("select portrait from user where id=#{id}")
    public String getOriginPortraitName(String user_id);

    //根据user_id获取用户剩余使用空间
    @Select("select resource_remain from user where id=#{userId}")
    public String getUserResourceSpaceRemain(int userId);

    //根据user_id获取用户最新的total_points数据
    @Select("select total_points from user where id=#{userId}")
    public int getTotalPoints(int userId);

    //根据user_id获取用户space的数据
    @Select("select resource_remain, resource_used, timestamp from user where id=#{userId}")
    public User getUserSpaceInfo(int userId);

    //根据openid获取用户的数据
    @Select("select * from user where openid=#{openId}")
    public User getUserByOpenId(String openId);



    /********************以下是插入user操作***********************/
    //注册操作， 默认用户名为手机号，因为注册时需要验证码，验证过该手机用户存在，绑定微信后再进行update个人信息处理
    @Insert("insert into user(timestamp,user_name,email,password,phone) values(#{timestamp},#{user_name},#{email},#{password},#{phone})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    public int registerNewAccount(User user);



    /********************以下是删除user操作***********************/
    //测试Junit时删除新添加的user数据
    @Delete("delete from user where email=#{email}")
    public int deleteUserInDb(String email);



    /********************以下是更新user操作***********************/
    //更新用户个人信息
    @Update("update user set user_name=#{user_name},role=#{role},phone=#{phone},nation=#{nation},portrait=#{portrait} where id=#{id}")
    public int updateUserInfo(User user);

    //更新用户新的可用resource空间
    @Update("update user set resource_remain=#{resource_remain} where id=#{id}")
    public String updateUserResourceSpaceRemain(User user);

    //更新用户最新使用的industry和sub_industry信息
    @Update("update user set last_store_code=#{last_store_code}, last_store_sub_code=#{last_store_sub_code} where id=#{user_id}")
    public String updateLastSelectedIndustry(@Param("user_id") Integer user_id, @Param("last_store_code") String last_store_code,
                                             @Param("last_store_sub_code") String last_store_sub_code);

    //更新用户积分数值
    @Update("update user set total_points=#{total_points} where id=#{id}")
    public int updateUserTotalPoints(User user);

    //释放用户表中过期的实例的空间
    //@Update("update user set resource_remain=resource_remain-#{space_expire} where id=#{user_id}")
    //public int removeExpireInstanceSpace(Instance instance);

    //更新用户framework和package_way的信息
    @Update("update user set framework=#{framework}, package_way=#{package_way} where id=#{id}")
    public int updateExportDefaultSetting(User user);

    //更新用户微信昵称和微信的头像等信息
    @Update("update user set user_name=#{nickname}, openid=#{openid}, portrait=#{headimgurl}, province=#{province}, " +
            "city=#{city}, unionid=#{unionid}, sex=#{sex} where id=#{user_id}")
    public int updateWeChatInfoToUser(WeChatInfo weChatInfo);

}
