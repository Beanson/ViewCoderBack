package viewcoder.operation.impl.personal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import viewcoder.tool.common.*;
import viewcoder.tool.config.GlobalConfig;
import viewcoder.tool.parser.form.FormData;
import viewcoder.tool.util.MybatisUtils;
import viewcoder.operation.entity.User;
import viewcoder.operation.entity.response.ResponseData;
import viewcoder.operation.entity.response.StatusCode;
import viewcoder.operation.impl.common.CommonService;
import com.aliyun.oss.OSSClient;
import org.apache.ibatis.session.SqlSession;

/**
 * Created by Administrator on 2018/2/15.
 */
public class Personal {

    private static Logger logger = LoggerFactory.getLogger(Personal.class);


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
            //新的头像数据不为空则插入oss
            if(CommonService.checkNotNull(user.getPortrait_file())){
                //若旧的头像数据是默认头像则设置新的头像数据，否则直接覆盖旧头像数据
                if(user.getPortrait().equals(Common.DEFAULT_PORTRAIT)){
                    user.setPortrait(CommonService.getTimeStamp()+Common.IMG_PNG);
                }
                //创建新的portrait
                String newPortraitToOss = GlobalConfig.getOssFileUrl(Common.PORTRAIT_IMG) + user.getPortrait();
                OssOpt.uploadFileToOss(newPortraitToOss, user.getPortrait_file().get(), ossClient);
            }

            //更新数据库操作
            int num = sqlSession.update(Mapper.UPDATE_USER_INFO, user);
            if (num > 0) {
                //返回最新user数据
                User userNew = sqlSession.selectOne(Mapper.GET_USER_DATA, user.getId());
                userNew.setSession_id(CommonObject.getLoginVerify().get(user.getId()));
                Assemble.responseSuccessSetting(responseData, userNew);

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
