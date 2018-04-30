package viewcoder.helper.msg;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import viewcoder.helper.msg.entity.ExpireNotifyMsg;
import viewcoder.helper.msg.entity.VerifyCodeMsg;
import viewcoder.tool.xml.XmlUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/4/28.
 */
public class SendMsg {

    /**
     * 发短信提醒过期续费
     *
     * @param list
     */
    public void sendExpireNotify(List<ExpireNotifyMsg> list) {
        HttpClient client = new HttpClient();
        PostMethod post = new PostMethod("http://sms.webchinese.cn/web_api/");
        post.addRequestHeader("Content-Type",
                "application/x-www-form-urlencoded;charset=gbk");// 在头文件中设置转码

        //循环遍历所有需要发送告知实例过期的user, 并提醒续费操作
        for (ExpireNotifyMsg msg :
                list) {
            //准备替换短信正文的数据
            Map<String, String> replaceData = new HashMap<String, String>();
            replaceData.put("name", msg.getName());
            replaceData.put("renewUrl", msg.getRenewUrl()); //点击进入续费的url
            String text = XmlUtil.getGeneralMsg("instance_expire_msg");
            //发送短信请求操作
            MsgHelper.sendMsg(client, post, text, replaceData, msg.getTo());
        }
        post.releaseConnection();
    }


    /**
     * 发送短信验证码
     */
    public void sendVerifyCode(VerifyCodeMsg msg) {
        HttpClient client = new HttpClient();
        PostMethod post = new PostMethod("http://sms.webchinese.cn/web_api/");
        post.addRequestHeader("Content-Type",
                "application/x-www-form-urlencoded;charset=gbk");// 在头文件中设置转码

        //准备替换短信正文的数据
        Map<String, String> replaceData = new HashMap<String, String>();
        replaceData.put("name", msg.getName());
        replaceData.put("verifyCode", msg.getCode()); //点击进入续费的url
        String text = XmlUtil.getGeneralMsg("verification_code");

        //发送短信请求操作
        MsgHelper.sendMsg(client, post, text, replaceData, msg.getTo());
        post.releaseConnection();
    }

}
