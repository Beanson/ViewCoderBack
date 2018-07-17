package viewcoder.operation.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import viewcoder.operation.entity.Feedback;
import viewcoder.operation.entity.User;

/**
 * Created by Administrator on 2018/7/17.
 * 客户feedback反馈sql mapper
 */
public interface FeedbackMapper {

    /********************以下是选择feedback操作***********************/
    //根据user_id获得该feedback的全部信息
    @Select("select * from feedback where user_id=#{user_id}")
    public Feedback getFeedbackData(int user_id);



    /********************以下是插入user操作***********************/
    @Insert("insert into feedback(user_id, subject, message, phone, email) values(#{user_id}, #{subject}, #{message}, #{phone}, #{email}) ")
    public int insertNewFeedback(Feedback feedback);




}
