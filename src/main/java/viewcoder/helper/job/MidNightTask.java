package viewcoder.helper.job;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.ListObjectsRequest;
import com.aliyun.oss.model.OSSObjectSummary;
import com.aliyun.oss.model.ObjectListing;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.quartz.*;
import viewcoder.exception.task.TaskException;
import viewcoder.operation.entity.Instance;
import viewcoder.operation.entity.User;
import viewcoder.operation.impl.common.CommonService;
import viewcoder.tool.common.Assemble;
import viewcoder.tool.common.Common;
import viewcoder.tool.common.Mapper;
import viewcoder.tool.common.OssOpt;
import viewcoder.tool.util.MybatisUtils;

import java.util.List;

/**
 * Created by Administrator on 2018/4/28.
 */
public class MidNightTask implements Job {

    private static Logger logger = Logger.getLogger(MidNightTask.class);

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
            //针对0的实例，如果是日/月/年套餐则进行发送
            //针对1的实例，如果是日/月/年套餐则进行发送
            //针对3的实例，如果是日/月/年套餐则进行发送
            //针对7的实例，如果是月/年套餐则进行发送

        } catch (Exception e) {
            MidNightTask.logger.error("MidNightTask error", e);

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
            if (instance.getExpire_days() <= 0 && instance.getSpace() > 0) {
                try {
                    //删除该instance实例条目
                    int delete_num = sqlSession.delete(Mapper.DELETE_EXPIRE_INSTANCE, instance);

                    //更新user表的资源空间
                    int update_num = sqlSession.update(Mapper.REMOVE_EXPIRE_INSTANCE_SPACE, instance);
                    if (update_num <= 0) {
                        MidNightTask.logger.warn("updateUserSpace num<=0 error, " + instance.toString());
                        throw new TaskException("remove user expire instance space update number <=0 ");

                    } else {
                        //更新成功，接着获取user表的timestamp和resource_used的信息
                        User user = sqlSession.selectOne(Mapper.GET_USER_SPACE_INFO, instance.getUser_id());

                        //检查是否为null
                        if (CommonService.checkNotNull(user.getTimestamp()) && CommonService.checkNotNull(user.getResource_remain())) {

                            //如果该用户可用的resource_remain空间小于0则设置该用户resource的ACL权限私有
                            if(Integer.parseInt(user.getResource_remain()) <= 0){
                                //如果update的number大于0，则进行OSS文件的ACL权限设置操作
                                //对该用户在OSS中的所有资源文件的ACL设置为私有访问
                                //文件结构为：viewcoder-bucket/upload_file/{{timestamp}}/
                                String ossFolderPrefix = Common.UPLOAD_FILES_FOLDER + "/" + user.getTimestamp() + "/";
                                OssOpt.updateAclConfig(ossClient, ossFolderPrefix, false);
                                MidNightTask.logger.debug("updateUserSpace successfully, " + instance.toString());
                            }
                            //手动commit操作，正确更新ACL或无需更新ACL，都对之前的数据库操作进行commit
                            sqlSession.commit();

                        } else {
                            throw new TaskException("get user timestamp null error");
                        }
                    }
                } catch (Exception e) {
                    MidNightTask.logger.error("updateUserSpace error: ", e);
                    sqlSession.rollback(); //回滚commit之前的操作
                }
            }
        }
    }


}


//TODO 3 防止XSS攻击，　保存文件到另一个域，用iframe装像jsfiddle一样
