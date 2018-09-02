package viewcoder.tool.job;

import org.apache.commons.text.StrSubstitutor;
import org.apache.ibatis.session.SqlSession;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import viewcoder.operation.entity.Orders;
import viewcoder.operation.entity.User;
import viewcoder.operation.impl.common.CommonService;
import viewcoder.operation.impl.purchase.Purchase;
import viewcoder.tool.common.Common;
import viewcoder.tool.common.CommonObject;
import viewcoder.tool.common.Mapper;
import viewcoder.tool.config.GlobalConfig;
import viewcoder.tool.mail.MailEntity;
import viewcoder.tool.mail.MailHelper;
import viewcoder.tool.msg.MsgHelper;
import viewcoder.tool.util.MybatisUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/8/29.
 */
public class ExpireJob implements Job {

    private static Logger logger = LoggerFactory.getLogger(ExpireJob.class);


    /**
     * 午夜task运行部分
     *
     * @param context
     * @throws JobExecutionException
     */
    public void execute(JobExecutionContext context)
            throws JobExecutionException {
        try {
            //C. 对即将过期的用户发送短信和邮件到期提醒
            sendMsgMailInform();

        } catch (Exception e) {
            ExpireJob.logger.error("ExpireJob error", e);
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
            String templateId = Common.MSG_TEMPLEATE_EXPIRE1;
            String mailUrl = GlobalConfig.getProperties(Common.MAIL_BASE_URL) + Common.MAIL_SERVICE_EXPIRE; //本地网页数据

            for (Orders order : orders) {
                int space = CommonObject.getServiceSpace(order.getService_id());
                User user = sqlSession.selectOne(Mapper.GET_USER_MAIL_PHONE_DATA, order.getUser_id());
                //判空返回处理
                if (!CommonService.checkNotNull(user) && !CommonService.checkNotNull(user.getEmail()) &&
                        !CommonService.checkNotNull(user.getPhone()) && space <= 0) continue;

                //邮件服务初始化
                MailEntity mailEntity = new MailEntity(user.getEmail(), Common.MAIL_SERVICE_EXPIRE_INFORM, Common.MAIL_HTML_TYPE);

                //准备替换原文的用户数据
                int expireDays = order.getExpire_days();
                replaceData.put("name", user.getUser_name());
                replaceData.put("service", CommonObject.getServiceName(order.getService_id()));
                replaceData.put("time", order.getExpire_date());
                replaceData.put("days", String.valueOf(expireDays));

                //发送短信操作${name} ${service} ${time} ${days}
                MsgHelper.sendSingleMsg(templateId, replaceData, user.getPhone(), Common.MSG_SIGNNAME_LIPHIN);

                //发送邮件操作
                String str = StrSubstitutor.replace(MailHelper.getHtmlData(mailUrl, true), replaceData);
                mailEntity.setTextAndContent(str);
                new MailHelper(mailEntity).send();
            }

        } catch (Exception e) {
            message = "Send mail and text err";
            ExpireJob.logger.error(message, e);

        } finally {
            sqlSession.close();
        }
    }
}
