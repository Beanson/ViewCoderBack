package viewcoder.operation.impl.project;

import com.alibaba.fastjson.JSON;
import viewcoder.exception.project.ProjectListException;
import viewcoder.operation.entity.*;
import viewcoder.tool.common.*;
import viewcoder.tool.config.GlobalConfig;
import viewcoder.tool.parser.form.FormData;
import viewcoder.tool.util.MybatisUtils;
import viewcoder.operation.entity.response.ResponseData;
import viewcoder.operation.entity.response.StatusCode;
import viewcoder.operation.impl.common.CommonService;
import com.aliyun.oss.OSSClient;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/2/3.
 */
public class ProjectList {

    private static Logger logger = Logger.getLogger(ProjectList.class);

    /**
     * ****************************************************************************
     * 根据用户id获取用户所有projects的数据
     */
    public static ResponseData getProjectListData(Object msg) {
        ResponseData responseData = new ResponseData(StatusCode.ERROR.getValue());
        SqlSession sqlSession = MybatisUtils.getSession();

        try {
            //接收前台传过来关于数据需要查询的userId
            Map<String, Object> map = FormData.getParam(msg);
//            Integer userId = Integer.parseInt((String) map.get(Common.USER_ID));
//            Integer parentId = Integer.parseInt((String) map.get(Common.PAGE_PARENT));
            sqlSession = MybatisUtils.getSession();
            //查找数据库返回projects数据
            List<Project> projects = sqlSession.selectList(Mapper.GET_PROJECT_LIST_DATA, map);

            //进行projects数据,并打包成ResponseData格式并回传
            getProjectDataLogic(projects, responseData);

        } catch (Exception e) {
            ProjectList.logger.error("getProjectData catch exception", e);
            Assemble.responseErrorSetting(responseData, 500,
                    "Get ProjectList Data with System Error");

        } finally {
            CommonService.databaseCommitClose(sqlSession, responseData, false);
        }

        return responseData;
    }

    /**
     * 数据库返回数据后进行逻辑处理，并打包成ResponseData数据
     *
     * @param projects     所有项目对象信息
     * @param responseData 返回数据打包
     */
    private static void getProjectDataLogic(List<Project> projects, ResponseData responseData) {

        //如果projects不为null则数据库查询projects成功，返回该projects数据到前端。 preProjects已经初始化，不为null
        //projects.size()为0也是可以的，说明该用户尚未创建任何项目
        if (projects != null) {
            Map<String, Object> map = new HashMap<>(1);
            map.put("myProjects", projects);
            Assemble.responseSuccessSetting(responseData, map);
        } else {
            ProjectList.logger.error("Get ProjectList Data with Database Error");
            Assemble.responseErrorSetting(responseData, 401, "Get ProjectList Data with Database Error");
        }
    }


    /**
     * *************************************************************************
     * 更新project名称
     */
    public static ResponseData modifyProjectName(Object msg) {

        ResponseData responseData = new ResponseData(StatusCode.ERROR.getValue());
        SqlSession sqlSession = MybatisUtils.getSession();

        try {
            Project project = (Project) FormData.getParam(msg, Project.class);
            int num = sqlSession.update(Mapper.MODIFY_PROJECT_NAME, project);

            //根据影响条目是否大于0判断是否更新项目名称成功
            if (num > 0) {
                Assemble.responseSuccessSetting(responseData, project.getProject_name());
            } else {
                ProjectList.logger.error("Modify Project Name with Database Error");
                Assemble.responseErrorSetting(responseData, 401,
                        "Modify Project Name with Database Error");
            }

        } catch (Exception e) {
            ProjectList.logger.error("modifyProjectName catch exception", e);
            Assemble.responseErrorSetting(responseData, 500,
                    "Modify Project Name with System Error");

        } finally {
            CommonService.databaseCommitClose(sqlSession, responseData, true);
        }
        return responseData;
    }


