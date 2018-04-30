package viewcoder.helper.msg;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang3.text.StrSubstitutor;


import java.util.Map;

/**
 * Created by Administrator on 2018/4/28.
 */
public class MsgHelper {

    /**
     * 请求中国网建调用发送短信服务
     * @param client http client客户端
     * @param post http post请求
     * @param text 模板消息
     * @param replaceData 需更换模板消息的占位符数据
     * @param msgTo 短信发送达的手机
     */
    public static void sendMsg(HttpClient client, PostMethod post, String text, Map<String, String> replaceData,
                               String msgTo){
        try{
            //进行占位符的replace拼装
            String content="";
            if (text != null && !text.isEmpty()) {
                content = StrSubstitutor.replace(text, replaceData);
            }
            //请求中国网建发送短信的请求数据
            NameValuePair[] data = {
                    new NameValuePair("Uid", "s******n"), // 注册的用户名
                    new NameValuePair("Key", "0753************"), // 注册成功后,网站分配的密钥（不是密码）
                    new NameValuePair("smsMob", msgTo), // 给该手机号码发送内容
                    new NameValuePair("smsText", content)
            };
            //发送短信请求到中国网建
            post.setRequestBody(data);
            client.executeMethod(post); //发短信的请求操作

            //同步发送接口返回数据
            if(!MsgHelper.verifyResponse(post)){
                //对发送失败的条目进行处理
            }
        }catch (Exception e){

        }
    }


    /**
     * 验证response实体是否为发送成功实体,发送成功返回true, 否则返回false
     * @param post
     * @return
     */
    public static boolean verifyResponse(PostMethod post){
        boolean responseStatus=false;
        try {
            if(post!=null){
                String response = new String(post.getResponseBodyAsString().getBytes("gbk"));
                //Header[] headers = post.getResponseHeaders();  //如果需要进行header其他确认则往下走
                if(post.getStatusCode()==200 && response.equals("***")){
                    responseStatus=true;
                }
            }
        }catch (Exception e){
            // TODO print logs
        }
        return responseStatus;
    }

}
