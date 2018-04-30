package viewcoder.operation.entity;

import io.netty.handler.codec.http.multipart.FileUpload;

/**
 * Created by Administrator on 2017/5/22.
 */
public class Project {

    private int id;
    private int user_id;
    private String project_name;
    private String project_file_name;
    private String last_modify_time;
    private int is_mobile;
    private String project_data;
    private String resource_size;
    private int is_public;
    private String industry_code;
    private String industry_sub_code;
    private int usage_amount;
    private int points;
    private int ref_id;

    private FileUpload psd_file;


    public Project() {
    }

    //更新project的resource_size时用到此方法
    public Project(int id, String resource_size) {
        this.id = id;
        this.resource_size = resource_size;
    }

    //拷贝非psd项目时用到此构造函数
    public Project(int user_id, String project_name, String project_file_name, String last_modify_time, String project_data, String resource_size) {
        this.user_id = user_id;
        this.project_name = project_name;
        this.project_file_name = project_file_name;
        this.last_modify_time = last_modify_time;
        this.project_data = project_data;
        this.resource_size = resource_size;
    }

    public Project(int id, int user_id, String project_name, String project_file_name, String last_modify_time, String project_data, FileUpload psd_file) {
        this.id = id;
        this.user_id = user_id;
        this.project_name = project_name;
        this.project_file_name = project_file_name;
        this.last_modify_time = last_modify_time;
        this.project_data = project_data;
        this.psd_file = psd_file;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getProject_name() {
        return project_name;
    }

    public void setProject_name(String project_name) {
        this.project_name = project_name;
    }

    public String getProject_file_name() {
        return project_file_name;
    }

    public void setProject_file_name(String project_file_name) {
        this.project_file_name = project_file_name;
    }

    public String getLast_modify_time() {
        return last_modify_time;
    }

    public void setLast_modify_time(String last_modify_time) {
        this.last_modify_time = last_modify_time;
    }

    public String getProject_data() {
        return project_data;
    }

    public void setProject_data(String project_data) {
        this.project_data = project_data;
    }

    public FileUpload getPsd_file() {
        return psd_file;
    }

    public void setPsd_file(FileUpload psd_file) {
        this.psd_file = psd_file;
    }

    public String getResource_size() {
        return resource_size;
    }

    public void setResource_size(String resource_size) {
        this.resource_size = resource_size;
    }

    public int getIs_mobile() {
        return is_mobile;
    }

    public void setIs_mobile(int is_mobile) {
        this.is_mobile = is_mobile;
    }

    public int getIs_public() {
        return is_public;
    }

    public void setIs_public(int is_public) {
        this.is_public = is_public;
    }

    public String getIndustry_code() {
        return industry_code;
    }

    public void setIndustry_code(String industry_code) {
        this.industry_code = industry_code;
    }

    public String getIndustry_sub_code() {
        return industry_sub_code;
    }

    public void setIndustry_sub_code(String industry_sub_code) {
        this.industry_sub_code = industry_sub_code;
    }

    public int getUsage_amount() {
        return usage_amount;
    }

    public void setUsage_amount(int usage_amount) {
        this.usage_amount = usage_amount;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public int getRef_id() {
        return ref_id;
    }

    public void setRef_id(int ref_id) {
        this.ref_id = ref_id;
    }

    @Override
    public String toString() {
        return "Project{" +
                "id=" + id +
                ", user_id=" + user_id +
                ", project_name='" + project_name + '\'' +
                ", project_file_name='" + project_file_name + '\'' +
                ", last_modify_time='" + last_modify_time + '\'' +
                ", is_mobile=" + is_mobile +
                ", project_data='" + project_data + '\'' +
                ", resource_size='" + resource_size + '\'' +
                ", is_public=" + is_public +
                ", industry_code='" + industry_code + '\'' +
                ", industry_sub_code='" + industry_sub_code + '\'' +
                ", usage_amount=" + usage_amount +
                ", points=" + points +
                ", ref_id=" + ref_id +
                ", psd_file=" + psd_file +
                '}';
    }
}