    /**
     * *************************************************************************
     * 对项目进行拷贝操作，被拷贝的可以是 自己项目/project store中项目
     */
    public static ResponseData copyProject(Object msg) {

        ResponseData responseData = new ResponseData(StatusCode.ERROR.getValue());
        SqlSession sqlSession = MybatisUtils.getSession();
        OSSClient ossClient = OssOpt.initOssClient();

        try {
            //解析请求数据
            HashMap<String, Object> data = FormData.getParam(msg);
            if (data != null && data.get(Common.PROJECT_ID) != null && data.get(Common.PROJECT_ID) != "" &&
                    data.get(Common.USER_ID) != null && data.get(Common.USER_ID) != "") {

                int projectId = Integer.parseInt(String.valueOf(data.get(Common.PROJECT_ID)));
                int userId = Integer.parseInt(String.valueOf(data.get(Common.USER_ID)));
                int refId = Integer.parseInt(String.valueOf(data.get(Common.REF_ID)));
                String projectName = String.valueOf(data.get(Common.PROJECT_NAME));
                String optType = String.valueOf(data.get(Common.OPT_TYPE));

                //1. 获取原项目信息
                Project projectOrigin = sqlSession.selectOne(Mapper.GET_PROJECT_DATA, projectId);

                //2.查看用户剩余空间是否足够拷贝新的项目
                String resourceRemain = sqlSession.selectOne(Mapper.GET_USER_RESOURCE_SPACE_REMAIN, userId);
                int preRestSpace = Integer.parseInt(resourceRemain) - Integer.parseInt(projectOrigin.getResource_size());
                if (preRestSpace > 0) {
                    Project projectCopy = new Project();
                    //3. 更新新项目的project表格、OSS中single_export和project_data文件拷贝
                    copyProjectUpdateProjectTableAndOss(sqlSession, userId, projectName, refId, projectCopy, projectOrigin, ossClient);
                    //4. 更新新项目user_upload_file表格
                    copyProjectUpdateUserUploadFileTable(sqlSession, projectId, projectCopy);

                    //6. 更新用户剩余空间
                    sqlSession.update(Mapper.UPDATE_USER_RESOURCE_SPACE_REMAIN, new User(userId,
                            String.valueOf(preRestSpace)));

                    //7. 根据不同操作类型执行相应其他操作
                    optType(sqlSession, optType, projectId);

                    //返回新拷贝的项目的id信息
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", projectCopy.getId());
                    map.put("project_name", projectCopy.getProject_name());
                    map.put("last_modify_time", projectCopy.getLast_modify_time());
                    map.put("timestamp", projectCopy.getTimestamp());
                    map.put("pc_version", projectCopy.getPc_version());
                    map.put("mo_version", projectCopy.getMo_version());

                    Assemble.responseSuccessSetting(responseData, map);
                } else {
                    Assemble.responseErrorSetting(responseData, 401,
                            "User has not enough space to copy such project");
                }
            } else {
                Assemble.responseErrorSetting(responseData, 402,
                        "project_id or user_id null error");
            }
        } catch (Exception e) {
            ProjectList.logger.debug("===copyProject occurs error", e);
            Assemble.responseErrorSetting(responseData, 500, e.toString());

        } finally {
            CommonService.databaseCommitClose(sqlSession, responseData, true);
            OssOpt.shutDownOssClient(ossClient);
        }
        return responseData;
    }

