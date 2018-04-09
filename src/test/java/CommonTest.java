import FrontEnd.helper.common.Common;
import FrontEnd.helper.common.OssOpt;
import FrontEnd.helper.config.GlobalConfig;
import FrontEnd.helper.encrypt.AESEncryptor;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.Callback;
import com.aliyun.oss.model.PutObjectResult;
import org.junit.Test;

import java.io.File;

/**
 * Created by Administrator on 2018/2/17.
 */

public class CommonTest {

    @Test
    public void testAESEncryptor() {
        String encodeStr = AESEncryptor.AESEncode("@Admin123*Go", "yF3L6IbHTma6QbgfopLcJ4JF2cvSbJ");
        System.out.println("encrypted data is:" + encodeStr);

        String decodeStr = AESEncryptor.AESDncode("@Admin123*Go", encodeStr);
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
}






