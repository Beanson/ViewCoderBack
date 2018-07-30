import org.apache.commons.codec.binary.Hex;
import org.apache.ibatis.session.SqlSession;
import viewcoder.operation.entity.UserUploadFile;
import viewcoder.operation.impl.common.CommonService;
import viewcoder.operation.impl.purchase.wechat.PayCommonUtil;
import viewcoder.tool.common.Common;
import viewcoder.tool.common.Mapper;
import viewcoder.tool.common.OssOpt;
import viewcoder.tool.config.GlobalConfig;
import viewcoder.tool.encrypt.AESEncryptor;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.PutObjectResult;
import org.junit.Test;
import viewcoder.tool.util.MybatisUtils;

import java.io.File;
import java.util.*;

/**
 * Created by Administrator on 2018/2/17.
 */

public class CommonTest {

    @Test
    public void weChatVerify() throws Exception{
        Map packageParams = new HashMap<String, String>();
        packageParams.put("appid","wx16c7efa55a7f976b");
        packageParams.put("bank_type","BOC_DEBIT");
        packageParams.put("cash_fee","10");
        packageParams.put("fee_type","CNY");
        packageParams.put("is_subscribe","Y");
        packageParams.put("mch_id","1503031011");
        packageParams.put("nonce_str","2324373739");
        packageParams.put("openid","oaCnbs6EiIYbXgc8aYlRRSlJvqGk");
        packageParams.put("out_trade_no","1532791477865");
        packageParams.put("result_code","SUCCESS");
        packageParams.put("return_code","SUCCESS");
        packageParams.put("sign","94C42B64245D52032A18CBC2C31FF9AB");
        packageParams.put("time_end","20180728232451");
        packageParams.put("total_fee","10");
        packageParams.put("trade_type","NATIVE");
        packageParams.put("transaction_id","4200000150201807289324621765");
        PayCommonUtil.isTenpaySign(Common.UTF8, packageParams, GlobalConfig.getProperties(Common.PAY_WECHAT_API_KEY));

        Map packageParams2 = new HashMap<String, String>();
        packageParams2.put("appid","wx16c7efa55a7f976b");
        packageParams2.put("bank_type","BOC_DEBIT");
        packageParams2.put("cash_fee","10");
        packageParams2.put("fee_type","CNY");
        packageParams2.put("is_subscribe","Y");
        packageParams2.put("mch_id","1503031011");

        packageParams2.put("openid","oaCnbs6EiIYbXgc8aYlRRSlJvqGk");
        packageParams2.put("nonce_str","2324373739");

        packageParams2.put("out_trade_no","1532791477865");
        packageParams2.put("result_code","SUCCESS");
        packageParams2.put("return_code","SUCCESS");
        //packageParams.put("sign","94C42B64245D52032A18CBC2C31FF9AB");
        packageParams2.put("time_end","20180728232451");
        packageParams2.put("total_fee","10");
        packageParams2.put("trade_type","NATIVE");
        packageParams2.put("transaction_id","4200000150201807289324621765");
//
        System.out.println("create sign:"+PayCommonUtil.createSign(Common.UTF8, packageParams2, GlobalConfig.getProperties(Common.PAY_WECHAT_API_KEY)));
    }

    @Test
    public void insertBatch()throws Exception{
        SqlSession sqlSession = MybatisUtils.getSession();
        List<UserUploadFile> list = new ArrayList<>();
        UserUploadFile userUploadFile = new UserUploadFile(1, 1, null,
                Common.FILE_TYPE_IMAGE, Common.FOLDER_FILE, CommonService.getTimeStamp(), null, "folder",
                "", String.valueOf(0), null, CommonService.getDateTime());
        list.add(userUploadFile);

        sqlSession.insert(Mapper.INSERT_BATCH_NEW_RESOURCE, list);
        sqlSession.close();
    }

    @Test
    public void testIntegerParse() throws Exception{
        System.out.println(1+CommonService.getTimeStamp());
        Thread.sleep(1000);
        System.out.println(CommonService.getTimeStamp());
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






