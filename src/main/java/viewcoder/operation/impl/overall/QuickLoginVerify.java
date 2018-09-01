package viewcoder.operation.impl.overall;

import io.netty.handler.codec.http.HttpRequest;
import org.apache.log4j.Logger;
import viewcoder.operation.impl.common.CommonService;
import viewcoder.tool.common.CommonObject;
import viewcoder.tool.config.GlobalConfig;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

/**
 * Created by Administrator on 2018/8/19.
 */
public class QuickLoginVerify {

    private static Logger logger = Logger.getLogger(QuickLoginVerify.class);

    /**
     * 请求域的检测
     *
     * @param request 请求信息
     * @return
     */
    public static boolean validateCrossReq(HttpRequest request) {
        //若不为生产环境则直接返回true，否则进行验证操作
        if(!GlobalConfig.isProdEnv()) return true;

        String origin = request.headers().get("origin");
        String referer = request.headers().get("referer");
        String validateStr = "";
        //获取cross域的值
        if (CommonService.checkNotNull(origin)) {
            validateStr = origin;

        } else if (CommonService.checkNotNull(referer)) {
            validateStr = referer;

        } else {
            //若origin和referer均为空则该请求非法
            return false;
        }

        //遍历每个合法域的值，查看是否有匹配的域
        boolean checkStatus = false;
        for (String cross : CommonObject.getValidCross()) {
            if (Objects.equals(cross, validateStr)) {
                checkStatus = true;
                break;
            }
        }

        //打印非法域信息
        if (!checkStatus) {
            QuickLoginVerify.logger.warn("Invalid cross: " + validateStr);
        }

        return checkStatus;
    }


    /**
     * 监测访问登录权限
     *
     * @param request http请求
     * @return
     */
    public static int checkLoginSession(HttpRequest request) {
        int statusCode = 0; //初始化登录验证状态码
        String userId = request.headers().get("user_id");
        String sessionId = request.headers().get("session_id");
        QuickLoginVerify.logger.debug("userId :" + userId + " sessionId: " + sessionId);

        if (CommonService.checkNotNull(userId) && CommonService.checkNotNull(sessionId)) {
            String targetSessionId = CommonObject.getLoginVerify().get(Integer.parseInt(userId));
            QuickLoginVerify.logger.debug("targetSessionId: " + targetSessionId);

            if (CommonService.checkNotNull(targetSessionId)) {
                if (Objects.equals(targetSessionId, sessionId)) {
                    statusCode = 1; //两者sessionId相同，认为同一个session
                } else {
                    statusCode = -1; //两者sessionId不同，认为不同session
                    QuickLoginVerify.logger.debug("reLogin suspect capture");
                }
            } else {
                //可能服务器重启
                statusCode = -1; //两者sessionId不同，认为不同session
                QuickLoginVerify.logger.debug("server reboot suspect capture");
            }
        }
        return statusCode;
    }


    /**
     * 打印所有header请求数据
     * @param request
     */
//    public static void printAllHeaders(HttpRequest request){
//        Iterator iter = request.headers().iterator();
//        while(iter.hasNext()){
//            Map.Entry<String, String> entry = (Map.Entry<String, String>) iter.next();
//            System.out.println("key= " + entry.getKey() + " and value= " + entry.getValue());
//        }
//    }
}
