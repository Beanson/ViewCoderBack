package viewcoder.operation.entity;

import io.netty.handler.codec.http.multipart.FileUpload;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/2/7.
 */
public class User implements Serializable{

    private int id;
    private String timestamp;
    private String portrait;
    private String user_name;
    private String email;
    private String password;
    private String open_id;
    private String phone;
    private String role;
    private String nation;
    private FileUpload portrait_file;
    private String resource_remain;
    private String resource_used;
    private String last_store_code;
    private String last_store_sub_code;
    private int total_usage_amount;
    private int total_points;
    private int framework;
    private int package_way;

    public User(){}

    public User(int id) {
        this.id = id;
    }

    //注册登录时使用
    public User(int id, String user_name, String email, String password){
        this.id=id;
        this.user_name = user_name;
        this.email = email;
        this.password=password;
    }

    //更新user的resource剩余空间时使用
    public User(int id, String resource_remain) {
        this.id = id;
        this.resource_remain = resource_remain;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPortrait() {
        return portrait;
    }

    public void setPortrait(String portrait) {
        this.portrait = portrait;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getNation() {
        return nation;
    }

    public void setNation(String nation) {
        this.nation = nation;
    }

    public String getOpen_id() {
        return open_id;
    }

    public void setOpen_id(String open_id) {
        this.open_id = open_id;
    }

    public FileUpload getPortrait_file() {
        return portrait_file;
    }

    public void setPortrait_file(FileUpload portrait_file) {
        this.portrait_file = portrait_file;
    }

    public String getResource_remain() {
        return resource_remain;
    }

    public void setResource_remain(String resource_remain) {
        this.resource_remain = resource_remain;
    }

    public String getLast_store_code() {
        return last_store_code;
    }

    public void setLast_store_code(String last_store_code) {
        this.last_store_code = last_store_code;
    }

    public String getLast_store_sub_code() {
        return last_store_sub_code;
    }

    public void setLast_store_sub_code(String last_store_sub_code) {
        this.last_store_sub_code = last_store_sub_code;
    }

    public int getTotal_usage_amount() {
        return total_usage_amount;
    }

    public void setTotal_usage_amount(int total_usage_amount) {
        this.total_usage_amount = total_usage_amount;
    }

    public int getTotal_points() {
        return total_points;
    }

    public void setTotal_points(int total_points) {
        this.total_points = total_points;
    }

    public String getResource_used() {
        return resource_used;
    }

    public void setResource_used(String resource_used) {
        this.resource_used = resource_used;
    }

    public int getFramework() {
        return framework;
    }

    public void setFramework(int framework) {
        this.framework = framework;
    }

    public int getPackage_way() {
        return package_way;
    }

    public void setPackage_way(int package_way) {
        this.package_way = package_way;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", timestamp='" + timestamp + '\'' +
                ", portrait='" + portrait + '\'' +
                ", user_name='" + user_name + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", open_id='" + open_id + '\'' +
                ", phone='" + phone + '\'' +
                ", role='" + role + '\'' +
                ", nation='" + nation + '\'' +
                ", portrait_file=" + portrait_file +
                ", resource_remain='" + resource_remain + '\'' +
                ", resource_used='" + resource_used + '\'' +
                ", last_store_code='" + last_store_code + '\'' +
                ", last_store_sub_code='" + last_store_sub_code + '\'' +
                ", total_usage_amount=" + total_usage_amount +
                ", total_points=" + total_points +
                ", framework=" + framework +
                ", package_way=" + package_way +
                '}';
    }
}
