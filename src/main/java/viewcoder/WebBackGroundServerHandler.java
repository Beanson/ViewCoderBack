package viewcoder;

/**
 * Created by Administrator on 2017/2/8.
 */

import com.mysql.fabric.Response;
import viewcoder.tool.common.Common;
import viewcoder.operation.entity.response.ResponseData;
import viewcoder.operation.impl.common.CommonService;
import viewcoder.operation.impl.logon.Logon;
import viewcoder.operation.impl.overall.Overall;
import viewcoder.operation.impl.personal.Personal;
import viewcoder.operation.impl.project.CreateProject;
import viewcoder.operation.impl.project.ProjectList;
import viewcoder.operation.impl.project.StoreList;
import viewcoder.operation.impl.purchase.AliPay;
import viewcoder.operation.impl.purchase.Purchase;
import viewcoder.operation.impl.purchase.wechat.WechatPay;
import viewcoder.operation.impl.render.Render;

import com.alibaba.fastjson.JSON;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import org.apache.log4j.Logger;

import static com.aliyun.oss.internal.OSSHeaders.ACCESS_CONTROL_ALLOW_ORIGIN;
//import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class WebBackGroundServerHandler extends SimpleChannelInboundHandler<Object> {
    /**
     * DefaultHttpDataFactory() usage:
     * HttpData will be in memory if less than default size (16KB).
     * DefaultHttpDataFactory(boolean useDisk)
     * HttpData will be always on Disk if useDisk is True, else always in Memory if False
     * DefaultHttpDataFactory(long minSize)
     * HttpData will be on Disk if the size of the file is greater than minSize, else it will be in memory
     */
    private static final HttpDataFactory factory = new DefaultHttpDataFactory(true); // always save to disk
    private static Logger logger = Logger.getLogger(WebBackGroundServerHandler.class);

    private static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
    private static final String HTTP_DATE_GMT_TIMEZONE = "GMT";
    public static final int HTTP_CACHE_SECONDS = 60;

    private HttpPostRequestDecoder decoder;
    private HttpRequest request;


    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        if (decoder != null) {
            decoder.cleanFiles();
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg != null) {
            if (msg instanceof HttpRequest) {
                HttpRequest request = this.request = (HttpRequest) msg;
                String uri = request.uri();
                CommonService.printHttpInvokeFunction(uri);

                /***************************************************************/
                /* Overall控制方法里*/
                if (uri.equals("/getUserInfoById")) {
                    ResponseData response = Overall.getUserInfoById(msg);
                    httpResponse(ctx, msg, response);
                }

                /*Login控制方法里*/
                /*登录注册方法**************************/
                //新用户注册请求
                if (uri.equals("/viewCoderRegister")) {
                    ResponseData response = Logon.ViewCoderRegister(msg);
                    httpResponse(ctx, msg, response);
                }
                //老用户登录请求
                else if (uri.equals("/viewCoderLogin")) {
                    ResponseData response = Logon.ViewCoderLogin(msg);
                    httpResponse(ctx, msg, response);
                }

                /***************************************************************/
                /*Personal专区**************************/
                //更换用户portrait预览操作
//                else if (uri.equals("/uploadPortrait")) {
//                    System.out.println("Come to uploadPortrait");
//                    ResponseData response = Personal.uploadPortrait(msg);
//                    httpResponse(ctx, msg, response);
//                }
                //更新用户资料信息
                else if (uri.equals("/updateUserInfo")) {
                    ResponseData response = Personal.updateUserInfo(msg);
                    httpResponse(ctx, msg, response);
                }

                /***************************************************************/
                /*Purchase专区**************************/
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
                //更新order信息到数据库
                else if (uri.equals("/aliPayNotify")) {
                    AliPay.aliPayNotify(msg);
                }
                //更新order信息到数据库
                else if (uri.equals("/wechatPayNotify")) {
                    WechatPay.weChatPayNotify(msg);
                }

                //删除order数据表表中条目
                else if (uri.equals("/deleteOrderItem")) {
                    ResponseData response = Purchase.deleteOrderItem(msg);
                    httpResponse(ctx, msg, response);
                }
                //获取用户最新的积分数据
                else if (uri.equals("/getTotalPoints")) {
                    ResponseData response = Purchase.getTotalPoints(msg);
                    httpResponse(ctx, msg, response);
                }

                /***************************************************************/
                /*Project 专区***********************************/
                /*Project Store页面********************/
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

                /*Project Store页面*******************/
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

                /*Create Project页面*******************/
                //新建空的project项目
                else if (uri.equals("/createEmptyProject")) {
                    ResponseData response = CreateProject.createEmptyProject(msg);
                    httpResponse(ctx, msg, response);
                }
                //新建PSD项目
                else if (uri.equals("/createPSDProject")) {
                    ResponseData response = CreateProject.createPSDProject(msg);
                    httpResponse(ctx, msg, response);
                }

                //Create Simulate页面（根据URL创建一个类似的网页）
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
                else if (uri.equals("/getProjectByTimeStamp")) {
                    ResponseData responseData = CreateProject.getProjectByTimeStamp(msg);
                    httpResponse(ctx, msg, responseData); //成功接收到请求，正常返回
                }

                /***************************************************************/
                /*Render专区 *************************/
                /*Table/List 获取数据*/
//                else if (uri.equals("/getTableData")) {
//                    httpResponse(ctx, msg, JSON.toJSONString(getTableData(request)));
//                } else if (uri.equals("/getListData")) {
//                    httpResponse(ctx, msg, JSON.toJSONString(getListData(request)));
//                }

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

                } else if (uri.equals("/testWechatPay")) {
                    Purchase.testWechatPay(msg);

                } else if (uri.equals("/testAliPay")) {
                    httpResponsePureHtml(ctx, msg, Purchase.testAliPay(msg));

                } else if (uri.equals("/netty")) {
                    httpResponse(ctx, msg, JSON.toJSONString("hello world"));

                } else {
                    httpResponse(ctx, msg, "server do not serve such request");
                    System.out.println("----------------------------come to invalid request-----------------------");
                }
            }
        }
    }


