package viewcoder.tool.job;

import com.aliyun.oss.OSSClient;
import org.apache.commons.text.StrSubstitutor;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.quartz.*;
import viewcoder.operation.entity.Orders;
import viewcoder.operation.entity.Project;
import viewcoder.operation.entity.UserUploadFile;
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

            //C. 对即将过期的用户发送短信和邮件到期提醒
            sendMsgMailInform();

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
                int space = getToReleaseSpace(order);
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
                        if (CommonService.checkNotNull(user) && CommonService.checkNotNull(user.getTimestamp()) &&
                                CommonService.checkNotNull(user.getResource_remain())) {

                            //如果该用户可用的resource_remain空间小于0则设置该用户resource的ACL权限私有
                            if (Integer.parseInt(user.getResource_remain()) <= 0) {
                                //对single_export资源文件设置ACK禁用操作
                                List<Project> projects = sqlSession.selectList(Mapper.GET_PROJECT_VERSION_DATA, order.getUser_id());
                                if (CommonService.checkNotNull(projects)) {
                                    String projectPrefix = GlobalConfig.getOssFileUrl(Common.SINGLE_EXPORT);
                                    for (Project project : projects) {
                                        String pathPc = projectPrefix + project.getPc_version() + Common.PROJECT_FILE_SUFFIX;
                                        String pathMo = projectPrefix + project.getMo_version() + Common.PROJECT_FILE_SUFFIX;
                                        OssOpt.updateAclConfig(ossClient, pathPc, false);
                                        OssOpt.updateAclConfig(ossClient, pathMo, false);
                                    }
                                }
                                //对upload_files资源文件设置ACK禁用操作
                                List<UserUploadFile> files = sqlSession.selectList(Mapper.GET_RESOURCE_NAME_DATA, order.getUser_id());
                                if (CommonService.checkNotNull(files)) {
                                    String filePrefix = GlobalConfig.getOssFileUrl(Common.UPLOAD_FILES);
                                    for (UserUploadFile file : files) {
                                        String pathFile = filePrefix + file.getTime_stamp() + Common.DOT_SUFFIX + file.getSuffix();
                                        OssOpt.updateAclConfig(ossClient, pathFile, false);
                                    }
                                }
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
     * 根据订单的服务id（serviceId）返回相应的资源空间space数据
     *
     * @param orders 订单数据
     * @return
     */
    private static int getToReleaseSpace(Orders orders) {
        int space = 0;
        //设置订购资源空间大小
        switch (orders.getService_id()) {
            case 1: {
                space = Common.SERVICE_TRY_RESOURCE;
                break;
            }
            case 2: {
                space = Common.SERVICE_MONTH_RESOURCE;
                break;
            }
            case 3: {
                space = Common.SERVICE_YEAR_RESOURCE;
                break;
            }
            case 4: {
                space = Common.SERVICE_BUSINESS_RESOURCE;
                break;
            }
            default: {
                //出错抛出不知类型的order的exception
                String message = "Unknown order service id: " + orders.getService_id();
                MidNightJob.logger.error(message);
            }
        }
        return space;
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


    /**
     * 调用发送短息和邮件的方法
     */
    public void sendMsgMailInform() {
        SqlSession sqlSession = MybatisUtils.getSession();
        String message = "";
        try {
            //orders表中所有expire_days为0, 1, 3, 7天且pay_way不为0的（非优惠方式）
            List<Orders> orders = sqlSession.selectList(Mapper.GET_TO_EXPIRE_ORDER_INSTANCE);

            //判空处理，若空返回
            if (!CommonService.checkNotNull(orders)) return;

            //对每条order信息进行发送信息提醒
            Map<String, String> replaceData = new HashMap<String, String>();
            String mailUrl = "", templateId = "";
            String url = GlobalConfig.getProperties(Common.SERVICE_SPACE_URL);

            for (Orders order : orders) {
                int space = getToReleaseSpace(order);
                User user = sqlSession.selectOne(Mapper.GET_USER_MAIL_PHONE_DATA, order.getUser_id());
                //判空返回处理
                if (!CommonService.checkNotNull(user) && !CommonService.checkNotNull(user.getEmail()) &&
                        !CommonService.checkNotNull(user.getPhone()) && space <= 0) continue;

                //邮件服务初始化
                MailEntity mailEntity = new MailEntity(user.getEmail(), Common.MAIL_SERVICE_EXPIRE_INFORM, Common.MAIL_HTML_TYPE);

                //准备替换原文的用户数据
                int spaceRemain = Integer.parseInt(user.getResource_remain()) - space;
                int expireDays = order.getExpire_days();
                replaceData.put("name", user.getUser_name());
                replaceData.put("service_name", CommonObject.getServiceName(order.getService_id()));
                replaceData.put("expire_date", order.getExpire_date());
                replaceData.put("url", url);
                replaceData.put("days", String.valueOf(expireDays));
                replaceData.put("space_remain", String.valueOf(spaceRemain));

                //根据剩余空间大小对应发送不同手机短信和邮件模板
                if (spaceRemain > 0) {
                    templateId = Common.MSG_TEMPLEATE_EXPIRE1;
                    mailUrl = GlobalConfig.getProperties(Common.MAIL_BASE_URL) + "expire_with_space.html"; //本地网页数据

                } else {
                    templateId = Common.MSG_TEMPLEATE_EXPIRE2;
                    mailUrl = GlobalConfig.getProperties(Common.MAIL_BASE_URL) + "expire_no_space.html"; //本地网页数据
                }

                //针对0的实例，提醒实例即将释放
//                templateId = Common.MSG_TEMPLEATE_RELEASE;
//                mailUrl = GlobalConfig.getProperties(Common.MAIL_BASE_URL) + "expire_release.html";


                //发送短信操作
                MsgHelper.sendSingleMsg(templateId, replaceData, user.getPhone(), Common.MSG_SIGNNAME_LIPHIN);

                //发送邮件操作
                String str = StrSubstitutor.replace(MailHelper.getHtmlData(mailUrl, true), replaceData);
                mailEntity.setTextAndContent(str);
                new MailHelper(mailEntity).send();
            }

        } catch (Exception e) {

        } finally {
            sqlSession.close();
        }
    }
}


