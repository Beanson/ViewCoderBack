package FrontEnd.helper.parser.form;

import java.util.HashMap;

/**
 * Created by Administrator on 2017/6/26.
 */
public class FormData {

    /**
     * 解析单一参数，并返回该参数的值，建议1个参数时用该方法
     * @param singleParam 参数名称
     * @return 参数值
     */
    public static String getParam(Object msg,String singleParam){
        return (String)new FormDataParse(msg,singleParam).getFormData(FormDataEnumType.SINGLE_PARAM);
    }

    /**
     * 如果有多个参数且无法确定参数值时，直接从http获取参数键值对
     * @return 用HashMap装载参数的值
     */
    public static HashMap<String,Object> getParam(Object msg){
        return (HashMap<String, Object>) new FormDataParse(msg).getFormData(FormDataEnumType.MULTI_PARAMS);
    }

    /**
     * 解析多参数，并用HashMap键值对方式装载返回值，建议2个以上参数用该方法
     * @param params 可变个数的参数
     * @return 用HashMap装载参数的值
     */
    public static HashMap<String,Object> getParam(Object msg,String ...params){
        return (HashMap<String, Object>) new FormDataParse(msg,params).getFormData(FormDataEnumType.MULTI_PARAMS);
    }


    /**
     * 解析实体类参数对象，并返回该实体类实例，建议4个及以上参数时用该方法
     * @param clazz 实体列对象
     * @return 返回实体类实例
     */
    public static Object getParam(Object msg,Class<?> clazz){
        return new FormDataParse(msg,clazz).getFormData(FormDataEnumType.ENTITY);
    }

}
