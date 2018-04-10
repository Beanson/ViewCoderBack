package FrontEnd.myBatis.entity;

import io.netty.handler.codec.http.multipart.FileUpload;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/2/7.
 */
public class User implements Serializable{

    private int id;
    private String portrait;
    private String user_name;
    private String email;
    private String password;
    private String phone;
    private String open_id;
    private String role;
    private String nation;
    private FileUpload portrait_file;
    private String resource_remain;
    private String last_store_type;
    private int total_usage_amount;
    private int points;
    private int preferential_amount;

    public User(){}

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

    public User(int id, String portrait, String user_name, String email, String password, String phone, String open_id,
                String role, String nation, FileUpload portrait_file, String resource_remain) {
        this.id = id;
        this.portrait = portrait;
        this.user_name = user_name;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.open_id = open_id;
        this.role = role;
        this.nation = nation;
        this.portrait_file=portrait_file;
        this.resource_remain=resource_remain;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getLast_store_type() {
        return last_store_type;
    }

    public void setLast_store_type(String last_store_type) {
        this.last_store_type = last_store_type;
    }

    public int getTotal_usage_amount() {
        return total_usage_amount;
    }

    public void setTotal_usage_amount(int total_usage_amount) {
        this.total_usage_amount = total_usage_amount;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public int getPreferential_amount() {
        return preferential_amount;
    }

    public void setPreferential_amount(int preferential_amount) {
        this.preferential_amount = preferential_amount;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", portrait='" + portrait + '\'' +
                ", user_name='" + user_name + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", phone='" + phone + '\'' +
                ", open_id='" + open_id + '\'' +
                ", role='" + role + '\'' +
                ", nation='" + nation + '\'' +
                ", portrait_file=" + portrait_file +
                ", resource_remain='" + resource_remain + '\'' +
                ", last_store_type='" + last_store_type + '\'' +
                ", total_usage_amount=" + total_usage_amount +
                ", points=" + points +
                ", preferential_amount=" + preferential_amount +
                '}';
    }
}
