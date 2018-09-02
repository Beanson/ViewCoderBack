package viewcoder.operation.impl.render;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.nio.cs.US_ASCII;
import viewcoder.exception.render.RenderException;
import viewcoder.tool.common.Assemble;
import viewcoder.tool.common.Common;
import viewcoder.tool.common.Mapper;
import viewcoder.tool.common.OssOpt;
import viewcoder.tool.config.GlobalConfig;
import viewcoder.tool.parser.form.FormData;
import viewcoder.tool.util.MybatisUtils;
import viewcoder.operation.entity.Project;
import viewcoder.operation.entity.User;
import viewcoder.operation.entity.UserUploadFile;
import viewcoder.operation.entity.response.ResponseData;
import viewcoder.operation.entity.response.StatusCode;
import viewcoder.operation.impl.common.CommonService;
import com.aliyun.oss.OSSClient;
import io.netty.handler.codec.http.multipart.FileUpload;
import org.apache.ibatis.session.SqlSession;

import java.util.*;

/**
 * Created by Administrator on 2018/2/16.
 */
public class Render {

    private static Logger logger = LoggerFactory.getLogger(Render.class);


    /**
     * 根据项目id获取项目名称数据，用于子项目跳转访问前使用
     * @param msg
     * @return
     */
    public static ResponseData getProjectName(Object msg){
        ResponseData responseData = new ResponseData(StatusCode.ERROR.getValue());
        SqlSession sqlSession = MybatisUtils.getSession();
        String message = "";
        try {
            String id = FormData.getParam(msg, Common.ID);
            String projectName = sqlSession.selectOne(Mapper.GET_PROJECT_NAME, Integer.parseInt(id));
            Assemble.responseSuccessSetting(responseData, projectName);

        }catch (Exception e){
            message = "System error";
            Render.logger.error(message, e);
            Assemble.responseErrorSetting(responseData, 500, message);

        }finally {
            CommonService.databaseCommitClose(sqlSession, responseData, false);
        }
        return responseData;
    }

    /**
     * ****************************************************************************
     * 根据项目id获取该项目渲染数据的信息
     *
     * @param msg http请求数据
     * @return
     */
    public static ResponseData getProjectRenderData(Object msg) {
        ResponseData responseData = new ResponseData(StatusCode.ERROR.getValue());
        SqlSession sqlSession = MybatisUtils.getSession();
        OSSClient ossClient = OssOpt.initOssClient();
        String message = "";
        try {
            //从http中获取项目id数据
            Map<String, Object> data = FormData.getParam(msg);
            String projectId = (String) data.get(Common.ID);
            String userId = (String) data.get(Common.USER_ID);
            String version = (String) data.get(Common.VERSION);

            //初始化子项目数据
            List<Project> projectChildren = null;
            //从数据库中根据项目Id获取项目渲染数据
            Project project = sqlSession.selectOne(Mapper.GET_PROJECT_DATA, Integer.parseInt(projectId));

            if (project != null && project.getUser_id() == Integer.parseInt(userId)) {
                //获取目标project渲染数据
                String projectData = getProjectRenderDataHandler(project, ossClient, version);
                if(project.getChild()>0){
                    //获取以该projectId为parent的所有project的id和refId数据数据
                    projectChildren = sqlSession.selectList(Mapper.GET_PROJECT_CHILDREN_LIST, Integer.parseInt(projectId));
                }

                //检测OSS中读取的数据是否有效
                if (CommonService.checkNotNull(projectData)) {
                    project.setProject_data(projectData);
                    Map<String, Object> map = new HashMap<>(2);
                    map.put(Common.PROJECT, project);
                    map.put(Common.CHILDREN, projectChildren);
                    Assemble.responseSuccessSetting(responseData, map);

                } else {
                    message = "getProjectRenderData data from oss null";
                    Render.logger.warn(message);
                    Assemble.responseErrorSetting(responseData, 400, message);
                }
            } else {
                message = "project data from http null";
                Render.logger.warn(message);
                Assemble.responseErrorSetting(responseData, 401, message);
            }

        } catch (Exception e) {
            message = "System error";
            Render.logger.error(message, e);
            Assemble.responseErrorSetting(responseData, 500, message);

        } finally {
            CommonService.databaseCommitClose(sqlSession, responseData, true);
            OssOpt.shutDownOssClient(ossClient);
        }
        return responseData;
    }


