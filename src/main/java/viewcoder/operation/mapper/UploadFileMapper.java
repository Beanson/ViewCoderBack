package viewcoder.operation.mapper;

import viewcoder.operation.entity.UserUploadFile;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * Created by Administrator on 2017/6/1.
 */
public interface UploadFileMapper {

    /********************以下是选择文件操作***********************/
    //查询对应project_id下的所有资源文件
    @Select("select * from user_upload_file where project_id=#{project_id}")
    public List<UserUploadFile> getAllResourceByProjectId(int project_id);

    //查询对应id的资源信息
    @Select("select * from user_upload_file where id=#{id}")
    public UserUploadFile getResourceData(int id);

    //查询对应user_id的所有资源的名称的信息
    @Select("select time_stamp, suffix from user_upload_file where user_id=#{user_id} and suffix is not null")
    public List<UserUploadFile> getResourceNameData(int user_id);

    //选择该用户对应类型的资源文件
    @Select("select * from user_upload_file where user_id=#{user_id} and file_type=#{file_type}")
    public List<UserUploadFile> getResourceByUserIdAndFileType(@Param("user_id") Integer user_id, @Param("file_type") String file_type);

    //查看user_upload_file表中该time_stamp字段引用数
    @Select("select count(*) from user_upload_file where time_stamp=#{timeStamp}")
    public int getResourceRefCount(String timeStamp);

    //如果删除的是文件夹则查找该文件夹下所有子文件资源
    @Select("select * from user_upload_file where project_id=#{project_id} and user_id=#{user_id} " +
            "and file_type=#{file_type} and relative_path like concat(#{relative_path},'%') ")
    public List<UserUploadFile> getFolderSubResource(UserUploadFile userUploadFile);

    //查找是否存在该记录在数据库中
    @Select("select count(*) from user_upload_file where project_id=#{project_id} and is_folder=#{is_folder} " +
            "and file_name=#{file_name} and relative_path=#{relative_path}")
    public int getRootFolderCount(UserUploadFile userUploadFile);



    /********************以下是删除文件操作***********************/
    //根据资源的id删除资源文件记录
    @Delete("delete from user_upload_file where id=#{id}")
    public int deleteResourceById(int id);

    //根据project_id删除user_upload_file表对应条目的数据
    @Delete("delete from user_upload_file where project_id=#{project_id}")
    public int deleteResourceByProjectId(int project_id);



    /********************以下是插入文件操作***********************/
    //插入新文件信息记录
    @Insert("insert into user_upload_file(project_id,user_id,widget_type,file_type,is_folder,time_stamp,suffix,file_name,relative_path," +
            "file_size,video_image_name) values(#{project_id},#{user_id},#{widget_type},#{file_type},#{is_folder},#{time_stamp}," +
            "#{suffix},#{file_name},#{relative_path},#{file_size},#{video_image_name})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    public int insertNewResource(UserUploadFile userUploadFile);


    @InsertProvider(type = SqlProvider.class, method = "insertBatchNewResource")
    public int insertBatchNewResource(@Param("list") List<UserUploadFile> files);


    /********************以下是更新文件信息操作***********************/
    //更新文件信息
    @Update("update user_upload_file set time_stamp=#{time_stamp} where id=#{id}")
    public int updateResourceTimeStamp(UserUploadFile userUploadFile);

    //重命名资源文件
    @Update("update user_upload_file set file_name=#{new_file_name} where id=#{id}")
    public int renameResourceById(@Param("new_file_name") String new_file_name, @Param("id") Integer id);

    //更新上传的视频文件
    @Update("update user_upload_file set video_image_name=#{video_image_name} where id=#{video_id}")
    public int updateVideoImage(@Param("video_image_name") String video_image_name, @Param("video_id") Integer video_id);


}





















