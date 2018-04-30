package viewcoder.helper.mail;

import org.jsoup.Jsoup;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.util.Properties;

/**
 * Created by Administrator on 2018/4/28.
 */
public class MailHelper {


    private String to;
    private String from;
    private String host;
    private String subject;
    private String type; //text, html
    private String textAndContent; //设置纯text或html数据
    private String attachment; //设置发送的附件文件名

    public MailHelper(String type) {
        this.type = type;
    }

    public String getTo() {
        return to;
    }

    public MailHelper setTo(String to) {
        this.to = to;
        return this;
    }

    public String getFrom() {
        return from;
    }

    public MailHelper setFrom(String from) {
        this.from = from;
        return this;
    }

    public String getHost() {
        return host;
    }

    public MailHelper setHost(String host) {
        this.host = host;
        return this;
    }

    public String getSubject() {
        return subject;
    }

    public MailHelper setSubject(String subject) {
        this.subject = subject;
        return this;
    }

    public String getType() {
        return type;
    }

    public MailHelper setType(String type) {
        this.type = type;
        return this;
    }

    public String getTextAndContent() {
        return textAndContent;
    }

    public MailHelper setTextAndContent(String textAndContent) {
        this.textAndContent = textAndContent;
        return this;
    }

    public String getAttachment() {
        return attachment;
    }

    public MailHelper setAttachment(String attachment) {
        this.attachment = attachment;
        return this;
    }


    /**
     * 初始化mail的基础数据
     */
    private MimeMessage mailInit() throws Exception {
        // Get system properties
        Properties properties = System.getProperties();
        // Setup mail server
        properties.setProperty("mail.smtp.host", host);
        // Get the default Session object.
        Session session = Session.getDefaultInstance(properties);
        // Create a default MimeMessage object.
        MimeMessage message = new MimeMessage(session);
        // Set From: header field of the header.
        message.setFrom(new InternetAddress(from));
        // Set To: header field of the header.
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
        // Set Subject: header field
        message.setSubject(subject);

        return message;
    }

    /**
     * 发送邮件方法
     */
    public void send() {
        try {
            MimeMessage message = mailInit();

            if (attachment != null && !attachment.isEmpty()) {

                Multipart multipart = attachmentEmail();
                //设置multipart的body体
                message.setContent(multipart);

            } else {
                //发送普通的text或html的邮件
                if (type.equals("text")) {
                    //发送text类型邮件
                    message.setText(textAndContent);

                } else if (type.equals("html")) {
                    //发送html类型邮件
                    message.setContent(textAndContent, "text/html");

                } else {
                    throw new Exception("Get type exception" + type);
                }
            }

            //发送message
            Transport.send(message);

        } catch (Exception e) {

        }
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
        if (type.equals("text")) {
            //发送text类型邮件
            textAndContentPart.setText(textAndContent);

        } else if (type.equals("html")) {
            //发送html类型邮件
            textAndContentPart.setContent(textAndContent, "text/html");

        } else {
            throw new Exception("Get type exception" + type);
        }

        // attachment部分
        BodyPart messageBodyPart = new MimeBodyPart();
        String filename = attachment;
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
            // print log with logger TODO
            e.printStackTrace();
        }
        return html;
    }


    @Override
    public String toString() {
        return "MailHelper{" +
                "to='" + to + '\'' +
                ", from='" + from + '\'' +
                ", host='" + host + '\'' +
                ", type='" + type + '\'' +
                ", attachment='" + attachment + '\'' +
                '}';
    }

}
