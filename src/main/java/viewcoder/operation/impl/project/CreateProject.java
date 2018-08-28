package viewcoder.operation.impl.project;

import viewcoder.exception.project.PSDAnalysisException;
import viewcoder.operation.entity.*;
import viewcoder.tool.common.*;
import viewcoder.tool.config.GlobalConfig;
import viewcoder.tool.parser.form.FormData;
import viewcoder.tool.util.MybatisUtils;
import viewcoder.operation.entity.response.ResponseData;
import viewcoder.operation.entity.response.StatusCode;
import viewcoder.operation.impl.common.CommonService;
import viewcoder.psd.PsdAnalysis;
import viewcoder.psd.entity.PsdInfo;
import com.alibaba.fastjson.JSON;
import com.aliyun.oss.OSSClient;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import viewcoder.url.Simulate;

import java.io.File;
import java.util.*;

import static org.apache.ibatis.javassist.CtClass.version;

/**
 * Created by Administrator on 2018/2/4.
 */
public class CreateProject {

    private static Logger logger = Logger.getLogger(CreateProject.class);

    private final static String PSD_FILE = "com.viewcoder.file.psd_parse_error";

    /**
     * ****************************************************************************
     * 获取创建新空项目的数据并插入到数据库，返回新项目的id值
     *
     * @param msg http接收的message对象数据
     */
    public static ResponseData createEmptyProject(Object msg) {

        ResponseData responseData = new ResponseData(StatusCode.ERROR.getValue());
        SqlSession sqlSession = MybatisUtils.getSession();
        OSSClient ossClient = OssOpt.initOssClient();
        String message = "";
        try {
            Project project = (Project) FormData.getParam(msg, Project.class);
            /*新创建空项目数据插入数据库*/
            int num = optHandler(sqlSession, project, Mapper.CREATE_EMPTY_PROJECT);

            //插入数据库结果分析，如果插入条目大于0则成功，否则失败
            if (num > 0) {
                //OSS创建空项目的project的数据文件
                insertNewProjectToOss(project, ossClient);
                //返回新建项目的project数据
                Assemble.responseSuccessSetting(responseData, project);

            } else {
                message = "Insert Empty Project Data To Database Error";
                CreateProject.logger.error(message);
                Assemble.responseErrorSetting(responseData, 401, message);
            }

        } catch (Exception e) {
            message = "System error";
            CreateProject.logger.error(message, e);
            Assemble.responseErrorSetting(responseData, 500, message);

        } finally {
            CommonService.databaseCommitClose(sqlSession, responseData, true);
            OssOpt.shutDownOssClient(ossClient);
        }
        return responseData;
    }


    /**
     * ****************************************************************************
     * 新建project store项目
     * 实际操作是拷贝项目的操作
     *
     * @param msg http接收的message对象数据
     */
    public static ResponseData createStoreProject(Object msg) {
        return ProjectList.copyProject(msg);
    }


    /**
     * ****************************************************************************
     * 解析PSD文件，生成新HTML项目，并把新项目数据插入数据库
     *
     * @param msg http接收的message对象数据
     * @return 返回新创建的psd项目的数据
     */
    public static ResponseData createPSDProject(Object msg) {

        ResponseData responseData = new ResponseData(StatusCode.ERROR.getValue());
        SqlSession sqlSession = MybatisUtils.getSession();
        OSSClient ossClient = OssOpt.initOssClient();
        Project project = new Project();
        String message = "";
        try {
            //如果保存成功，则对该文件进行解析处理
            //解析psd文件参数变量数据
            project = (Project) FormData.getParam(msg, Project.class);

            //查看用户可用空间是否还满足该PSD文件的解析
            String resourceRemain = sqlSession.selectOne(Mapper.GET_USER_RESOURCE_SPACE_REMAIN, project.getUser_id());
            long preRemainSize = Integer.parseInt(resourceRemain) - project.getPsd_file().length();
            //如果该用户有足够空间进行PSD文件解析，则创建该项目，否则不创建
            if (preRemainSize > 0) {
                //设置项目占用空间，
                project.setResource_size(String.valueOf(project.getPsd_file().length()));
                //根据opt值进行不同操作，若为新建psd的project项目，则数据插入数据库，从而获取project_id
                optHandler(sqlSession, project, Mapper.CREATE_PSD_PROJECT);
                //解析file文件数据并保存到指定位置，如果解析成功将删除该psd文件，如果解析失败将保留作为后续程序调优参考文件
                parsePSDFileLogic(responseData, project.getPsd_file().getFile(), project, sqlSession, ossClient, preRemainSize);

            } else {
                message = "User resource space not enough";
                CreateProject.logger.warn(message);
                Assemble.responseErrorSetting(responseData, 402, message);
            }

        } catch (Exception e) {
            message = "System error";
            CreateProject.logger.error(message, e);
            Assemble.responseErrorSetting(responseData, 500, message);

        } finally {
            //如果该PSD文件无法解析则上传到OSS云端
            //TODO 该步骤重新开ossClient实例并异步处理
            uploadErrorPsdFile(responseData, project, ossClient);

            //资源关闭和释放
            CommonService.databaseCommitClose(sqlSession, responseData, true);
            OssOpt.shutDownOssClient(ossClient);
        }
        return responseData;
    }


