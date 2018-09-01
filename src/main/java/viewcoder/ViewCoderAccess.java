package viewcoder;

import com.alibaba.fastjson.JSON;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import org.apache.log4j.Logger;
import viewcoder.operation.entity.response.ResponseData;
import viewcoder.operation.impl.company.CompanyOpt;
import viewcoder.operation.impl.logon.Register;
import viewcoder.operation.impl.logon.SignIn;
import viewcoder.operation.impl.overall.Overall;
import viewcoder.operation.impl.personal.Personal;
import viewcoder.operation.impl.project.CreateProject;
import viewcoder.operation.impl.project.ProjectList;
import viewcoder.operation.impl.project.StoreList;
import viewcoder.operation.impl.purchase.AliPay;
import viewcoder.operation.impl.purchase.Purchase;
import viewcoder.operation.impl.purchase.wechat.WechatPay;
import viewcoder.operation.impl.render.Render;
import viewcoder.tool.common.Common;
import viewcoder.tool.encrypt.ECCUtil;
import viewcoder.tool.parser.form.FormData;

import java.text.Normalizer;
import java.util.Map;

import static com.aliyun.oss.internal.OSSHeaders.ACCESS_CONTROL_ALLOW_ORIGIN;
import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * Created by Administrator on 2017/2/8.
 */

public class ViewCoderAccess {

    private static Logger logger = Logger.getLogger(ViewCoderAccess.class);

    /**
     * 暴露第三方回调的url链接无需cross域验证
     * @param request request请求对象
     * @param msg request请求护具
     * @param ctx 返回通道
     * @return
     */
    public static boolean nonCrossVerify(HttpRequest request, Object msg, ChannelHandlerContext ctx) {
        //获取uri数据
        String uri = request.uri();

        //记录并返回消息体是否被消费了
        boolean messagePurchase = true;

        /* **************************************************************/
        /*Purchase专区*/
        //更新order信息到数据库
        if (uri.equals("/aliPayNotify")) {
            String response = AliPay.aliPayNotify(msg);
            httpResponsePureHtml(ctx, msg, response);
        }
        //更新order信息到数据库
        else if (uri.equals("/wechatPayNotify")) {
            String response = WechatPay.weChatPayNotify(msg);
            httpResponsePureHtml(ctx, msg, response);
        }
        else if(uri.equals("/testPure")){
            httpResponsePureHtml(ctx, msg, "123");
        }
        //若尚未消费该事件，则返回false
        else {
            messagePurchase = false;
        }
        return messagePurchase;
    }


    /**
     * 无需获取登录状态才能访问的链接请求
     *
     * @param request request请求对象
     * @param msg request请求护具
     * @param ctx 返回通道
     */
    public static boolean nonLoginAccess(HttpRequest request, Object msg, ChannelHandlerContext ctx) {
        //获取uri数据
        String uri = request.uri();

        //记录并返回消息体是否被消费了
        boolean messagePurchase = true;
         /* **************************************************************/
         /*Login专区*/
         /*注册操作---------------------------*/
        //新用户注册请求
        if (uri.equals("/viewCoderRegister")) {
            ResponseData response = Register.ViewCoderRegister(msg);
            httpResponse(ctx, msg, response);
        }
        //注册成功后扫码绑定手机操作
        else if (uri.equals("/updateWeChatInfoToUser")) {
            ResponseData response = Register.updateWeChatInfoToUser(msg);
            httpResponse(ctx, msg, response);
        }
        //获取注册验证码
        if (uri.equals("/getRegisterVerifyCode")) {
            ResponseData response = Register.getRegisterVerifyCode(msg);
            httpResponse(ctx, msg, response);
        }

        /*登录操作----------------------------*/
        //用户登录请求
        else if (uri.equals("/viewCoderLogin")) {
            ResponseData response = SignIn.ViewCoderLogin(msg);
            httpResponse(ctx, msg, response);
        }
        //获取手机验证码信息
        else if (uri.equals("/getSignVerifyCode")) {
            ResponseData response = SignIn.getSignVerifyCode(msg);
            httpResponse(ctx, msg, response);
        }
        //验证登录验证码是否一致
        else if (uri.equals("/signInVerifyCodeCheck")) {
            ResponseData response = SignIn.signInVerifyCodeCheck(msg);
            httpResponse(ctx, msg, response);
        }
        //用户直接手机微信登录
        else if (uri.equals("/weChatUserLogin")) {
            ResponseData response = SignIn.weChatUserLogin(msg);
            httpResponse(ctx, msg, response);
        }

        /* **************************************************************/
         /*Overall专区*/
        //客户发送反馈消息
        else if (uri.equals("/sendSuggestion")) {
            ResponseData response = Overall.sendSuggestion(msg);
            httpResponse(ctx, msg, response);
        }

        //若尚未消费该事件，则返回false
        else {
            messagePurchase = false;
        }
        return messagePurchase;
    }


