package viewcoder.helper.mail;

import com.sun.mail.util.MailSSLSocketFactory;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import viewcoder.tool.common.Common;
import viewcoder.tool.config.GlobalConfig;
import viewcoder.tool.encrypt.AESEncryptor;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.File;
import java.security.GeneralSecurityException;
import java.util.Properties;


/**
 * Created by Administrator on 2018/4/28.
 */
public class MailHelper {

    private static Logger logger = Logger.getLogger(MailHelper.class);
    private MailEntity mailEntity;

    public MailHelper() {
    }

    public MailHelper(MailEntity mailEntity) {
        this.mailEntity = mailEntity;
    }

    /**
     * 初始化mail的基础数据
     */
    private MimeMessage mailInit() throws Exception {

        //设置邮件发送基础配置
        // Get system properties
        Properties properties = System.getProperties();
        // Setup mail server
        properties.setProperty("mail.transport.protocol", GlobalConfig.getProperties(Common.MAIL_PROTOCOL));
        properties.setProperty("mail.smtp.host", GlobalConfig.getProperties(Common.MAIL_HOST));
        properties.setProperty("mail.smtp.port", GlobalConfig.getProperties(Common.MAIL_PORT));
        // 指定验证为true
        properties.setProperty("mail.smtp.auth", GlobalConfig.getProperties(Common.MAIL_AUTH));
        properties.setProperty("mail.smtp.timeout", GlobalConfig.getProperties(Common.MAIL_TIMEOUT));

        //开启安全协议
        MailSSLSocketFactory sf = null;
        try {
            sf = new MailSSLSocketFactory();
            sf.setTrustAllHosts(true);
        } catch (GeneralSecurityException e) {
            MailHelper.logger.error("mailInit use MailSSLSocketFactory error: ", e);
        }
        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.smtp.ssl.socketFactory", sf);

        // 验证账号及密码，密码需要是第三方授权码
        Authenticator auth = new Authenticator() {
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(GlobalConfig.getProperties(Common.MAIL_FROM),
                        AESEncryptor.AESDncode(Common.AES_KEY, GlobalConfig.getProperties(Common.MAIL_PASS)));
            }
        };
        //设置发送操作变量
        Session session = Session.getInstance(properties, auth);
        MimeMessage message = new MimeMessage(session);
        //设置发送者名称，不然会account的@之前部分作为发送者名称
        String username = MimeUtility.encodeText(GlobalConfig.getProperties(Common.MAIL_USERNAME))+
                "<"+GlobalConfig.getProperties(Common.MAIL_FROM)+">";
        message.setFrom(new InternetAddress(username));
        message.setSubject(mailEntity.getSubject());

        //设置接收邮件用户地址
        if (mailEntity.getMultiTo() != null && mailEntity.getMultiTo().size() > 0) {
            //发送邮件给多用户操作
            InternetAddress[] address = null;
            try {
                address = mailEntity.getMultiTo().toArray(new InternetAddress[mailEntity.getMultiTo().size()]);
                message.addRecipients(Message.RecipientType.TO, address);

            } catch (AddressException e) {
                MailHelper.logger.debug("mailInit parse multi email exception: ", e);
            }
        } else {
            //邮件发给单用户操作
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(mailEntity.getTo()));
        }
        return message;
    }


    /**
     * 设置attachment邮件的multipart部分
     *
     * @return
     * @throws Exception
     */
    private Multipart attachmentEmail() throws Exception {
        //发送attachment邮件
        Multipart multipart = new MimeMultipart();

        //设置text或html的body体
        BodyPart textAndContentPart = new MimeBodyPart();
        // Fill the message
        if (mailEntity.getType().equals("text")) {
            //发送text类型邮件
            textAndContentPart.setText(mailEntity.getTextAndContent());

        } else if (mailEntity.getType().equals("html")) {
            //发送html类型邮件
            textAndContentPart.setContent(mailEntity.getTextAndContent(), "text/html");

        } else {
            throw new Exception("attachmentEmail Get type exception" + mailEntity.getType());
        }

        // attachment部分
        BodyPart messageBodyPart = new MimeBodyPart();
        String filename = mailEntity.getAttachment();
        DataSource source = new FileDataSource(filename);
        messageBodyPart.setDataHandler(new DataHandler(source));
        messageBodyPart.setFileName(filename);

        // 添加邮件body体
        multipart.addBodyPart(textAndContentPart);
        // 添加邮件福建体
        multipart.addBodyPart(messageBodyPart);

        return multipart;
    }


    /**
     * 获取html的数据的方法
     */
    public static String getHtmlData(String url, boolean isLocalFile) {
        String html = "";
        try {
            //获取本地的html的file或网络html的file
            if (isLocalFile) {
                //从本地获取html文件
                html = Jsoup.parse(new File(url), "UTF-8").html();

            } else {
                //从网络获取html文件
                html = Jsoup.connect(url).get().html();
            }

        } catch (Exception e) {
            MailHelper.logger.error("getHtmlData error: ", e);
        }
        return html;
    }


    /**
     * 发送邮件方法
     */
    public void send() {
        try {
            MimeMessage message = mailInit();
            if (mailEntity.getAttachment() != null && !mailEntity.getAttachment().isEmpty()) {
                Multipart multipart = attachmentEmail();
                //设置multipart的body体
                message.setContent(multipart);

            } else {
                //发送普通的text或html的邮件
                if (mailEntity.getType().equals("text")) {
                    //发送text类型邮件
                    message.setText(mailEntity.getTextAndContent());

                } else if (mailEntity.getType().equals("html")) {
                    //发送html类型邮件
                    message.setContent(mailEntity.getTextAndContent(), "text/html; charset=UTF-8");

                } else {
                    throw new Exception("Get type exception" + mailEntity.getType());
                }
            }
            //发送message
            Transport.send(message);

        } catch (Exception e) {
            MailHelper.logger.error("send error: ", e);
        }
    }
}