    /**
     * 解析新建PSD项目的PSD解析处理文件逻辑
     *
     * @param responseData 返回包装数据对象
     * @param file         psd文件路径
     * @param project      PSD的项目数据，项目整体数据，如项目名称，用户id等
     * @param sqlSession   mybatis数据库操作session句柄
     * @throws PSDAnalysisException
     */
    private static void parsePSDFileLogic(ResponseData responseData, File file, Project project, SqlSession sqlSession,
                                          OSSClient ossClient, long preRemainSize) throws PSDAnalysisException {
        try {
            //如果保存成功则进行解析PSD文件数据信息并返回内容数据
            PsdAnalysis psdAnalysis = new PsdAnalysis(project, ossClient, sqlSession);
            psdAnalysis.parse(file);
            PsdInfo psdInfo = psdAnalysis.exportData();

            //如果解析后有项目数据则进行保存更新到数据库
            if (psdInfo != null) {

                //设置psdInfo的project_data数据到OSS中
                project.setProject_data(JSON.toJSONString(psdInfo));
                //project data文件更新插入oss中
                insertNewProjectToOss(project, ossClient);

                //图片信息batch插入userUploadFile表中
                sqlSession.insert(Mapper.INSERT_BATCH_NEW_RESOURCE, psdAnalysis.getUploadFileList());

                //更新用户新的可用空间，减去resource size后的大小
                sqlSession.update(Mapper.UPDATE_USER_RESOURCE_SPACE_REMAIN, new User(project.getUser_id(), String.valueOf(preRemainSize)));

                //插入成功，返回新生成的project_id和project_data数据，或原来的project_id新project_data
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("id", project.getId());
                map.put("project_data", project.getProject_data());
                Assemble.responseSuccessSetting(responseData, map);

            } else {
                //解析PSD文件失败
                throw new PSDAnalysisException("parsePSDFileLogic: Parse PSD file error");
            }
        } catch (Exception e) {
            CreateProject.logger.warn("===PSD Analysis error", e);
            throw new PSDAnalysisException(e);
        }
    }


    /**
     * 如果responseData的status_code不是200，则删除该项目数据，并上传该问题PSD文件到OSS中
     *
     * @param responseData 返回数据包
     * @param project      项目数据
     */
    private static void uploadErrorPsdFile(ResponseData responseData, Project project, OSSClient ossClient) {
        //若操作过程出错并且不是由于空间不足引起的，则把问题psd插入保存，后续研发
        if (responseData.getStatus_code() != 200 && responseData.getException_code() != 402) {
            ProjectList.deleteProjectOpt(project.getId(), project.getUser_id());
            try {
                //上传该特殊的不能解析的PSD文件到OSS中
                String psdFile = GlobalConfig.getOssFileUrl(Common.PSD_PARSE_ERROR) +
                        CommonService.getTimeStamp() + Common.IMG_PSG;
                OssOpt.uploadFileToOss(psdFile, project.getPsd_file().getFile(), ossClient);

            } catch (Exception e) {
                CreateProject.logger.debug("Upload special psd file to common error");
            }
        }
    }


