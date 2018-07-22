package viewcoder.operation.impl.render;

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
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by Administrator on 2018/2/16.
 */
public class Render {

    private static Logger logger = Logger.getLogger(Render.class);


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
        try {
            //从http中获取项目id数据
            Map<String, Object> data = FormData.getParam(msg);
            String projectId = (String) data.get(Common.ID);
            String userId = (String) data.get(Common.USER_ID);
            String version = (String) data.get(Common.VERSION);
            //从数据库中根据项目Id获取项目渲染数据
            Project project = sqlSession.selectOne(Mapper.GET_PROJECT_DATA, Integer.parseInt(projectId));
            if (project != null && project.getUser_id() == Integer.parseInt(userId)) {
                //获取的结果数据记录初始化
                Map<String, Object> map = new HashMap<>(2);
                map.put(Common.VERSION, Common.PC_V);
                String projectData = getProjectRenderDataHandler(map, project, ossClient, version);

                //检测OSS中读取的数据是否有效
                if (CommonService.checkNotNull(projectData)) {
                    project.setProject_data(projectData);
                    map.put(Common.PROJECT, project);
                    Assemble.responseSuccessSetting(responseData, map);

                } else {
                    Assemble.responseErrorSetting(responseData, 400,
                            "RenderException getProjectRenderData: project data from oss null");
                }
            } else {
                Assemble.responseErrorSetting(responseData, 401,
                        "RenderException getProjectRenderData: project null");
            }
        } catch (Exception e) {
            Render.logger.error("===RenderException getProjectRenderData with error: ", e);
            Assemble.responseErrorSetting(responseData, 500,
                    "RenderException getProjectRenderData: system error");

        } finally {
            //对数据库进行后续提交和关闭操作等
            CommonService.databaseCommitClose(sqlSession, responseData, true);
            OssOpt.shutDownOssClient(ossClient);
        }
        return responseData;
    }


    /**
     * 根据传入的手机版或电脑版，获取该project的渲染数据
     *
     * @param map       返回数据map
     * @param project   获取该页面的项目数据
     * @param ossClient oss句柄
     * @param version   记录手机版还是电脑版
     * @return
     */
    private static String getProjectRenderDataHandler(Map<String, Object> map, Project project, OSSClient ossClient,
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
            //如果手机版为空则从新获取电脑版数据
            if (!CommonService.checkNotNull(projectData)) {
                projectData = OssOpt.getOssFile(ossClient, projectPCDataFile);
            } else {
                map.put(Common.VERSION, Common.MOBILE_V);
            }
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

        try {
            //从http请求中获取用户需要的resource文件信息，并查找数据库
            HashMap<String, Object> map = FormData.getParam(msg, Common.USER_ID, Common.FILE_TYPE);
            List<UserUploadFile> userUploadFiles = sqlSession.selectList(Mapper.GET_RESOURCE_BY_USERID_AND_FILETYPE, map);
            Assemble.responseSuccessSetting(responseData, userUploadFiles);

        } catch (Exception e) {
            Render.logger.debug("===RenderException getUploadResource with error: " + e);
            Assemble.responseErrorSetting(responseData, 500,
                    "RenderException getUploadResource system error");

        } finally {
            //对数据库进行后续提交和关闭操作等
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

        ResponseData responseData = new ResponseData(StatusCode.ERROR.getValue());
        SqlSession sqlSession = MybatisUtils.getSession();
        OSSClient ossClient = OssOpt.initOssClient();

        try {
            //获取用户上传资源文件信息
            UserUploadFile userUploadFile = (UserUploadFile) FormData.getParam(msg, UserUploadFile.class);
            long newUserResSpace = 0;
            if (userUploadFile.getIs_folder() != 1) {
                //检查是否有足够空间接收该上传文件资源
                String resourceRemain = sqlSession.selectOne(Mapper.GET_USER_RESOURCE_SPACE_REMAIN, userUploadFile.getUser_id());
                newUserResSpace = Integer.parseInt(resourceRemain) - userUploadFile.getFile().length();
                //如果接收资源后用户可用空间大于0则接收资源文件
                if (newUserResSpace > 0) {
                    uploadResourceOpt(sqlSession, userUploadFile, ossClient, responseData, newUserResSpace);
                } else {
                    Assemble.responseErrorSetting(responseData, 403,
                            "User has not enough space");
                }
            } else {
                uploadResourceOpt(sqlSession, userUploadFile, ossClient, responseData, newUserResSpace);
            }

        } catch (Exception e) {
            Render.logger.debug("===RenderException uploadResource with error: " + e);
            Assemble.responseErrorSetting(responseData, 500,
                    "RenderException uploadResource system error");

        } finally {
            //对数据库进行后续提交和关闭操作等
            CommonService.databaseCommitClose(sqlSession, responseData, true);
            OssOpt.shutDownOssClient(ossClient);
        }
        return responseData;
    }

    /**
     * 上传文件逻辑操作
     *
     * @param sqlSession      sql句柄
     * @param userUploadFile  上传的文件对象信息
     * @param ossClient       oss句柄
     * @param responseData    返回数据组装
     * @param newUserResSpace 预添加后的空间
     * @throws Exception
     */
    private static void uploadResourceOpt(SqlSession sqlSession, UserUploadFile userUploadFile, OSSClient ossClient,
                                          ResponseData responseData, long newUserResSpace) throws Exception {
        //资源文件插入数据库操作
        int influence_num = sqlSession.insert(Mapper.INSERT_NEW_RESOURCE, userUploadFile);
        //如果上传文件插入数据库成功则将该文件存OSS
        if (influence_num > 0) {
            //后续操作成功后将返回backData
            Map<String, Object> backData = new HashMap<>();
            backData.put("id", userUploadFile.getId());

            if (userUploadFile.getIs_folder() != 1) {
                //文件保存文件到OSS
                String fileName = GlobalConfig.getOssFileUrl(Common.UPLOAD_FILES) + userUploadFile.getTime_stamp() +
                        "." + userUploadFile.getSuffix();
                OssOpt.uploadFileToOss(fileName, userUploadFile.getFile().get(), ossClient);

                //更新用户resource_remain大小
                int userUpdateNum = sqlSession.update(Mapper.UPDATE_USER_RESOURCE_SPACE_REMAIN,
                        new User(userUploadFile.getUser_id(), String.valueOf(newUserResSpace)));
                //更新project的占用空间大小的数据
                String projectResSpace = sqlSession.selectOne(Mapper.GET_PROJECT_RESOURCE_SIZE, userUploadFile.getProject_id());
                long newProjectResSpace = Integer.parseInt(projectResSpace) + userUploadFile.getFile().length();
                int projectUpdateNum = sqlSession.update(Mapper.UPDATE_PROJECT_RESOURCE_SIZE,
                        new Project(userUploadFile.getProject_id(), String.valueOf(newProjectResSpace)));

                //返回成功信息
                if (userUpdateNum > 0 && projectUpdateNum > 0) {
                    backData.put("user_resource_remain", newUserResSpace);
                    backData.put("project_resource_space", newProjectResSpace);
                    Assemble.responseSuccessSetting(responseData, backData);
                }
            } else {
                //新建文件夹resource，返回成功code
                Assemble.responseSuccessSetting(responseData, backData);
            }
        } else {
            //新建resource插入数据库失败，返回402
            Assemble.responseErrorSetting(responseData, 402,
                    "Insert resource to database error");
        }
    }


//    /**
//     * 保存文件到磁盘中
//     *
//     * @param userUploadFile 用户上传的文件信息
//     * @return
//     */
//    @Deprecated
//    private static boolean saveFileToDisk(UserUploadFile userUploadFile) {
//        //初始化返回保存文件状态为false
//        boolean saveFileStatus = false;
//        try {
//            //上传的文件保存到文件系统中, 重新命名文件的文件名
//            String new_file_path = GlobalConfig.getSysFileUrl(Common.UPLOAD_FILES) + "/" + userUploadFile.getTime_stamp() + "." + userUploadFile.getSuffix();
//            File file = new File(new_file_path);
//
//            //把上传的文件复制到新建的文件中，并返回该文件信息
//            if (userUploadFile.getFile().renameTo(file)) {
//                saveFileStatus = true;
//            }
//        } catch (Exception e) {
//            RenderException.logger.debug("===RenderException saveFileToDisk with exception: " + e);
//        }
//        return saveFileStatus;
//    }


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

        try {
            //获取要重命名resource的id，和resource的名称
            HashMap<String, Object> map = FormData.getParam(msg, Common.ID, Common.NEW_FILE_NAME);
            //更新资源名称到数据库操作
            int influence_num = sqlSession.update(Mapper.RENAME_RESOURCE_BY_ID, map);
            if (influence_num > 0) {
                //如果更新数据库成功则成功返回
                Assemble.responseSuccessSetting(responseData, null);

            } else {
                //否则返回401 错误代号
                Assemble.responseErrorSetting(responseData, 401,
                        "RenderException renameResource update database error");
            }
        } catch (Exception e) {
            Render.logger.error("RenderException renameResource: ", e);
            Assemble.responseErrorSetting(responseData, 500,
                    "RenderException renameResource system error");

        } finally {
            //对数据库进行后续提交和关闭操作等
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
        OSSClient ossClient = OssOpt.initOssClient();

        try {
            //获取要删除的resource的id，project_id和user_id,后两个参数做校验
            Map<String, Object> map = FormData.getParam(msg, Common.PROJECT_ID, Common.USER_ID, Common.ID);
            int id = Integer.parseInt(String.valueOf(map.get(Common.ID)));
            int user_id = Integer.parseInt(String.valueOf(map.get(Common.USER_ID)));
            int project_id = Integer.parseInt(String.valueOf(map.get(Common.PROJECT_ID)));

            //验证删除resource要求是否合法
            Project project = sqlSession.selectOne(Mapper.GET_PROJECT_DATA, project_id);
            //如果确认该user有对应project的resource后开始删除操作
            if (project.getUser_id() == user_id) {
                UserUploadFile userUploadFile = sqlSession.selectOne(Mapper.GET_RESOURCE_DATA, id);
                //删除该资源在oss中的占用
                deleteResourceLogic(userUploadFile, responseData, ossClient, sqlSession);

            } else {
                Assemble.responseErrorSetting(responseData, 402,
                        "deleteResource invalid delete opt");
            }
        } catch (Exception e) {
            Render.logger.error("RenderException deleteResource error: ", e);
            Assemble.responseErrorSetting(responseData, 500,
                    "RenderException deleteResource system error");
        } finally {
            //对数据库进行后续提交和关闭操作等
            CommonService.databaseCommitClose(sqlSession, responseData, true);
            OssOpt.shutDownOssClient(ossClient);
        }
        return responseData;
    }

    /**
     * 删除OSS中的resource操作
     *
     * @param userUploadFile 删除的resource信息
     * @param responseData   返回信息组装
     * @param ossClient      oss句柄
     * @param sqlSession     sql句柄
     */
    private static void deleteResourceLogic(UserUploadFile userUploadFile, ResponseData responseData,
                                            OSSClient ossClient, SqlSession sqlSession) throws RenderException {
        int deleteNum = 0, deleteResourceSize = 0;
        if (userUploadFile.getIs_folder() != 1) {
            //如果删除的文件资源是文件
            //删除文件在数据库user_upload_file的条目
            deleteNum = sqlSession.delete(Mapper.DELETE_RESOURCE_BY_ID, userUploadFile.getId());
            deleteResourceSize = Integer.parseInt(userUploadFile.getFile_size());
            OssOpt.deleteResourceOSSFile(userUploadFile, ossClient, sqlSession);

        } else {
            //如果删除的文件资源是文件夹则进行级联删除
            //设置进入文件夹后的relative_path路径
            userUploadFile.setRelative_path(userUploadFile.getRelative_path() + userUploadFile.getFile_name() + "/");
            //查找数据库中所有该relative_path路径下文件
            List<UserUploadFile> list = sqlSession.selectList(Mapper.GET_FOLDER_SUB_RESOURCE, userUploadFile);
            //删除文件夹在数据库user_upload_file的条目
            deleteNum = sqlSession.delete(Mapper.DELETE_RESOURCE_BY_ID, userUploadFile.getId());

            //循环监测并监测是否引用数为零并删除oss中对应文件数据
            for (UserUploadFile eachUploadFile :
                    list) {
                //数据库删除resource条目操作
                int temp = sqlSession.delete(Mapper.DELETE_RESOURCE_BY_ID, eachUploadFile.getId());
                if (temp > 0) {
                    deleteResourceSize += Integer.parseInt(eachUploadFile.getFile_size());
                    deleteNum++;
                } else {
                    throw new RenderException("===Delete relative path resources error, id: " + eachUploadFile.getId());
                }
            }
            //OSS删除resource文件
            OssOpt.deleteResourceBatch(list, sqlSession, ossClient);
        }
        //删除资源后对User和Project的资源空间的更新
        updateDelResRemain(sqlSession, userUploadFile, deleteResourceSize, deleteNum, responseData);
    }

    /**
     * 删除资源后对User和Project的资源空间的更新
     *
     * @param sqlSession         sql句柄
     * @param userUploadFile     用户提交需删除的资源文件信息
     * @param deleteResourceSize 删除的资源文件空间
     * @param deleteNum          删除数量
     * @param responseData       返回数据包装
     */
    private static void updateDelResRemain(SqlSession sqlSession, UserUploadFile userUploadFile, int deleteResourceSize,
                                           int deleteNum, ResponseData responseData) {

        //更新User的资源剩余空间
        String userResourceRemain = sqlSession.selectOne(Mapper.GET_USER_RESOURCE_SPACE_REMAIN, userUploadFile.getUser_id());
        int newUserResourceRemain = Integer.parseInt(userResourceRemain) + deleteResourceSize;
        int userUpdateNum = sqlSession.update(Mapper.UPDATE_USER_RESOURCE_SPACE_REMAIN, new User(userUploadFile.getUser_id(),
                String.valueOf(newUserResourceRemain)));

        //更新Project的资源占用空间
        String projectResourceSpace = sqlSession.selectOne(Mapper.GET_PROJECT_RESOURCE_SIZE, userUploadFile.getProject_id());
        int newProjectResourceSpace = Integer.parseInt(projectResourceSpace) - deleteResourceSize;
        int projectUpdateNum = sqlSession.update(Mapper.UPDATE_PROJECT_RESOURCE_SIZE, new Project(userUploadFile.getProject_id(),
                String.valueOf(newProjectResourceSpace)));

        //返回数据包装
        if (deleteNum > 0 && userUpdateNum > 0 && projectUpdateNum > 0) {
            Map<String, Integer> map = new HashMap<>();
            map.put("user_resource_remain", newUserResourceRemain);
            map.put("project_resource_space", newProjectResourceSpace);
            Assemble.responseSuccessSetting(responseData, map);

        } else {
            Assemble.responseErrorSetting(responseData, 401,
                    "delete db or oss error");
        }
        Render.logger.debug("===Delete Resource: \n" +
                "deleteNum: " + deleteNum +
                "userUpdateNum" + userUpdateNum +
                "projectUpdateNum" + projectUpdateNum);
    }


    /**
     * ****************************************************************************
     * 上传video的一帧截图
     *
     * @param msg http上传数据
     * @return 返回视频某一帧图片的URL
     */
    public static ResponseData uploadVideoImage(Object msg) {

        ResponseData responseData = new ResponseData(StatusCode.ERROR.getValue());
        SqlSession sqlSession = MybatisUtils.getSession();
        OSSClient ossClient = OssOpt.initOssClient();

        try {
            //获取video的image信息
            HashMap<String, Object> videoImage = FormData.getParam(msg, Common.VIDEO_IMAGE, Common.VIDEO_ID,
                    Common.VIDEO_IMAGE_NAME);
            //获取视频截帧的帧文件信息
            FileUpload fileUpload = (FileUpload) videoImage.get(Common.VIDEO_IMAGE);
            //视频图片更新数据库操作，之前只上传处理了视频，还未添加媒体截图，现添加视频截图到数据库
            int num = sqlSession.update(Mapper.UPDATE_VIDEO_IMAGE, videoImage);

            if (num > 0) {
                //如果添加视频截帧到数据库成功，进行上传该截帧文件
                String uploadFileName = GlobalConfig.getOssFileUrl(Common.UPLOAD_FILES) +
                        videoImage.get(Common.VIDEO_IMAGE_NAME);
                OssOpt.uploadFileToOss(uploadFileName, fileUpload.get(), ossClient);
                Assemble.responseSuccessSetting(responseData, null);
            } else {
                Assemble.responseErrorSetting(responseData, 401,
                        "RenderException uploadVideoImage: insert to db error");
            }
        } catch (Exception e) {
            Render.logger.error("uploadVideoImage error: ", e);
            Assemble.responseErrorSetting(responseData, 500,
                    "RenderException deleteResource system error");
        } finally {
            //对数据库进行后续提交和关闭操作等
            CommonService.databaseCommitClose(sqlSession, responseData, true);
            //对OSS资源连接释放
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

        try {
            Project project = (Project) FormData.getParam(msg, Project.class);
            int num = sqlSession.update(Mapper.SAVE_PROJECT_DATA, project);
            if (num > 0) {
                String projectDataFile = null;
                //根据version值对应同步不同oss文件
                if(Objects.equals(project.getVersion(), Common.MOBILE_V)){
                    projectDataFile = GlobalConfig.getOssFileUrl(Common.PROJECT_DATA) + project.getMo_version() + Common.PROJECT_DATA_SUFFIX;
                }else{
                    projectDataFile = GlobalConfig.getOssFileUrl(Common.PROJECT_DATA) + project.getPc_version() + Common.PROJECT_DATA_SUFFIX;
                }
                //创建新的project_data数据并同步到OSS中
                OssOpt.uploadFileToOss(projectDataFile, project.getProject_data().getBytes(), ossClient);
                Assemble.responseSuccessSetting(responseData, null);
            } else {
                Assemble.responseErrorSetting(responseData, 401,
                        "RenderException saveProjectData: ");
            }
        } catch (Exception e) {
            Render.logger.error("RenderException saveProjectData error: ", e);
            Assemble.responseErrorSetting(responseData, 500,
                    "RenderException saveProjectData system error");
        } finally {
            //对数据库进行后续提交和关闭操作等
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

        try {
            User user = (User) FormData.getParam(msg, User.class);
            int num = sqlSession.update(Mapper.UPDATE_EXPORT_DEFAULT_SETTING, user);
            if (num > 0) {
                //更新成功，则返回成功
                Assemble.responseSuccessSetting(responseData, null);
            } else {
                //更新失败
                Assemble.responseErrorSetting(responseData, 401, "Db update error");
            }
        } catch (Exception e) {
            //系统错误
            Assemble.responseErrorSetting(responseData, 500, "system error");

        } finally {
            //对数据库进行后续提交和关闭操作等
            CommonService.databaseCommitClose(sqlSession, responseData, true);
        }
        return responseData;
    }


}
