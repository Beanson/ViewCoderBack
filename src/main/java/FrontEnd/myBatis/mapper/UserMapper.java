package FrontEnd.myBatis.mapper;

/**
 * Created by Administrator on 2017/2/8.
 */
import FrontEnd.myBatis.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@CacheNamespace(flushInterval = 50000)
public interface UserMapper {

    /********************以下是选择user操作***********************/
    //根据user_id获得该user的全部信息
    @Select("select * from user where id=#{user_id}")
    public User getUserData(int user_id);

    //登录验证
    @Select("select * from user where email=#{email} and password=#{password}")
    public List<User> loginValidation(User user);

    //注册验证
    @Select("select * from user where email=#{email}")
    public List<User> registerAccountCheck(User user);

    //获取用户原portrait数据信息
    @Select("select portrait from user where id=#{id}")
    public String getOriginPortraitName(String user_id);

    //根据user_id获取用户剩余使用空间
    @Select("select resource_remain from user where id=#{userId}")
    public String getUserResourceSpaceRemain(int userId);



    /********************以下是插入user操作***********************/
    //注册操作
    @Insert("insert into user(portrait,user_name,email,password) values(#{portrait},#{user_name},#{email},#{password})")
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






}
