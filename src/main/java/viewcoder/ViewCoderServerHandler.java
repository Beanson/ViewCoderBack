package viewcoder;

/**
 * Created by Administrator on 2017/2/8.
 */

import viewcoder.operation.entity.response.StatusCode;
import viewcoder.operation.impl.overall.QuickLoginVerify;
import viewcoder.tool.common.Common;
import viewcoder.operation.entity.response.ResponseData;
import viewcoder.operation.impl.common.CommonService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import org.apache.log4j.Logger;

import java.util.Iterator;
import java.util.Map;

import static org.bouncycastle.asn1.x500.style.RFC4519Style.l;


public class ViewCoderServerHandler extends SimpleChannelInboundHandler<Object> {
    private static Logger logger = Logger.getLogger(ViewCoderServerHandler.class);

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg != null) {
            if (msg instanceof HttpRequest) {
                HttpRequest request = (HttpRequest) msg;

                //OPTIONS类型的方法，直接返回
                if (request.method() == HttpMethod.OPTIONS) {
                    ViewCoderServerHandler.logger.debug("Come To OPTIONS Method");
                    ViewCoderAccess.httpResponse(ctx, msg, new ResponseData(StatusCode.OK.getValue()));
                    return;
                }

                //暴露第三方回调的url链接无需cross域验证
                if(!ViewCoderAccess.nonCrossVerify(request, msg, ctx)){
                    //异源请求filter验证
                    if(!QuickLoginVerify.validateCrossReq(request)){
                        ViewCoderServerHandler.logger.debug("Come from invalid cross origin");
                        ViewCoderAccess.httpResponse(ctx, msg, new ResponseData(StatusCode.ERROR.getValue()));
                        return;
                    }

                    //无需获取登录状态才能访问的链接请求
                    if (!ViewCoderAccess.nonLoginAccess(request, msg, ctx)) {
                        //需要获取登录状态才能访问的链接请求，防止重复登录可操作的请求
                        if (QuickLoginVerify.checkLoginSession(request) == Common.RELOGIN_ALERT) {
                            ResponseData responseData = new ResponseData(StatusCode.ERROR.getValue());
                            responseData.setVerify_code(StatusCode.RELOGIN_ALERT.getValue());
                            ViewCoderAccess.httpResponse(ctx, msg, responseData);

                        } else {
                            //登录状态才能访问的链接请求
                            CommonService.printHttpInvokeFunction(request.uri());
                            ViewCoderAccess.loginAccess(request, msg, ctx);
                        }
                    }
                }
            }
        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }


//    private void reset() {
//        // destroy the decoder to release all resources
//        request = null;
//        decoder.destroy();
//        decoder = null;
//    }

}
