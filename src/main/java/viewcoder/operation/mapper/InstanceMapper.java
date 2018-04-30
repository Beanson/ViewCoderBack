package viewcoder.operation.mapper;

/**
 * Created by Administrator on 2017/2/8.
 */

import viewcoder.operation.entity.Instance;
import org.apache.ibatis.annotations.*;

import java.util.List;

//@CacheNamespace(flushInterval = 3000)
public interface InstanceMapper {

    /********************以下是选择instance操作***********************/
    //根据user_id获得该相关的instance信息
    @Select("select * from instance where id=#{user_id}")
    public List<Instance> getInstanceByUserId(int user_id);

    //选择即将过期和已经过期的实例
    @Select("select * from instance where expire_days in (0, 1, 3, 7)")
    public List<Instance> getToExpireInstance(int user_id);


    /********************以下是插入instance操作***********************/



    /********************以下是删除instance操作***********************/
    //删除过期了的账户
    @Delete("delete from instance where id=#{instanceId}")
    public int deleteExpireInstance(int instanceId);


    /********************以下是更新instance操作***********************/
    //设置过期时间减少一天
    @Update("update instance set expire_days=(expire_days-1)")
    public int updateInstanceExpireDays(int instanceId);

}
