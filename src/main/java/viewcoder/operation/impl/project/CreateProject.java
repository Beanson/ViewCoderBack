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
import viewcoder.url.barrer.SimulateBarrer;

import java.io.File;
import java.util.*;

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

        try {
            Project project = (Project) FormData.getParam(msg, Project.class);

            /*新创建空项目数据插入数据库*/
            int num = sqlSession.insert(Mapper.CREATE_EMPTY_PROJECT, project);

            //插入数据库结果分析，如果插入条目大于0则成功，否则失败
            if (num > 0) {
                //OSS创建空项目的project的数据文件
                String projectDataFile = GlobalConfig.getOssFileUrl(Common.PROJECT_DATA) + project.getTimestamp() + Common.PROJECT_DATA_SUFFIX;
                OssOpt.uploadFileToOss(projectDataFile, project.getProject_data().getBytes(), ossClient);

                //返回新建项目的project_id数据
                Map<String, Integer> map = new HashMap<>();
                map.put("id", project.getId());
                Assemble.responseSuccessSetting(responseData, map);

            } else {
                CreateProject.logger.error("Insert Empty Project Data To Database Error");
                Assemble.responseErrorSetting(responseData, 401,
                        "Insert Empty Project Data To Database Error");
            }
        } catch (Exception e) {
            CreateProject.logger.error("createEmptyProject Server Error", e);
            Assemble.responseErrorSetting(responseData, 500,
                    "Server Error");
        } finally {
            CreateProject.logger.debug("createEmptyProject responseData" + responseData);
            //如果整个流程准确无误地实现则对数据库操作进行提交，否则不提交
            CommonService.databaseCommitClose(sqlSession, responseData, true);
            //关闭oss对象
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

        try {
            //如果保存成功，则对该文件进行解析处理
            //解析psd文件参数变量数据
            project = (Project) FormData.getParam(msg, Project.class);

            //查看用户可用空间是否还满足该PSD文件的解析
            String resourceRemain = sqlSession.selectOne(Mapper.GET_USER_RESOURCE_SPACE_REMAIN, project.getUser_id());
            long preRemainSize = Integer.parseInt(resourceRemain) - project.getPsd_file().length();
            //如果该用户有足够空间进行PSD文件解析，则创建该项目，否则不创建
            if (preRemainSize > 0) {
                //a.设置项目占用空间，b.新建psd的project项目数据并插入数据库，从而获取project_id
                project.setResource_size(String.valueOf(project.getPsd_file().length()));
                int insert_num = sqlSession.insert(Mapper.CREATE_PSD_PROJECT, project);

                //如果插入数据库成功则进行解析PSD文件
                if (insert_num > 0) {
                    //解析file文件数据并保存到指定位置，如果解析成功将删除该psd文件，如果解析失败将保留作为后续程序调优参考文件
                    parsePSDFileLogic(responseData, project.getPsd_file().getFile(), project, sqlSession, ossClient,
                            preRemainSize);

                } else {
                    //数据库插入失败
                    Assemble.responseErrorSetting(responseData, 401,
                            "Insert new PSD project to database error");
                }
            } else {
                Assemble.responseErrorSetting(responseData, 402, "User resource space not enough");
            }

        } catch (Exception e) {
            CreateProject.logger.error("createPSDProject Server Error", e);
            Assemble.responseErrorSetting(responseData, 500,
                    "Server Error");

        } finally {
            //如果该PSD文件无法解析则上传到OSS云端
            uploadErrorPsdFile(responseData, project, sqlSession, ossClient);
            //资源关闭和释放
            CommonService.databaseCommitClose(sqlSession, responseData, true);
            OssOpt.shutDownOssClient(ossClient);
            CreateProject.logger.debug("createPSDProject responseData" + responseData);
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
            PsdAnalysis psdAnalysis = new PsdAnalysis(project.getId(), project.getUser_id(), project.getProject_name(),
                    sqlSession, ossClient);
            psdAnalysis.parse(file);
            PsdInfo psdInfo = psdAnalysis.exportData();

            //如果解析后有项目数据则进行保存更新到数据库
            if (psdInfo != null) {

                //设置psdInfo的project_data数据到OSS中
                project.setProject_data(JSON.toJSONString(psdInfo));
                String projectDataFile = GlobalConfig.getOssFileUrl(Common.PROJECT_DATA) + project.getTimestamp() +
                        Common.PROJECT_DATA_SUFFIX;
                OssOpt.uploadFileToOss(projectDataFile, project.getProject_data().getBytes(), ossClient);

                //插入成功，返回新生成的project_id和project_data数据
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("id", project.getId());
                map.put("project_data", project.getProject_data());
                Assemble.responseSuccessSetting(responseData, map);
                //更新用户新的可用空间，减去resource size后的大小
                sqlSession.update(Mapper.UPDATE_USER_RESOURCE_SPACE_REMAIN, new User(project.getUser_id(),
                        String.valueOf(preRemainSize)));

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
     * @param sqlSession   sql句柄
     */
    private static void uploadErrorPsdFile(ResponseData responseData, Project project, SqlSession sqlSession, OSSClient ossClient) {
        //对status code进行判断处理
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

        } else {
            sqlSession.commit();
        }
    }


    /**
     * 把resource数据插入到数据库user_upload_file表中，对外开发调用
     *
     * @param projectId    项目id，用来标识项目的唯一性
     * @param userId       用户id，用来标识注册了的用户的唯一性
     * @param widgetType   组件类型，如Common_Image等
     * @param fileType     资源类型，分别是 1:图片资源, 2:视频资源, 3:音频资源, 4:下载文件资源
     * @param isFolder     1：表示文件夹，0：表示非文件夹文件
     * @param timeStamp    时间戳，用来唯一性标识非文件夹文件的文件名
     * @param suffix       非文件夹文件的文件后缀
     * @param fileName     文件夹 或 非文件夹文件 的 文件名
     * @param relativePath 文件夹 或 非文件夹文件 相对于文件存储基路径下的 相对路径
     * @param fileSize     非文件夹文件的大小
     * @param videoImage   视频文件的帧图片文件全名，包含后缀名，用来展示给用户视频未播放前的一帧图片
     * @param createTime   该资源的创建时间/插入数据库时间
     * @param sqlSession   mybatis数据库操作session句柄
     */
    public static int insertWidgetToDB(int projectId, int userId, String widgetType, int fileType, int isFolder,
                                       String timeStamp, String suffix, String fileName, String relativePath,
                                       String fileSize, String videoImage, String createTime, SqlSession sqlSession) {

        return sqlSession.insert(Mapper.INSERT_NEW_RESOURCE, new UserUploadFile(projectId, userId, widgetType, fileType, isFolder, timeStamp,
                suffix, fileName, relativePath, fileSize, videoImage, createTime));

    }


    /**
     * ****************************************************************************
     * 生成Simulate类型的project，根据URL地址，生成和其类似的网页
     *
     * @param msg 传递的消息类型
     */
    public static ResponseData createSimulateProject(Object msg) {

        ResponseData responseData = new ResponseData(StatusCode.ERROR.getValue());
        try {
            //获取从前端传递过来的数据
            Map<String, Object> map = FormData.getParam(msg);
            String projectName = (String) map.get(Common.PROJECT_NAME);
            String webUrl = (String) map.get(Common.WEB_URL);
            String timestamp = (String) map.get(Common.TIME_STAMP);
            String versions = (String) map.get(Common.VERSIONS);
            Integer userId = Integer.parseInt((String) map.get(Common.USER_ID));
            Integer browserWidth = Integer.parseInt((String) map.get(Common.BROWSER_WIDTH));
            Integer browserHeight = Integer.parseInt((String) map.get(Common.BROWSER_HEIGHT));

            //初始化项目创建的进度
            ProjectProgress projectProgress = new ProjectProgress(userId, Common.PROJECT_SIMULATE, projectName, timestamp, 0);
            CommonObject.getProgressList().add(projectProgress);
            //异步解析URL网站元素操作
            createSimulateOpt(webUrl, projectProgress, browserWidth, browserHeight, userId, projectName, versions);
            //正确解析传递的参数及其类型，并成功调用URL解析网站元素操作，返回正确数据
            Assemble.responseSuccessSetting(responseData, null);

        } catch (Exception e) {
            CreateProject.logger.debug("createSimulateProject error:", e);
        }
        return responseData;
    }


    /**
     * 异步解析URL网站元素操作
     * TODO 后面采用云主机进行请求响应，而慢操作放在物理主机中运行
     */
    public static void createSimulateOpt(String webUrl, ProjectProgress projectProgress, int browserWidth, int browserHeight,
                                         int userId, String projectName, String versions) throws Exception {
        //创建解析URL网站元素的线程
        Thread simulateParseThread = new Thread(new Runnable() {
            @Override
            public void run() {
                SqlSession sqlSession = null;
                ResponseData responseData = new ResponseData(StatusCode.ERROR.getValue());//用来记录程序执行状态
                OSSClient ossClient = OssOpt.initOssClient();
                try {
                    //获取URL资源项目元数据
                    String projectData = Simulate.createProject(webUrl, projectProgress, browserWidth, browserHeight);

                    //如果获取解析后项目数据不为空则进行插入数据库等操作，否则全局变量中记录错误消息
                    if (projectData != null) {
                        //准备project实体类数据
                        Project project = new Project();
                        project.setUser_id(userId);
                        project.setProject_name(projectName);
                        //设置该timestamp操作后数据库和缓存同步, 也是后续single_export和project_data的文件名
                        project.setTimestamp(projectProgress.getTimeStamp());
                        project.setLast_modify_time(CommonService.getDateTime());

                        //把新创建的simulate的project插入数据库, 在这里启动数据库操作，不然可能会报超时错
                        sqlSession = MybatisUtils.getSession();
                        int num = sqlSession.insert(Mapper.CREATE_SIMULATE_PROJECT, project);
                        if (num > 0) {
                            //project_data数据同步到OSS中
                            String projectDataFile = GlobalConfig.getOssFileUrl(Common.PROJECT_DATA) +
                                    project.getTimestamp() + Common.PROJECT_DATA_SUFFIX;
                            OssOpt.uploadFileToOss(projectDataFile, projectData.getBytes(), ossClient);

                            Assemble.responseSuccessSetting(responseData, null);
                            CreateProject.logger.debug("createSimulateOpt success, project id is: " + project.getId());
                            projectProgress.setProgress(100);
                        } else {
                            projectProgress.setProgress(-1); //设置进度信息，插入数据库操作失败
                            CreateProject.logger.debug("createSimulateOpt with DB error, num<=0 ");
                        }
                    } else {
                        projectProgress.setProgress(-2); //设置进度信息，获取project信息失败
                        CreateProject.logger.debug("createSimulateOpt with error, get projectData null ");
                    }
                } catch (Exception e) {
                    CreateProject.logger.debug("createSimulateOpt error:", e);
                    projectProgress.setProgress(-3); //设置进度信息，系统发生错误

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

        try {
            //获取解析的项目标识参数
            Map<String, Object> map = FormData.getParam(msg);
            String timestamp = (String) map.get(Common.TIME_STAMP);

            //遍历所有正在创建的PSD或URL项目，根据timestamp获取其对应进度
            List<ProjectProgress> list = CommonObject.getProgressList();
            Iterator<ProjectProgress> iterator = list.iterator();
            while (iterator.hasNext()) {
                ProjectProgress progress = iterator.next();
                if (progress.getTimeStamp() != null && progress.getTimeStamp().equals(timestamp)) {
                    //找到条目，则返回进度信息
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
            CreateProject.logger.debug("getSimulateRate error:", e);
        }
        return responseData;
    }


    /**
     * 根据timestamp来获取项目数据
     *
     * @param msg http请求数据
     * @return
     */
    public static ResponseData getProjectByTimeStamp(Object msg) {
        ResponseData responseData = new ResponseData(StatusCode.ERROR.getValue());
        SqlSession sqlSession = MybatisUtils.getSession();
        OSSClient ossClient = OssOpt.initOssClient();
        try {
            //获取解析的项目标识参数
            Map<String, Object> map = FormData.getParam(msg);
            String timestamp = (String) map.get(Common.TIME_STAMP);
            Project project = sqlSession.selectOne(Mapper.GET_PROJECT_BY_TIMESTAMP, timestamp);
            //如果项目数据不为空则返回该数据
            if (project != null) {
                //从OSS中获取timestamp版本的project data
                String projectDataFile = GlobalConfig.getOssFileUrl(Common.PROJECT_DATA) +
                        timestamp + Common.PROJECT_DATA_SUFFIX;
                String projectData = OssOpt.getOssFile(ossClient, projectDataFile);

                //检测OSS中读取的数据是否有效
                if (CommonService.checkNotNull(projectData)) {
                    project.setProject_data(projectData);
                    Assemble.responseSuccessSetting(responseData, project);
                } else {
                    Assemble.responseErrorSetting(responseData, 400,
                            "getProjectByTimeStamp: project data from oss null");
                }
            }
        } catch (Exception e) {
            CreateProject.logger.debug("getProjectByTimeStamp error:", e);

        } finally {
            CommonService.databaseCommitClose(sqlSession, responseData, false);
            OssOpt.shutDownOssClient(ossClient);
        }
        return responseData;
    }

}
