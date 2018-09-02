package viewcoder.operation.impl.logon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import viewcoder.tool.cache.GlobalCache;
import viewcoder.tool.common.Common;
import viewcoder.tool.msg.MsgHelper;
import viewcoder.operation.impl.common.CommonService;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2018/2/2.
 */
public class Logon {

    private static Logger logger = LoggerFactory.getLogger(Logon.class);

    /**
     * 生成验证码并发送到手机
     *
     * @param phone 手机号
     */
    public static void generatePhoneVerifyCode(String phone) {
        //获取6为数验证码
        String sixDigits = CommonService.generateSixDigits();
        //发送验证码到用户手机
        Map<String, String> map = new HashMap<>();
        map.put(Common.CODE, sixDigits);
        //发送短信验证码
        MsgHelper.sendSingleMsg(Common.MESG_VERIFY_CODE, map, phone, Common.MSG_SIGNNAME_LIPHIN);
        //验证码存储到cache中
        GlobalCache.getRegisterVerifyCache().put(phone, sixDigits);
    }


}
