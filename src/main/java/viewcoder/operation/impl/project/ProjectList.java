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
import java.util.*;

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

                //1、获取前端传递过来的数据
                int projectId = Integer.parseInt(String.valueOf(data.get(Common.PROJECT_ID)));
                int userId = Integer.parseInt(String.valueOf(data.get(Common.USER_ID)));
                int refId = Integer.parseInt(String.valueOf(data.get(Common.REF_ID)));
                int parent = Integer.parseInt(String.valueOf(data.get(Common.PARENT)));
                String optType = String.valueOf(data.get(Common.OPT_TYPE));

                //2、 获取原项目信息
                List<Project> projectsOrigin = sqlSession.selectList(Mapper.GET_ALL_RELATED_PROJECT, userId);

                //3、把list数据打包成map数据
                Map<Integer, List<Project>> projects = packProjectListToMap(projectsOrigin, projectId,parent);

                //4、 更新新项目的project表格、OSS中single_export和project_data文件拷贝
                insertCopyRecord(userId, parent, parent, refId, projects, sqlSession, ossClient);

                //5、根据不同操作类型执行相应其他操作
                optType(sqlSession, optType, projectId);

                //6、返回200成功信息
                Assemble.responseSuccessSetting(responseData, null);

            } else {
                Assemble.responseErrorSetting(responseData, 402,
                        "project_id or user_id null error");
            }
        } catch (Exception e) {
            ProjectList.logger.debug("copyProject occurs error", e);
            Assemble.responseErrorSetting(responseData, 500, e.toString());

        } finally {
            CommonService.databaseCommitClose(sqlSession, responseData, true);
            OssOpt.shutDownOssClient(ossClient);
        }
        return responseData;
    }


    /**
     * 把list数据打包成map数据
     *
     * @param projectsOrigin 原始项目数据
     * @param projectId  目标项目层级id
     * @param parent 父项目的id值
     * @return
     * @throws Exception
     */
    private static Map<Integer, List<Project>> packProjectListToMap(List<Project> projectsOrigin, int projectId,
                                                                    int parent) throws Exception {

        //获取原项目project表数据监测
        if (!CommonService.checkNotNull(projectsOrigin)) {
            throw new ProjectListException("Get Original Project info null error");
        }

        //把list数据重新打包成map格式
        // parentId(Integer) : projectId1, projectId2, projectId3 (List<Project>)
        Map<Integer, List<Project>> projects = new HashMap<>();
        for (Project project : projectsOrigin) {

            //保证parent为0的project只能是传入的projectId；若该projectId不是根目录则parent为0的无记录
            if (project.getParent() == parent && project.getId() != projectId) {
                continue;
            }

            //获取map中该parent key的value
            List<Project> list = projects.get(project.getParent());
            if (CommonService.checkNotNull(list)) {
                //如果该list不为空则往该list中添加project数据
                list.add(project);
            } else {
                //如果该list为空则实例化该list，并添加到projects的map中
                list = new ArrayList<>();
                list.add(project);
                projects.put(project.getParent(), list);
            }
        }
//        for (Map.Entry<Integer, List<Project>> entry : projects.entrySet()) {
//            System.out.println("key= " + entry.getKey() + " and value= " + entry.getValue().toString());
//        }

        return projects;
    }


    /**
     * 新项目页面插入数据库 + 生成oss文件数据
     *
     * @param userId     用户id，商城项目拷贝使用时必须的
     * @param parent     被拷贝的项目的父项目的id号
     * @param newParent  新的项目的父项目的id号
     * @param projects   记录所有parentId：projectId列表 的源数据
     * @param sqlSession sql句柄
     * @param ossClient  oss句柄
     * @throws Exception
     */
    private static void insertCopyRecord(int userId, int parent, int newParent, int refId, Map<Integer, List<Project>> projects,
                                         SqlSession sqlSession, OSSClient ossClient) throws Exception {

        //获取该父id下的所有子页面元素
        List<Project> children = projects.get(parent);

        if (CommonService.checkNotNull(children)) {
            //如果children不为空则进行递归逻辑处理
            //每个子元素插入数据库
            for (Project project : children) {
                //设置新插入记录的数据
                //分别设置新的timestamp，pc_version 和 mo_version
                String pcVersion = project.getPc_version();
                String moVersion = project.getMo_version();
                String timeStamp = project.getTimestamp();

                //字符串末尾拼接
                project.setPc_version(pcVersion + 1);
                project.setMo_version(moVersion + 1);

                //如果timestamp和mobile相等则timestamp赋值mobile版本，否则赋值pc版本
                if (Objects.equals(project.getTimestamp(), moVersion)) {
                    project.setTimestamp(project.getMo_version());
                } else {
                    project.setTimestamp(project.getPc_version());
                }

                //设置userId, 对于拷贝商城项目是必要的
                project.setUser_id(userId);
                //设置新的父页面id
                project.setParent(newParent);
                //引用id
                project.setRef_id(refId);
                //新的修改时间
                project.setLast_modify_time(CommonService.getDateTime());
                //新的项目名称
                project.setProject_name(project.getProject_name() + "_copy");

                //获取父元素的旧id
                int oldId = project.getId();
                //插入数据库
                int num = sqlSession.insert(Mapper.CREATE_COPY_PROJECT, project);

                if (num > 0) {
                    //父元素记录的子元素数目+1
                    if(project.getParent()!=0){
                        addParentChildPageNum(project.getParent(), sqlSession);
                    }

                    /*拷贝pc版本项目project data数据*/
                    ossProjectHtmlCopy(ossClient, GlobalConfig.getOssFileUrl(Common.PROJECT_DATA),
                            Common.PROJECT_DATA_SUFFIX, pcVersion, project.getPc_version());
                    /*拷贝项目导出project file单文件数据*/
                    ossProjectHtmlCopy(ossClient, GlobalConfig.getOssFileUrl(Common.SINGLE_EXPORT),
                            Common.PROJECT_FILE_SUFFIX, timeStamp, project.getPc_version());

                    /*拷贝mobile版本项目project data数据*/
                    ossProjectHtmlCopy(ossClient, GlobalConfig.getOssFileUrl(Common.PROJECT_DATA),
                            Common.PROJECT_DATA_SUFFIX, moVersion, project.getMo_version());
                     /*拷贝项目导出project file单文件数据*/
                    ossProjectHtmlCopy(ossClient, GlobalConfig.getOssFileUrl(Common.SINGLE_EXPORT),
                            Common.PROJECT_FILE_SUFFIX, timeStamp, project.getMo_version());


                    //每个子元素递归调用
                    int newId = project.getId();
                    insertCopyRecord(userId, oldId, newId, refId, projects, sqlSession, ossClient);
                }
            }
        } else {
            //如果无child元素则返回
            return;
        }
    }


    /**
     * 查找OSS中是否存在该HTML文件和project data文件，如存在则进行拷贝
     *
     * @param ossClient oss句柄
     * @param origin    源项目数据
     * @param copy      拷贝的新项目数据
     */
    public static void ossProjectHtmlCopy(OSSClient ossClient, String prefix, String suffix,
                                          String origin, String copy) {
        //拷贝OSS中文件
        String sourceFileName = prefix + origin + suffix;
        String destFileName = prefix + copy + suffix;
        Boolean found = OssOpt.getObjectExist(ossClient, sourceFileName);
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
                //记录该store的引用次数
                int num = sqlSession.insert(Mapper.UPDATE_USAGE_AMOUNT_PLUS, projectId);
                break;
            }
            default: {
                break;
            }
        }
    }


    /**
     * *********************************************************************************************
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
     * @param userId 将要删除的项目的user的id
     * @return
     */
    public static ResponseData deleteProjectOpt(int projectId, int userId) {

        ResponseData responseData = new ResponseData(StatusCode.ERROR.getValue());
        SqlSession sqlSession = MybatisUtils.getSession();
        OSSClient ossClient = OssOpt.initOssClient();
        try {
            Project project = sqlSession.selectOne(Mapper.GET_PROJECT_DATA, projectId);
            //1、验证该用户是否是该project的所有者
            if (project.getUser_id() == userId) {

                //2、获取原项目信息
                List<Project> projectsOrigin = sqlSession.selectList(Mapper.GET_ALL_RELATED_PROJECT, userId);

                //3、把list数据打包成map数据
                Map<Integer, List<Project>> projects = packProjectListToMap(projectsOrigin, projectId, project.getParent());

                //4、删除该project的所有数据
                deleteProjectRecordAndFile(project.getParent(), sqlSession, ossClient, projects);

                //返回操作正确码
                Assemble.responseSuccessSetting(responseData,null);

            } else {
                String message = "User:" + userId + " is not such project:" + projectId + " owner";
                ProjectList.logger.warn("deleteProjectOpt error: " + message);
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
     * 递归删除该项目和其子项目的记录和OSS文件操作
     * @param parent 父项目
     * @param sqlSession sql句柄
     * @param ossClient oss句柄
     * @param projects map集合的关联project
     */
    private static void deleteProjectRecordAndFile(int parent, SqlSession sqlSession, OSSClient ossClient,
                                                    Map<Integer, List<Project>> projects) {

        List<String> files = new ArrayList<>();
        List<Project> children = projects.get(parent);

        if(CommonService.checkNotNull(children)){

            for (Project project : children) {
                //1、删除该projectId的数据库条目
                sqlSession.delete(Mapper.DELETE_PROJECT_BY_ID, project.getId());
                sqlSession.delete(Mapper.DELETE_RESOURCE_BY_PROJECT_ID, project.getId());

                //2、添加即将删除oss中html和project data数据
                files.add(GlobalConfig.getOssFileUrl(Common.SINGLE_EXPORT) + project.getPc_version() + Common.PROJECT_FILE_SUFFIX);
                files.add(GlobalConfig.getOssFileUrl(Common.PROJECT_DATA) + project.getPc_version() + Common.PROJECT_DATA_SUFFIX);
                files.add(GlobalConfig.getOssFileUrl(Common.SINGLE_EXPORT) + project.getMo_version() + Common.PROJECT_FILE_SUFFIX);
                files.add(GlobalConfig.getOssFileUrl(Common.PROJECT_DATA) + project.getMo_version() + Common.PROJECT_DATA_SUFFIX);

                //3、update父节点的child的记录
                minusParentChildPageNum(project.getParent(),sqlSession);

                //4、以该projectId为parentId循环递归调用删除子项目记录
                deleteProjectRecordAndFile(project.getId(), sqlSession, ossClient, projects);
            }

            //6、批量删除OSS中HTML文件和Project Data文件
            OssOpt.deleteFileInOssBatch(files, ossClient);
        }
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


    /**
     * 该page的子页面的数目+1
     */
    public static void addParentChildPageNum(int parentId, SqlSession sqlSession) {
        //如果parent不等于0，则对该parent的页面项目进行子页面的添加操作
        if (parentId != 0) {
            int num = sqlSession.update(Mapper.UPDATE_CHILD_NUM_PLUS, parentId);
        }
    }

    /**
     * 该page的子页面的数目-1
     */
    public static void minusParentChildPageNum(int parentId, SqlSession sqlSession) {
        //如果parent不等于0，则对该parent的页面项目进行子页面的添加操作
        if (parentId != 0) {
            int num = sqlSession.update(Mapper.UPDATE_CHILD_NUM_MINUS, parentId);
        }
    }
}