    /**
     * 拷贝获取原项目 project 表数据并对新项目插入这些数据
     *
     * @param sqlSession  sql句柄
     * @param userId      用户id
     * @param projectCopy 请求拷贝的项目对象
     * @throws ProjectListException
     */
    private static void copyProjectUpdateProjectTableAndOss(SqlSession sqlSession, int userId, String projectName, int refId,
                                                            Project projectCopy, Project projectOrigin, OSSClient ossClient)
            throws ProjectListException {

        //获取原项目project表数据
        if (projectOrigin == null || projectOrigin.getId() == 0) {
            throw new ProjectListException("Get Original Project Info From DB \"Project Table\" Null Exception");
        }

        //初始化新项目project表数据
        projectCopy.setUser_id(userId);
        projectCopy.setProject_name(projectName);
        projectCopy.setLast_modify_time(CommonService.getDateTime());
        projectCopy.setResource_size(projectOrigin.getResource_size());
        projectCopy.setRef_id(refId);
        projectCopy.setPc_version(projectOrigin.getPc_version() + 1);
        //Mo version 如果有则赋值，无则不用
        if (CommonService.checkNotNull(projectOrigin.getMo_version())) {
            projectCopy.setMo_version(projectOrigin.getMo_version() + 1);
        }
        //timestamp万一不等于任何一个version，则默认赋值pc_version
        if (projectOrigin.getTimestamp().equals(projectOrigin.getPc_version()) ||
                projectOrigin.getTimestamp().equals(projectOrigin.getMo_version())) {
            projectCopy.setTimestamp(projectOrigin.getTimestamp() + 1);

        } else {
            projectCopy.setTimestamp(projectOrigin.getPc_version() + 1);
        }

        //新项目project插入数据库操作
        int num = sqlSession.insert(Mapper.CREATE_COPY_PROJECT, projectCopy);
        if (num <= 0) {
            throw new ProjectListException("Insert Copied Project Data To DB \"Project table\" Exception");
        }

        /*更新OSS文件拷贝操作*/
        ossProjectHtmlCopy(ossClient, projectOrigin.getPc_version(), projectCopy.getPc_version());
        if (CommonService.checkNotNull(projectOrigin.getMo_version())) {
            ossProjectHtmlCopy(ossClient, projectOrigin.getMo_version(), projectCopy.getMo_version());
        }
    }

    /**
     * 拷贝获取原项目user_upload_file表数据并对新项目插入这些数据
     *
     * @param sqlSession  sql句柄
     * @param projectId   项目id
     * @param projectCopy 请求拷贝的项目对象
     */
    public static void copyProjectUpdateUserUploadFileTable(SqlSession sqlSession, int projectId, Project projectCopy) {
        //获取原项目user_upload_file表数据
        List<UserUploadFile> userUploadFiles = sqlSession.selectList(Mapper.GET_ALL_RESOURCE_BY_PROJECT_ID, projectId);
        for (UserUploadFile file :
                userUploadFiles) {
            UserUploadFile copyUploadFile = new UserUploadFile(
                    projectCopy.getId(), //project_id
                    projectCopy.getUser_id(),
                    file.getWidget_type(),
                    file.getFile_type(),
                    file.getIs_folder(),
                    file.getTime_stamp(),
                    file.getSuffix(),
                    file.getFile_name(),
                    file.getRelative_path(),
                    file.getFile_size(),
                    file.getVideo_image_name(),
                    projectCopy.getLast_modify_time()
            );
            //逐一添加到数据库中
            sqlSession.insert(Mapper.INSERT_NEW_RESOURCE, copyUploadFile);
        }
    }

    /**
     * 查找OSS中是否存在该HTML文件，如存在则进行拷贝
     *
     * @param ossClient oss句柄
     * @param origin    源项目数据
     * @param copy      拷贝的新项目数据
     */
    public static void ossProjectHtmlCopy(OSSClient ossClient, String origin, String copy) {
        String sourceFileName, destFileName;
        Boolean found;

        //拷贝single_export文件
        sourceFileName = GlobalConfig.getOssFileUrl(Common.SINGLE_EXPORT) + origin + Common.PROJECT_FILE_SUFFIX;
        destFileName = GlobalConfig.getOssFileUrl(Common.SINGLE_EXPORT) + copy + Common.PROJECT_FILE_SUFFIX;
        found = OssOpt.getObjectExist(ossClient, sourceFileName);
        if (found) {
            OssOpt.copyProject(ossClient, sourceFileName, destFileName);
        }
        //拷贝project_data中文件
        sourceFileName = GlobalConfig.getOssFileUrl(Common.PROJECT_DATA) + origin + Common.PROJECT_DATA_SUFFIX;
        destFileName = GlobalConfig.getOssFileUrl(Common.PROJECT_DATA) + copy + Common.PROJECT_DATA_SUFFIX;
        found = OssOpt.getObjectExist(ossClient, sourceFileName);
        if (found) {
            OssOpt.copyProject(ossClient, sourceFileName, destFileName);
        }
    }