    /**
     * ****************************************************************************
     * 生成Simulate类型的project，根据URL地址，生成和其类似的网页
     *
     * @param msg 传递的消息类型
     */
    public static ResponseData createSimulateProject(Object msg) {
        ResponseData responseData = new ResponseData(StatusCode.ERROR.getValue());
        String message = "";
        try {
            //获取从前端传递过来的数据
            Project project = (Project) FormData.getParam(msg, Project.class);

            //初始化项目创建的进度
            ProjectProgress projectProgress = new ProjectProgress(project.getUser_id(), Common.PROJECT_SIMULATE,
                    project.getProject_name(), project.getPc_version(), 0);
            CommonObject.getProgressList().add(projectProgress);

            //异步解析URL网站元素操作
            createSimulateOpt(project, projectProgress);
            //正确解析传递的参数及其类型，并成功调用URL解析网站元素操作，返回正确数据
            Assemble.responseSuccessSetting(responseData, null);

        } catch (Exception e) {
            message = "System error";
            CreateProject.logger.debug(message, e);
            Assemble.responseErrorSetting(responseData, 500, message);
        }
        return responseData;
    }


    /**
     * 异步解析URL网站元素操作
     * TODO 后面采用云主机进行请求响应，而慢操作放在物理主机中运行
     */
    public static void createSimulateOpt(Project project, ProjectProgress projectProgress) throws Exception {
        //创建解析URL网站元素的线程
        Thread simulateParseThread = new Thread(new Runnable() {
            @Override
            public void run() {
                ResponseData responseData = new ResponseData(StatusCode.ERROR.getValue());//用来记录程序执行状态
                OSSClient ossClient = OssOpt.initOssClient();
                SqlSession sqlSession = null;
                String message = "";
                try {
                    //获取URL资源项目元数据
                    String projectData = Simulate.createProject(project.getWeb_url(), projectProgress, project.getTarget_width());

                    //如果获取解析后项目数据不为空则进行插入数据库等操作，否则全局变量中记录错误消息
                    if (projectData != null) {
                        //准备project实体类数据
                        project.setProject_data(projectData);
                        project.setLast_modify_time(CommonService.getDateTime());

                        //把新创建的simulate的project插入数据库, 在这里启动数据库操作，不然可能会报超时错
                        sqlSession = MybatisUtils.getSession();
                        int num = optHandler(sqlSession, project, Mapper.CREATE_SIMULATE_PROJECT);

                        if (num > 0) {
                            //project_data数据同步到OSS中
                            insertNewProjectToOss(project, ossClient);
                            Assemble.responseSuccessSetting(responseData, null);
                            message = "createSimulateOpt success, project id is: " + project.getId();
                            CreateProject.logger.debug(message);
                            projectProgress.setProgress(100);

                        } else {
                            projectProgress.setProgress(-1); //设置进度信息，插入数据库操作失败
                            message = "createSimulateOpt with DB error, num<=0 ";
                            CreateProject.logger.warn(message);

                        }
                    } else {
                        projectProgress.setProgress(-2); //设置进度信息，获取project信息失败
                        message = "createSimulateOpt with error, get projectData null ";
                        CreateProject.logger.warn(message);
                    }

                } catch (Exception e) {
                    projectProgress.setProgress(-3); //设置进度信息，系统发生错误
                    message = "createSimulateOpt error";
                    CreateProject.logger.error(message, e);

                } finally {
                    OssOpt.shutDownOssClient(ossClient);
                    CommonService.databaseCommitClose(sqlSession, responseData, true);
                }
            }
        });

        try {
            //启动线程开始运行解析网站元素操作
            simulateParseThread.start();

        } catch (Exception e) {
            //线程运行出现问题后马上停止
            CreateProject.logger.debug("createSimulateOpt error:", e);
            simulateParseThread.interrupt();
            simulateParseThread = null;
        }
    }


