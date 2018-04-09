package FrontEnd.myBatis.operation.personal;

import FrontEnd.helper.common.Assemble;
import FrontEnd.helper.common.Common;
import FrontEnd.helper.common.Mapper;
import FrontEnd.helper.common.OssOpt;
import FrontEnd.helper.config.GlobalConfig;
import FrontEnd.helper.parser.form.FormData;
import FrontEnd.myBatis.MybatisUtils;
import FrontEnd.myBatis.entity.User;
import FrontEnd.myBatis.entity.response.ResponseData;
import FrontEnd.myBatis.entity.response.StatusCode;
import FrontEnd.myBatis.operation.common.CommonService;
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
                Assemble.responseErrorSetting(responseData, 401,
                        "Personal updateUserInfo num: " + num);
            }
        } catch (Exception e) {
            Assemble.responseErrorSetting(responseData, 500, "Personal updateUserInfo error");

        } finally {
            CommonService.databaseCommitClose(sqlSession, responseData, true);
            OssOpt.shutDownOssClient(ossClient);
        }
        return responseData;
    }
}
