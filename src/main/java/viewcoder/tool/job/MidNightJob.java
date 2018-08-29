package viewcoder.tool.job;

import com.aliyun.oss.OSSClient;
import org.apache.commons.text.StrSubstitutor;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.quartz.*;
import viewcoder.operation.entity.Orders;
import viewcoder.operation.entity.Project;
import viewcoder.operation.entity.UserUploadFile;
import viewcoder.operation.impl.purchase.Purchase;
import viewcoder.tool.mail.MailEntity;
import viewcoder.tool.mail.MailHelper;
import viewcoder.tool.msg.MsgHelper;
import viewcoder.operation.entity.User;
import viewcoder.operation.impl.common.CommonService;
import viewcoder.tool.common.Common;
import viewcoder.tool.common.CommonObject;
import viewcoder.tool.common.Mapper;
import viewcoder.tool.common.OssOpt;
import viewcoder.tool.config.GlobalConfig;
import viewcoder.tool.util.MybatisUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/4/28.
 */
public class MidNightJob implements Job {

    private static Logger logger = Logger.getLogger(MidNightJob.class);

    /**
     * 午夜task运行部分
     *
     * @param context
     * @throws JobExecutionException
     */
    public void execute(JobExecutionContext context)
            throws JobExecutionException {
        try {
            //A. 获取orders表expire_days为0的全部订单，并更新user信息
            updateUserSpace();

            //B. orders表expire_days>=0的记录比原来减少1天
            updateOrderExpireDays();

        } catch (Exception e) {
            MidNightJob.logger.error("MidNightJob error", e);
        }
    }


    /**
     * 释放过期的实例资源，并更新User表的可用空间
     */
    public void updateUserSpace() {
        //资源初始化操作
        SqlSession sqlSession = MybatisUtils.getSession();
        OSSClient ossClient = OssOpt.initOssClient();
        Map<String, Object> map = new HashMap<>(2);
        String message = "";

        try {
            //获取过期订单信息
            List<Orders> ordersList = sqlSession.selectList(Mapper.GET_EXPIRED_ORDER_INSTANCE);

            //判空返回处理
            if (!CommonService.checkNotNull(ordersList)) {
                return;
            }

            //循环更新user表resource_remain减去需要释放的表空间
            for (Orders order : ordersList) {

                //根据serviceId获取相应space数据
                int space = CommonObject.getServiceSpace(order.getService_id());
                if (space <= 0) continue;

                try {
                    //更新user表的资源空间
                    map.put(Common.SPACE_EXPIRE, space);
                    map.put(Common.USER_ID, order.getUser_id());
                    int update_num = sqlSession.update(Mapper.REMOVE_EXPIRE_ORDER_SPACE, map);

                    if (update_num <= 0) {
                        //更新失败进行下一个操作
                        message = "updateUserSpace num<=0 error:" + order.toString();
                        MidNightJob.logger.warn(message);

                    } else {
                        //对sql句柄进行一次commit去锁
                        sqlSession.commit();

                        //更新成功，接着获取user表的timestamp和resource_used的信息
                        User user = sqlSession.selectOne(Mapper.GET_USER_SPACE_INFO, order.getUser_id());
                        //判空操作
                        if (CommonService.checkNotNull(user)) {
                            int resourceRemain = user.getResource_total() - user.getResource_used();
                            //如果该用户可用的resource_remain空间小于0则设置该用户resource的ACL权限私有
                            if (resourceRemain <= 0) {
                                CommonService.setACKOpt(sqlSession, ossClient, order.getUser_id(),false);
                            }

                        } else {
                            //获取用户信息失败
                            message = "Get user info null error" + user;
                            MidNightJob.logger.warn(message);
                        }
                    }
                } catch (Exception e) {
                    //系统发生错误
                    message = "system error 1";
                    MidNightJob.logger.error(message, e);
                }
            }
        } catch (Exception e) {
            //系统发生错误
            message = "system error 2";
            MidNightJob.logger.error(message, e);

        } finally {
            ossClient.shutdown();
            sqlSession.close();
        }
    }


    /**
     * 更新orders表中每条记录的expire_days不为-1的记录的expire_days值减1
     */
    private static void updateOrderExpireDays() {
        String message = "";
        SqlSession sqlSession = MybatisUtils.getSession();

        try {
            int num = sqlSession.update(Mapper.UPDATE_ORDER_INSTANCE_EXPIRE_DAYS);

        } catch (Exception e) {
            message = "update expire days error";
            MidNightJob.logger.error(message, e);

        } finally {
            sqlSession.commit();
            sqlSession.close();
        }
    }

}


