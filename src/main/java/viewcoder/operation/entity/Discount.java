package viewcoder.operation.entity;

/**
 * Created by Administrator on 2018/8/11.
 * 折扣优惠实体类
 */
public class Discount {

    private int id;
    private String company_credit;
    private String serial_num;
    private int user_id;
    private String phone;
    private String email;
    private int dis_way;
    private int dis_amount;

    public Discount() {
    }

    public Discount(int id, String company_credit, String serial_num, int user_id, String phone, String email, int dis_way, int dis_amount) {
        this.id = id;
        this.company_credit = company_credit;
        this.serial_num = serial_num;
        this.user_id = user_id;
        this.phone = phone;
        this.email = email;
        this.dis_way = dis_way;
        this.dis_amount = dis_amount;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCompany_credit() {
        return company_credit;
    }

    public void setCompany_credit(String company_credit) {
        this.company_credit = company_credit;
    }

    public String getSerial_num() {
        return serial_num;
    }

    public void setSerial_num(String serial_num) {
        this.serial_num = serial_num;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getDis_way() {
        return dis_way;
    }

    public void setDis_way(int dis_way) {
        this.dis_way = dis_way;
    }

    public int getDis_amount() {
        return dis_amount;
    }

    public void setDis_amount(int dis_amount) {
        this.dis_amount = dis_amount;
    }

    @Override
    public String toString() {
        return "Discount{" +
                "id=" + id +
                ", company_credit='" + company_credit + '\'' +
                ", serial_num='" + serial_num + '\'' +
                ", user_id=" + user_id +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", dis_way=" + dis_way +
                ", dis_amount=" + dis_amount +
                '}';
    }
}