    /**
     * 需要用户已登录状态才可访问的请求
     *
     * @param request uri请求地址
     * @param msg request请求护具
     * @param ctx 返回通道
     */
    public static void loginAccess(HttpRequest request, Object msg, ChannelHandlerContext ctx) {
        //获取uri数据
        String uri = request.uri();

        /* **************************************************************/
        /* Overall专区*/
        //根据用户id号来获取个人信息
        if (uri.equals("/getUserInfoByIdAndSessionId")) {
            ResponseData response = Overall.getUserInfoByIdAndSessionId(msg);
            httpResponse(ctx, msg, response);
        }
        //退出登录时操作
        else if (uri.equals("/logoutUserAccount")) {
            ResponseData response = Overall.logoutUserAccount(msg);
            httpResponse(ctx, msg, response);
        }

        /* **************************************************************/
        /*Personal专区**************************/
        //更新用户资料信息
        else if (uri.equals("/updateUserInfo")) {
            ResponseData response = Personal.updateUserInfo(msg);
            httpResponse(ctx, msg, response);
        }

        /* **************************************************************/
        /*Purchase专区*/
        //计算扩容或续期的价格
        else if (uri.equals("/refreshInstance")) {
            ResponseData response = Purchase.refreshInstance(msg);
            httpResponse(ctx, msg, response);
        }
        //计算扩容或续期的价格
        else if (uri.equals("/calculateExtendPrice")) {
            ResponseData response = Purchase.calculateExtendPrice(msg);
            httpResponse(ctx, msg, response);
        }

        //查询所有订单内容
        else if (uri.equals("/getOrderList")) {
            ResponseData response = Purchase.getOrderList(msg);
            httpResponse(ctx, msg, response);
        }
        //查询相应条件的order信息
        else if (uri.equals("/getTargetOrderList")) {
            ResponseData response = Purchase.getTargetOrderList(msg);
            httpResponse(ctx, msg, response);
        }
        //插入新的order信息到数据库
        else if (uri.equals("/insertNewOrderItem")) {
            ResponseData response = Purchase.insertNewOrderItem(msg);
            if (response.getStatus_code() == Common.STATUS_CODE_OK) {
                if (response.getMark() == 1) {
                    //支付宝类型返回数据
                    httpResponsePureHtml(ctx, msg, response.getData().toString());

                } else if (response.getMark() == 2) {
                    //微信支付类型返回数据
                    httpResponse(ctx, msg, response);

                } else if (response.getMark() == 3) {
                    //积分兑换套餐返回数据
                    httpResponse(ctx, msg, response);
                }
            } else {
                httpResponse(ctx, msg, response);
            }
        }

        //下载支付哈希凭证信息
        //Deprecated
        else if (uri.equals("/getPayInfo")) {
            ResponseData response = Purchase.getPayInfo(msg);
            httpResponse(ctx, msg, response);
        }
        //验证支付哈希凭证信息
        //Deprecated
        else if (uri.equals("/verifyCert")) {
            ResponseData response = ECCUtil.verifyCert(msg);
            httpResponse(ctx, msg, response);
        }

        //删除order数据表中条目
        else if (uri.equals("/deleteOrderItem")) {
            ResponseData response = Purchase.deleteOrderItem(msg);
            httpResponse(ctx, msg, response);
        }
        //获取用户最新的积分数据
        else if (uri.equals("/getTotalPoints")) {
            ResponseData response = Purchase.getTotalPoints(msg);
            httpResponse(ctx, msg, response);
        }

        /* **************************************************************/
        /*Project 专区*/
        /*Project Store页面*/
        //根据用户id获取用户所有project所有数据
        else if (uri.equals("/getProjectListData")) {
            ResponseData response = ProjectList.getProjectListData(msg);
            httpResponse(ctx, msg, response);
        }
        //拷贝复制项目
        else if (uri.equals("/copyProject")) {
            ResponseData response = ProjectList.copyProject(msg);
            httpResponse(ctx, msg, response);
        }
        //更改项目名称
        else if (uri.equals("/modifyProjectName")) {
            ResponseData response = ProjectList.modifyProjectName(msg);
            httpResponse(ctx, msg, response);
        }
        //删除项目
        else if (uri.equals("/deleteProject")) {
            ResponseData response = ProjectList.deleteProject(msg);
            httpResponse(ctx, msg, response);
        }
        //更新项目开放程度
        else if (uri.equals("/updateProjectOpenness")) {
            ResponseData response = StoreList.updateProjectOpenness(msg);
            httpResponse(ctx, msg, response);
        }
        //获取指定类型的project store的project数据
        else if (uri.equals("/getTargetStoreWebModel")) {
            ResponseData response = StoreList.getTargetStoreWebModel(msg);
            httpResponse(ctx, msg, response);
        }
        //更新最新用户在project_store页面选择的industry类型到数据库
        else if (uri.equals("/updateLastSelectedIndustry")) {
            ResponseData response = StoreList.updateLastSelectedIndustry(msg);
            httpResponse(ctx, msg, response);
        }

        /*Create Project页面*/
        //新建空的project项目
        else if (uri.equals("/createEmptyProject")) {
            ResponseData response = CreateProject.createEmptyProject(msg);
            httpResponse(ctx, msg, response);
        }
        //创建商城项目
        else if (uri.equals("/createStoreProject")) {
            ResponseData response = CreateProject.createStoreProject(msg);
            httpResponse(ctx, msg, response);
        }
        //新建PSD项目
        else if (uri.equals("/createPSDProject")) {
            ResponseData response = CreateProject.createPSDProject(msg);
            httpResponse(ctx, msg, response);
        }
        //创建URL项目（根据URL创建一个类似的网页）
        else if (uri.equals("/createSimulateProject")) {
            ResponseData response = CreateProject.createSimulateProject(msg);
            httpResponse(ctx, msg, response); //成功接收到请求，正常返回
        }
        //获取Simulate项目渲染进度页面
        else if (uri.equals("/getProjectRate")) {
            ResponseData responseData = CreateProject.getProjectRate(msg);
            httpResponse(ctx, msg, responseData); //成功接收到请求，正常返回
        }
        //通过timestamp获取project数据，用在PSD和URL项目中
        else if (uri.equals("/getProjectByTimestamp")) {
            ResponseData responseData = CreateProject.getProjectByTimestamp(msg);
            httpResponse(ctx, msg, responseData); //成功接收到请求，正常返回
        }

        /* **************************************************************/
        /*Render专区*/
        //根据项目id获取项目名称数据，用于子项目跳转访问前使用
        else if (uri.equals("/getProjectName")) {
            ResponseData response = Render.getProjectName(msg);
            httpResponse(ctx, msg, response);
        }
        //根据project的id获取该project的渲染信息
        else if (uri.equals("/getProjectRenderData")) {
            ResponseData response = Render.getProjectRenderData(msg);
            httpResponse(ctx, msg, response);
        }
        //查看客户上传了的各种resource文件
        else if (uri.equals("/getUploadedResource")) {
            ResponseData response = Render.getUploadResource(msg);
            httpResponse(ctx, msg, response);
        }
        //查看客户账号是否有足够空间上传该文件信息
        else if (uri.equals("/uploadSpaceDetect")) {
            ResponseData response = Render.uploadSpaceDetect(msg);
            httpResponse(ctx, msg, response);
        }
        //上传resource资源文件
        else if (uri.equals("/uploadResource")) {
            ResponseData response = Render.uploadResource(msg);
            httpResponse(ctx, msg, response);
        }
        //上传的视频截图文件
        else if (uri.equals("/uploadVideoImage")) {
            ResponseData response = Render.uploadVideoImage(msg);
            httpResponse(ctx, msg, response);
        }
        //删除对应id的resource
        else if (uri.equals("/deleteResource")) {
            ResponseData response = Render.deleteResource(msg);
            httpResponse(ctx, msg, response);
        }
        //重命名对应id的resource
        else if (uri.equals("/renameResource")) {
            ResponseData response = Render.renameResource(msg);
            httpResponse(ctx, msg, response);
        }
        //保存项目数据
        else if (uri.equals("/saveProjectData")) {
            ResponseData response = Render.saveProjectData(msg);
            httpResponse(ctx, msg, response);
        }
        //网页前端默认的导出设置
        else if (uri.equals("/updateExportDefaultSetting")) {
            ResponseData response = Render.updateExportDefaultSetting(msg);
            httpResponse(ctx, msg, response);
        }

        /* **************************************************************/
        /*Company专区*/
        //企业获取订单折扣条目操作
        else if (uri.equals("/getCompanyDiscountOrder")) {
            ResponseData response = CompanyOpt.getCompanyDiscountOrder(msg);
            httpResponse(ctx, msg, response);
        }

        /* **************************************************************/
        /*测试专区*/
        else if (uri.equals("/netty")) {
            Map<String, Object> map = FormData.getParam(msg);
            ViewCoderAccess.logger.debug("come to netty test link");
            httpResponse(ctx, msg, JSON.toJSONString("hello world"));

        } else {
            String message = "server do not serve such request: " + uri;
            httpResponse(ctx, msg, message);
            ViewCoderAccess.logger.debug(message);
        }
    }