    /**
     * 根据不同操作类型执行相应其他处理
     * 由于copy project和create store project两种操作公用copy project实现方法，
     * 存在部分定制化需求在此方法中执行
     *
     * @param sqlSession sql句柄
     * @param optType    操作类型
     * @param projectId  项目id号
     */
    private static void optType(SqlSession sqlSession, String optType, int projectId) {
        switch (optType) {
            case Common.STORE_TYPE: {
                int num = sqlSession.insert(Mapper.UPDATE_USAGE_AMOUNT, projectId);
                break;
            }
            default: {
                break;
            }
        }
    }


    /**
     * *************************************************************************
     * 删除项目后台操作
     * 数据库需删除project表中对应project_id条目，user_upload_file表中对应project_id条目
     * 文件需删除project_file
     *
     * @param msg
     * @return
     */
    public static ResponseData deleteProject(Object msg) {
        Map<String, Object> map = FormData.getParam(msg, Common.PROJECT_ID, Common.USER_ID);
        int projectId = Integer.parseInt(String.valueOf(map.get(Common.PROJECT_ID)));
        int userId = Integer.parseInt(String.valueOf(map.get(Common.USER_ID)));
        return deleteProjectOpt(projectId, userId);
    }

    /**
     * 删除项目的具体操作
     *
     * @param projectId 将要删除的项目id
     * @param userId    将要删除的项目的user的id
     * @return
     */
    public static ResponseData deleteProjectOpt(int projectId, int userId) {

        ResponseData responseData = new ResponseData(StatusCode.ERROR.getValue());
        SqlSession sqlSession = MybatisUtils.getSession();
        OSSClient ossClient = OssOpt.initOssClient();
        try {
            Project project = sqlSession.selectOne(Mapper.GET_PROJECT_DATA, projectId);
            //验证该用户是否是该project的所有者
            if (project.getUser_id() == userId) {
                //分别从数据库中查出要删除的数据
                List<UserUploadFile> list = sqlSession.selectList(Mapper.GET_ALL_RESOURCE_BY_PROJECT_ID, projectId);
                //数据库进行删除操作
                int deleteProjectNum = sqlSession.delete(Mapper.DELETE_PROJECT_BY_ID, projectId);
                int deleteUploadFileNum = sqlSession.delete(Mapper.DELETE_RESOURCE_BY_PROJECT_ID, projectId);

                //删除OSS中single_export中的project_file html文件、project_data中项目数据文件和所有user_upload_file引用数为0的组件文件
                deleteProjectLogicAnalyse(deleteProjectNum, deleteUploadFileNum, responseData, list, project, ossClient,
                        sqlSession);
            } else {
                String message = "User:" + userId + " is not such project:" + projectId + " owner";
                ProjectList.logger.debug("deleteProjectOpt error: " + message);
                Assemble.responseErrorSetting(responseData, 401, message);
            }
        } catch (Exception e) {
            ProjectList.logger.error("===deleteProjectOpt error: ", e);
            Assemble.responseErrorSetting(responseData, 500, "Delete Project Occurs Error");

        } finally {
            //对数据库进行后续提交和关闭操作等
            CommonService.databaseCommitClose(sqlSession, responseData, true);
            OssOpt.shutDownOssClient(ossClient);
        }
        return responseData;
    }

