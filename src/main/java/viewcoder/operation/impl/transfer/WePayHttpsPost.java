package viewcoder.operation.impl.transfer;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyStore;
import java.util.Iterator;

/**
 * Created by Administrator on 2018/10/29.
 */
public class WePayHttpsPost {

    private static Logger logger = LoggerFactory.getLogger(WePayHttpsPost.class.getName());
    private static SSLContext sc = null;

    static {
//        try {
//            KeyStore ks = KeyStore.getInstance("PKCS12");
//            FileInputStream fis = new FileInputStream("/root/ca/apiclient_cert.p12");
//            ks.load(fis, "1503031011".toCharArray());
//            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
//            kmf.init(ks, "1503031011".toCharArray());
//            sc = SSLContext.getInstance("TLS");
//            sc.init(kmf.getKeyManagers(), null, null);
//            logger.error("--------load p12 cert file success: ");
//
//        } catch (Exception e) {
//            logger.error("--------init p12 cert file error: ", e);
//        }

    }


    public static CloseableHttpResponse postOpt(String urlStr, String message) {
        try {

            FileInputStream instream = null;
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            instream = new FileInputStream(new File("/root/ca/apiclient_cert.p12"));
            keyStore.load(instream, "1503031011".toCharArray());

            if (null != instream) {
                instream.close();
            }

            SSLContext sslcontext = SSLContexts.custom().loadKeyMaterial(keyStore,"1503031011".toCharArray()).build();
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext, new String[]{"TLSv1"}, null, SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
            HttpPost httpPost = new HttpPost(urlStr);

            // 得指明使用UTF-8编码，否则到API服务器XML的中文不能被成功识别
            httpPost.addHeader("Content-Type", "text/xml");
            httpPost.setEntity(new StringEntity(message, "UTF-8"));
            return HttpClients.custom().setSSLSocketFactory(sslsf).build().execute(httpPost);

//            if (isLoadCert) {
//                // 加载含有证书的http请求
//                return HttpClients.custom().setSSLSocketFactory(CertUtil.initCert()).build().execute(httpPost);
//            } else {
//                return HttpClients.custom().build().execute(httpPost);
//            }


//            URL url = new URL(urlStr); // here is your URL path
//            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
//            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
//            connection.setSSLSocketFactory(sc.getSocketFactory());
//            if (connection instanceof HttpsURLConnection) {
//                ((HttpsURLConnection) connection).setSSLSocketFactory(sc.getSocketFactory());
//            }

//            conn.setReadTimeout(15000 /* milliseconds */);
//            conn.setConnectTimeout(15000 /* milliseconds */);
//            conn.setRequestMethod("POST");
//            conn.setDoInput(true);
//            conn.setDoOutput(true);
//
//            OutputStream os = conn.getOutputStream();
//            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
//            writer.write(message);
//
//            writer.flush();
//            writer.close();
//            os.close();
//
//            int responseCode = conn.getResponseCode();
//            if (responseCode == HttpsURLConnection.HTTP_OK) {
//                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//                StringBuffer sb = new StringBuffer("");
//                String line = "";
//
//                while (true) {
//                    if ((line = in.readLine()) != null) {
//                        sb.append(line);
//
//                    } else {
//                        break;
//                    }
//                }
//
//                in.close();
//                return sb.toString();
//
//            } else {
//                return new String("false : " + responseCode);
//            }
        } catch (Exception e) {
            logger.error("http post error", e);
            return null;
        }
    }


}
