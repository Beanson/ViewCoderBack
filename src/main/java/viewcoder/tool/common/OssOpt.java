package viewcoder.tool.common;

import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.*;
import org.apache.ibatis.session.SqlSession;
import viewcoder.operation.entity.UserUploadFile;
import viewcoder.operation.impl.common.CommonService;
import viewcoder.tool.config.GlobalConfig;
import viewcoder.tool.encrypt.AESEncryptor;
import com.aliyun.oss.OSSClient;
import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/2/17.
 */
public class OssOpt {

    private static Logger logger = Logger.getLogger(OssOpt.class.getName());

    // endpoint以杭州为例，其它region请按实际情况填写
    //TODO 上ECS后把 END_POINT 设置为ECS处的连接
    private static final String END_POINT = "com.viewcoder.oss.endpoint.outer";
    //private static final String END_POINT = "com.viewcoder.oss.endpoint.inner";
    // 云账号AccessKey有所有API访问权限，建议遵循阿里云安全最佳实践，创建并使用RAM子账号进行API访问或日常运维，请登录 https://ram.console.aliyun.com 创建
    private static final String VIEWCODER_BUCKET = "viewcoder-bucket";

    /************************ 初始化和关闭ossClient流 *********************/
    /**
     * 初始化OSSClient客户端
     *
     * @return 返回OSSClient的实例
     */
    public static OSSClient initOssClient() {
        return new OSSClient(GlobalConfig.getProperties(END_POINT), Common.ALI_ACCESSKEY_ID, Common.ALI_ACCESSKEY_SECRET);
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
        PutObjectResult result = ossClient.putObject(VIEWCODER_BUCKET, fileName, new ByteArrayInputStream(content));
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

    /**
     * 查看在数据库中该文件的引用如果为0则删除该资源在OSS中的文件
     *
     * @param userUploadFile 上传的文件
     */
    public static void deleteResourceOSSFile(UserUploadFile userUploadFile, OSSClient ossClient, SqlSession sqlSession) {
        //查看该文件在数据库中的引用数
        int refCount = sqlSession.selectOne(Mapper.GET_RESOURCE_REF_COUNT, userUploadFile.getTime_stamp());
        //如果无记录再引用该资源，则允许删除操作
        if (refCount <= 0) {
            //如果数据库中无该字段的其他引用，则删除OSS对应文件
            String deleteFileName = GlobalConfig.getOssFileUrl(Common.UPLOAD_FILES) +
                    userUploadFile.getTime_stamp() + "." + userUploadFile.getSuffix();
            OssOpt.deleteFileInOss(deleteFileName, ossClient);

            //如果video_image_name不为空，且file_type为2，则也删除对应的video_image
            if (userUploadFile.getVideo_image_name() != null && !userUploadFile.getVideo_image_name().isEmpty() &&
                    userUploadFile.getFile_type() == 2) {
                String deleteVideoName = GlobalConfig.getOssFileUrl(Common.UPLOAD_FILES) +
                        userUploadFile.getVideo_image_name();
                //oss删除video图片资源
                OssOpt.deleteFileInOss(deleteVideoName, ossClient);
            }
        }
    }

    /**
     * 批量删除OSS文件
     *
     * @param list       即将删除的组件的列表
     * @param sqlSession sql句柄
     * @param ossClient  oss句柄
     */
    public static void deleteResourceBatch(List<UserUploadFile> list, SqlSession sqlSession, OSSClient ossClient) {
        //装载到即将删除列表中
        List<String> widgetList = new ArrayList<>();
        List<String> videoImageList = new ArrayList<>();
        for (UserUploadFile userUploadFile :
                list) {
            //查看该文件在数据库中的引用数
            int refCount = sqlSession.selectOne(Mapper.GET_RESOURCE_REF_COUNT, userUploadFile.getTime_stamp());
            //如果引用计数为0则添加该文件名到即将删除列表
            if (refCount <= 0) {
                widgetList.add(GlobalConfig.getOssFileUrl(Common.UPLOAD_FILES) +
                        userUploadFile.getTime_stamp() + "." + userUploadFile.getSuffix());

                if (userUploadFile.getVideo_image_name() != null && !userUploadFile.getVideo_image_name().isEmpty() &&
                        userUploadFile.getFile_type() == 2) {
                    videoImageList.add(GlobalConfig.getOssFileUrl(Common.UPLOAD_FILES) + userUploadFile.getVideo_image_name());
                }
            }
        }
        //批量删除资源文件
        OssOpt.deleteFileInOssBatch(widgetList, ossClient);
        //批量删除video图片资源文件
        OssOpt.deleteFileInOssBatch(videoImageList, ossClient);
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

    /**
     * 拷贝OSS文件
     *
     * @param ossClient      oss句柄
     * @param sourceFileName 原文件名
     * @param destFileName   目标文件名
     */
    public static void copyProject(OSSClient ossClient, String sourceFileName, String destFileName) {
        OssOpt.copyObjectOss(ossClient, sourceFileName, destFileName);
    }


    /*************************** 查看资源文件是否存在方法 *************************/
    /**
     * 查看资源是否存在方法
     *
     * @param ossClient oss句柄
     * @param fileName  存储在oss中的文件名
     * @return
     */
    public static boolean getObjectExist(OSSClient ossClient, String fileName) {
        boolean found = ossClient.doesObjectExist(VIEWCODER_BUCKET, fileName);
        OssOpt.logger.debug("===get Object exist: " + found);
        return found;
    }


    /***************************** 设置文件的ACK操作 *********************************/

    /**
     * 更新文件的ACL设置
     *
     * @param ossClient
     * @param openness
     */
    public static void updateAclConfig(OSSClient ossClient, String prefix, boolean openness) {
        // 构造ListObjectsRequest请求
        ListObjectsRequest listObjectsRequest = new ListObjectsRequest(VIEWCODER_BUCKET);
        listObjectsRequest.setPrefix(prefix);
        // 递归列出fun目录下的所有文件
        ObjectListing listing = ossClient.listObjects(listObjectsRequest);
        // 遍历该文件夹下的所有文件
        for (OSSObjectSummary objectSummary : listing.getObjectSummaries()) {
            //根据是否公开设置其访问权限
            if (openness) {
                ossClient.setObjectAcl(VIEWCODER_BUCKET, objectSummary.getKey(), CannedAccessControlList.PublicRead);
            } else {
                ossClient.setObjectAcl(VIEWCODER_BUCKET, objectSummary.getKey(), CannedAccessControlList.Private);
            }

        }
    }

}








