package viewcoder.helper.mail;

import org.apache.commons.lang3.text.StrSubstitutor;
import viewcoder.helper.mail.entity.ExpireNotifyMail;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/4/28.
 */
public class SendMail {

    /**
     * 发邮件提醒过期续费
     * @param list
     */
    public void sendExpireNotify(List<ExpireNotifyMail> list) {

        //循环遍历list的所有发送的人
        for (ExpireNotifyMail expireNotifyMail:
                list) {
            //准备替换文件正文的数据
            Map<String, String> replaceData = new HashMap<String, String>();
            replaceData.put("name", expireNotifyMail.getName());
            replaceData.put("renewUrl", expireNotifyMail.getRenewUrl()); //点击进入续费的url

            //准备html数据
            String url= "src/main/resources/mail/expire_notification.html"; //本地网页数据
            String html = MailHelper.getHtmlData(url, false);

            //进行占位符的replace拼装
            String content="";
            if (html != null && !html.isEmpty()) {
                content = StrSubstitutor.replace(html, replaceData);
            }

            //设置mail数据
            MailHelper mailHelper = new MailHelper("html")
                    .setHost("localhost")
                    .setFrom("***@qq.com")
                    .setTo(expireNotifyMail.getTo())
                    .setSubject("过期提醒")
                    .setTextAndContent(content);

            //发送mail
            mailHelper.send();
        }
    }
}