    /**
     * 返回http请求相关消息
     *
     * @param ctx 通信通道
     * @param msg 请求的引用
     */
    public static void httpResponse(ChannelHandlerContext ctx, Object msg, Object dataBack) {

        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(JSON.toJSONString(dataBack).getBytes()));
        response.headers().set(CONTENT_TYPE, Common.RETURN_JSON);
        commonResponse(ctx, msg, response);
        ViewCoderAccess.logger.debug("Return Response Data: \n" + dataBack.toString());
    }


    /**
     * 返回http请求相关消息
     *
     * @param ctx 通信通道
     * @param msg 请求的引用
     */
    public static void httpResponsePureHtml(ChannelHandlerContext ctx, Object msg, String htmlData) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(htmlData.getBytes()));
        response.headers().set(CONTENT_TYPE, Common.RETURN_TEXT_HTML);
        commonResponse(ctx, msg, response);
        ViewCoderAccess.logger.debug("Return html pure data response");
    }


    /**
     * 返回json数据和HTML数据相同的消息体
     *
     * @param ctx      通信通道
     * @param msg      请求数据
     * @param response 请求返回消息封装体
     */
    private static void commonResponse(ChannelHandlerContext ctx, Object msg, FullHttpResponse response) {
        if (HttpUtil.is100ContinueExpected((HttpMessage) msg)) {
            ctx.write(new DefaultFullHttpResponse(HTTP_1_1, CONTINUE));
        }
        boolean keepAlive = HttpUtil.isKeepAlive((HttpMessage) msg);
        response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
        response.headers().set(ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        response.headers().set(ACCESS_CONTROL_ALLOW_METHODS, "POST");
        response.headers().set(ACCESS_CONTROL_ALLOW_HEADERS, "user_id, session_id, *");
        response.headers().set(ACCEPT, "*");
        if (!keepAlive) {
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        } else {
            ctx.writeAndFlush(response);
        }
    }

}