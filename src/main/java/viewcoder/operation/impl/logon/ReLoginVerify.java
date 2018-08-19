package viewcoder.operation.impl.logon;

import io.netty.handler.codec.http.HttpRequest;
import org.apache.log4j.Logger;
import viewcoder.operation.impl.common.CommonService;
import viewcoder.tool.common.CommonObject;
import java.util.Objects;

/**
 * Created by Administrator on 2018/8/19.
 */
public class ReLoginVerify {

    private static Logger logger = Logger.getLogger(ReLoginVerify.class);

    public static int checkLoginSession(HttpRequest request) {
        int statusCode = 0; //初始化登录验证状态码
        String userId = request.headers().get("user_id");
        String sessionId = request.headers().get("session_id");
        String targetSessionId = CommonObject.getLoginVerify().get(Integer.parseInt(userId));
        ReLoginVerify.logger.debug("userId :"+ userId+" sessionId: "+sessionId+", targetSessionId:"+targetSessionId);

        if(CommonService.checkNotNull(targetSessionId)){
            if(Objects.equals(targetSessionId, sessionId)){
                statusCode =1; //两者sessionId相同，认为同一个session
            }else {
                statusCode=-1; //两者sessionId不同，认为不同session
                ReLoginVerify.logger.debug("reLogin suspect capture");
            }
        }else {
            if(CommonService.checkNotNull(userId) && CommonService.checkNotNull(sessionId)){
                CommonObject.getLoginVerify().put(Integer.parseInt(userId), sessionId);
                statusCode = 1;
                ReLoginVerify.logger.debug("reAssign capture");
            }
        }
        return statusCode;
    }
}