//    private void reset() {
//        request = null;
//        // destroy the decoder to release all resources
//        decoder.destroy();
//        decoder = null;
//    }


    /**
     * Sets the content type header for the HTTP Response
     *
     * @param response HTTP response
     * @param file     file to extract content type
     */
//    private static void setContentTypeHeader(HttpResponse response, File file) {
//        MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
//        response.headers().set(CONTENT_TYPE, mimeTypesMap.getContentType(file.getPath()));
//    }

    /**
     * Sets the Date and Cache headers for the HTTP Response
     *
     * @param response
     * HTTP response
     * @param fileToCache
     * file to extract content type
     */
//    private static void setDateAndCacheHeaders(HttpResponse response, File fileToCache) {
//        SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
//        dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));
//
//        // Date header
//        Calendar time = new GregorianCalendar();
//        response.headers().set(DATE, dateFormatter.format(time.getTime()));
//
//        // Add cache headers
//        time.add(Calendar.SECOND, HTTP_CACHE_SECONDS);
//        response.headers().set(EXPIRES, dateFormatter.format(time.getTime()));
//        response.headers().set(CACHE_CONTROL, "private, max-age=" + HTTP_CACHE_SECONDS);
//        response.headers().set(
//                LAST_MODIFIED, dateFormatter.format(new Date(fileToCache.lastModified())));
//    }


