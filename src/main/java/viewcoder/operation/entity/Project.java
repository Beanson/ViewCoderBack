package viewcoder.operation.entity;

import io.netty.handler.codec.http.multipart.FileUpload;

/**
 * Created by Administrator on 2017/5/22.
 */
public class Project {

    private int id;
    private int user_id;
    private int parent;
    private String version; //记录创建时候的版本，pc版还是mobile版
    private String project_name;
    private String last_modify_time;
    private String pc_version; //电脑版在oss中的文件名
    private String mo_version; //手机版在oss中的文件名
    private String project_data; //项目数据， 用于接收项目数据，进行OSS操作，不进行数据库操作
    private String resource_size;
    private int is_public;
    private String industry_code;
    private String industry_sub_code;
    private int usage_amount;
    private int points;
    private int ref_id;
    private int child;
    private FileUpload psd_file;


    public Project() {
    }

    //更新project的resource_size时用到此方法
    public Project(int id, String resource_size) {
        this.id = id;
        this.resource_size = resource_size;
    }

    //拷贝非psd项目时用到此构造函数
    public Project(int user_id, String project_name, String last_modify_time, String project_data, String resource_size) {
        this.user_id = user_id;
        this.project_name = project_name;
        this.last_modify_time = last_modify_time;
        this.project_data = project_data;
        this.resource_size = resource_size;
    }

    public Project(int id, int user_id, String project_name, String last_modify_time, String project_data, FileUpload psd_file) {
        this.id = id;
        this.user_id = user_id;
        this.project_name = project_name;
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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getParent() {
        return parent;
    }

    public void setParent(int parent) {
        this.parent = parent;
    }

    public String getProject_name() {
        return project_name;
    }

    public void setProject_name(String project_name) {
        this.project_name = project_name;
    }

    public String getLast_modify_time() {
        return last_modify_time;
    }

    public void setLast_modify_time(String last_modify_time) {
        this.last_modify_time = last_modify_time;
    }

    public String getPc_version() {
        return pc_version;
    }

    public void setPc_version(String pc_version) {
        this.pc_version = pc_version;
    }

    public String getMo_version() {
        return mo_version;
    }

    public void setMo_version(String mo_version) {
        this.mo_version = mo_version;
    }

    public String getProject_data() {
        return project_data;
    }

    public void setProject_data(String project_data) {
        this.project_data = project_data;
    }

    public String getResource_size() {
        return resource_size;
    }

    public void setResource_size(String resource_size) {
        this.resource_size = resource_size;
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

    public FileUpload getPsd_file() {
        return psd_file;
    }

    public void setPsd_file(FileUpload psd_file) {
        this.psd_file = psd_file;
    }

    public int getChild() {
        return child;
    }

    public void setChild(int child) {
        this.child = child;
    }

    @Override
    public String toString() {
        return "Project{" +
                "id=" + id +
                ", user_id=" + user_id +
                ", parent=" + parent +
                ", version=" + version +
                ", project_name='" + project_name + '\'' +
                ", last_modify_time='" + last_modify_time + '\'' +
                ", pc_version='" + pc_version + '\'' +
                ", mo_version='" + mo_version + '\'' +
                ", project_data='" + project_data + '\'' +
                ", resource_size='" + resource_size + '\'' +
                ", is_public=" + is_public +
                ", industry_code='" + industry_code + '\'' +
                ", industry_sub_code='" + industry_sub_code + '\'' +
                ", usage_amount=" + usage_amount +
                ", points=" + points +
                ", ref_id=" + ref_id +
                ", child=" + child +
                ", psd_file=" + psd_file +
                '}';
    }
}
