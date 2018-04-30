package viewcoder.tool.util;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * Created by Administrator on 2018/2/19.
 */
public class HttpUtil {

    private static Logger logger = Logger.getLogger(HttpUtil.class);

    /**
     * Http Post的请求数据公用方法
     *
     * @param uri
     * @param map
     * @return
     */
    public static String httpClientUploadFile(String uri, Map<String, Object> map) {

        //初始化httpclient实例
        CloseableHttpClient httpClient = HttpClients.createDefault();
        String result = "";
        try {
            HttpPost httpPost = new HttpPost(uri);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            //遍历获取需进行传递的参数
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if (entry.getValue() instanceof File) {
                    File file = (File) entry.getValue();
                    builder.addBinaryBody(entry.getKey(), file, ContentType.MULTIPART_FORM_DATA, file.getName());
                    HttpUtil.logger.debug("file type: "+file.getName());
                } else {
                    HttpUtil.logger.debug("text type: "+entry.getValue());
                    builder.addTextBody(entry.getKey(), String.valueOf(entry.getValue()));
                }
            }
            HttpEntity entity = builder.build();
            httpPost.setEntity(entity);
            // 执行提交操作
            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity responseEntity = response.getEntity();
            if (responseEntity != null) {
                // 将响应内容转换为字符串
                result = EntityUtils.toString(responseEntity, Charset.forName("UTF-8"));
            }
        } catch (Exception e) {
            HttpUtil.logger.warn("Httpclient Post Error: ",e);
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                HttpUtil.logger.warn("Httpclient Close Error: ",e);
            }
        }
        return result;
    }


    /**
     * Http Post的发送纯文本方法
     *
     * @param uri 请求URL
     * @param text 发送的纯文本
     * @return
     */
    public static String httpClientPureText(String uri, String text) {

        //初始化httpclient实例
        CloseableHttpClient httpClient = HttpClients.createDefault();
        String result = "";
        try {
            HttpPost httpPost = new HttpPost(uri);
            StringEntity stringEntity=new StringEntity(text,ContentType.TEXT_PLAIN);
            httpPost.setEntity(stringEntity);
            // 执行提交操作
            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity responseEntity = response.getEntity();
            if (responseEntity != null) {
                // 将响应内容转换为字符串
                result = EntityUtils.toString(responseEntity, Charset.forName("UTF-8"));
            }
        } catch (Exception e) {
            HttpUtil.logger.warn("Httpclient Post Error: ",e);
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                HttpUtil.logger.warn("Httpclient Close Error: ",e);
            }
        }
        return result;
    }
}
