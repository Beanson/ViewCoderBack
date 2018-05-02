package viewcoder.helper.mail;

import org.apache.commons.lang3.text.StrSubstitutor;
import org.jsoup.Jsoup;
import viewcoder.tool.common.Common;
import viewcoder.tool.config.GlobalConfig;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Created by Administrator on 2018/4/28.
 */
public class MailEntity {

    private String host;
    private String from;
    private String to;
    private List<InternetAddress> multiTo;
    private String pass;
    private String protocol;
    private String auth;
    private String timeout;
    private String subject;
    private String type; //text, html
    private String textAndContent; //设置纯text或html数据
    private String attachment; //设置发送的附件文件名

    public MailEntity() {
    }

    public MailEntity(String to, String subject, String type) {
        this.to = to;
        this.subject = subject;
        this.type = type;
    }

    //发送单用户邮件
    public MailEntity(String to, String subject, String type, String textAndContent, String attachment) {
        this.to = to;
        this.subject = subject;
        this.type = type;
        this.textAndContent = textAndContent;
        this.attachment = attachment;
    }

    //发送多用户邮件
    public MailEntity(List<InternetAddress> multiTo, String subject, String type, String textAndContent, String attachment) {
        this.multiTo = multiTo;
        this.subject = subject;
        this.type = type;
        this.textAndContent = textAndContent;
        this.attachment = attachment;
    }

    public String getTo() {
        return to;
    }

    public MailEntity setTo(String to) {
        this.to = to;
        return this;
    }

    public String getFrom() {
        return from;
    }

    public MailEntity setFrom(String from) {
        this.from = from;
        return this;
    }

    public String getHost() {
        return host;
    }

    public MailEntity setHost(String host) {
        this.host = host;
        return this;
    }

    public String getSubject() {
        return subject;
    }

    public MailEntity setSubject(String subject) {
        this.subject = subject;
        return this;
    }

    public String getType() {
        return type;
    }

    public MailEntity setType(String type) {
        this.type = type;
        return this;
    }

    public String getTextAndContent() {
        return textAndContent;
    }

    public MailEntity setTextAndContent(String textAndContent) {
        this.textAndContent = textAndContent;
        return this;
    }

    public List<InternetAddress> getMultiTo() {
        return multiTo;
    }

    public void setMultiTo(List<InternetAddress> multiTo) {
        this.multiTo = multiTo;
    }

    public String getAttachment() {
        return attachment;
    }

    public MailEntity setAttachment(String attachment) {
        this.attachment = attachment;
        return this;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getAuth() {
        return auth;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }

    public String getTimeout() {
        return timeout;
    }

    public void setTimeout(String timeout) {
        this.timeout = timeout;
    }


    @Override
    public String toString() {
        return "MailEntity{" +
                "host='" + host + '\'' +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", multiTo=" + multiTo +
                ", pass='" + pass + '\'' +
                ", protocol='" + protocol + '\'' +
                ", auth='" + auth + '\'' +
                ", timeout='" + timeout + '\'' +
                ", subject='" + subject + '\'' +
                ", type='" + type + '\'' +
                ", textAndContent='" + textAndContent + '\'' +
                ", attachment='" + attachment + '\'' +
                '}';
    }
}
