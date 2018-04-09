package FrontEnd.helper.common;

import FrontEnd.myBatis.entity.response.ResponseData;
import FrontEnd.myBatis.entity.response.StatusCode;

/**
 * Created by Administrator on 2018/2/2.
 */
public class Assemble {

    /**
     * 返回异常消息打包
     * @param responseData 返回消息体
     * @param exception_code 返回消息异常代号
     * @param exception 返回消息异常信息
     */
    public static void responseErrorSetting(ResponseData responseData,int exception_code, String exception){
        responseData.setStatus_code(StatusCode.ERROR.getValue());
        responseData.setException_code(exception_code);
        responseData.setException(exception);
    }

    /**
     * 返回成功成功打包
     * @param responseData 返回消息体
     * @param object 成功消息的数据
     */
    public static void responseSuccessSetting(ResponseData responseData,Object object){
        responseData.setStatus_code(StatusCode.OK.getValue());
        responseData.setData(object);
    }
}
