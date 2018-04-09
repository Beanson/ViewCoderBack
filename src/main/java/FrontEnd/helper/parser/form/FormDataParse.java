package FrontEnd.helper.parser.form;

import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.multipart.*;
import org.apache.log4j.Logger;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;


/**
 * Created by Administrator on 2017/6/16.
 * 该类用来解析用formData封装请求上传的数据
 */
public class FormDataParse {

    private static Logger logger= Logger.getLogger(FormData.class);
    protected static final HttpDataFactory factory =new DefaultHttpDataFactory(true); // always save to disk
    protected HttpPostRequestDecoder decoder;
    protected HttpRequest request;
    protected String singleParam;
    protected String[] multiParams;
    protected Class<?>clazz;
    protected File file;
    protected Object formData,msg;
    protected HashMap<String,Object> hashMap;

    /**构造方法*/
    /*****************************************************************************************/
    /** 构造函数 base方法 */
    public FormDataParse(Object msg) {
        this.msg=msg;
        this.request = (HttpRequest) msg;
        decoder = new HttpPostRequestDecoder(factory, request);//解析请求数据。
    }

    /** 构造函数 处理formData数据请求上传，只请求一个参数方法，返回单一字符串 */
    public FormDataParse(Object msg, String singleParam) {
        this(msg);
        this.singleParam=singleParam;
    }

    /** 构造函数 处理formData数据请求上传，请求多个参数方法 ，返回HashMap*/
    public FormDataParse(Object msg, String... multiParams) {
        this(msg);
        this.multiParams=multiParams;
        hashMap=new HashMap<>();
        for (String param :
                multiParams) {
            hashMap.put(param,null);
        }
    }

    /** 构造函数 处理formData数据请求上传，实体类装载请求数据，返回实体类*/
    public FormDataParse(Object msg, Class<?> entityName) {
        this(msg);
        try {
            clazz = Class.forName(entityName.getName());
            formData=clazz.newInstance();//新建实体类对应的空对象实例，准备装载发送过来的数据
            logger.debug("come into parsing entity");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    /**参数解析方法*/
    /*****************************************************************************************/
    /**
     * SINGLE_PARAM 类型的处理方法
     * deal with chunk data
     * 处理接收到的chunk数据
     * @param data 传递chunk data过去解析
     */
    protected String parseFormDataSingleParam(InterfaceHttpData data) {
        System.out.println("New FormData Upload SingleParam : "+ " \n\rThe data is : " + data+ "  End\n");
        try {
            //解析参数类型数据
            if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
                Attribute attribute = (Attribute) data;//用Attribute装载数据
                if(attribute.getName().equals(singleParam)){
                    return attribute.getValue();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * MULTI_PARAMS 类型的处理方法
     * deal with chunk data
     * 处理接收到的chunk数据
     * @param data 传递chunk data过去解析
     */
    protected HashMap<String, Object> parseFormDataMultiParams(InterfaceHttpData data) {
        System.out.println("New FormData Upload: MultiParams"+ " \n\rThe data is : " + data+ "  End\n");
        try {
            //解析参数类型数据
            if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
                Attribute attribute = (Attribute) data;//用Attribute装载数据
                hashMap.put(attribute.getName(),attribute.getValue());
            }
            else if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.FileUpload) {
                FileUpload fileUpload = (FileUpload) data;
                if (fileUpload.isCompleted()) {
                    hashMap.put(fileUpload.getName(),fileUpload.copy());
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return hashMap;
    }

    /**
     *  ENTITY 类型的处理方法
     * deal with chunk data
     * 处理接收到的chunk数据
     * @param data 传递chunk data过去解析
     */
    protected Object parseFormDataEntity(InterfaceHttpData data) {
        System.out.println("New FormData Upload Entity: "+ " \n\rThe data is : " + data+ "  End\n");
        try {
            //解析参数类型数据
            if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
                Attribute attribute = (Attribute) data;//用Attribute装载数据
                Field field=clazz.getDeclaredField(attribute.getName());//通过attribute的name反射获取对应参数变量
                field.setAccessible(true);//通过放射取消访问field权限检查
                field.set(formData,DataParser.dataParse(field.getType().getName(),attribute.getValue()));//填充实体类数据
            }
            else if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.FileUpload) {
                FileUpload fileUpload = (FileUpload) data;
                if (fileUpload.isCompleted()) {
                    Field field=clazz.getDeclaredField(fileUpload.getName());//通过attribute的name反射获取对应参数变量
                    field.setAccessible(true);//通过反射取消访问field权限检查
                    field.set(formData,fileUpload.copy());//填充实体类数据
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        logger.debug("parse data:"+formData.toString());
        return formData;
    }


    /**
     * 返回formData信息
     * @return
     */
    public Object getFormData(FormDataEnumType type){
        Object backData=null;
        // New chunk is received
        if (msg instanceof HttpContent) {
            HttpContent chunk = (HttpContent) msg;
            decoder.offer(chunk);
            try {
                while (decoder.hasNext()) {
                    InterfaceHttpData data = decoder.next();
                    if (data != null) {
                        try {
                            switch (type){
                                case SINGLE_PARAM:backData=parseFormDataSingleParam(data);break;
                                case MULTI_PARAMS:backData=parseFormDataMultiParams(data);break;
                                case ENTITY:backData=parseFormDataEntity(data);break;
                            }
                        } finally {
                            data.release();
                        }
                    }
                }
            } catch (HttpPostRequestDecoder.EndOfDataDecoderException e1) {
                // end of content chunk by chunk
            }
            if (chunk instanceof LastHttpContent) {
                reset();
            }
        }
        return backData;
    }


    /** release the resource 释放资源 */
    protected void reset() {
        request = null;
        decoder.destroy();
        decoder = null;
        //clazz=null;
    }

}
