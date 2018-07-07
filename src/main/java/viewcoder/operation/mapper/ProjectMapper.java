package viewcoder.operation.mapper;

/**
 * Created by Administrator on 2017/2/8.
 */

import viewcoder.operation.entity.Project;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

public interface ProjectMapper {

    /********************以下是获取项目信息操作***********************/
    //根据用户user_id获取该用户所有project的数据
    @Select("select * from project where user_id=#{user_id} and parent=#{parent}")
    public List<Project> getProjectListData(@Param("user_id") String user_id, @Param("parent") String parent);

    //根据项目id获取该项目数据信息
    @Select("select * from project where id=#{id}")
    public Project getProjectData(int id);

    //根据项目timestamp获取该项目数据信息, 需要返回所有，后node服务端生成HTML页面
    @Select("select * from project where timestamp=#{timestamp}")
    public Project getProjectDataByTimestamp(String timestamp);

    //根据项目id获取对应project的resource_size数据信息
    @Select("select resource_size from project where id=#{id}")
    public String getProjectResourceSize(int id);

    //根据项目id获取对应project的resource_size数据信息
    @Select("select id, project_name, timestamp, last_modify_time, pc_version, mo_version, resource_size, usage_amount, points " +
            "from project where is_public=1 and industry_code=#{industry_code} and industry_sub_code=#{industry_sub_code}")
    public List<Project> getTargetStoreData(@Param("industry_code") String industry_code, @Param("industry_sub_code") String industry_sub_code);


    /********************以下是创建项目操作***********************/
    //创建新建project
    @Insert("insert into project(user_id,project_name,timestamp,last_modify_time,pc_version,resource_size) " +
            "values(#{user_id},#{project_name},#{timestamp},#{last_modify_time},#{timestamp},#{resource_size})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    public int createEmptyProject(Project project);

    //创建新建PSD的project
    //不需insert psd_file_name这条记录，因为psd文件只在内存中解析，如果解析出错保存psd文件到OSS中的error_psd_file中
    @Insert("insert into project(user_id,project_name,timestamp,pc_version,last_modify_time,resource_size) " +
            "values(#{user_id},#{project_name},#{timestamp},#{timestamp}, #{last_modify_time},#{resource_size})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    public int createPSDProject(Project project);

    //拷贝后，创建新的project项目
    @Insert("insert into project(user_id,project_name,timestamp,last_modify_time,pc_version,mo_version,resource_size,ref_id) " +
            "values(#{user_id},#{project_name},#{timestamp},#{last_modify_time},#{pc_version},#{mo_version},#{resource_size},#{ref_id})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    public int createCopyProject(Project project);

    //通过URL，创建一个和source URL类似的Simulate project
    @Insert("insert into project(user_id,parent,project_name,timestamp,last_modify_time,pc_version) " +
            "values(#{user_id},#{parent},#{project_name},#{timestamp},#{last_modify_time},#{timestamp})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    public int createSimulateProject(Project project);


    /********************以下是删除项目操作***********************/
    //根据project_id删除project表对应条目的数据
    @Delete("delete from project where id=#{id}")
    public int deleteProjectById(int id);

    //测试在数据库中根据项目文件名删除项目
//    @Delete("delete from project where timestamp=#{timestamp}")
//    public int deleteProjectByFileName(String timestamp);


    /********************以下是更新项目名操作***********************/
    //更新project名称
    @Update("update project set project_name=#{project_name} where id=#{id}")
    public int modifyProjectName(Project Project);

    //更新project条目内容
    //根据项目传递过来参数进行更新项目公开程度状态
    @SelectProvider(type = SqlProvider.class, method = "saveProjectData")
    public int saveProjectData(Map<String, Object> map);

    //更新project的resource_size的大小
    @Update("update project set resource_size=#{resource_size} where id=#{id}")
    public int updateProjectResourceSize(Project Project);

    //根据项目传递过来参数进行更新项目公开程度状态
    @SelectProvider(type = SqlProvider.class, method = "updateProjectOpenness")
    public int updateProjectOpenness(Map<String, Object> map);

    //更新project的引用次数
    @Update("update project set usage_amount=usage_amount+1 where id=#{projectId}")
    public int updateUsageAmount(int projectId);

    //更新child页面的数目
    @Update("update project set child=child+1 where id=#{projectId}")
    public int updateChildNum(int projectId);

}