    /**
     * 根据传入的手机版或电脑版，获取该project的渲染数据
     *
     * @param project   获取该页面的项目数据
     * @param ossClient oss句柄
     * @param version   记录手机版还是电脑版
     * @return
     */
    private static String getProjectRenderDataHandler(Project project, OSSClient ossClient,
                                                      String version) {
        //项目数据信息获取
        String projectData = null;

        //分别电脑版和手机版的OSS中文件路径信息
        String projectPCDataFile = GlobalConfig.getOssFileUrl(Common.PROJECT_DATA) +
                project.getPc_version() + Common.PROJECT_DATA_SUFFIX;
        String projectMODataFile = GlobalConfig.getOssFileUrl(Common.PROJECT_DATA) +
                project.getMo_version() + Common.PROJECT_DATA_SUFFIX;

        //根据version值对应获取电脑版或手机版
        if (Objects.equals(version, Common.MOBILE_V)) {
            projectData = OssOpt.getOssFile(ossClient, projectMODataFile);

        } else {
            projectData = OssOpt.getOssFile(ossClient, projectPCDataFile);
        }
        return projectData;
    }


    /**
     * ****************************************************************************
     * 用户获取所有对应类型的上传过的资源信息
     *
     * @param msg http请求数据
     * @return
     */
    public static ResponseData getUploadResource(Object msg) {
        ResponseData responseData = new ResponseData(StatusCode.ERROR.getValue());
        SqlSession sqlSession = MybatisUtils.getSession();
        String message = "";
        try {
            //从http请求中获取用户需要的resource文件信息，并查找数据库
            HashMap<String, Object> map = FormData.getParam(msg, Common.USER_ID, Common.FILE_TYPE);
            List<UserUploadFile> userUploadFiles = sqlSession.selectList(Mapper.GET_RESOURCE_BY_USERID_AND_FILETYPE, map);
            Assemble.responseSuccessSetting(responseData, userUploadFiles);

        } catch (Exception e) {
            message = "System error";
            Render.logger.debug(message, e);
            Assemble.responseErrorSetting(responseData, 500, message);

        } finally {
            CommonService.databaseCommitClose(sqlSession, responseData, false);
        }
        return responseData;
    }


    /**
     * ****************************************************************************
     * 试探上传资源前是否有足够容纳空间
     *
     * @param msg
     * @return
     */
    public static ResponseData uploadSpaceDetect(Object msg) {
        String message = "";
        ResponseData responseData = new ResponseData(StatusCode.ERROR.getValue());
        SqlSession sqlSession = MybatisUtils.getSession();
        try {
            UserUploadFile userUploadFile = (UserUploadFile) FormData.getParam(msg, UserUploadFile.class);
            User user = checkSpace(userUploadFile);

            if (user.getNewUserResSpace() > 0) {
                Assemble.responseSuccessSetting(responseData, null);

            } else {
                message = "No enough space";
                Render.logger.warn(message);
                Assemble.responseErrorSetting(responseData, 400, message);
            }

        } catch (Exception e) {
            message = "System error";
            Render.logger.error(message, e);
            Assemble.responseErrorSetting(responseData, 500, message);

        } finally {
            CommonService.databaseCommitClose(sqlSession, responseData, false);
        }
        return responseData;
    }

