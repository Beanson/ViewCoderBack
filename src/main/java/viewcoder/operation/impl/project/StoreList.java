package viewcoder.operation.impl.project;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import viewcoder.tool.common.Assemble;
import viewcoder.tool.common.Common;
import viewcoder.tool.common.Mapper;
import viewcoder.tool.parser.form.FormData;
import viewcoder.tool.util.MybatisUtils;
import viewcoder.operation.entity.Project;
import viewcoder.operation.entity.response.ResponseData;
import viewcoder.operation.entity.response.StatusCode;
import viewcoder.operation.impl.common.CommonService;
import org.apache.ibatis.session.SqlSession;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/2/3.
 */
public class StoreList {

    private static Logger logger = LoggerFactory.getLogger(StoreList.class);

    /**
     * ****************************************************************************
     * 根据用户id获取用户所有projects的数据
     */
    public static ResponseData getTargetStoreWebModel(Object msg) {

        ResponseData responseData = new ResponseData(StatusCode.ERROR.getValue());
        SqlSession sqlSession = MybatisUtils.getSession();
        String message = "";

        try {
            //接收前台传过来关于获取指定行业的project model数据
            Map<String, Object> data = FormData.getParam(msg, Common.USER_ID, Common.INDUSTRY_CODE, Common.INDUSTRY_SUB_CODE);

            //查找数据库返回storeList的数据
            List<Project> projects = sqlSession.selectList(Mapper.GET_TARGET_STORE_DATA, data);

            //进行projects数据,并打包成ResponseData格式并回传
            //projects为null也是可以的，说明尚未有任何该类型的商城项目
            Assemble.responseSuccessSetting(responseData, projects);

        } catch (Exception e) {
            message = "System error";
            StoreList.logger.error(message, e);
            Assemble.responseErrorSetting(responseData, 500, message);

        } finally {
            CommonService.databaseCommitClose(sqlSession, responseData, false);
        }
        return responseData;
    }


    /**
     * 更新project的openness状态
     *
     * @return
     */
    public static ResponseData updateProjectOpenness(Object msg) {
        ResponseData responseData = new ResponseData(StatusCode.ERROR.getValue());
        SqlSession sqlSession = MybatisUtils.getSession();
        String message = "";
        try {
            //接收前台传过来关于获取指定行业的project model数据
            Map<String, Object> data = FormData.getParam(msg);

            //更新openness状态并返回影响条目数量
            int num = sqlSession.update(Mapper.UPDATE_PROJECT_OPENNESS, data);

            //进行projects数据,并打包成ResponseData格式并回传
            if (num != 0) {
                Assemble.responseSuccessSetting(responseData, null);

            } else {
                message = "updateProjectOpenness with Database Error";
                StoreList.logger.error(message);
                Assemble.responseErrorSetting(responseData, 401, message);
            }

        } catch (Exception e) {
            message = "System error";
            StoreList.logger.error(message, e);
            Assemble.responseErrorSetting(responseData, 500, message);

        } finally {
            CommonService.databaseCommitClose(sqlSession, responseData, true);
        }
        return responseData;
    }


    /**
     * 更新最新用户在project_store页面选择的industry类型到数据库
     *
     * @param msg
     * @return
     */
    public static ResponseData updateLastSelectedIndustry(Object msg) {
        ResponseData responseData = new ResponseData(StatusCode.ERROR.getValue());
        SqlSession sqlSession = MybatisUtils.getSession();
        String message = "";
        try {
            //接收前台传过来关于获取指定行业的project model数据
            //传递三个数据：user_id, industry_code, industry_sub_code
            Map<String, Object> data = FormData.getParam(msg);

            //更新openness状态并返回影响条目数量
            int num = sqlSession.update(Mapper.UPDATE_USER_LAST_SELECTED_INDUSTRY, data);

            //进行projects数据,并打包成ResponseData格式并回传
            if (num != 0) {
                Assemble.responseSuccessSetting(responseData, null);

            } else {
                message = "updateLastSelectedIndustry with Database Error";
                StoreList.logger.error(message);
                Assemble.responseErrorSetting(responseData, 401, message);
            }

        } catch (Exception e) {
            message = "System error";
            StoreList.logger.error(message, e);
            Assemble.responseErrorSetting(responseData, 500, message);

        } finally {
            CommonService.databaseCommitClose(sqlSession, responseData, true);
        }
        return responseData;
    }

}
