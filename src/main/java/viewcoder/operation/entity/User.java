package viewcoder.operation.entity;

import io.netty.handler.codec.http.multipart.FileUpload;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/2/7.
 */
public class User implements Serializable{

    private int id;
    private String account;
    private String timestamp;
    private String portrait;
    private String user_name;
    private String email;
    private String password;
    private String openid;
    private String phone;
    private String verifyCode; //用于注册时使用和登录验证使用
    private String role;
    private String nation;
    private FileUpload portrait_file;
    private int resource_total;
    private int resource_used;
    private String last_store_code;
    private String last_store_sub_code;
    private int total_usage_amount;
    private int total_points;
    private int framework;
    private int package_way;
    private String dis_serial_num; //企业优惠码序列号
    private String province;
    private String city;
    private String unionid; //各种微信平台唯一识别id
    private int sex; //性别
    private String session_id; //该用户的session_id
    private int ack; //标识该用户下的项目资源可访问状态，1可访问，0被锁定

    //辅助数据
    private int newUserResSpace;

    public User(){}

    public User(int id) {
        this.id = id;
    }

    //设置ack权限时初始化操作
    public User(int id, int ack) {
        this.id = id;
        this.ack = ack;
    }

    //注册登录时使用
    public User(int id, String user_name, String email, String password){
        this.id=id;
        this.user_name = user_name;
        this.email = email;
        this.password=password;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
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

    public String getVerifyCode() {
        return verifyCode;
    }

    public void setVerifyCode(String verifyCode) {
        this.verifyCode = verifyCode;
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

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }

    public FileUpload getPortrait_file() {
        return portrait_file;
    }

    public void setPortrait_file(FileUpload portrait_file) {
        this.portrait_file = portrait_file;
    }

    public int getResource_total() {
        return resource_total;
    }

    public void setResource_total(int resource_total) {
        this.resource_total = resource_total;
    }

    public void setResource_used(int resource_used) {
        this.resource_used = resource_used;
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

    public int getResource_used() {
        return resource_used;
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

    public String getDis_serial_num() {
        return dis_serial_num;
    }

    public void setDis_serial_num(String dis_serial_num) {
        this.dis_serial_num = dis_serial_num;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getUnionid() {
        return unionid;
    }

    public void setUnionid(String unionid) {
        this.unionid = unionid;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public String getSession_id() {
        return session_id;
    }

    public void setSession_id(String session_id) {
        this.session_id = session_id;
    }

    public int getNewUserResSpace() {
        return newUserResSpace;
    }

    public void setNewUserResSpace(int newUserResSpace) {
        this.newUserResSpace = newUserResSpace;
    }

    public int getAck() {
        return ack;
    }

    public void setAck(int ack) {
        this.ack = ack;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", account='" + account + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", portrait='" + portrait + '\'' +
                ", user_name='" + user_name + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", openid='" + openid + '\'' +
                ", phone='" + phone + '\'' +
                ", verifyCode='" + verifyCode + '\'' +
                ", role='" + role + '\'' +
                ", nation='" + nation + '\'' +
                ", portrait_file=" + portrait_file +
                ", resource_total=" + resource_total +
                ", resource_used=" + resource_used +
                ", last_store_code='" + last_store_code + '\'' +
                ", last_store_sub_code='" + last_store_sub_code + '\'' +
                ", total_usage_amount=" + total_usage_amount +
                ", total_points=" + total_points +
                ", framework=" + framework +
                ", package_way=" + package_way +
                ", dis_serial_num='" + dis_serial_num + '\'' +
                ", province='" + province + '\'' +
                ", city='" + city + '\'' +
                ", unionid='" + unionid + '\'' +
                ", sex=" + sex +
                ", session_id='" + session_id + '\'' +
                ", ack=" + ack +
                ", newUserResSpace=" + newUserResSpace +
                '}';
    }
}