    /**
     * 获取PSD或URL项目元素的解析进度
     *
     * @param msg 传递的参数
     * @return
     */
    public static ResponseData getProjectRate(Object msg) {
        ResponseData responseData = new ResponseData(StatusCode.ERROR.getValue());
        String message = "";
        try {
            //获取解析的项目标识参数
            Map<String, Object> map = FormData.getParam(msg);
            String pcVersion = (String) map.get(Common.PC_VERSION);

            //遍历所有正在创建的PSD或URL项目，根据pcVersion获取其对应进度
            List<ProjectProgress> list = CommonObject.getProgressList();
            Iterator<ProjectProgress> iterator = list.iterator();
            while (iterator.hasNext()) {
                ProjectProgress progress = iterator.next();

                //找到条目，则返回进度信息
                if (progress.getPc_version() != null && progress.getPc_version().equals(pcVersion)) {
                    Assemble.responseSuccessSetting(responseData, progress.getProgress());

                    //如果解析URL的元素比例已经到达100，则把该缓存参数去掉
                    //如果进度出现负数，则说明程序执行出现问题，返回前端后，后台数据进行删除
                    //如果出现0,20,80,100之外的，删除该progress
                    switch (progress.getProgress()) {
                        case 0: {
                            break;
                        }
                        case 20: {
                            break;
                        }
                        case 80: {
                            break;
                        }
                        case 100: {
                            iterator.remove();
                            break;
                        }
                        default: {
                            iterator.remove();
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            message = "System error";
            CreateProject.logger.debug(message, e);
            Assemble.responseErrorSetting(responseData, 500, message);
        }
        return responseData;
    }


    /**
     * 根据pcVersion来获取项目数据
     *
     * @param msg http请求数据
     * @return
     */
    public static ResponseData getProjectByPCVersion(Object msg) {
        ResponseData responseData = new ResponseData(StatusCode.ERROR.getValue());
        SqlSession sqlSession = MybatisUtils.getSession();
        OSSClient ossClient = OssOpt.initOssClient();
        String message = "";
        try {
            //获取解析的项目标识参数
            Map<String, Object> map = FormData.getParam(msg);
            String pcVersion = (String) map.get(Common.PC_VERSION);
            Project project = sqlSession.selectOne(Mapper.GET_PROJECT_BY_PCVERSION, pcVersion);

            //如果项目数据不为空则返回该数据
            if (project != null) {
                //从OSS中获取pc version版本的project data
                String projectDataFile = GlobalConfig.getOssFileUrl(Common.PROJECT_DATA) +
                        pcVersion + Common.PROJECT_DATA_SUFFIX;
                String projectData = OssOpt.getOssFile(ossClient, projectDataFile);

                //检测OSS中读取的数据是否有效
                if (CommonService.checkNotNull(projectData)) {
                    project.setProject_data(projectData);
                    Assemble.responseSuccessSetting(responseData, project);

                } else {
                    message = "getProjectByPCVersion data from oss null";
                    CreateProject.logger.warn(message);
                    Assemble.responseErrorSetting(responseData, 400, message);
                }
            }

        } catch (Exception e) {
            message = "System error";
            CreateProject.logger.debug(message, e);
            Assemble.responseErrorSetting(responseData, 500, message);

        } finally {
            CommonService.databaseCommitClose(sqlSession, responseData, false);
            OssOpt.shutDownOssClient(ossClient);
        }
        return responseData;
    }


    /**
     * 新建项目数据插入到OSS中，有pc版和mobile版
     *
     * @param project 项目数据
     */
    public static void insertNewProjectToOss(Project project, OSSClient ossClient) {
        //OSS创建空项目的project的数据文件
        String pcVersionData = GlobalConfig.getOssFileUrl(Common.PROJECT_DATA) + project.getPc_version() + Common.PROJECT_DATA_SUFFIX;
        String moVersionData = GlobalConfig.getOssFileUrl(Common.PROJECT_DATA) + project.getMo_version() + Common.PROJECT_DATA_SUFFIX;

        //如果选择版本是创建手机版则电脑版的project data为空，否则手机版的project data为空
        if (Objects.equals(project.getVersion(), Common.MOBILE_V)) {
            OssOpt.uploadFileToOss(pcVersionData, new byte[0], ossClient);
            OssOpt.uploadFileToOss(moVersionData, project.getProject_data().getBytes(), ossClient);
        } else {
            OssOpt.uploadFileToOss(pcVersionData, project.getProject_data().getBytes(), ossClient);
            OssOpt.uploadFileToOss(moVersionData, new byte[0], ossClient);
        }
    }


    /**
     * 根据传入的操作类型执行特定的不同
     *
     * @param sqlSession sql句柄
     * @param sql        sql操作引用
     */
    private static int optHandler(SqlSession sqlSession, Project project, String sql) {
        int num = 0;
        switch (project.getOpt()) {
            case 1: {
                //新项目创建，由于是根目录，无需跟新父项目child个数
                num = sqlSession.insert(sql, project);
                break;
            }
            case 2: {
                //子项目创建，更新父项目的child个数
                num = sqlSession.insert(sql, project);
                //如果该页面为子页面，则增加子页面后，父页面的子页面数目更改
                ProjectList.addParentChildPageNum(project.getParent(), sqlSession);
                break;
            }
            case 3: {
                //重构当前页面，无需更新任何数据库条目，只需更新OSS即可
                num = 1;
                break;
            }
            default: {
                break;
            }
        }
        return num;
    }

}










