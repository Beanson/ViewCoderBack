package viewcoder.operation.impl.personal;

import viewcoder.tool.common.Assemble;
import viewcoder.tool.common.Common;
import viewcoder.tool.common.Mapper;
import viewcoder.tool.common.OssOpt;
import viewcoder.tool.config.GlobalConfig;
import viewcoder.tool.parser.form.FormData;
import viewcoder.tool.util.MybatisUtils;
import viewcoder.operation.entity.User;
import viewcoder.operation.entity.response.ResponseData;
import viewcoder.operation.entity.response.StatusCode;
import viewcoder.operation.impl.common.CommonService;
import com.aliyun.oss.OSSClient;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;

/**
 * Created by Administrator on 2018/2/15.
 */
public class Personal {

    private static Logger logger = Logger.getLogger(Personal.class);


    /**
     * 用户更新个人资料信息
     *
     * @param msg http请求信息
     * @return
     */
    public static ResponseData updateUserInfo(Object msg) {

        ResponseData responseData = new ResponseData(StatusCode.ERROR.getValue());
        SqlSession sqlSession = MybatisUtils.getSession();
        OSSClient ossClient = OssOpt.initOssClient();
        String message = "";

        try {
            //获取用户新数据信息
            User user = (User) FormData.getParam(msg, User.class);
            //获取用户旧数据
            User userOrigin = sqlSession.selectOne(Mapper.GET_USER_DATA, user.getId());
            int num = sqlSession.update(Mapper.UPDATE_USER_INFO, user);
            //如果更改条目大于0则继续进行
            if (num > 0) {
                //如果上传有新的portrait文件则进行更新到OSS操作
                if (user.getPortrait_file() != null) {
                    //TODO 直接覆盖旧的portrait也可
                    //删除旧portrait操作
                    String oldPortraitToOss = GlobalConfig.getOssFileUrl(Common.PORTRAIT_IMG) + userOrigin.getPortrait();
                    OssOpt.deleteFileInOss(oldPortraitToOss, ossClient);
                    //创建新的portrait操作
                    String newPortraitToOss = GlobalConfig.getOssFileUrl(Common.PORTRAIT_IMG) + user.getPortrait();
                    OssOpt.uploadFileToOss(newPortraitToOss, user.getPortrait_file().get(), ossClient);
                }
                //返回封装数据到前端
                Assemble.responseSuccessSetting(responseData, null);

            } else {
                message = "Personal updateUserInfo num: " + num;
                Personal.logger.warn(message);
                Assemble.responseErrorSetting(responseData, 401, message);
            }
        } catch (Exception e) {
            message = "Personal updateUserInfo error";
            Personal.logger.error(message, e);
            Assemble.responseErrorSetting(responseData, 500, message);

        } finally {
            CommonService.databaseCommitClose(sqlSession, responseData, true);
            OssOpt.shutDownOssClient(ossClient);
        }
        return responseData;
    }
}
