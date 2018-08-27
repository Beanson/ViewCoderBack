package viewcoder.operation.impl.overall;

import io.netty.handler.codec.http.HttpRequest;
import org.apache.log4j.Logger;
import viewcoder.operation.impl.common.CommonService;
import viewcoder.tool.common.CommonObject;

import java.util.Objects;

/**
 * Created by Administrator on 2018/8/19.
 */
public class QuickLoginVerify {

    private static Logger logger = Logger.getLogger(QuickLoginVerify.class);

    public static int checkLoginSession(HttpRequest request) {
        int statusCode = 0; //初始化登录验证状态码
        String userId = request.headers().get("user_id");
        String sessionId = request.headers().get("session_id");
        QuickLoginVerify.logger.debug("userId :" + userId + " sessionId: " + sessionId);

        if (CommonService.checkNotNull(userId) && CommonService.checkNotNull(sessionId)) {
            String targetSessionId = CommonObject.getLoginVerify().get(Integer.parseInt(userId));
            QuickLoginVerify.logger.debug( "targetSessionId: " + targetSessionId);

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
}
