package viewcoder.operation.mapper;

import viewcoder.operation.entity.Project;
import viewcoder.operation.entity.UserUploadFile;
import viewcoder.tool.common.Common;
import viewcoder.operation.impl.common.CommonService;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/3/12.
 */
public class SqlProvider {

    /**
     * 动态根据传递查询条件生成sql语句
     *
     * @param map
     * @return
     */
    public String targetOrdersSqlProvider(Map<String, Object> map) {
        StringBuilder stringBuilder = new StringBuilder();
        if (CommonService.checkNotNull(map.get(Common.USER_ID))) {
            stringBuilder.append("select * from orders where user_id=" + map.get(Common.USER_ID));

            //验证针对service_id的查询
            if (map.get(Common.SERVICE_ID) != null && !map.get(Common.SERVICE_ID).toString().equals("all")) {
                stringBuilder.append(" and service_id=" + map.get(Common.SERVICE_ID));
            }

//            //验证针对pay_status的查询
//            if (map.get(Common.PAY_STATUS) != null  && map.get(Common.PAY_STATUS) != "all") {
//                stringBuilder.append(" and pay_status=" + map.get(Common.PAY_STATUS));
//            }

//            //验证针对pay_way的查询
//            if (map.get(Common.PAY_WAY) != null ) {
//                stringBuilder.append(" and pay_way=" + map.get(Common.PAY_WAY));
//            }

            //针对order_date的左开区间的查询
            if (map.get(Common.ORDER_FROM_DATE) != null) {
                stringBuilder.append(" and order_date>='" + map.get(Common.ORDER_FROM_DATE) + "'");
            }

            //针对order_date的右开区间的查询
            if (map.get(Common.ORDER_END_DATE) != null) {
                stringBuilder.append(" and order_date<='" + map.get(Common.ORDER_END_DATE) + "'");
            }
        }
        return stringBuilder.toString();
    }


    /**
     * 根据项目传递过来参数进行更新项目公开程度状态
     *
     * @param map
     * @return
     */
    public String updateProjectOpenness(Map<String, Object> map) {
        StringBuilder stringBuilder = new StringBuilder();
        int is_public = Integer.parseInt((map.get(Common.IS_PUBLIC)).toString());

        //需检查user_id和project_id同时不为空才继续进行update操作
        if (CommonService.checkNotNull(map.get(Common.USER_ID)) && CommonService.checkNotNull(map.get(Common.PROJECT_ID))) {
            stringBuilder.append("update project set is_public=" + is_public);

            //如果提交公开待审核状态则进行添加对应的industry类型
            if (is_public == 1) {
                stringBuilder.append(" , industry_code='" + map.get(Common.INDUSTRY_CODE) + "'");
                stringBuilder.append(" , industry_sub_code='" + map.get(Common.INDUSTRY_SUB_CODE) + "'");

            } else if (is_public == 0) {
                //若设置不公开则设置industry类型等为空
                stringBuilder.append(" , industry_code=''");
                stringBuilder.append(" , industry_sub_code=''");
            }
            stringBuilder.append(" where id=" + map.get(Common.PROJECT_ID) + " and user_id=" + map.get(Common.USER_ID));
        }
        return stringBuilder.toString();
    }


    /**
     * 返回插入该文件下所有上传的file记录
     * @param parameters 插入数据库的list文件
     * @return
     */
    public String insertBatchNewResource(Map<String, Object> parameters) {
        //获取传递过来的数据
        List<UserUploadFile> files = (List<UserUploadFile>) parameters.get("list");
        StringBuilder stringBuilder = new StringBuilder();
        //有file记录才生成sql操作
        if(CommonService.checkNotNull(files)){
            //数据插入字段定义
            stringBuilder.append("insert into user_upload_file(user_id, widget_type, file_type, is_folder, " +
                    "time_stamp, suffix, file_name, relative_path, file_size, video_image_name) values \n");

            //数据级联添加
            for (int i = 0; i < files.size(); i++) {
                UserUploadFile file = files.get(i);
                stringBuilder.append("('" + file.getUser_id() + "', '" + file.getWidget_type() +
                        "', '" + file.getFile_type() + "', '" + file.getIs_folder() + "', '" + file.getTime_stamp() +
                        "', '" + file.getSuffix() + "', '" + file.getFile_name() + "', '" + file.getRelative_path() +
                        "', '" + file.getFile_size() + "', '" + file.getVideo_image_name() + "')");

                //逗号分行
                if (i + 1 < files.size()) {
                    stringBuilder.append(",\n");
                }
            }
            //添加结束符
            stringBuilder.append(";");
        }

        return stringBuilder.toString();
    }

}