    /**
     * ****************************************************************************
     * 上传文件资源
     *
     * @param msg http请求数据
     * @return
     */
    public static ResponseData uploadResource(Object msg) {
        String message = "";
        ResponseData responseData = new ResponseData(StatusCode.ERROR.getValue());
        User user = new User();
        try {
            //获取用户上传资源文件信息
            UserUploadFile userUploadFile = (UserUploadFile) FormData.getParam(msg, UserUploadFile.class);

            if (userUploadFile.getIs_folder() != 1) {
                //查看检查是否有足够空间接收该上传文件资源
                user = checkSpace(userUploadFile);
                //如果接收资源后用户可用空间大于0则接收资源文件
                if (user.getNewUserResSpace() > 0) {
                    uploadResourceOpt(userUploadFile, responseData, user);

                } else {
                    message = "No enough space: " + user.getNewUserResSpace();
                    Render.logger.warn(message);
                    Assemble.responseErrorSetting(responseData, 403, message);
                }
            } else {
                //文件夹类型，无需检查空间直接上传
                uploadResourceOpt(userUploadFile, responseData, user);
            }

        } catch (Exception e) {
            message = "System error";
            Render.logger.error(message, e);
            Assemble.responseErrorSetting(responseData, 500, message);
        }
        return responseData;
    }


    /**
     * 检查是否有足够空间上传
     *
     * @param userUploadFile 将要上传的资源文件
     * @return
     */
    private static User checkSpace(UserUploadFile userUploadFile) {
        SqlSession sqlSession = MybatisUtils.getSession();
        int newUserResSpace = 0;
        String message = "";
        User user = new User();
        try {
            user = sqlSession.selectOne(Mapper.GET_USER_RESOURCE_SPACE_INFO, userUploadFile.getUser_id());
            int resourceRemain = user.getResource_total() - user.getResource_used();
            int fileSize = (int) Math.round(userUploadFile.getFile_size() / Common.FILE_SIZE_TO_KB); //设置以KB为单位的资源空间
            userUploadFile.setFile_size(fileSize);
            newUserResSpace = resourceRemain - fileSize;
            user.setNewUserResSpace(newUserResSpace);

        } catch (Exception e) {
            message = "System error";
            Render.logger.error(message, e);

        } finally {
            sqlSession.close();
        }
        return user;
    }


    /**
     * 上传文件逻辑操作
     *
     * @param userUploadFile 上传的文件对象信息
     * @param responseData   返回数据组装
     * @param user           装载newResourceSpace的user对象
     * @throws Exception
     */
    private static void uploadResourceOpt(UserUploadFile userUploadFile, ResponseData responseData,
                                          User user) throws Exception {
        String message = "";
        //A. 上传到oss中---------------------------------------------------------------------
        if (userUploadFile.getIs_folder() != 1) {
            OSSClient ossClient = OssOpt.initOssClient();
            try {
                String fileName = GlobalConfig.getOssFileUrl(Common.UPLOAD_FILES) + userUploadFile.getTime_stamp() +
                        Common.DOT_SUFFIX + userUploadFile.getSuffix();
                OssOpt.uploadFileToOss(fileName, userUploadFile.getFile().get(), ossClient);

            } catch (Exception e) {
                message = "System error";
                Render.logger.error(message);
                throw new Exception(message);

            } finally {
                OssOpt.shutDownOssClient(ossClient);
            }
        }

        //B. 数据库更新操作，因为上传到oss步骤是耗时步骤，因此sql数据库直到该步骤才开启-------------------
        SqlSession sqlSession = MybatisUtils.getSession();
        try {
            //插入数据库操作
            int influence_num = sqlSession.insert(Mapper.INSERT_NEW_RESOURCE, userUploadFile);
            //根据插入结果返回相应数据
            if (influence_num > 0) {
                //准备返回的结果数据
                Map<String, Object> backData = new HashMap<>();
                backData.put(Common.ID, userUploadFile.getId());

                //非文件夹则更新占用空间数据
                if (userUploadFile.getIs_folder() != 1) {
                    //更新用户resource_used大小
                    int newResourceUsed = user.getResource_used() + userUploadFile.getFile_size();
                    user.setResource_used(newResourceUsed);
                    int userUpdateNum = sqlSession.update(Mapper.UPDATE_USER_RESOURCE_SPACE_USED, user);

                    //返回成功信息
                    if (userUpdateNum > 0) {
                        backData.put("user_resource_remain", user.getNewUserResSpace());
                        Assemble.responseSuccessSetting(responseData, backData);

                    } else {
                        message = "update resource size db error:" + userUpdateNum;
                        Render.logger.warn(message);
                        Assemble.responseErrorSetting(responseData, 432, message);
                    }

                } else {
                    //新建文件夹resource，返回成功code
                    Assemble.responseSuccessSetting(responseData, backData);
                }
            } else {
                //新建resource插入数据库失败，返回402
                message = "Insert resource to database error";
                Render.logger.error(message);
                Assemble.responseErrorSetting(responseData, 402, message);
            }

        } catch (Exception e) {
            message = "System error";
            Render.logger.error(message, e);
            Assemble.responseErrorSetting(responseData, 412, message);

        } finally {
            CommonService.databaseCommitClose(sqlSession, responseData, true);
        }
    }


