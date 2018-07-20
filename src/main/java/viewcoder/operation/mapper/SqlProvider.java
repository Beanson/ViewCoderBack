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
     * 保存项目操作，根据不同传递的版本号参数更新不同内容
     *
     * @param project 项目数据
     * @return
     * @deprecated 无需更新pc_version和mo_version, 一次生成以后都使用通用的version
     */
    public String saveProjectData(Project project) {
        StringBuilder stringBuilder = new StringBuilder();

        //初始化固定更新部分
        stringBuilder.append("update project set last_modify_time='" + project.getLast_modify_time() + "'");
        stringBuilder.append(" ,timestamp='" + project.getTimestamp() + "'");

        //根据传递的版本类型而对应更新不同版本数据
        if (CommonService.checkNotNull(project.getMo_version())) {
            stringBuilder.append(" ,mo_version='" + project.getTimestamp() + "'");
        } else {
            stringBuilder.append(" ,pc_version='" + project.getTimestamp() + "'");
        }
        stringBuilder.append(" where id=" + project.getId());

        return stringBuilder.toString();
    }


    /**
     * 返回插入该文件下所有上传的file记录
     * @param files 插入数据库的list文件
     * @return
     */
    public String insertBatchNewResource(List<UserUploadFile> files) {
        StringBuilder stringBuilder = new StringBuilder();

        //数据插入字段定义
        stringBuilder.append("insert into user_upload_file(project_id, user_id, widget_type, is_folder, " +
                "time_stamp, suffix, file_name, relative_path, file_size, video_image_name, create_time) values \n");

        //数据级联添加
        for (int i = 0; i < files.size(); i++) {
            UserUploadFile file = files.get(i);
            stringBuilder.append("('" + file.getProject_id() + "', '" + file.getUser_id() +
                    "', '" + file.getWidget_type() + "', '" + file.getIs_folder() + "', '" + file.getTime_stamp() +
                    "', '" + file.getSuffix() + "', '" + file.getFile_name() + "', '" + file.getRelative_path() +
                    "', '" + file.getFile_size() + "', '" + file.getVideo_image_name() + "', '" + file.getCreate_time() + "')");

            //逗号分行
            if (i + 1 < files.size()) {
                stringBuilder.append(",\n");
            }
        }
        //添加结束符
        stringBuilder.append(";");
        return stringBuilder.toString();
    }

}





