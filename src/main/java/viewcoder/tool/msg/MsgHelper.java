package viewcoder.tool.msg;

import com.alibaba.fastjson.JSON;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendBatchSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendBatchSmsResponse;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import org.apache.log4j.Logger;
import viewcoder.tool.common.Common;
import viewcoder.tool.config.GlobalConfig;


import java.util.Map;


/**
 * Created by Administrator on 2018/4/28.
 */
public class MsgHelper {

    private static Logger logger = Logger.getLogger(MsgHelper.class);
    private static final String product = GlobalConfig.getProperties(Common.MSG_PRODUCT);//短信API产品名称（短信产品名固定，无需修改）
    private static final String domain = GlobalConfig.getProperties(Common.MSG_DOMAIN);//短信API产品域名（接口地址固定，无需修改）
    private static final String endPointName = GlobalConfig.getProperties(Common.MSG_ENDPOINT);
    private static final String regionId = GlobalConfig.getProperties(Common.MSG_REGIONID);
    private static final String defaultConnectTimeout="sun.net.client.defaultConnectTimeout";
    private static final String defaultReadTimeout="sun.net.client.defaultReadTimeout";

    /**
     * 初始化调用阿里云发送短信业务的句柄
     * @return 返回调用类的句柄
     * @throws Exception
     */
    public static IAcsClient initMsg() throws Exception{
        //可自助调整超时时间
        System.setProperty(defaultConnectTimeout, Common.TIMEOUT_10000);
        System.setProperty(defaultReadTimeout, Common.TIMEOUT_10000);

        //初始化ascClient,暂时不支持多region（请勿修改）
        IClientProfile profile = DefaultProfile.getProfile(regionId, Common.ALI_ACCESSKEY_ID, Common.ALI_ACCESSKEY_SECRET);
        DefaultProfile.addEndpoint(endPointName, regionId, product, domain);
        return new DefaultAcsClient(profile);
    }


    /**
     * 请求阿里云短息业务进行发送短信
     * @param msgTemplate   消息模板id
     * @param replaceData   需更换模板消息的占位符数据
     * @param msgTo         短信发送达的手机
     * @param signName      消息签名
     */
    public static void sendSingleMsg(String msgTemplate, Map<String, String> replaceData, String msgTo, String signName) {

        try {
            //初始化发送句柄对象
            IAcsClient acsClient = initMsg();
            //组装请求对象-具体描述见控制台-文档部分内容
            SendSmsRequest request = new SendSmsRequest();
            //必填:待发送手机号
            request.setPhoneNumbers(msgTo);
            //必填:短信签名-可在短信控制台中找到
            request.setSignName(signName);
            //必填:短信模板-可在短信控制台中找到
            request.setTemplateCode(msgTemplate);
            //可选:模板中的变量替换JSON串,如模板内容为"亲爱的${name},您的验证码为${code}"时,此处的值为
            request.setTemplateParam(JSON.toJSONString(replaceData));

            //hint 此处可能会抛出异常，注意catch
            SendSmsResponse sendSmsResponse = acsClient.getAcsResponse(request);
            if (sendSmsResponse.getCode() != null && sendSmsResponse.getCode().equals(Common.STATUS_OK)) {
                //请求成功
                MsgHelper.logger.debug("sendSingleMsg with success: msgTo:"+ msgTo+", signName:"+signName+", msgTemplate:"+msgTemplate);
            }
        }catch (Exception e){
            MsgHelper.logger.error("sendSingleMsg with error: ",e);
        }

    }


    /**
     * 请求阿里云短息业务进行发送短信
     * @param msgTemplate   消息模板id
     * @param replaceData   需更换模板消息的占位符数据
     * @param msgTos        短信发送达的手机号
     * @param signName      消息签名
     */
    public static void sendBatchMsg(String msgTemplate, Map<String, String> replaceData, String msgTos, String signName) {

        try{
            IAcsClient acsClient = initMsg();
            //组装请求对象
            SendBatchSmsRequest request = new SendBatchSmsRequest();
            //使用post提交
            request.setMethod(MethodType.POST);
            //必填:待发送手机号。支持JSON格式的批量调用，批量上限为100个手机号码,批量调用相对于单条调用及时性稍有延迟,验证码类型的短信推荐使用单条调用的方式
            request.setPhoneNumberJson(msgTos);
            //必填:短信签名-支持不同的号码发送不同的短信签名
            request.setSignNameJson(signName);
            //必填:短信模板-可在短信控制台中找到
            request.setTemplateCode(msgTemplate);
            //必填:模板中的变量替换JSON串,如模板内容为"亲爱的${name},您的验证码为${code}"时,此处的值为
            //友情提示:如果JSON中需要带换行符,请参照标准的JSON协议对换行符的要求,比如短信内容中包含\r\n的情况在JSON中需要表示成\\r\\n,否则会导致JSON在服务端解析失败
            request.setTemplateParamJson(JSON.toJSONString(replaceData));

            //请求失败这里会抛ClientException异常
            SendBatchSmsResponse sendSmsResponse = acsClient.getAcsResponse(request);
            if (sendSmsResponse.getCode() != null && sendSmsResponse.getCode().equals(Common.STATUS_OK)) {
                //请求成功
                MsgHelper.logger.debug("sendSingleMsg with success: msgTo:"+ msgTos+", signName:"+signName+", msgTemplate:"+msgTemplate);
            }
        }catch (Exception e){
            MsgHelper.logger.error("sendSingleMsg with error: ",e);
        }
    }

}
