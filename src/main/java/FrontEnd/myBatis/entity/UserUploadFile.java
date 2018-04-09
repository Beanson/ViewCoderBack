package FrontEnd.myBatis.entity;

import io.netty.handler.codec.http.multipart.FileUpload;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/5/22.
 */
public class UserUploadFile implements Serializable{

    private int id;
    private int project_id;
    private int user_id;
    private String widget_type;
    private int file_type;
    private int is_folder;
    private String time_stamp;
    private String suffix;
    private String file_name;
    private String relative_path;
    private String file_size;
    private String video_image_name;
    private String create_time;
    private FileUpload file;

    public UserUploadFile() {
    }

    //测试使用
    public UserUploadFile(int project_id, int user_id, int file_type, String relative_path) {
        this.project_id = project_id;
        this.user_id = user_id;
        this.file_type = file_type;
        this.relative_path = relative_path;
    }

    public UserUploadFile(int project_id, int user_id, String widget_type, int file_type, int is_folder, String time_stamp, String suffix, String file_name, String relative_path, String file_size, String video_image_name, String create_time) {
        this.project_id = project_id;
        this.user_id = user_id;
        this.widget_type = widget_type;
        this.file_type = file_type;
        this.is_folder=is_folder;
        this.time_stamp = time_stamp;
        this.suffix = suffix;
        this.file_name = file_name;
        this.relative_path = relative_path;
        this.file_size = file_size;
        this.video_image_name=video_image_name;
        this.create_time = create_time;
    }

    public UserUploadFile(int id, int project_id, int user_id, String widget_type, int file_type, int is_folder, String time_stamp, String suffix, String file_name, String relative_path, String file_size, String video_image_name, String create_time, FileUpload file) {
        this.id = id;
        this.project_id = project_id;
        this.user_id = user_id;
        this.widget_type = widget_type;
        this.file_type = file_type;
        this.is_folder=is_folder;
        this.time_stamp = time_stamp;
        this.suffix = suffix;
        this.file_name = file_name;
        this.relative_path = relative_path;
        this.file_size = file_size;
        this.video_image_name = video_image_name;
        this.create_time = create_time;
        this.file = file;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProject_id() {
        return project_id;
    }

    public void setProject_id(int project_id) {
        this.project_id = project_id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getWidget_type() {
        return widget_type;
    }

    public void setWidget_type(String widget_type) {
        this.widget_type = widget_type;
    }

    public int getFile_type() {
        return file_type;
    }

    public void setFile_type(int file_type) {
        this.file_type = file_type;
    }

    public int getIs_folder() {
        return is_folder;
    }

    public void setIs_folder(int is_folder) {
        this.is_folder = is_folder;
    }

    public String getTime_stamp() {
        return time_stamp;
    }

    public void setTime_stamp(String time_stamp) {
        this.time_stamp = time_stamp;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String getFile_name() {
        return file_name;
    }

    public void setFile_name(String file_name) {
        this.file_name = file_name;
    }

    public String getRelative_path() {
        return relative_path;
    }

    public void setRelative_path(String relative_path) {
        this.relative_path = relative_path;
    }

    public String getFile_size() {
        return file_size;
    }

    public void setFile_size(String file_size) {
        this.file_size = file_size;
    }

    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }

    public FileUpload getFile() {
        return file;
    }

    public void setFile(FileUpload file) {
        this.file = file;
    }

    public String getVideo_image_name() {
        return video_image_name;
    }

    public void setVideo_image_name(String video_image_name) {
        this.video_image_name = video_image_name;
    }


    @Override
    public String toString() {
        return "UserUploadFile{" +
                "id=" + id +
                ", project_id=" + project_id +
                ", user_id=" + user_id +
                ", widget_type='" + widget_type + '\'' +
                ", file_type='" + file_type + '\'' +
                ", is_folder=" + is_folder +
                ", time_stamp='" + time_stamp + '\'' +
                ", suffix='" + suffix + '\'' +
                ", file_name='" + file_name + '\'' +
                ", relative_path='" + relative_path + '\'' +
                ", file_size='" + file_size + '\'' +
                ", video_image_name='" + video_image_name + '\'' +
                ", create_time='" + create_time + '\'' +
                ", file=" + file +
                '}';
    }
}