    /**
     * ****************************************************************************
     * 重命名对应id的resource文件
     *
     * @param msg http上传数据
     * @return
     */
    public static ResponseData renameResource(Object msg) {
        ResponseData responseData = new ResponseData(StatusCode.ERROR.getValue());
        SqlSession sqlSession = MybatisUtils.getSession();
        String message = "";
        try {
            //获取要重命名resource的id，和resource的名称
            HashMap<String, Object> map = FormData.getParam(msg);
            int influence_num = sqlSession.update(Mapper.RENAME_RESOURCE_BY_ID, map);
            if (influence_num > 0) {
                //如果更新数据库成功则成功返回
                Assemble.responseSuccessSetting(responseData, null);

            } else {
                message = "update database error";
                Render.logger.warn(message);
                Assemble.responseErrorSetting(responseData, 401, message);
            }

        } catch (Exception e) {
            message = "System error";
            Render.logger.error(message, e);
            Assemble.responseErrorSetting(responseData, 500, message);

        } finally {
            CommonService.databaseCommitClose(sqlSession, responseData, true);
        }
        return responseData;
    }


    /**
     * ****************************************************************************
     * 删除对应id的resource
     *
     * @param msg http上传数据
     * @return
     */
    public static ResponseData deleteResource(Object msg) {
        ResponseData responseData = new ResponseData(StatusCode.ERROR.getValue());
        SqlSession sqlSession = MybatisUtils.getSession();
        OSSClient ossClient = null;
        String message = "";
        try {
            //获取要删除的resource的id，project_id和user_id,后两个参数做校验
            Map<String, Object> map = FormData.getParam(msg);
            int id = Integer.parseInt(String.valueOf(map.get(Common.ID)));
            int userId = Integer.parseInt(String.valueOf(map.get(Common.USER_ID)));

            //记录删除资源数目条目的记录
            Map<String, Integer> record = new HashMap<>(3);
            record.put(Common.RESOURCE_DELETE_DB_NUM, 0);
            record.put(Common.RESOURCE_DELETE_SIZE, 0);

            //即将删除的oss资源数据装载，一起batch删除操作
            List<String> widgetList = new ArrayList<>();

            //A. 获取数据库中该资源相关信息
            UserUploadFile userUploadFile = sqlSession.selectOne(Mapper.GET_RESOURCE_DATA, id);
            //删除该资源在oss中的占用和数据库中信息
            deleteResourceLogic(userUploadFile, sqlSession, record, widgetList);

            //B. OSS删除resource文件
            ossClient = OssOpt.initOssClient(); //延迟实例化
            OssOpt.deleteFileInOssBatch(widgetList, ossClient);

            //C. 更新删除资源后user表对应的空间
            updateDelResRemain(sqlSession, userId, record);

            //返回成功数据
            Assemble.responseSuccessSetting(responseData, null);

        } catch (Exception e) {
            message = "System error";
            Render.logger.error(message, e);
            Assemble.responseErrorSetting(responseData, 500, message);

        } finally {
            CommonService.databaseCommitClose(sqlSession, responseData, true);
            OssOpt.shutDownOssClient(ossClient);
        }
        return responseData;
    }


