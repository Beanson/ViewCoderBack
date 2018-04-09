package FrontEnd.helper.common;

import FrontEnd.helper.config.GlobalConfig;
import FrontEnd.helper.encrypt.AESEncryptor;
import FrontEnd.myBatis.entity.response.ResponseData;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.CopyObjectResult;
import com.aliyun.oss.model.DeleteObjectsRequest;
import com.aliyun.oss.model.DeleteObjectsResult;
import com.aliyun.oss.model.PutObjectResult;
import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.List;

/**
 * Created by Administrator on 2018/2/17.
 */
public class OssOpt {

    private static Logger logger = Logger.getLogger(OssOpt.class.getName());

    // endpoint以杭州为例，其它region请按实际情况填写
    //TODO 上ECS后把 END_POINT 设置为ECS处的连接
    private static final String END_POINT = "com.viewcoder.oss.endpoint.outer";
    // 云账号AccessKey有所有API访问权限，建议遵循阿里云安全最佳实践，创建并使用RAM子账号进行API访问或日常运维，请登录 https://ram.console.aliyun.com 创建
    private static final String ACCESS_KEY_ID = "com.viewcoder.oss.access.key";
    private static final String ACCESS_KEY_SECRET = "com.viewcoder.oss.access.secret";
    private static final String VIEWCODER_BUCKET = "viewcoder-bucket";


    /************************ 初始化和关闭ossClient流 *********************/
    /**
     * 初始化OSSClient客户端
     *
     * @return 返回OSSClient的实例
     */
    public static OSSClient initOssClient() {
        return new OSSClient(GlobalConfig.getProperties(END_POINT),
                AESEncryptor.AESDncode(Common.AES_KEY, GlobalConfig.getProperties(ACCESS_KEY_ID)),
                AESEncryptor.AESDncode(Common.AES_KEY, GlobalConfig.getProperties(ACCESS_KEY_SECRET)));
    }

    /**
     * 对OSSClient客户端进行关闭
     *
     * @param ossClient 客户端句柄
     */
    public static void shutDownOssClient(OSSClient ossClient) {
        if (ossClient != null) {
            ossClient.shutdown();
        }

    }


    /**************************** 上传资源文件方法 *************************/
    /**
     * 上传文件资源
     *
     * @param content  byte类型的上传文件流
     * @param fileName 上传文件名称
     */
    public static void uploadFileToOss(String fileName, byte[] content, OSSClient ossClient) {
        OssOpt.logger.debug("===upload resource file to common: " + fileName);
        PutObjectResult result=ossClient.putObject(VIEWCODER_BUCKET, fileName, new ByteArrayInputStream(content));
    }

    /**
     * 上传文件资源
     *
     * @param inputStream 上传文件流
     * @param fileName    上传文件名称
     */
    public static void uploadFileToOss(String fileName, InputStream inputStream, OSSClient ossClient) {
        OssOpt.logger.debug("===upload resource file to common: " + fileName);
        ossClient.putObject(VIEWCODER_BUCKET, fileName, inputStream);
    }

    /**
     * 上传文件资源
     *
     * @param file     文件类型
     * @param fileName 上传文件名称
     */
    public static void uploadFileToOss(String fileName, File file, OSSClient ossClient) {
        OssOpt.logger.debug("===upload resource file to common: " + fileName);
        ossClient.putObject(VIEWCODER_BUCKET, fileName, file);
    }


    /**************************** 删除资源文件方法 ***************************/
    /**
     * 删除在OSS上对应文件名的的文件
     *
     * @param fileName 要删除的文件名
     */
    public static void deleteFileInOss(String fileName, OSSClient ossClient) {
        OssOpt.logger.debug("===delete resource file in common: " + fileName);
        ossClient.deleteObject(VIEWCODER_BUCKET, fileName);
    }

    /**
     * 级联删除在OSS上对应文件名的的文件
     *
     * @param list 要删除的文件list
     */
    public static void deleteFileInOssBatch(List<String> list, OSSClient ossClient) {
        if (list != null && list.size() > 0) {
            DeleteObjectsResult deleteObjectsResult = ossClient.deleteObjects(
                    new DeleteObjectsRequest(VIEWCODER_BUCKET).withKeys(list));
            List<String> deletedObjects = deleteObjectsResult.getDeletedObjects();
            OssOpt.logger.debug("===delete batch resource files in common: \n" +
                    "list list: " + list + "\n" +
                    "delete result: " + deletedObjects + "\n");
        }
    }


    /**************************** 拷贝资源文件方法 ***************************/
    /**
     * 拷贝OSS文件
     *
     * @param ossClient      oss句柄
     * @param sourceFileName 原文件名
     * @param destFileName   目标文件名
     */
    public static void copyObjectOss(OSSClient ossClient, String sourceFileName, String destFileName) {
        // 拷贝Object
        CopyObjectResult result = ossClient.copyObject(VIEWCODER_BUCKET, sourceFileName, VIEWCODER_BUCKET, destFileName);
        OssOpt.logger.debug("===Copy resource file: source: " + sourceFileName + ", dest: " + destFileName +
                "ETag: " + result.getETag() + ", LastModified: " + result.getLastModified());
    }


    /*************************** 查看资源文件是否存在方法 *************************/
    /**
     * 查看资源是否存在方法
     * @param ossClient oss句柄
     * @param fileName 存储在oss中的文件名
     * @return
     */
    public static boolean getObjectExist(OSSClient ossClient, String fileName){
        boolean found= ossClient.doesObjectExist(VIEWCODER_BUCKET,fileName);
        OssOpt.logger.debug("===get Object exist: "+found);
        return found;
    }
}








