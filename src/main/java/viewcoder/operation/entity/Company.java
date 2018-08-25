package viewcoder.operation.entity;

/**
 * Created by Administrator on 2018/8/11.
 * Company实体类
 */
public class Company {

    private int id;
    private String timestamp;
    private String credit;
    private String name;
    private String address;
    private String contact;
    private int dis_way;
    private int dis_amount;
    private int feed_way;
    private int feed_amount;
    private String remark;
    private String account;

    public Company() {
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

    public String getCredit() {
        return credit;
    }

    public void setCredit(String credit) {
        this.credit = credit;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
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

    public int getFeed_way() {
        return feed_way;
    }

    public void setFeed_way(int feed_way) {
        this.feed_way = feed_way;
    }

    public int getFeed_amount() {
        return feed_amount;
    }

    public void setFeed_amount(int feed_amount) {
        this.feed_amount = feed_amount;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    @Override
    public String toString() {
        return "Company{" +
                "id=" + id +
                ", timestamp='" + timestamp + '\'' +
                ", credit='" + credit + '\'' +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", contact='" + contact + '\'' +
                ", dis_way=" + dis_way +
                ", dis_amount=" + dis_amount +
                ", feed_way=" + feed_way +
                ", feed_amount=" + feed_amount +
                ", remark='" + remark + '\'' +
                ", account='" + account + '\'' +
                '}';
    }
}