/*****************************************************************************************/
    /**
     * send message back to client as responses to http request
     * 返回http请求相关消息
     *
     * @param ctx message channel 通信通道
     * @param msg the request message reference 请求的引用
     */
    public void httpResponse(ChannelHandlerContext ctx, Object msg, Object dataBack) {
        if (HttpUtil.is100ContinueExpected((HttpMessage) msg)) {
            System.out.println("is 100 continue");
            ctx.write(new DefaultFullHttpResponse(HTTP_1_1, CONTINUE));
        }

        WebBackGroundServerHandler.logger.debug("Return Response Data: \n" + dataBack.toString());
        boolean keepAlive = HttpUtil.isKeepAlive((HttpMessage) msg);
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(JSON.toJSONString(dataBack).getBytes()));
        response.headers().set(CONTENT_TYPE, "application/json"); //when set json string should be like json format
        //response.headers().set(CONTENT_TYPE, "text/plain");// by using CONTENT as content
        response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
        response.headers().set(ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        response.headers().set(ACCESS_CONTROL_ALLOW_METHODS, "POST");
        //response.headers().set(ACCESS_CONTROL_ALLOW_HEADERS,"x-requested-with,content-type");
        response.headers().set(ACCESS_CONTROL_ALLOW_HEADERS, "*");
        response.headers().set(ACCEPT, "*");
        if (!keepAlive) {
            System.out.println("not to keep alive");
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        } else {
            ctx.writeAndFlush(response);
        }
    }

    /**
     * send message back to client as responses to http request
     * 返回http请求相关消息
     *
     * @param ctx message channel 通信通道
     * @param msg the request message reference 请求的引用
     */
    public void httpResponsePureHtml(ChannelHandlerContext ctx, Object msg, String htmlData) {
        if (HttpUtil.is100ContinueExpected((HttpMessage) msg)) {
            System.out.println("is 100 continue");
            ctx.write(new DefaultFullHttpResponse(HTTP_1_1, CONTINUE));
        }

        boolean keepAlive = HttpUtil.isKeepAlive((HttpMessage) msg);
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(htmlData.getBytes()));
        response.headers().set(CONTENT_TYPE, "text/html;charset=utf-8"); //when set json string should be like json format
        response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
        response.headers().set(ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        response.headers().set(ACCESS_CONTROL_ALLOW_METHODS, "POST");
        response.headers().set(ACCESS_CONTROL_ALLOW_HEADERS, "*");
        response.headers().set(ACCEPT, "*");
        if (!keepAlive) {
            System.out.println("not to keep alive");
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        } else {
            ctx.writeAndFlush(response);
        }
    }


    //请求table数据示例
//    public String getTableData(HttpRequest httpRequest) {
//        String[] param = DecodeParam.decode(httpRequest);
//        if (Integer.valueOf(param[1]) % 2 == 0) {
//            return "[{\"portrait\":\"/assets/img/render/table_img/boy.png\",\"name\":\"Solo\",\"number\":23535223100,\"grade\":\"一年级\"},{\"portrait\":\"/assets/img/render/table_img/girl.png\",\"name\":\"Yang\",\"number\":231023310,\"grade\":\"三年级\"},{\"portrait\":\"/assets/img/render/table_img/girl.png\",\"name\":\"Judiper\",\"number\":\"2234233100\",\"grade\":\"五年级\"}]";
//        } else {
//            return "[{\"portrait\":\"/assets/img/render/table_img/girl.png\",\"name\":\"Judy\",\"number\":3423523100,\"grade\":\"二年级\"},{\"portrait\":\"/assets/img/render/table_img/boy.png\",\"name\":\"Pobber\",\"number\":231324230,\"grade\":\"四年级\"},{\"portrait\":\"/assets/img/render/table_img/boy.png\",\"name\":\"Ohapa\",\"number\":245453100,\"grade\":\"六年级\"}]";
//        }
//    }
//
//    //请求list数据示例
//    public String getListData(HttpRequest httpRequest) {
//        String[] param = DecodeParam.decode(httpRequest);
//        if (Integer.valueOf(param[1]) % 2 == 0) {
//            return "[{\"product_img\":\"/assets/img/render/list_img/pad.png\",\"price\":\"¥4850.00\",\"product_info\":\"          WowWee智能玩具机器人Mip手势遥控跳舞玩具\"},{\"product_img\":\"/assets/img/render/list_img/pad.png\",\"price\":\"¥4850.00\",\"product_info\":\"          WowWee智能玩具机器人Mip手势遥控跳舞玩具\"},{\"product_img\":\"/assets/img/render/list_img/pad.png\",\"price\":\"¥4850.00\",\"product_info\":\"          WowWee智能玩具机器人Mip手势遥控跳舞玩具\"}]";
//        } else {
//            return "[{\"product_img\":\"/assets/img/render/list_img/power.png\",\"price\":\"¥98.00\",\"product_info\":\"          卡通兔子充电宝苹果手机通用移动电源10000毫安\"},{\"product_img\":\"/assets/img/render/list_img/power.png\",\"price\":\"¥98.00\",\"product_info\":\"          卡通兔子充电宝苹果手机通用移动电源10000毫安\"},{\"product_img\":\"/assets/img/render/list_img/power.png\",\"price\":\"¥98.00\",\"product_info\":\"          卡通兔子充电宝苹果手机通用移动电源10000毫安\"}]";
//        }
//    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

}
