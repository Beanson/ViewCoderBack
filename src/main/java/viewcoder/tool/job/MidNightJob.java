package viewcoder.tool.job;

import com.aliyun.oss.OSSClient;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.quartz.*;
import viewcoder.exception.task.TaskException;
import viewcoder.tool.mail.MailEntity;
import viewcoder.tool.mail.MailHelper;
import viewcoder.tool.msg.MsgHelper;
import viewcoder.operation.entity.Instance;
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
        SqlSession sqlSession = MybatisUtils.getSession();

        try {
            //A. instance表expire_days比原来减少1天
            updateInstanceExpireDays(sqlSession);
            //B. 获取所有expire_days为0, 1, 3, 7天的instance
            List<Instance> targetInstance = getTargetInstance(sqlSession);
            //C. 针对0的实例, 释放用户资源并删除该instance条目
            updateUserSpace(targetInstance, sqlSession);
            //D. 短信和邮件服务调用发送通知
            sendMsgMail(targetInstance, sqlSession);

        } catch (Exception e) {
            MidNightJob.logger.error("MidNightJob error", e);

        } finally {
            sqlSession.close();
        }
    }

    /**
     * 更新所有实例的过期时间
     *
     * @param sqlSession
     */
    public void updateInstanceExpireDays(SqlSession sqlSession) throws TaskException {
        int num = sqlSession.update(Mapper.UPDATE_INSTANCE_EXPIRE_DAYS);
        if (num <= 0) {
            throw new TaskException("updateInstanceExpireDays error, num is: " + num);
        }
    }

    /**
     * 获取指定枚举集合的instance的信息
     * 　0, 1, 3, 7天的过期时间
     *
     * @param sqlSession
     */
    public List<Instance> getTargetInstance(SqlSession sqlSession) throws TaskException {
        List<Instance> list = sqlSession.selectList(Mapper.GET_TO_EXPIRE_INSTANCE);
        if (list != null) {
            return list;
        } else {
            throw new TaskException("getTargetInstance error, list is null");
        }
    }

    /**
     * 释放过期的实例资源，并更新User表的可用空间
     *
     * @param targetInstance
     * @param sqlSession
     */
    public void updateUserSpace(List<Instance> targetInstance, SqlSession sqlSession) {

        OSSClient ossClient = OssOpt.initOssClient(); //初始化ossclient客户端连接
        for (Instance instance :
                targetInstance) {
            //更新user表resource_remain减去该instance的space空间
            if (instance.getExpire_days() <= 0 && Integer.parseInt(instance.getSpace()) > 0) {
                try {
                    //删除该instance实例条目
                    int delete_num = sqlSession.delete(Mapper.DELETE_EXPIRE_INSTANCE, instance);

                    //更新user表的资源空间
                    int update_num = sqlSession.update(Mapper.REMOVE_EXPIRE_INSTANCE_SPACE, instance);
                    if (update_num <= 0) {
                        MidNightJob.logger.warn("updateUserSpace num<=0 error, " + instance.toString());
                        throw new TaskException("remove user expire instance space update number <=0 ");

                    } else {
                        //更新成功，接着获取user表的timestamp和resource_used的信息
                        User user = sqlSession.selectOne(Mapper.GET_USER_SPACE_INFO, instance.getUser_id());

                        //检查是否为null
                        if (CommonService.checkNotNull(user.getTimestamp()) && CommonService.checkNotNull(user.getResource_remain())) {

                            //如果该用户可用的resource_remain空间小于0则设置该用户resource的ACL权限私有
                            if (Integer.parseInt(user.getResource_remain()) <= 0) {
                                //如果update的number大于0，则进行OSS文件的ACL权限设置操作
                                //对该用户在OSS中的所有资源文件的ACL设置为私有访问
                                //文件结构为：viewcoder-bucket/upload_file/{{timestamp}}/
                                String ossFolderPrefix = GlobalConfig.getProperties(Common.UPLOAD_FILES) + "/" + user.getTimestamp() + "/";
                                OssOpt.updateAclConfig(ossClient, ossFolderPrefix, false);
                                MidNightJob.logger.debug("updateUserSpace successfully, " + instance.toString());
                            }
                            //手动commit操作，正确更新ACL或无需更新ACL，都对之前的数据库操作进行commit
                            sqlSession.commit();

                        } else {
                            throw new TaskException("get user timestamp null error");
                        }
                    }
                } catch (Exception e) {
                    MidNightJob.logger.error("updateUserSpace error: ", e);
                    sqlSession.rollback(); //回滚commit之前的操作
                }
            }
        }
    }


    /**
     * 调用发送短息和邮件的方法
     *
     * @param targetInstance 目标实例
     * @param sqlSession     sql句柄语句
     */
    public void sendMsgMail(List<Instance> targetInstance, SqlSession sqlSession) throws Exception {

        for (Instance instance :
                targetInstance) {
            User user = sqlSession.selectOne(Mapper.GET_USER_DATA, instance.getUser_id());
            MailEntity mailEntity = new MailEntity(user.getEmail(), Common.MAIL_INSTANCE_EXPIRE_NOTIFICATION, Common.MAIL_HTML_TYPE);
            String mailUrl = "", templateId = "";
            //准备替换原文的用户数据
            int spaceRemain = Integer.parseInt(user.getResource_remain()) - Integer.parseInt(instance.getSpace());
            Map<String, String> replaceData = new HashMap<String, String>();
            replaceData.put("name", user.getUser_name());
            replaceData.put("service_name", CommonObject.getServiceName(instance.getService_id()));
            replaceData.put("expire_date", instance.getEnd_date());
            replaceData.put("url", GlobalConfig.getProperties(Common.SERVICE_SPACE_URL));
            replaceData.put("days", String.valueOf(instance.getExpire_days()));
            replaceData.put("space_remain", String.valueOf(spaceRemain));

            //根据不同剩余过期时间不同处理方式
            int expireDays = instance.getExpire_days();
            if (expireDays == 0) {
                //针对0的实例，提醒实例已经释放
                templateId = Common.MSG_TEMPLEATE_RELEASE;
                mailUrl = GlobalConfig.getProperties(Common.MAIL_BASE_URL) + "expire_release.html";

            } else if (expireDays == 1 || expireDays == 3 || expireDays == 7) {
                //针对1，3, 7的实例，如果是日/月/年套餐则进行发送
                //根据剩余空间大小对应发送不同手机短信和邮件模板
                if (spaceRemain > 0) {
                    templateId = Common.MSG_TEMPLEATE_EXPIRE1;
                    mailUrl = GlobalConfig.getProperties(Common.MAIL_BASE_URL) + "expire_with_space.html"; //本地网页数据

                } else {
                    templateId = Common.MSG_TEMPLEATE_EXPIRE2;
                    mailUrl = GlobalConfig.getProperties(Common.MAIL_BASE_URL) + "expire_no_space.html"; //本地网页数据
                }
            } else {
                throw new TaskException("Task job send message unknown expire days: " + expireDays);
            }

            //发送短信操作
            MsgHelper.sendSingleMsg(templateId, replaceData, user.getPhone(), Common.MSG_SIGNNAME_LIPHIN);
            //发送邮件操作
            String str = StrSubstitutor.replace(MailHelper.getHtmlData(mailUrl, true), replaceData);
            System.out.println(str);
            mailEntity.setTextAndContent(str);
            new MailHelper(mailEntity).send();
        }
    }
}


//TODO 3 防止XSS攻击，　保存文件到另一个域，用iframe装像jsfiddle一样
