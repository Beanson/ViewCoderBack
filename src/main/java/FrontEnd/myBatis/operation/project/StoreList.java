package FrontEnd.myBatis.operation.project;

import FrontEnd.exceptions.project.ProjectListException;
import FrontEnd.helper.common.Assemble;
import FrontEnd.helper.common.Common;
import FrontEnd.helper.common.Mapper;
import FrontEnd.helper.common.OssOpt;
import FrontEnd.helper.config.GlobalConfig;
import FrontEnd.helper.parser.form.FormData;
import FrontEnd.myBatis.MybatisUtils;
import FrontEnd.myBatis.entity.Project;
import FrontEnd.myBatis.entity.User;
import FrontEnd.myBatis.entity.UserUploadFile;
import FrontEnd.myBatis.entity.response.ResponseData;
import FrontEnd.myBatis.entity.response.StatusCode;
import FrontEnd.myBatis.operation.common.CommonService;
import com.aliyun.oss.OSSClient;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/2/3.
 */
public class StoreList {

    private static Logger logger = Logger.getLogger(StoreList.class);

    /**
     * ****************************************************************************
     * 根据用户id获取用户所有projects的数据
     */
    public static ResponseData getTargetStoreWebModel(Object msg) {

        ResponseData responseData = new ResponseData(StatusCode.ERROR.getValue());
        SqlSession sqlSession = MybatisUtils.getSession();

        try {
            //接收前台传过来关于获取指定行业的project model数据
            Map<String, Object> data = FormData.getParam(msg, Common.USER_ID, Common.INDUSTRY_CODE, Common.INDUSTRY_SUB_CODE);
            sqlSession = MybatisUtils.getSession();

            //查找数据库返回storeList的数据
            List<Project> projects = sqlSession.selectList(Mapper.GET_TARGET_STORE_DATA, data);

            //进行projects数据,并打包成ResponseData格式并回传
            //projects为null也是可以的，说明尚未有任何该类型的商城项目
            Assemble.responseSuccessSetting(responseData, projects);

        } catch (Exception e) {
            StoreList.logger.error("getTargetStoreWebModel catch exception", e);
            Assemble.responseErrorSetting(responseData, 500,
                    "getTargetStoreWebModel Data with System Error");

        } finally {
            CommonService.databaseCommitClose(sqlSession, responseData, false);
        }
        return responseData;
    }


    /**
     * 更新project的openness状态
     * @return
     */
    public static ResponseData updateProjectOpenness(Object msg){
        ResponseData responseData = new ResponseData(StatusCode.ERROR.getValue());
        SqlSession sqlSession = MybatisUtils.getSession();

        try {
            //接收前台传过来关于获取指定行业的project model数据
            Map<String, Object> data = FormData.getParam(msg);
            sqlSession = MybatisUtils.getSession();

            //更新openness状态并返回影响条目数量
            int num = sqlSession.update(Mapper.UPDATE_PROJECT_OPENNESS, data);

            //进行projects数据,并打包成ResponseData格式并回传
            if (num != 0) {
                Assemble.responseSuccessSetting(responseData, null);
            } else {
                StoreList.logger.error("updateProjectOpenness with Database Error");
                Assemble.responseErrorSetting(responseData, 401, "updateProjectOpenness with Database Error");
            }

        } catch (Exception e) {
            StoreList.logger.error("updateProjectOpenness catch exception", e);
            Assemble.responseErrorSetting(responseData, 500,
                    "updateProjectOpenness Data with System Error");

        } finally {
            CommonService.databaseCommitClose(sqlSession, responseData, true);
        }
        return responseData;
    }


    /**
     * 更新最新用户在project_store页面选择的industry类型到数据库
     * @param msg
     * @return
     */
    public static ResponseData updateLastSelectedIndustry(Object msg){
        ResponseData responseData = new ResponseData(StatusCode.ERROR.getValue());
        SqlSession sqlSession = MybatisUtils.getSession();

        try {
            //接收前台传过来关于获取指定行业的project model数据
            //传递三个数据：user_id, industry_code, industry_sub_code
            Map<String, Object> data = FormData.getParam(msg);
            sqlSession = MybatisUtils.getSession();

            //更新openness状态并返回影响条目数量
            int num = sqlSession.update(Mapper.UPDATE_USER_LAST_SELECTED_INDUSTRY, data);

            //进行projects数据,并打包成ResponseData格式并回传
            if (num != 0) {
                Assemble.responseSuccessSetting(responseData, null);
            } else {
                StoreList.logger.error("updateLastSelectedIndustry with Database Error");
                Assemble.responseErrorSetting(responseData, 401, "updateLastSelectedIndustry with Database Error");
            }

        } catch (Exception e) {
            StoreList.logger.error("updateLastSelectedIndustry catch exception", e);
            Assemble.responseErrorSetting(responseData, 500,
                    "updateLastSelectedIndustry Data with System Error");

        } finally {
            CommonService.databaseCommitClose(sqlSession, responseData, true);
        }
        return responseData;
    }

}
