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
            getStoreDataLogic(projects, responseData);

        } catch (Exception e) {
            StoreList.logger.error("getTargetStoreWebModel catch exception", e);
            Assemble.responseErrorSetting(responseData, 500,
                    "getTargetStoreWebModel Data with System Error");

        } finally {
            StoreList.logger.debug("ResponseData responseData" + responseData);
            sqlSession.close();
        }
        return responseData;
    }

    /**
     * 数据库返回数据后进行逻辑处理，并打包成ResponseData数据
     *
     * @param projects     所有项目对象信息
     * @param responseData 返回数据打包
     */
    private static void getStoreDataLogic(List<Project> projects, ResponseData responseData) {

        //如果projects不为null则数据库查询projects成功，返回该projects数据到前端
        //projects.size()为0也是可以的，说明该用户尚未创建任何项目
        if (projects != null) {
            Assemble.responseSuccessSetting(responseData, projects);
        } else {
            StoreList.logger.error("getTargetStoreWebModel with Database Error");
            Assemble.responseErrorSetting(responseData, 401, "getTargetStoreWebModel with Database Error");
        }
    }

}
