package viewcoder.operation.impl.transfer;

import com.alibaba.fastjson.JSONObject;
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
        try {
            KeyStore ks = KeyStore.getInstance("PKCS12");
            FileInputStream fis = new FileInputStream("/root/ca/apiclient_cert.p12");
            ks.load(fis, "1503031011".toCharArray());
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks, "1503031011".toCharArray());
            sc = SSLContext.getInstance("TLS");
            sc.init(kmf.getKeyManagers(), null, null);
            logger.error("--------load p12 cert file success: ");

        }catch (Exception e){
            logger.error("--------init p12 cert file error: ", e);
        }
    }


    public static String postOpt(String urlStr, String message) {
        try {
            URL url = new URL(urlStr); // here is your URL path
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            if (connection instanceof HttpsURLConnection) {
                ((HttpsURLConnection)connection).setSSLSocketFactory(sc.getSocketFactory());
            }

            conn.setReadTimeout(15000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(message);

            writer.flush();
            writer.close();
            os.close();

            int responseCode=conn.getResponseCode();
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                BufferedReader in=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuffer sb = new StringBuffer("");
                String line="";

                while((line = in.readLine()) != null) {
                    sb.append(line);
                    break;
                }
                in.close();
                return sb.toString();

            } else {
                return new String("false : " + responseCode);
            }
        }
        catch(Exception e){
            logger.error("http post error", e);
            return new String("Exception: " + e.getMessage());
        }
    }


}
