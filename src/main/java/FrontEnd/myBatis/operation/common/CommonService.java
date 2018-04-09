package FrontEnd.myBatis.operation.common;

import FrontEnd.helper.common.Common;
import FrontEnd.helper.common.Mapper;
import FrontEnd.helper.common.OssOpt;
import FrontEnd.helper.config.GlobalConfig;
import FrontEnd.helper.test.TestResponseOpt;
import FrontEnd.helper.util.HttpUtil;
import FrontEnd.myBatis.entity.User;
import FrontEnd.myBatis.entity.UserUploadFile;
import FrontEnd.myBatis.entity.response.ResponseData;
import FrontEnd.myBatis.entity.response.StatusCode;
import com.alibaba.fastjson.JSON;
import com.aliyun.oss.OSSClient;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.junit.Assert;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/2/20.
 */
public class CommonService {

    private static Logger logger = Logger.getLogger(CommonService.class);

    /**
     * 返回时间戳公用方法
     *
     * @return
     */
    public static String getTimeStamp() {
        return String.valueOf(System.currentTimeMillis());
    }

    /**
     * 返回时间点公用方法
     *
     * @return
     */
    public static String getDateTime() {
        Date day = new Date();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return df.format(day);
    }

    /**
     * 打印描述时间方法，用于获取代码运行时间
     *
     * @param depict
     */
    public static void calculateTime(String depict) {
        CommonService.logger.debug("=====================" + "\n" + depict + ": " + getDateTime() + "\n");
    }

    /**
     * 进入http调用后打印request的方法名
     *
     * @param functionName http调用的方法名
     */
    public static void printHttpInvokeFunction(String functionName) {
        CommonService.logger.debug("Come into http request function: " + functionName);
    }


    /**
     * 对数据库进行后续提交和关闭操作
     *
     * @param sqlSession   sql数据库操作句柄
     * @param responseData api调用返回的数据
     * @param toCommit     是否做了需要数据库提交的操作
     */
    public static void databaseCommitClose(SqlSession sqlSession, ResponseData responseData, boolean toCommit) {
        //传入参数定义是否需要提交操作，纯粹数据库查询则不需提交操作
        if (toCommit) {
            //如果整个流程准确无误地实现则对数据库操作进行提交，否则不提交
            if (responseData.getStatus_code() == StatusCode.OK.getValue()) {
                sqlSession.commit();
            }
        }
        sqlSession.close(); //数据库操作完毕，关闭连接，释放资源
    }

    /**
     * 查看在redis数据库中该文件的引用如果为0则删除该资源在OSS中的文件
     *
     * @param userUploadFile 上传的文件
     */
//    @Deprecated
//    public static void deleteResourceOSSFile(UserUploadFile userUploadFile, OSSClient ossClient) {
//        //查看该文件在redis中的引用数
//        Jedis jedis = RedisJava.getInstance();
//        int refCount = Integer.parseInt(jedis.get(userUploadFile.getTime_stamp())) - 1;
//
//        if (refCount > 0) {
//            //如果引用数大于零说明还有其他项目引用该资源，则直接更新该条目数目即可
//            jedis.set(userUploadFile.getTime_stamp(), String.valueOf(refCount));
//
//        } else {
//            //如果引用数小于等于零，则直接在redis中删除该条目，并且删除OSS中对应文件
//            jedis.del(userUploadFile.getTime_stamp());
//            //删除OSS对应文件
//            String deleteFileName = GlobalConfig.getOssFileUrl(Common.UPLOAD_FILES) +
//                    userUploadFile.getTime_stamp() + "." + userUploadFile.getSuffix();
//            OssOpt.deleteFileInOss(deleteFileName,ossClient);
//        }
//    }

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


    /**
     * 进行http的request和response数据的断言
     *
     * @param map         发送请求数据
     * @param functionUri 发送请求基于baseUrl的方法请求
     * @param errorCode   验证errorCode
     */
    public static void junitReqRespVerify(Map<String, Object> map, String functionUri, int errorCode) {
        String result = HttpUtil.httpClientUploadFile(Common.BASE_HTTP_URL + functionUri, map);
        ResponseData responseData = JSON.parseObject(result, ResponseData.class);
        Assert.assertNotNull(responseData);
        if (responseData.getStatus_code() == 400) {
            Assert.assertEquals(responseData.getException_code(), errorCode);
        }
        CommonService.logger.debug(functionUri + " get result: " + result);
    }


    /**
     * 进行http的request和response数据的断言
     * 带有一个返回callback函数
     *
     * @param map         发送请求数据
     * @param functionUri 发送请求基于baseUrl的方法请求
     * @param errorCode   验证errorCode
     */
    public static void junitReqRespVerify(Map<String, Object> map, String functionUri, int errorCode,
                                          TestResponseOpt testResponseOpt) {
        String result = HttpUtil.httpClientUploadFile(Common.BASE_HTTP_URL + functionUri, map);
        ResponseData responseData = JSON.parseObject(result, ResponseData.class);
        Assert.assertNotNull(responseData);
        if (responseData.getStatus_code() == 400) {
            Assert.assertEquals(responseData.getException_code(), errorCode);
        }
        testResponseOpt.doResponseOpt(responseData);
        CommonService.logger.debug(functionUri + " get result: " + result);
    }
}

















