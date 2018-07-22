import org.apache.commons.codec.binary.Hex;
import viewcoder.tool.common.Common;
import viewcoder.tool.common.OssOpt;
import viewcoder.tool.config.GlobalConfig;
import viewcoder.tool.encrypt.AESEncryptor;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.PutObjectResult;
import org.junit.Test;

import java.io.File;

/**
 * Created by Administrator on 2018/2/17.
 */

public class CommonTest {

    @Test
    public void testIntegerParse(){
        System.out.println(Long.parseLong("11532265451135000")+1);
    }

    @Test
    public void testAESEncryptor() {
        String encodeStr = AESEncryptor.AESEncode(Common.AES_KEY, "");
        System.out.println("encrypted data is:" + encodeStr);

        String decodeStr = AESEncryptor.AESDncode(Common.AES_KEY, encodeStr);
        System.out.println("decoded data is:" + decodeStr);
    }

    @Test
    public void testConfig() {
        String ACCESS_KEY_ID = "com.viewcoder.oss.access.key";
        String ACCESS_KEY_SECRET = "com.viewcoder.oss.access.secret";
        System.out.println(AESEncryptor.AESDncode(Common.AES_KEY, GlobalConfig.getProperties(ACCESS_KEY_ID)));
        System.out.println(AESEncryptor.AESDncode(Common.AES_KEY, GlobalConfig.getProperties(ACCESS_KEY_SECRET)));
    }


    @Test
    public void testOssUpload() {
        OSSClient ossClient = OssOpt.initOssClient();
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource("files/simple.psd").getFile());
            //OssOpt.uploadFileToOss("upload_files/simple.psd", file, ossClient);
            PutObjectResult putObjectResult = ossClient.putObject("viewcoder-bucket","upload_files/simple.psd",file);
            System.out.println(putObjectResult);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            OssOpt.shutDownOssClient(ossClient);
        }
    }


    /**
     * Copy文件时如果源文件找不到会报错
     */
    @Test
    public void testOssCopy() {
        OSSClient ossClient = OssOpt.initOssClient();
        try {
            OssOpt.copyObjectOss(ossClient, "single_export/dubbo框架.jpg", "single_export/456.jpg");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            OssOpt.shutDownOssClient(ossClient);
        }
    }

    /**
     * 删除文件时如果
     */
    @Test
    public void testOssDelete() {
        OSSClient ossClient = OssOpt.initOssClient();
        try {
            OssOpt.deleteFileInOss("single_export/dubbo框架.jpg", ossClient);
            System.out.println("hello world");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            OssOpt.shutDownOssClient(ossClient);
        }
    }


    @Test
    public void parseData(){
        String a[] = new String[2];
        a[0]="123";
        a[1]="456";
        Object b = a;
        String c [] = (String[]) b;
        System.out.println(c[0]+","+c[1]);
    }

    @Test
    public void testHex() throws Exception{
        String str = "31353330343935343239353234323031383037303232313030313030343335303538323333393332333331323031382d30372d30322030393a33373a343731302e3130316e756c6c";
        byte[] decode = Hex.decodeHex(str.toCharArray());
        System.out.println(new String(decode,"UTF-8"));
    }
}