    /**
     * 删除OSS中的resource操作
     *
     * @param userUploadFile 删除的resource信息
     * @param sqlSession     sql句柄
     * @param record         记录删除资源数据
     */
    private static void deleteResourceLogic(UserUploadFile userUploadFile, SqlSession sqlSession,
                                            Map<String, Integer> record, List<String> widgetList) throws RenderException {
        int deleteNum = 0;

        //根据该文件本身是文件夹还是文件进行相应操作
        if (userUploadFile.getIs_folder() ==0) {
            //如果删除的文件资源是文件, a. 删除文件在数据库user_upload_file的; 2. 装载对应的OSS文件，后续删除
            deleteNum = sqlSession.delete(Mapper.DELETE_RESOURCE_BY_ID, userUploadFile.getId());
            OssOpt.addToOssDeleteList(userUploadFile, widgetList);
            //添加将要删除的资源空间大小
            record.put(Common.RESOURCE_DELETE_SIZE, record.get(Common.RESOURCE_DELETE_SIZE) + userUploadFile.getFile_size());

        } else {
            //如果删除的文件资源是文件夹则进行级联删除
            //设置进入文件夹后的relative_path路径
            userUploadFile.setRelative_path(userUploadFile.getRelative_path() + userUploadFile.getFile_name() + "/");
            //查找数据库中所有该relative_path路径下文件
            List<UserUploadFile> list = sqlSession.selectList(Mapper.GET_FOLDER_SUB_RESOURCE, userUploadFile);
            //删除文件夹在数据库user_upload_file的条目
            deleteNum = sqlSession.delete(Mapper.DELETE_RESOURCE_BY_ID, userUploadFile.getId());

            //循环该文件夹下所有资源文件并递归执行上述操作
            for (UserUploadFile eachUploadFile : list) {
                deleteResourceLogic(eachUploadFile, sqlSession, record, widgetList);
            }
        }
        //数据库删除记录操作数目添加
        record.put(Common.RESOURCE_DELETE_DB_NUM, record.get(Common.RESOURCE_DELETE_DB_NUM) + deleteNum);
    }

    /**
     * 删除资源后对User和Project的资源空间的更新
     *
     * @param sqlSession sql句柄
     * @param userId     用户id
     * @param record     记录删除资源空间大小和数量等信息
     */
    private static void updateDelResRemain(SqlSession sqlSession, int userId, Map<String, Integer> record) {
        //更新User的已使用空间
        Map<String, Integer> map = new HashMap<>(2);
        map.put(Common.USER_ID, userId);
        map.put(Common.RESOURCE_USED, record.get(Common.RESOURCE_DELETE_SIZE));
        int userUpdateNum = sqlSession.update(Mapper.REDUCE_USER_RESOURCE_SPACE_USED, map);
    }