    /**
     * 删除项目返回数据的逻辑处理
     *
     * @param deleteProjectNum    删除project表的条目数目
     * @param deleteUploadFileNum 删除user_upload_file表的条目数目
     * @param responseData        返回数据response data
     */
    private static void deleteProjectLogicAnalyse(int deleteProjectNum, int deleteUploadFileNum, ResponseData responseData,
                                                  List<UserUploadFile> list, Project project, OSSClient ossClient,
                                                  SqlSession sqlSession) {
        //批量删除oss中所有该项目的HTML文件和Project data文件

        List<String> files = new ArrayList<>();
        //添加删除OSS中PC端HTML文件
        files.add(GlobalConfig.getOssFileUrl(Common.SINGLE_EXPORT) + project.getPc_version() + Common.PROJECT_FILE_SUFFIX);
        //添加删除OSS中PC端Project Data中文件
        files.add(GlobalConfig.getOssFileUrl(Common.PROJECT_DATA) + project.getPc_version() + Common.PROJECT_DATA_SUFFIX);

        //如果有手机版则添加手机版数据
        if (CommonService.checkNotNull(project.getMo_version())) {
            //添加删除OSS中Mobile端HTML文件
            files.add(GlobalConfig.getOssFileUrl(Common.SINGLE_EXPORT) + project.getMo_version() + Common.PROJECT_FILE_SUFFIX);
            //添加删除OSS中Mobile端Project Data中文件
            files.add(GlobalConfig.getOssFileUrl(Common.PROJECT_DATA) + project.getMo_version() + Common.PROJECT_DATA_SUFFIX);
        }
        //batch job删除HTML文件和Project Data文件
        OssOpt.deleteFileInOssBatch(files, ossClient);


        //批量删除OSS中对应user_upload_file引用为0的组件文件
        OssOpt.deleteResourceBatch(list, sqlSession, ossClient);

        //更新用户可用空间
        int updateUserResourceNum = addUserResourceSpace(sqlSession, project.getUser_id(),
                project.getResource_size());

        //确保各种操作数据库条目大于0后返回成功，无需deleteUploadFileNum>0的验证，因为可能项目无任何资源widget
        if (deleteProjectNum > 0 && updateUserResourceNum > 0) {
            //返回删除project_file文件成功删除项目成功
            Assemble.responseSuccessSetting(responseData, null);
        } else {
            Assemble.responseErrorSetting(responseData, 402, "Delete Project Items In DB Error");
        }
        //打印日志监测数据返回情况
        ProjectList.logger.debug("===deleteProjectOpt status: \n" +
                "deleteProjectNum：" + deleteProjectNum + "\n" +
                "deleteUploadFileNum：" + deleteUploadFileNum + "\n" +
                "updateUserResourceNum：" + updateUserResourceNum + "\n");
    }

    /**
     * 用户可用空间添加到数据库中
     *
     * @param sqlSession   sql句柄
     * @param userId       用户id
     * @param projectSpace 项目占用空间
     * @return
     */
    private static int addUserResourceSpace(SqlSession sqlSession, int userId, String projectSpace) {
        //获取用户可用resource空间信息
        String originalResourceSize = sqlSession.selectOne(Mapper.GET_USER_RESOURCE_SPACE_REMAIN, userId);
        ProjectList.logger.debug("userId: "+userId+" original size:" + originalResourceSize + " projectSpace" + projectSpace);
        int newResourceSize = Integer.parseInt(originalResourceSize) + Integer.parseInt(projectSpace);
        //更新用户resource空间信息
        return sqlSession.update(Mapper.UPDATE_USER_RESOURCE_SPACE_REMAIN, new User(userId, String.valueOf(newResourceSize)));
    }


    /**
     * 级联删除文件
     */
    public static boolean deleteDir(File file) {
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) {
                deleteDir(f);
            }
        }
        return file.delete();
    }


}
