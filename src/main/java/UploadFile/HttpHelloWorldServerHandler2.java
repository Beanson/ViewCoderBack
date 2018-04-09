package UploadFile;

/**
 * Created by Administrator on 2016/8/13.
 */

import com.alibaba.fastjson.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedFile;
import org.junit.*;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.text.SimpleDateFormat;
import java.util.*;


import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class HttpHelloWorldServerHandler2 extends SimpleChannelInboundHandler<Object> {
//    private static final byte[] CONTENT = { 'H', 'e', 'l', 'l', 'o', ' ', 'W', 'o', 'r', 'l', 'd' };
//    private static final Logger logger = LoggerFactory.getLogger(HttpHelloWorldServerHandler2.class);

    /** DefaultHttpDataFactory() usage:
    HttpData will be in memory if less than default size (16KB).
    DefaultHttpDataFactory(boolean useDisk)
    HttpData will be always on Disk if useDisk is True, else always in Memory if False
    DefaultHttpDataFactory(long minSize)
    HttpData will be on Disk if the size of the file is greater than minSize, else it will be in memory
    */
    private static final HttpDataFactory factory =new DefaultHttpDataFactory(true); // always save to disk
    private HttpPostRequestDecoder decoder;
    private HttpRequest request;
    public String uploadFile="success";
    public String num="0";

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Global.allChannels.add(ctx.channel());
        super.channelActive(ctx);
        System.out.println("active");

    }


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
                //System.out.println("http request");
                HttpRequest request = this.request = (HttpRequest) msg;
                String uri=request.uri();
                System.out.println("------------------------------------------------------");
                //System.out.println("uri is:"+uri);

                if(uri.equals("/upload")){
                    System.out.println("Come into upload");
                    decoder = new HttpPostRequestDecoder(factory, request);
                    if (msg instanceof HttpContent) {
                        // New chunk is received
                        HttpContent chunk = (HttpContent) msg;
                        decoder.offer(chunk);
                        readHttpDataChunkByChunk();
                        // example of reading only if at the end
                        if (chunk instanceof LastHttpContent) {
                            reset();
                        }
                        recordUploadInfoToDB();
                    }
                    httpResponse(ctx,msg,JSON.toJSONString(uploadFile));
                    uploadFile=null;

                }
                else if(uri.equals("/download")){
                    System.out.println("Come into download");
                    downloadFile(request,ctx);

                }else if(uri.equals("/getDirInfo")){
                    System.out.println("come into getDirInfo");
                    String response=getFileDirInfo(request);
                    httpResponse(ctx,msg,response);
                    response=null;

                }else if(uri.equals("/createFolder")){
                    System.out.println("come to createFolder");
                    String response=createFolder(request);
                    httpResponse(ctx,msg,response);
                    response=null;

                }else if(uri.equals("/openOneFile")){
                    System.out.println("come to openOneFile");
                }
                else if(uri.equals("/deleteFile")){
                    System.out.println("come to deleteFile");
                    String response=deleteFile(request);
                    httpResponse(ctx,msg,response);
                    response=null;

                }else if(uri.equals("/setOnOrOffCourse")){
                    System.out.println("come to setOnOrOffCourse");
                    String response=setOnOrOffCourse(request);
                    httpResponse(ctx,msg,response);
                    response=null;

                }
                else if(uri.equals("/test")){
                    System.out.println("come to test");
                    httpResponse(ctx,msg,("num is: "+(num)));
                    num=null;
                }
                else{
                    System.out.println("come into else");
                    httpResponse(ctx,msg,"sorry incorrect uri");
                }
            }
            else if(msg instanceof WebSocketFrame) {
                System.out.println("websocket request");
                handleWebSocketFrame(ctx, (WebSocketFrame) msg);
            }
        }
        //httpResponse(ctx,msg,"hello world");
    }

    /** handle websocket here 这里处理websocket协议 */
    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
        if (frame instanceof TextWebSocketFrame) {
            String request = ((TextWebSocketFrame) frame).text();
            Global.allChannels.writeAndFlush(new TextWebSocketFrame(request));//we must wrap into websocketframe before we send out
        } else {
            //System.out.println("enter2");
            String message = "unsupported frame type: " + frame.getClass().getName();
            throw new UnsupportedOperationException(message);
        }
    }

    /** deal with http request upload files
     *  处理http请求上传文件*/
    private void readHttpDataChunkByChunk() {
        try {
            while (decoder.hasNext()) {
                InterfaceHttpData data = decoder.next();
                if (data != null) {
                    try {
                        writeHttpData(data);
                    } finally {
                        data.release();
                    }
                }
            }
        } catch (HttpPostRequestDecoder.EndOfDataDecoderException e1) {
            // end of content chunk by chunk
        }
    }



    /**
     * deal with chunk data
     * 处理接收到的chunk数据
     * @param data
     */
    String account="",prefix="";
    Map<String,String> files=new HashMap<>();
    private void writeHttpData(InterfaceHttpData data) {
        //upload files to the system
        //上传文件到系统

        //this is the attribute data about some information to the upload file
        //这是一些关于该上传的文件的信息
        System.out.println("BODY FileUpload: "+ " \n\rThe data is : " + data+ "  End\r\n");
        if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
            Attribute attribute = (Attribute) data;
            if(attribute.getName().equals("account")){
                try {
                    account=attribute.getValue();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(attribute.getName().equals("prefix")){
                try {
                    prefix=attribute.getValue();
                    System.out.println("here get prefix");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.FileUpload) {
            FileUpload fileUpload = (FileUpload) data;
            if (fileUpload.isCompleted()) {
                //filename contains the full path and file name while name just contain file name
                //filename属性包含文件路径和文件名，而name属性只包含文件名
                String fileInfo=prefix+"/"+fileUpload.getName();
                files.put(fileInfo,account);
                String path="F:/XAMPP/htdocs/CourseSupport/ftp/"+fileInfo;
                System.out.println("upload to path:"+path);
                File file=new File(path);
                boolean mkdir=file.getParentFile().mkdirs();//have return value true or false
                //if create new folder, it should be added to files array
                //如果新建一个文件夹，应该添加到新建的文件的数组中
                if(mkdir){
                    String folderPath=path.substring(0,path.lastIndexOf('/')).replace("F:/XAMPP/htdocs/CourseSupport/ftp/","");
                    files.put(folderPath,account);
                }
                try {
                    if(fileUpload.renameTo(file)){
                        ConvertToPdf.changeFileToPdf(fileInfo,fileUpload.getFilename());
                    }//have return value true or false

                } catch (IOException e) {
                    uploadFile="failure";
                    e.printStackTrace();
                }
            } else {
                System.out.println("\tFile to be continued but should not!\r\n");
            }
        }
    }

    /**
     * record the student operation to the database
     * 把学生的操作记录在数据库中
     */
    public void recordUploadInfoToDB(){
        try{
            // teacher role need not to record into database at all after upload files
            // 老师角色上传文件不需要进行存库
            if(!account.equals("Teacher")){
                //connect to redis 连接redis数据库
                Jedis con = new Jedis("127.0.0.1");
                con.auth("beanson");
                Pipeline pipe = con.pipelined();

                //insert to redis StudentOperation hashmap
                //插入到redis数据库中名字为StudentOperation的hashmap
                pipe.hmset("StudentOperation",files);

                //pipe operation to improve performance,though could be seen
                //just insert one record in it
                //pipe操作能提高性能，虽然插入一条并不体现出来
                pipe.sync();
                pipe.bgsave();
                con.close();
            }

            //release resource 释放资源
            files.clear();
            prefix="";
            account="";
        }catch (Exception e){
            uploadFile="failure";
        }
    }


    /**
     * get file or dir information based on a given base dir
     * 根据给定的文件路径来获取该级文件夹下的文件名
     */
    public String getFileDirInfo(HttpRequest httpRequest){
        String response=null;
        if(httpRequest instanceof FullHttpRequest){
            ByteBuf data = ((FullHttpRequest)httpRequest).content();
            String receive=data.toString(StandardCharsets.UTF_8); //receive dir=嵌入式课件/第一节课
            System.out.println(receive);
            String str[]=receive.split("\\="); //str[0]="dir" str[1]="嵌入式课件/第一节课"
            String finalPath=null;
            if(str.length>1){
                finalPath=str[1];
            }else{
                finalPath="";
            }
            Path path= Paths.get("F:","XAMPP","htdocs","CourseSupport","ftp",finalPath);
            BasicFileAttributeView basicView= Files.getFileAttributeView(path,BasicFileAttributeView.class);
            try {
                //judge whether it is a file or folder
                //判断是否为路径还是最终文件
                if(basicView.readAttributes().isDirectory()){
                    response=JSON.toJSONString(WalkFileTree.getDirectoryFiles(path));
                    System.out.println("response:"+response);
                }else{
                    openSingleFile(path);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return response;
    }


    /**
     * create folder to file system based on given path
     * 根据上传的路径创建文件夹
     * @return 返回状态值
     */
    public String createFolder(HttpRequest httpRequest){
        String response="success";
        if(httpRequest instanceof FullHttpRequest){
            ByteBuf data = ((FullHttpRequest)httpRequest).content();
            //receive: name=ftp_2016/嵌入式课件/实验一/跑马灯程序&studentId=2013100
            String receive=data.toString(StandardCharsets.UTF_8);
            System.out.println("receive create new folder: "+receive);
            //str[0]="name" str[1]="嵌入式课件/实验一/跑马灯程序"
            // str[2]=studentId str[3]=2013100
            String str[]=receive.split("\\=|\\&");
            String path1="F:/XAMPP/htdocs/CourseSupport/ftp/"+str[1];
            File file1=new File(path1);
            if(file1.mkdirs()){
                if(!str[3].equals("Teacher")){
                    writeDataToRedis(str[1],str[3]);
                }
            }else{
                response="failure";
            }
            String path2="F:/XAMPP/htdocs/CourseSupport/pdf/"+str[1];
            File file2=new File(path2);
            if(!file2.mkdirs()){
                response="failure";
            }
        }
        return JSON.toJSONString(response);
    }


    /**
     * to delete the selected file with matched right
     * 删除权限范围内的相应指定的文件
     * @param httpRequest get fileName from httpRequest 从httpRequest中获取文件名
     * @return return back status 返回处理结果状态
     */
    ArrayList<String> toDeleteField=new ArrayList<>();
    public String deleteFile(HttpRequest httpRequest){
        String response="success";
        Jedis con = new Jedis("127.0.0.1");
        Pipeline pipe=con.pipelined();
        con.auth("beanson");

        if(httpRequest instanceof FullHttpRequest){
            ByteBuf data = ((FullHttpRequest)httpRequest).content();
            //toDeleteFile: file=ftp_2016/嵌入式课件/实验一/跑马灯程序&account=2013100
            String receive=data.toString(StandardCharsets.UTF_8);
            System.out.println("receive delete file "+receive);
            //str[0]="file" str[1]="ftp_2016/嵌入式课件/实验一/跑马灯程序"
            //str[2]=account str[3]=2013100
            //str[4]=validation str[5]='student' or teacher validation
            String str[]=receive.split("\\=|\\&");
            //get the select file or folder path to delete 获取指定文件或文件夹路径
            //judge whether it has the authentic 判断是否有删除权限


            if(str[5].equals("student")){
                //means student operation 学生进行文件删除操作

                if(getVertification(str[1],str[3],con)){
                    String path1="F:/XAMPP/htdocs/CourseSupport/ftp/"+str[1];
                    File file1=new File(path1);
                    //cascading deletion 级联删除
                    if(!deleteDir(file1,true)){
                        response="failure";
                    }

                    //to delete folders and files within 2016_pdf folder
                    //删除2016_pdf文件夹下的文件或文件夹
                    String path2=null;
                    //judge whether it is a file or a folder
                    //判断该路径下是否为文件还是文件夹
                    if(str[1].substring(str[1].lastIndexOf('/') + 1).contains(".")){
                        path2="F:/XAMPP/htdocs/CourseSupport/pdf/"+str[1]+".pdf";
                    }else{
                        path2="F:/XAMPP/htdocs/CourseSupport/pdf/"+str[1];
                    }
                    System.out.println("delete pdf path"+path2);
                    File file2=new File(path2);
                    //cascading deletion 级联删除
                    if(!deleteDir(file2,false)){
                        response="failure";
                    }

                    //delete item within redis 在redis中删除记录
                    for (String eachFile :
                            toDeleteField) {
                        System.out.println("add: "+eachFile);
                        pipe.hdel("StudentOperation",eachFile);
                    }
                    pipe.sync();
                    pipe.bgsave();
                }else{
                    response="failure";
                }

            }else{
                if(con.get("TeaValid").equals(str[5])){
                    //means teacher operation 老师进行文件操作

                    String path1="F:/XAMPP/htdocs/CourseSupport/ftp/"+str[1];
                    File file1=new File(path1);
                    //cascading deletion 级联删除
                    if(!deleteDir(file1,true)){
                        response="failure";
                    }

                    //to delete folders and files within 2016_pdf folder
                    //删除2016_pdf文件夹下的文件或文件夹
                    String path2=null;
                    //judge whether it is a file or a folder
                    //判断该路径下是否为文件还是文件夹
                    if(str[1].substring(str[1].lastIndexOf('/') + 1).contains(".")){
                        path2="F:/XAMPP/htdocs/CourseSupport/pdf/"+str[1]+".pdf";
                    }else{
                        path2="F:/XAMPP/htdocs/CourseSupport/pdf/"+str[1];
                    }
                    System.out.println("delete pdf path"+path2);
                    File file2=new File(path2);
                    //cascading deletion 级联删除
                    if(!deleteDir(file2,false)){
                        response="failure";
                    }

                    //delete item within redis 在redis中删除记录
                    for (String eachFile :
                            toDeleteField) {
                        System.out.println("add: "+eachFile);
                        pipe.hdel("StudentOperation",eachFile);
                    }
                    pipe.sync();
                    pipe.bgsave();

                }else{
                    response="failure";
                }

            }

            //close con and pipe 关闭con和pipe流
            try {
                pipe.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            con.close();
            toDeleteField.clear();
        }
        return JSON.toJSONString(response);
    }

    /**
     * this method to delete a file or directory containing with files or not
     * 方法用来删除一个文件或者文件夹（里面有或没有文件）
     * @param file 待删除的文件或文件夹
     */
    public boolean deleteDir(File file,boolean mark) {
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) {
                deleteDir(f,mark);
                //mark true means the ftp folder and redis should or not delete such item
                //mark为true值代表redis数据库是否需要删除ftp文件夹中的文件的记录
                if(mark){
                    String temp=f.getPath().replace("\\","/").replace("F:/XAMPP/htdocs/CourseSupport/ftp/","");
                    toDeleteField.add(temp);
                }
            }
        }
        if(mark){
            String temp2=file.getPath().replace("\\","/").replace("F:/XAMPP/htdocs/CourseSupport/ftp/","");
            toDeleteField.add(temp2);
        }
        return file.delete();
    }


    /**
     * judge whether it has the verification to delete it or not
     * by given file path and account
     * 基于给定的文件名（全路径名）和账号来判断是否有删除权限
     * @param fileName 文件全名
     * @param account 账号
     * @return
     */
    //@org.junit.TestUtil
    public boolean getVertification(String fileName,String account,Jedis con){
        //get value based on given field and key 基于给定的键和域的值
        String back=con.hget("StudentOperation",fileName);
        if(back!=null){
            //if equals to a given student account 判断是否和给定账号相等
            if(back.equals(account)){
                System.out.println("validate delete");
                return true;
            }else{
                System.out.println("not validate delete");
            }
        }
        else{
            System.out.println("No Such Directory");
        }
        con.close();
        return false;
    }


    /**
     * to record student operation on
     * @param fileName  文件名
     * @param studentId 学号
     */
    public void writeDataToRedis(String fileName,String studentId){
        //connect to redis [judging commonly student operate download more often which
        // do not need to connect to redis so we just connect it when it is in deed]
        //连接到redis数据库中[鉴于大多数学生操作是下载文件，该操作不需用到数据库，故没有提取连库操作出来]
        Jedis con = new Jedis("127.0.0.1");
        con.auth("beanson");
        Pipeline pipe = con.pipelined();

        //insert to redis StudentOperation hashmap
        //插入到redis数据库中名字为StudentOperation的hashmap
        Map<String,String> map=new HashMap<>();
        map.put(fileName,studentId);
        pipe.hmset("StudentOperation",map);

        //pipe operation to improve performance,though could be seen
        //just insert one record in it
        //pipe操作能提高性能，虽然插入一条并不体现出来
        pipe.sync();
        pipe.bgsave();
        con.close();
    }

    /**
     * open the file on web when student click to the file name
     * 当学生点击文件名直接后进行打开操作
     * @param path 文件路径
     */
    public void openSingleFile(Path path){

    }

    private void reset() {
        request = null;
        // destroy the decoder to release all resources
        decoder.destroy();
        decoder = null;
    }


    /**
     * set course on or off
     * 开启或结束某项课程
     *
     * ps: within redis the key named as "TeaValid" contain the validation
     * 在redis中存储有教师登录的key，名称为TeaValid，内包含教师认证信息
     *
     * Insert and Interface shall be two keys, each "on" or "off"
     * Insert 和 Interface是两个键，对应的值为on或off
     */
    public String setOnOrOffCourse(HttpRequest httpRequest){
        String response="failure";
        if(httpRequest instanceof FullHttpRequest){
            ByteBuf data = ((FullHttpRequest)httpRequest).content();
            //receive: name=Insert&status=on&validation=232143532
            String receive=data.toString(StandardCharsets.UTF_8);
            System.out.println("receive course on or off: "+receive);
            // str[0]="name" str[1]="Insert"
            // str[2]=status str[3]="on"
            // str[4]=validation str[5]="232143532"
            String str[]=receive.split("\\=|\\&");

            Jedis con = new Jedis("127.0.0.1");
            con.auth("beanson");
            String validation=con.get("TeaValid");
            //teacher validate 教师认证
            if(str[5].equals(validation)) {
                //operation to one specific key
                //对指定一个key进行操作
                if(con.set(str[1],str[3]).equals("OK")){
                    response="success";
                }
            }
        }
        return JSON.toJSONString(response);
    }


/**********************************************************************************************/
    public void downloadFile(HttpRequest httpRequest,ChannelHandlerContext ctx) throws IOException {
        if(httpRequest instanceof FullHttpRequest){
            ByteBuf data = ((FullHttpRequest)httpRequest).content();
            //receive: name=Insert/text.txt
            String receive=data.toString(StandardCharsets.UTF_8);
            System.out.println("receive course on or off: "+receive);
            // str[0]="name" str[1]="Insert/text.txt"
            String str[]=receive.split("\\=|\\&");

            // prepare the file to be download
            Path path=Paths.get("F:","XAMPP","htdocs","CourseSupport","ftp",str[1]);
            System.out.println("to download file path: "+path);
            File file = new File(path.toString());
            RandomAccessFile raf = null;
            try {
                raf = new RandomAccessFile(file, "r");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            long fileLength = raf.length();

            //set response header
            HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
            response.headers().set(CONTENT_TYPE, "application/json"); //when set json string should be like json format
            response.headers().set(ACCESS_CONTROL_ALLOW_ORIGIN,"*");
            response.headers().set(ACCESS_CONTROL_ALLOW_HEADERS,"*");
            HttpHeaders.setContentLength(response, fileLength);
            setContentTypeHeader(response, file);
            setDateAndCacheHeaders(response, file);
            if (HttpHeaders.isKeepAlive(request)) {
                response.headers().set(CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
            }

            // Write the initial line and the header.
            ctx.write(response);

            // Write the content.
            ChannelFuture sendFileFuture;
            ChannelFuture lastContentFuture;
            if (ctx.pipeline().get(SslHandler.class) == null) {
                //System.out.println("ssl not enable use zero-copy");
                // SSL not enabled - can use zero-copy file transfer.
                sendFileFuture =
                        ctx.write(new DefaultFileRegion(raf.getChannel(), 0, fileLength), ctx.newProgressivePromise());
                // Write the end marker.
                lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
            } else {
                //System.out.println("ssl enable use zero-copy");
                // SSL enabled - cannot use zero-copy file transfer.
                sendFileFuture =
                        ctx.writeAndFlush(new HttpChunkedInput(new ChunkedFile(raf, 0, fileLength, 8192)),
                                ctx.newProgressivePromise());
                // HttpChunkedInput will write the end marker (LastHttpContent) for us.
                lastContentFuture = sendFileFuture;
            }

            sendFileFuture.addListener(new ChannelProgressiveFutureListener() {

                public void operationProgressed(ChannelProgressiveFuture future, long progress, long total) {
                    if (total < 0) { // total unknown
                        System.err.println(future.channel() + " Transfer progress: " + progress);
                    } else {
                        System.err.println(future.channel() + " Transfer progress: " + progress + " / " + total);
                    }
                }


                public void operationComplete(ChannelProgressiveFuture future) {
                    System.err.println(future.channel() + " Transfer complete.");
                }
            });

            // Decide whether to close the connection or not.
            if (!HttpHeaders.isKeepAlive(request)) {
                // Close the connection when the whole content is written out.
                lastContentFuture.addListener(ChannelFutureListener.CLOSE);
            }

        }
    }
    /**
     * Sets the content type header for the HTTP Response
     *
     * @param response
     *            HTTP response
     * @param file
     *            file to extract content type
     */
    private static void setContentTypeHeader(HttpResponse response, File file) {
        MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
        response.headers().set(CONTENT_TYPE, mimeTypesMap.getContentType(file.getPath()));
    }
    /**
     * Sets the Date and Cache headers for the HTTP Response
     *
     * @param response
     *            HTTP response
     * @param fileToCache
     *            file to extract content type
     */
    public static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
    public static final String HTTP_DATE_GMT_TIMEZONE = "GMT";
    public static final int HTTP_CACHE_SECONDS = 60;
    private static void setDateAndCacheHeaders(HttpResponse response, File fileToCache) {
        SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
        dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));

        // Date header
        Calendar time = new GregorianCalendar();
        response.headers().set(DATE, dateFormatter.format(time.getTime()));

        // Add cache headers
        time.add(Calendar.SECOND, HTTP_CACHE_SECONDS);
        response.headers().set(EXPIRES, dateFormatter.format(time.getTime()));
        response.headers().set(CACHE_CONTROL, "private, max-age=" + HTTP_CACHE_SECONDS);
        response.headers().set(
                LAST_MODIFIED, dateFormatter.format(new Date(fileToCache.lastModified())));
    }



/*****************************************************************************************/
    /**
     * send message back to client as responses to http request
     * 返回http请求相关消息
     * @param ctx message channel 通信通道
     * @param msg the request message reference 请求的引用
     */
    public void httpResponse(ChannelHandlerContext ctx, Object msg,String dataBack){
        if (HttpUtil.is100ContinueExpected((HttpMessage) msg)) {
            System.out.println("is 100 continue");
            ctx.write(new DefaultFullHttpResponse(HTTP_1_1, CONTINUE));
        }

        //String str="{\"response\":\"true\"}";
        boolean keepAlive = HttpUtil.isKeepAlive((HttpMessage) msg);
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(dataBack.getBytes()));
        response.headers().set(CONTENT_TYPE, "application/json"); //when set json string should be like json format
        //response.headers().set(CONTENT_TYPE, "text/plain");// by using CONTENT as content
        response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
        response.headers().set(ACCESS_CONTROL_ALLOW_ORIGIN,"*");
        response.headers().set(ACCESS_CONTROL_ALLOW_METHODS,"POST");
        //response.headers().set(ACCESS_CONTROL_ALLOW_HEADERS,"x-requested-with,content-type");
        response.headers().set(ACCESS_CONTROL_ALLOW_HEADERS,"*");
        response.headers().set(ACCEPT,"*");
        if (!keepAlive) {
            System.out.println("not to keep alive");
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        } else {
            ctx.writeAndFlush(response);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }





//    The original receive data chunk by chunk classified by type: Attribute or FileUpload
//    private void writeHttpData(InterfaceHttpData data) {
//        String value;//attribute 接收键值对
//        String path;//file path included name save to system 上传的文件连同其目录
//        if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
//
//            //get attribute info 获取属性信息
//            Attribute attribute = (Attribute) data;
//            try {
//                value = attribute.getValue();
//            } catch (IOException e1) {
//                e1.printStackTrace();
//                return;
//            }
//            //print attribute value 打印属性信息
//            if (value.length() > 100) {
//                System.out.println("\r\nBODY Attribute: " + attribute.getHttpDataType().name() + ": "
//                        + attribute.getUser_name() + " data too long\r\n");
//            } else {
//                System.out.println("\r\nBODY Attribute: " + attribute.getHttpDataType().name() + ": "
//                        + attribute + "\r\n");
//            }
//
//            if(Objects.equals(attribute.getUser_name(), "response")){
//                dataBack= JSON.toJSONString(WalkFileTree.getDirectoryFiles(value));
//                System.out.println("dataBack is: "+dataBack);
//            }
//
//        }
//        //upload files to the system
//        //上传文件到系统
//        System.out.println("BODY FileUpload: "+ " \n\rThe data is : " + data+ "  End\r\n");
//        if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.FileUpload) {
//            FileUpload fileUpload = (FileUpload) data;
//            if (fileUpload.isCompleted()) {
//                if (fileUpload.length() < 10000) {
//                    System.out.println("\tContent of file\r\n");
//                    try {
//                        System.out.println(fileUpload.getString(fileUpload.getCharset()));
//                    } catch (IOException e1) {
//                        // do nothing for the example
//                        e1.printStackTrace();
//                    }
//                    System.out.println("\r\n");
//                } else {
//                    System.out.println("\tFile too long to be printed out:" + fileUpload.length() + "\r\n");
//                }
//                System.out.println("whether in memory: "+fileUpload.isInMemory());
//                // fileUpload.isInMemory();// tells if the file is in Memory
//                // or on File
//
//                //filename contains the full path and file name while name just contain file name
//                //filename属性包含文件路径和文件名，而name属性只包含文件名
//                path="E:/CourseSupport/"+fileUpload.getFilename();
//                File file=new File(path);
//                //File file=new File("F://AllPattern/save.txt");
//                boolean makeDir=file.getParentFile().mkdirs();
//                System.out.println("make dir:"+makeDir);
//                boolean judge=false;
//                try {
//                    fileUpload.renameTo(file);//have return value true or false
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                System.out.println("save to another location: "+judge);  // enable to move into another
//                 File dest
//                 the File of to delete file
//            } else {
//                System.out.println("\tFile to be continued but should not!\r\n");
//            }
//        }
//    }
}