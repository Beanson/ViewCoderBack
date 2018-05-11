package viewcoder.operation.impl.project;

import viewcoder.exception.project.PSDAnalysisException;
import viewcoder.tool.common.Common;
import viewcoder.tool.common.Mapper;
import viewcoder.tool.common.OssOpt;
import viewcoder.tool.config.GlobalConfig;
import viewcoder.tool.common.Assemble;
import viewcoder.tool.parser.form.FormData;
import viewcoder.tool.util.MybatisUtils;
import viewcoder.operation.entity.Project;
import viewcoder.operation.entity.User;
import viewcoder.operation.entity.UserUploadFile;
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
import java.util.HashMap;
import java.util.Map;

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

        try {
            Project project = (Project) FormData.getParam(msg, Project.class);

            /*新创建空项目数据插入数据库*/
            int num = sqlSession.insert(Mapper.CREATE_EMPTY_PROJECT, project);

            //插入数据库结果分析，如果插入条目大于0则成功，否则失败
            if (num > 0) {
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
            if (responseData.getStatus_code() == StatusCode.OK.getValue()) {
                sqlSession.commit();
            }
            sqlSession.close();
        }
        return responseData;
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
            sqlSession.close();
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
                                          OSSClient ossClient, long preRemainSize)
            throws PSDAnalysisException {

        try {
            //如果保存成功则进行解析PSD文件数据信息并返回内容数据
            PsdAnalysis psdAnalysis = new PsdAnalysis(project.getId(), project.getUser_id(), project.getProject_name(),
                    sqlSession, ossClient);
            psdAnalysis.parse(file);
            PsdInfo psdInfo = psdAnalysis.exportData();

            //如果解析后有项目数据则进行保存更新到数据库
            if (psdInfo != null) {

                //设置psdInfo的project_data数据
                project.setProject_data(JSON.toJSONString(psdInfo));
                int num = sqlSession.update(Mapper.UPDATE_PSD_PROJECT_DATA, project);

                if (num > 0) {
                    //插入成功，返回新生成的project_id和project_data数据
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("id", project.getId());
                    map.put("project_data", project.getProject_data());
                    Assemble.responseSuccessSetting(responseData, map);
                    //更新用户新的可用空间，减去resource size后的大小
                    sqlSession.update(Mapper.UPDATE_USER_RESOURCE_SPACE_REMAIN, new User(project.getUser_id(),
                            String.valueOf(preRemainSize)));

                } else {
                    //数据库更新PSD项目解析后的数据失败
                    throw new PSDAnalysisException("parsePSDFileLogic: Update new PSD project data to database error");
                }
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
            ProjectList.deleteProjectOpt(project.getId(),project.getUser_id());
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
     * 生成Simulate类型的project
     * @param msg 传递的消息类型
     */
    public static void createSimulateProject(Object msg){

        ResponseData responseData = new ResponseData(StatusCode.ERROR.getValue());
        SqlSession sqlSession = MybatisUtils.getSession();

        try{

        }catch (Exception e){
            CreateProject.logger.debug("createSimulateProject error:", e);

        }finally {
            CommonService.databaseCommitClose(sqlSession,responseData,true);
        }
    }

}