    /**
     * ****************************************************************************
     * 上传video的一帧截图
     * 视频图片更新数据库操作，之前只上传处理了视频，
     * 还未添加媒体截图，现添加视频截图到数据库
     *
     * @param msg http上传数据
     * @return 返回视频某一帧图片的URL
     */
    public static ResponseData uploadVideoImage(Object msg) {
        ResponseData responseData = new ResponseData(StatusCode.ERROR.getValue());
        SqlSession sqlSession = MybatisUtils.getSession();
        OSSClient ossClient = OssOpt.initOssClient();
        String message = "";
        try {
            HashMap<String, Object> videoImage = FormData.getParam(msg);
            //获取视频截帧的帧文件信息
            FileUpload fileUpload = (FileUpload) videoImage.get(Common.VIDEO_IMAGE);
            int num = sqlSession.update(Mapper.UPDATE_VIDEO_IMAGE, videoImage);

            if (num > 0) {
                //如果添加视频截帧到数据库成功，进行上传该截帧文件
                String uploadFileName = GlobalConfig.getOssFileUrl(Common.UPLOAD_FILES) +
                        videoImage.get(Common.VIDEO_IMAGE_NAME);
                OssOpt.uploadFileToOss(uploadFileName, fileUpload.get(), ossClient);
                Assemble.responseSuccessSetting(responseData, null);

            } else {
                message = "uploadVideoImage insert db error";
                Render.logger.warn(message);
                Assemble.responseErrorSetting(responseData, 401, message);
            }

        } catch (Exception e) {
            message = "System error";
            Render.logger.error(message, e);
            Assemble.responseErrorSetting(responseData, 500, message);

        } finally {
            CommonService.databaseCommitClose(sqlSession, responseData, true);
            OssOpt.shutDownOssClient(ossClient);
        }
        return responseData;
    }


    /**
     * 更新project数据和时间
     *
     * @param msg http上传数据
     * @return
     */
    public static ResponseData saveProjectData(Object msg) {
        ResponseData responseData = new ResponseData(StatusCode.ERROR.getValue());
        SqlSession sqlSession = MybatisUtils.getSession();
        OSSClient ossClient = OssOpt.initOssClient();
        String message = "";
        try {
            Project project = (Project) FormData.getParam(msg, Project.class);
            int num = sqlSession.update(Mapper.SAVE_PROJECT_DATA, project);

            if (num > 0) {
                //根据version值对应同步不同oss文件
                String projectDataFile = null;
                if (Objects.equals(project.getVersion(), Common.MOBILE_V)) {
                    //移动端版本
                    projectDataFile = GlobalConfig.getOssFileUrl(Common.PROJECT_DATA) + project.getMo_version() + Common.PROJECT_DATA_SUFFIX;
                } else {
                    //PC端版本
                    projectDataFile = GlobalConfig.getOssFileUrl(Common.PROJECT_DATA) + project.getPc_version() + Common.PROJECT_DATA_SUFFIX;
                }
                //创建新的project_data数据并同步到OSS中
                OssOpt.uploadFileToOss(projectDataFile, project.getProject_data().getBytes(), ossClient);
                Assemble.responseSuccessSetting(responseData, null);

            } else {
                message = "saveProjectData db error";
                Render.logger.warn(message);
                Assemble.responseErrorSetting(responseData, 401, message);
            }

        } catch (Exception e) {
            message = "System error";
            Render.logger.error(message, e);
            Assemble.responseErrorSetting(responseData, 500, message);

        } finally {
            CommonService.databaseCommitClose(sqlSession, responseData, true);
            OssOpt.shutDownOssClient(ossClient);
        }
        return responseData;
    }


    /**
     * 更新默认的导出设置操作
     *
     * @param msg 传入的信息体
     * @return
     */
    public static ResponseData updateExportDefaultSetting(Object msg) {
        ResponseData responseData = new ResponseData(StatusCode.ERROR.getValue());
        SqlSession sqlSession = MybatisUtils.getSession();
        String message = "";
        try {
            User user = (User) FormData.getParam(msg, User.class);
            int num = sqlSession.update(Mapper.UPDATE_EXPORT_DEFAULT_SETTING, user);

            if (num > 0) {
                //更新成功，则返回成功
                Assemble.responseSuccessSetting(responseData, null);

            } else {
                message = "Db update error";
                Render.logger.warn(message);
                Assemble.responseErrorSetting(responseData, 401, message);
            }

        } catch (Exception e) {
            message = "System error";
            Render.logger.error(message, e);
            Assemble.responseErrorSetting(responseData, 500, message);

        } finally {
            CommonService.databaseCommitClose(sqlSession, responseData, true);
        }
        return responseData;
    }


}
