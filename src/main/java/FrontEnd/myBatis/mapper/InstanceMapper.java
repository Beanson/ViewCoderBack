package FrontEnd.myBatis.mapper;

/**
 * Created by Administrator on 2017/2/8.
 */

import FrontEnd.myBatis.entity.Instance;
import FrontEnd.myBatis.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

//@CacheNamespace(flushInterval = 3000)
public interface InstanceMapper {

    /********************以下是选择instance操作***********************/
    //根据user_id获得该相关的instance信息
    @Select("select * from instance where id=#{user_id}")
    public List<Instance> getInstanceByUserId(int user_id);



    /********************以下是插入instance操作***********************/



    /********************以下是删除instance操作***********************/



    /********************以下是更新instance操作***********************/

}
