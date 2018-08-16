package viewcoder.operation.entity;

/**
 * Created by Administrator on 2018/3/11.
 */
public class Orders {

    private int id;
    private String out_trade_no;
    private String trade_no;
    private int user_id;
    private int service_id;
    private int service_num;
    private String order_date;
    private String pay_date;
    private String expire_date;
    private int pay_status;
    private int pay_way;
    private String price;
    private String company_credit;
    private String subject;

    public Orders() {
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOut_trade_no() {
        return out_trade_no;
    }

    public void setOut_trade_no(String out_trade_no) {
        this.out_trade_no = out_trade_no;
    }

    public String getTrade_no() {
        return trade_no;
    }

    public void setTrade_no(String trade_no) {
        this.trade_no = trade_no;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public int getService_id() {
        return service_id;
    }

    public void setService_id(int service_id) {
        this.service_id = service_id;
    }

    public int getService_num() {
        return service_num;
    }

    public void setService_num(int service_num) {
        this.service_num = service_num;
    }

    public String getOrder_date() {
        return order_date;
    }

    public void setOrder_date(String order_date) {
        this.order_date = order_date;
    }

    public String getPay_date() {
        return pay_date;
    }

    public void setPay_date(String pay_date) {
        this.pay_date = pay_date;
    }

    public String getExpire_date() {
        return expire_date;
    }

    public void setExpire_date(String expire_date) {
        this.expire_date = expire_date;
    }

    public int getPay_way() {
        return pay_way;
    }

    public int getPay_status() {
        return pay_status;
    }

    public void setPay_status(int pay_status) {
        this.pay_status = pay_status;
    }

    public void setPay_way(int pay_way) {
        this.pay_way = pay_way;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getCompany_credit() {
        return company_credit;
    }

    public void setCompany_credit(String company_credit) {
        this.company_credit = company_credit;
    }

    @Override
    public String toString() {
        return "Orders{" +
                "id=" + id +
                ", out_trade_no='" + out_trade_no + '\'' +
                ", trade_no='" + trade_no + '\'' +
                ", user_id=" + user_id +
                ", service_id=" + service_id +
                ", service_num=" + service_num +
                ", order_date='" + order_date + '\'' +
                ", pay_date='" + pay_date + '\'' +
                ", expire_date='" + expire_date + '\'' +
                ", pay_status=" + pay_status +
                ", pay_way=" + pay_way +
                ", price='" + price + '\'' +
                ", company_credit='" + company_credit + '\'' +
                ", subject='" + subject + '\'' +
                '}';
    }
}
